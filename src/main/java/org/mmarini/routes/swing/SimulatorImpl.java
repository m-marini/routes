/*
 *
 * Copyright (c) 2021 Marco Marini, marco.marini@mmarini.org
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

package org.mmarini.routes.swing;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.processors.MulticastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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
public class SimulatorImpl<T> implements Simulator<T> {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorImpl.class);

    /**
     *
     * Returns a simulator.
     * <p>
     * The simulator is bound to a new dedicated thread worker that serializes the
     * activities
     * </p>
     *
     * @param <T>     the event type
     * @param builder the function with two parameter T, Double returning the next event
     * @param toTime  the function that apply the status to return the simulation time
     */
    public static <T> SimulatorImpl<T> create(final BiFunction<T, Double, T> builder, final Function<T, Double> toTime) {
        return new SimulatorImpl<>(Schedulers.newThread().createWorker(), null, builder, toTime);
    }

    private final Worker worker;
    private final MulticastProcessor<T> events;
    private final BiFunction<T, Double, T> builder;
    private final Function<T, Double> toTime;
    private final Deque<Runnable> queue = new ConcurrentLinkedDeque<>();
    private double speed;
    private Status status;
    private Seed seed;
    private long interval;
    private TimeUnit unit;

    /**
     * Creates the simulator.
     *
     * @param worker  the assigned worker
     * @param seed    the seed of event
     * @param builder the event builder function
     * @param toTime  the event time function
     */
    protected SimulatorImpl(final Worker worker, final Seed seed, final BiFunction<T, Double, T> builder,
                            final Function<T, Double> toTime) {
        this.worker = worker;
        this.seed = seed;
        this.builder = builder;
        this.toTime = toTime;
        this.speed = 1;
        status = Status.IDLE;
        events = MulticastProcessor.create();
        events.start();
        interval = 0;
        unit = TimeUnit.MILLISECONDS;
    }

    /**
     * Deque the queue
     */
    private void deque() {
        for (; ; ) {
            Runnable task = queue.poll();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    @Override
    public Flowable<T> getEvents() {
        return events;
    }

    @Override
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Process an event generating the new seed ad emitting the event.
     *
     * @param event the event
     */
    private void processEvent(final T event) {
        logger.debug("processEvent({})", event.hashCode());
        events.onNext(event);
        seed = new Seed(event);
    }

    /**
     * Process the generation of next event.
     * <p>
     * The next seed is generated and the next event is emitted
     * </p>
     */
    private void processNext() {
        if (status == Status.ACTIVE) {
            deque();
            if (seed != null) {
                try {
                    T event = seed.event;
                    final long start = worker.now(unit);
                    final Seed next = seed.next();
                    events.onNext(next.event);
                    seed = next;
                    logger.debug("processNext() processed {}->{}", event.hashCode(), next.event.hashCode());
                    worker.schedule(this::processNext, interval + start - worker.now(unit), unit);
                } catch (final Throwable e) {
                    logger.error("processNext() error:", e);
                    status = Status.FAILED;
                    events.onError(e);
                }
            }
        } else {
            logger.debug("processNext() on status {}", status);
        }
    }

    /**
     * Process a transition request.
     *
     * @param transition the transition
     */
    private void processRequest(final UnaryOperator<T> transition) {
        final int corrId = transition.hashCode();
        logger.debug("processRequest({})", corrId);
        if (seed != null) {
            try {
                T event = seed.event;
                final T newEvent = transition.apply(event);
                seed = seed.event(newEvent);
                logger.debug("processRequest({}) processed {}->{}", corrId, event.hashCode(), newEvent.hashCode());
                events.onNext(newEvent);
            } catch (final Throwable e) {
                logger.error("processRequest(" + corrId + ") error:", e);
                status = Status.FAILED;
                events.onError(e);
            }
        } else {
            logger.warn("processRequest({}) ignored: no seed:", corrId);
        }
    }

    /**
     * Process the setting of simulation speed.
     *
     * @param speed the simulation speed
     */
    private void processSpeed(final double speed) {
        logger.debug("processSpeed({})", speed);
        this.speed = speed;
    }

    /**
     * Process the start of simulation.
     */
    private void processStart() {
        logger.debug("processStart()");
        if (status == Status.IDLE) {
            status = Status.ACTIVE;
            seed = seed != null ? seed.now() : null;
            processNext();
        }
    }

    /**
     * Process the stop of simulation.
     */
    private void processStop() {
        logger.debug("processStop()");
        status = Status.IDLE;
    }

    @Override
    public SimulatorImpl<T> request(final UnaryOperator<T> transition) {
        logger.debug("request({})", transition.hashCode());
        queue.offer(() -> processRequest(transition));
        return this;
    }

    @Override
    public SimulatorImpl<T> setEvent(final T event) {
        logger.debug("setEvent({})", event);
        queue.offer(() -> processEvent(event));
        return this;
    }

    @Override
    public SimulatorImpl<T> setInterval(final long interval, final TimeUnit unit) {
        this.interval = interval;
        this.unit = unit;
        return this;
    }

    @Override
    public SimulatorImpl<T> setSpeed(final double speed) {
        logger.debug("setSpeed({})", speed);
        queue.offer(() -> processSpeed(speed));
        return this;
    }

    @Override
    public SimulatorImpl<T> start() {
        logger.debug("start()");
        worker.schedule(this::processStart);
        return this;
    }

    @Override
    public SimulatorImpl<T> stop() {
        logger.debug("stop()");
        worker.schedule(this::processStop);
        return this;
    }

    /**
     * The simulator status.
     */
    enum Status {
        IDLE, ACTIVE, FAILED
    }

    /**
     * The seed generator of event
     */
    public class Seed {

        private final long time;
        private final T event;

        /**
         * Creates a seed.
         *
         * @param time  the clock instant in nanoseconds
         * @param event the last event
         */
        public Seed(final long time, final T event) {
            this.time = time;
            this.event = event;
        }

        /**
         * Creates a seed at current clock instant.
         *
         * @param event the event
         */
        public Seed(final T event) {
            this(System.nanoTime(), event);
        }

        /**
         * Returns the seed at the original instant with an event.
         *
         * @param event the event
         */
        public Seed event(final T event) {
            return new Seed(time, event);
        }

        /**
         * Returns the seed at the current time applying the builder function.
         *
         * @throws Throwable in case of builder error
         */
        public Seed next() throws Throwable {
            final long t = System.nanoTime();
            final double dt = (t - time) * 1e-9;
            final double time = toTime.apply(event) + dt * speed;
            final T newEvent = builder.apply(event, time);
            return new Seed(t, newEvent);
        }

        /**
         * Returns the original seed at the current time.
         */
        public Seed now() {
            return new Seed(System.nanoTime(), event);
        }
    }
}
