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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.MulticastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 
 * @param <T>
 */
public class Simulator<T> {
	public class Seed {

		private final long time;
		private final T event;

		/**
		 * @param time
		 * @param event
		 */
		public Seed(final long time, final T event) {
			this.time = time;
			this.event = event;
		}

		/**
		 * @param time
		 * @param event
		 */
		public Seed(final T event) {
			this(System.nanoTime(), event);
		}

		/**
		 * 
		 * @param event
		 * @return
		 */
		public Seed event(final T event) {
			return new Seed(time, event);
		}

		/**
		 * @return the traffics
		 */
		public T getEvent() {
			return event;
		}

		/**
		 *
		 * @return
		 * @throws Throwable
		 */
		public Seed next() throws Throwable {
			final long t = System.nanoTime();
			final double dt = (t - time) * 1e-9;
			final double time = toTime.apply(event) + dt * speed;
			final T newEvent = builder.apply(event, time);
			return new Seed(t, newEvent);
		}

		/**
		 * 
		 * @return
		 */
		public Seed now() {
			return new Seed(System.nanoTime(), event);
		}
	}

	enum Status {
		IDLE, ACTIVE, FAILED
	}

	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	/**
	 * 
	 * @param <T>
	 * @param event0
	 * @param builder
	 * @param toTime
	 * @return
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

	/**
	 * 
	 * @param worker
	 * @param event
	 * @param builder
	 * @param toTime
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
	}

	/**
	 * @return the events
	 */
	public Flowable<T> getEvents() {
		return events;
	}

	/**
	 * @return the seed
	 */
	Optional<Seed> getSeed() {
		return seed;
	}

	/**
	 * @return the speed
	 */
	double getSpeed() {
		return speed;
	}

	/**
	 * @return the status
	 */
	Status getStatus() {
		return status;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	private Simulator<T> processEvent(final T event) {
		logger.debug("processEvent({})", event);
		events.onNext(event);
		seed = Optional.of(new Seed(event));
		return this;
	}

	/**
	 * 
	 * @return
	 */
	private Simulator<T> processNext() {
		if (status == Status.ACTIVE) {
			seed.ifPresent(s -> {
				try {
					final Simulator<T>.Seed next = s.next();
					events.onNext(next.getEvent());
					seed = Optional.of(next);
					worker.schedule(this::processNext);
				} catch (final Throwable e) {
					status = Status.FAILED;
					events.onError(e);
				}
			});
		}
		return this;
	}

	/**
	 * 
	 * @param transition
	 * @return
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
	 * 
	 * @param speed
	 * @return
	 */
	private Simulator<T> processSpeed(final double speed) {
		logger.debug("processSpeed({})", speed);
		this.speed = speed;
		return this;
	}

	/**
	 * 
	 * @return
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
	 * 
	 * @return
	 */
	private Simulator<T> processStop() {
		logger.debug("processStart()");
		status = Status.IDLE;
		return this;
	}

	/**
	 * 
	 * @param transition
	 * @return
	 */
	public Simulator<T> request(final Function<T, T> transition) {
		logger.debug("request(...)");
		worker.schedule(() -> {
			processRequest(transition);
		});
		return this;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	public Simulator<T> setEvent(final T event) {
		logger.debug("setEvent({})", event);
		worker.schedule(() -> {
			processEvent(event);
		});
		return this;
	}

	/**
	 * 
	 * @param speed
	 * @return
	 */
	public Simulator<T> setSpeed(final double speed) {
		logger.debug("setSpeed({})", speed);
		worker.schedule(() -> {
			processSpeed(speed);
		});
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public Simulator<T> start() {
		logger.debug("start()");
		worker.schedule(this::processStart);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public Simulator<T> stop() {
		logger.debug("stop()");
		worker.schedule(this::processStop);
		return this;
	}

}
