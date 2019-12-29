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

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 *
 */
public class Simulator {
	private final Subject<SimulationStatus> output;
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
		output = PublishSubject.create();
		output.subscribe(status -> {
			final long time = System.nanoTime();
			final long dt = time - prevTime;
			prevTime = time;
			simulationStatus = status;
			if (running) {
//				Single.timer(100, TimeUnit.MILLISECONDS).map(x -> next(status, dt * 1e-9)).subscribe(next -> {
//					output.onNext(next);
//				});
				Single.fromSupplier(() -> next(status, dt * 1e-9)).subscribeOn(Schedulers.computation())
						.subscribe(next -> {
							output.onNext(next);
						});
			}
		});
	}

	/**
	 * Returns the output
	 */
	public Subject<SimulationStatus> getOutput() {
		return output;
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
		simulationStatus = status;
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
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public Simulator stop() {
		running = false;
		return this;
	}
}
