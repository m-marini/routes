//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.model.v2;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.MulticastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A generic simulator.
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
public class Simulator<T> {

	/** The seed generator of event */
	public class Seed {

		private final long time;
		private final T event;

		/**
		 * Creates a seed
		 *
		 * @param time  the clock instant in nanosecons
		 * @param event the last event
		 */
		public Seed(final long time, final T event) {
			this.time = time;
			this.event = event;
		}

		/**
		 * Creates a seed at current clock instant
		 *
		 * @param event the event
		 */
		public Seed(final T event) {
			this(System.nanoTime(), event);
		}

		/**
		 * Returns the seed at the original instant with a event
		 *
		 * @param event the event
		 */
		public Seed event(final T event) {
			return new Seed(time, event);
		}

		/** Returns the seed event */
		public T getEvent() {
			return event;
		}

		/**
		 * Returns the seed at the current time applying the builder function
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

		/** Returns the original seed at the current time */
		public Seed now() {
			return new Seed(System.nanoTime(), event);
		}
	}

	/** The simulator status */
	enum Status {
		IDLE, ACTIVE, FAILED
	}

	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	/**
	 * Returns a simulator
	 * <p>
	 * The simulator is bound to a new dedicated thread worker that serializes the
	 * activities
	 * </p>
	 *
	 * @param <T>     the event type
	 * @param builder the event builder function
	 * @param toTime  the event time function
	 */
	public static <T> Simulator<T> create(final BiFunction<T, Double, T> builder, final Function<T, Double> toTime) {
		return new Simulator<T>(Schedulers.newThread().createWorker(), Optional.empty(), builder, toTime);
	}

	private final Worker worker;
	private final MulticastProcessor<T> events;
	private final BiFunction<T, Double, T> builder;
	private final Function<T, Double> toTime;
	private double speed;
	private Status status;
	private Optional<Seed> seed;
	private long interval;
	private TimeUnit unit;

	/**
	 * Creates the simulator
	 *
	 * @param worker  the assigned worker
	 * @param seed    the seed of event
	 * @param builder ethe event builfer function
	 * @param toTime  the event time function
	 */
	protected Simulator(final Worker worker, final Optional<Seed> seed, final BiFunction<T, Double, T> builder,
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

	/** Returns the events flow */
	public Flowable<T> getEvents() {
		return events;
	}

	/** Returns the minimum simulation interval */
	public long getInterval() {
		return interval;
	}

	/** Returns the event seed */
	Optional<Seed> getSeed() {
		return seed;
	}

	/** Returns the simulation speed */
	double getSpeed() {
		return speed;
	}

	/** Returns the simulation status */
	Status getStatus() {
		return status;
	}

	/** Returns the time unit of interval */
	public TimeUnit getUnit() {
		return unit;
	}

	/**
	 * Process an event generating the new seed ad emitting the event
	 *
	 * @param event the event
	 * @return the simulator
	 */
	private Simulator<T> processEvent(final T event) {
		logger.debug("processEvent({})", event);
		events.onNext(event);
		seed = Optional.of(new Seed(event));
		return this;
	}

	/**
	 * Process the generation of next event. The next seed is generated and the next
	 * event is emitted
	 *
	 * @return the simulator
	 */
	private Simulator<T> processNext() {
		if (status == Status.ACTIVE) {
			seed.ifPresent(s -> {
				try {
					final long start = worker.now(unit);
					final Simulator<T>.Seed next = s.next();
					events.onNext(next.getEvent());
					seed = Optional.of(next);
					worker.schedule(this::processNext, interval + start - worker.now(unit), unit);
				} catch (final Throwable e) {
					status = Status.FAILED;
					events.onError(e);
				}
			});
		}
		return this;
	}

	/**
	 * Process a transition request
	 *
	 * @param transition the transition
	 * @return the simulator
	 */
	private Simulator<T> processRequest(final Function<T, T> transition) {
		logger.debug("processRequest(...)");
		seed.ifPresent(s -> {
			try {
				final T newEvent = transition.apply(s.getEvent());
				seed = Optional.of(s.event(newEvent));
				events.onNext(newEvent);
			} catch (final Throwable e) {
				status = Status.FAILED;
				events.onError(e);
			}
		});
		return this;
	}

	/**
	 * Process the setting of simulation speed
	 *
	 * @param speed the simulation speed
	 * @return the simulator
	 */
	private Simulator<T> processSpeed(final double speed) {
		logger.debug("processSpeed({})", speed);
		this.speed = speed;
		return this;
	}

	/**
	 * Process the start of simulation
	 *
	 * @return the simulator
	 */
	private Simulator<T> processStart() {
		logger.debug("processStart()");
		if (status == Status.IDLE) {
			status = Status.ACTIVE;
			processNext();
		}
		return this;
	}

	/**
	 * Process the stop of simulation
	 *
	 * @return the simulator
	 */
	private Simulator<T> processStop() {
		logger.debug("processStart()");
		status = Status.IDLE;
		return this;
	}

	/**
	 * Enqueues the transition request
	 *
	 * @param transition the transition
	 * @return the simulator
	 */
	public Simulator<T> request(final Function<T, T> transition) {
		logger.debug("request(...)");
		worker.schedule(() -> {
			processRequest(transition);
		});
		return this;
	}

	/**
	 * Enqueues the a new event
	 *
	 * @param event the event
	 * @return the simulator
	 */
	public Simulator<T> setEvent(final T event) {
		logger.debug("setEvent({})", event);
		worker.schedule(() -> {
			processEvent(event);
		});
		return this;
	}

	/**
	 * Set the minimum simulation interval
	 *
	 * @param interval the interval to set
	 * @param unit     the time unit
	 * @return the simulator
	 */
	public Simulator<T> setInterval(final long interval, final TimeUnit unit) {
		this.interval = interval;
		this.unit = unit;
		return this;
	}

	/**
	 * Enqueue the simulation speed.
	 *
	 * @param speed the simulation speed
	 * @return the simulator
	 */
	public Simulator<T> setSpeed(final double speed) {
		logger.debug("setSpeed({})", speed);
		worker.schedule(() -> {
			processSpeed(speed);
		});
		return this;
	}

	/**
	 * Enqueue the start of simulation
	 *
	 * @return the simulator
	 */
	public Simulator<T> start() {
		logger.debug("start()");
		worker.schedule(this::processStart);
		return this;
	}

	/**
	 * Enqueue the stop of simulation
	 *
	 * @return the simulation
	 */
	public Simulator<T> stop() {
		logger.debug("stop()");
		worker.schedule(this::processStop);
		return this;
	}
}
