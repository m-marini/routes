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

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 *
 */
public class Simulator {
	private static final long DEFAULT_MIN_TIME_NS = 100000000L;
	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	private final BehaviorSubject<SimulationStatus> output;
	private final long minTimeNs = DEFAULT_MIN_TIME_NS;
	private Optional<Subject<SimulationStatus>> stoppedSubj;
	private boolean running;
	private double speed;
	private SimulationStatus simulationStatus;
	private long prevTime;

	/**
	 *
	 */
	public Simulator() {
		super();
		speed = 1;
		output = BehaviorSubject.create();
		output.subscribe(this::handleNextReady);
		stoppedSubj = Optional.empty();
	}

	/**
	 * Returns the output
	 */
	public Subject<SimulationStatus> getOutput() {
		return output;
	}

	/**
	 *
	 * @param next
	 */
	private Simulator handleNextReady(final SimulationStatus next) {
		final long time = System.nanoTime();
		// Compute interval
		final long dt = time - prevTime;
		prevTime = time;
		simulationStatus = next;
		if (running) {
			final long waitTime = minTimeNs - dt;
			if (waitTime >= 0) {
				// Reschedule for next status
				Single.fromSupplier(() -> this.next(next, dt * 1e-9)).delay(waitTime, TimeUnit.NANOSECONDS)
						.subscribe(output::onNext);
			} else {
				// Reschedule for next status
				Single.fromSupplier(() -> this.next(next, dt * 1e-9)).subscribeOn(Schedulers.computation())
						.subscribe(output::onNext);
			}
		} else {
			stoppedSubj.ifPresent(subj -> {
				logger.debug("Simulator stopped.");
				subj.onNext(next);
				subj.onComplete();
			});
			stoppedSubj = Optional.empty();
		}
		return this;
	}

	/**
	 * Returns the next simulation status
	 *
	 * @param status initial status
	 * @param dt     the real interval time
	 */
	private SimulationStatus next(final SimulationStatus status, final double dt) {
		final double time = status.getTime() + dt * speed;
		return StatusBuilder.create(status, time).build();
	}

	/**
	 *
	 * @param speed
	 * @return
	 */
	public Simulator setSimulationSpeed(final double speed) {
		this.speed = speed;
		return this;
	}

	/**
	 *
	 * @param status
	 * @return
	 */
	public Simulator setSimulationStatus(final SimulationStatus status) {
		logger.debug("setSimulationStatus {}", status);
		simulationStatus = status;
		if (!running) {
			output.onNext(simulationStatus);
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public Simulator start() {
		if (!running) {
			running = true;
			prevTime = System.nanoTime();
			output.onNext(simulationStatus);
			logger.debug("Simulator started ...");
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public Single<SimulationStatus> stop() {
		if (running) {
			running = false;
			logger.debug("Stopping simulator ...");
			final Subject<SimulationStatus> subj = PublishSubject.create();
			stoppedSubj = Optional.of(subj);
			return subj.singleOrError();
		} else {
			return Single.just(simulationStatus);
		}
	}
}
