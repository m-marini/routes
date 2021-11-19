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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.lang.Math.round;

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
public class AdaptiveSimulator<T> implements Simulator<T> {

    public static final double NANOS = 1e-9;
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveSimulator.class);
    private static long maxElaps;

    /**
     * Returns a simulator.
     * <p>
     * The simulator is bound to a new dedicated thread worker that serializes the
     * activities
     * </p>
     *
     * @param <T>       the event type
     * @param event     the initial event
     * @param nextEvent the function with two parameter T, Double returning the next event
     * @param copy      the copy function
     */
    public static <T> AdaptiveSimulator<T> create(T event,
                                                  BiFunction<T, Double, Tuple2<T, Double>> nextEvent,
                                                  UnaryOperator<T> copy) {
        return new AdaptiveSimulator<>(Schedulers.newThread().createWorker(),
                event,
                nextEvent,
                copy);
    }

    private final Worker worker;
    private final BiFunction<T, Double, Tuple2<T, Double>> nextEvent;
    private final Deque<ProcessRequest> queue;
    private final UnaryOperator<T> copy;
    private Consumer<Double> onSpeed;
    private Consumer<T> onEvent;
    private double speed;
    private Status status;
    private Instant lastEvent; // Last event instance
    private T event;
    private long eventInterval;
    private double simulatedTime; // cumulative simulation time

    /**
     * Creates the simulator.
     *
     * @param worker    the assigned worker
     * @param seed      the seed event
     * @param nextEvent the event nextStatus function
     * @param copy      the copy function
     */
    protected AdaptiveSimulator(Worker worker, T seed,
                                BiFunction<T, Double, Tuple2<T, Double>> nextEvent,
                                UnaryOperator<T> copy) {
        Objects.requireNonNull(worker);
        Objects.requireNonNull(seed);
        Objects.requireNonNull(nextEvent);
        this.queue = new ConcurrentLinkedDeque<>();
        this.worker = worker;
        this.event = seed;
        this.copy = copy;
        this.nextEvent = nextEvent;
        this.speed = 1;
        this.status = Status.IDLE;
    }

    /**
     * Deque the queue
     */
    private void deque() {
        for (; ; ) {
            ProcessRequest request = queue.poll();
            if (request == null) {
                break;
            }
            event = request.transition.apply(event);
            request.result.onSuccess(event);
        }
    }

    private void emittEvent(T event) {
        if (onEvent != null) {
            onEvent.accept(event);
        }
    }

    private void emittSpeed(double speed) {
        if (onSpeed != null) {
            onSpeed.accept(speed);
        }
    }

    void processCycle() {
        while (status == Status.ACTIVE) {
            long simClockTime = round(simulatedTime / NANOS / speed);
            deque();
            // Computes the simulation time
            double simulatingTime = eventInterval * NANOS * speed - simulatedTime;
            // Computes the next event
            long a = System.nanoTime();
            Tuple2<T, Double> tuple = nextEvent.apply(event, simulatingTime);
            long elaps = System.nanoTime() - a;
            if (elaps > maxElaps) {
                maxElaps = elaps;
                logger.info("max elaps {} ms", maxElaps / 1e6);
            }
            event = tuple._1;
            simulatedTime += tuple._2;
            Instant now = Instant.now();
            long processTime = Duration.between(lastEvent, now).toNanos();
            if (processTime >= eventInterval) {
                // Overload
                // Emits data
                // reset simulation time trak last event instant
                emittEvent(copy.apply(event));
                double actualSpeed = simulatingTime / (processTime * NANOS);
                emittSpeed(actualSpeed);
                simulatedTime = 0;
                lastEvent = now;
                // reschedule no wait
            } else if (simClockTime >= eventInterval) {
                // Underload
                emittEvent(copy.apply(event));
                emittSpeed(speed);
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
    public Single<T> pushEvent(T event) {
        SingleSubject<T> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> copy.apply(event), result));
        return result;
    }

    @Override
    public Single<T> request(UnaryOperator<T> transition) {
        SingleSubject<T> result = SingleSubject.create();
        queue.offer(new ProcessRequest(transition, result));
        return result;
    }

    @Override
    public Simulator<T> setEventInterval(Duration interval) {
        eventInterval = interval.toNanos();
        return this;
    }

    @Override
    public AdaptiveSimulator<T> setOnEvent(Consumer<T> onEvent) {
        this.onEvent = onEvent;
        return this;
    }

    @Override
    public AdaptiveSimulator<T> setOnSpeed(Consumer<Double> onSpeed) {
        this.onSpeed = onSpeed;
        return this;
    }

    @Override
    public Single<T> setSpeed(double speed) {
        SingleSubject<T> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> {
            this.speed = speed;
            return e;
        }, result));
        return result;
    }

    @Override
    public Single<T> start() {
        logger.debug("Starting simulation ...");
        if (status == Status.IDLE) {
            SingleSubject<T> result = SingleSubject.create();
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
    public Single<T> stop() {
        logger.debug("Stopping simulation ...");
        SingleSubject<T> result = SingleSubject.create();
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
        final UnaryOperator<T> transition;
        final SingleSubject<T> result;

        ProcessRequest(UnaryOperator<T> transition, SingleSubject<T> result) {
            this.transition = transition;
            this.result = result;
        }
    }

}
