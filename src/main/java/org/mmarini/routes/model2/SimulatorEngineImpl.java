/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.routes.model2;

import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import org.mmarini.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static java.lang.Math.round;
import static org.mmarini.routes.swing.UIConstants.NANOSPS;

/**
 * Generic simulator.
 * <p>
 * A simulator generates events representing the evolution in the time of a
 * model. It starts with an initial event at an initial time and generates the
 * new events by a builder function of the previous event and the simulation
 * time simulation.<br>
 * The simulation clock ticks can be set to a specifics intervals.<br>
 * The simulation time flows at at simulation speed respecting to the clock
 * time.<br>
 * </p>
 *
 * @param <T> the event type
 */
public class SimulatorEngineImpl<T, S> implements SimulatorEngine<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorEngineImpl.class);

    /**
     * Returns a simulator.
     * <p>
     * The simulator is bound to a new dedicated thread worker that serializes the
     * activities
     * </p>
     *
     * @param <T>         the event type
     * @param initialSeed the initial seed
     * @param nextSeed    the function returning next seed applying a seed and the time interval
     * @param emit        the function returning the event applying a seed
     */
    public static <T, S> SimulatorEngineImpl<T, S> create(S initialSeed,
                                                          BiFunction<S, Double, Tuple2<S, Double>> nextSeed,
                                                          Function<S, T> emit) {
        return new SimulatorEngineImpl<>(Schedulers.newThread().createWorker(),
                initialSeed, nextSeed, emit);
    }

    private final Worker worker;
    private final Deque<ProcessRequest> queue;
    private final BiFunction<S, Double, Tuple2<S, Double>> nextSeed;
    private final Function<S, T> emit;
    private S seed;
    private DoubleConsumer onSpeed;
    private Consumer<T> onEvent;
    private double speed;
    private Status status;
    private Instant lastEvent; // Last event instance
    private long eventInterval;
    private double simulatedTime; // cumulative simulation time

    /**
     * Creates the simulator.
     *
     * @param worker      the assigned worker
     * @param initialSeed the initial seed
     * @param nextSeed    the function returning next seed applying a seed and the time interval
     * @param emit        the function returning the event applying a seed
     */
    protected SimulatorEngineImpl(Worker worker,
                                  S initialSeed,
                                  BiFunction<S, Double, Tuple2<S, Double>> nextSeed,
                                  Function<S, T> emit) {
        Objects.requireNonNull(worker);
        Objects.requireNonNull(initialSeed);
        Objects.requireNonNull(nextSeed);
        Objects.requireNonNull(emit);
        this.nextSeed = nextSeed;
        this.emit = emit;
        this.worker = worker;
        this.queue = new ConcurrentLinkedDeque<>();
        this.speed = 1;
        this.seed = initialSeed;
        this.status = Status.IDLE;
    }

    /**
     * Deque the queue
     */
    private void deque() {
        for (; ; ) {
            ProcessRequest request = queue.poll();
            if (request != null) {
                seed = request.transition.apply(seed);
                request.result.onSuccess(seed);
            } else {
                break;
            }
        }
    }

    private void emitEvent(T event) {
        if (onEvent != null) {
            onEvent.accept(event);
        }
    }

    private void emitSpeed(double speed) {
        if (onSpeed != null) {
            onSpeed.accept(speed);
        }
    }

    void processCycle() {
        while (status == Status.ACTIVE) {
            long simClockTime = round(simulatedTime * NANOSPS / speed);
            deque();
            // Computes the simulation time
            double simulatingTime = speed * eventInterval / NANOSPS - simulatedTime;
            // Computes the next event
            Tuple2<S, Double> tuple = nextSeed.apply(seed, simulatingTime);
            seed = tuple._1;
            simulatedTime += tuple._2;
            Instant now = Instant.now();
            long processTime = Duration.between(lastEvent, now).toNanos();
            if (processTime >= eventInterval) {
                // Overload
                // Emits data
                // reset simulation time track last event instant
                emitEvent(emit.apply(seed));
                double actualSpeed = simulatingTime / processTime * NANOSPS;
                emitSpeed(actualSpeed);
                simulatedTime = 0;
                lastEvent = now;
                // reschedule no wait
            } else if (simClockTime >= eventInterval) {
                // Underloaded
                emitEvent(emit.apply(seed));
                emitSpeed(speed);
                long pause = eventInterval - processTime;
                // reschedule pause
                worker.schedule(this::processCycle1, pause, TimeUnit.NANOSECONDS);
                break;
            }
        }
    }

    void processCycle1() {
        simulatedTime = 0;
        lastEvent = Instant.now();
        processCycle();
    }

    @Override
    public Single<S> pushSeed(S seed) {
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> seed, result));
        return result;
    }

    @Override
    public Single<S> request(UnaryOperator<S> transition) {
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(transition, result));
        return result;
    }

    @Override
    public SimulatorEngineImpl<T, S> setEventInterval(Duration interval) {
        eventInterval = interval.toNanos();
        return this;
    }

    @Override
    public SimulatorEngineImpl<T, S> setOnEvent(Consumer<T> onEvent) {
        this.onEvent = onEvent;
        return this;
    }

    @Override
    public SimulatorEngineImpl<T, S> setOnSpeed(DoubleConsumer onSpeed) {
        this.onSpeed = onSpeed;
        return this;
    }

    @Override
    public Single<S> setSpeed(double speed) {
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> {
            this.speed = speed;
            return e;
        }, result));
        return result;
    }

    @Override
    public Single<S> start() {
        logger.debug("Starting simulation ...");
        if (status == Status.IDLE) {
            SingleSubject<S> result = SingleSubject.create();
            queue.offer(new ProcessRequest(e -> {
                status = Status.ACTIVE;
                return e;
            }, result));
            worker.schedule(this::startProcess);
            return result;
        }
        return Single.error(new IllegalArgumentException("Simulator in wrong state: " + status));
    }

    void startProcess() {
        logger.debug("Simulation started.");
        simulatedTime = 0;
        deque();
        lastEvent = Instant.now();
        processCycle();
    }

    @Override
    public Single<S> stop() {
        logger.debug("Stopping simulation ...");
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> {
            status = Status.IDLE;
            return e;
        }
                , result));
        return result;
    }

    /**
     * The simulator status.
     */
    enum Status {
        IDLE, ACTIVE, FAILED
    }

    class ProcessRequest {
        final UnaryOperator<S> transition;
        final SingleSubject<S> result;

        ProcessRequest(UnaryOperator<S> transition, SingleSubject<S> result) {
            this.transition = transition;
            this.result = result;
        }
    }

}
