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

import io.reactivex.rxjava3.core.Single;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
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
 * @param <S> the seed type
 */
public interface SimulatorEngine<T, S> {
    /**
     * Returns the seed after pushing the new seed in the flow
     *
     * @param seed the new seed
     */
    Single<S> pushSeed(final S seed);

    /**
     * Returns the seed after the transition
     *
     * @param transition the transition
     */
    Single<S> request(final UnaryOperator<S> transition);

    /**
     * Returns the engine after setting the interval of event emission
     *
     * @param interval the interval to set
     */
    SimulatorEngine<T, S> setEventInterval(Duration interval);

    /**
     * Returns the engine after setting event consumer
     *
     * @param onEvent the event consumer
     */
    SimulatorEngine<T, S> setOnEvent(Consumer<T> onEvent);

    /**
     * Returns the engine after setting event consumer
     *
     * @param onSpeed the speed consumer
     */
    SimulatorEngine<T, S> setOnSpeed(DoubleConsumer onSpeed);

    /**
     * Returns the seed when changing the simulation speed
     *
     * @param speed the simulation speed
     */
    Single<S> setSpeed(final double speed);

    /**
     * Returns the first seed after the simulation stopping
     */
    Single<S> start();

    /**
     * Returns the last seed after the simulation stopping
     */
    Single<S> stop();
}
