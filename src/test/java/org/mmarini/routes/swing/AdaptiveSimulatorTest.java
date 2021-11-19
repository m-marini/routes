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

package org.mmarini.routes.swing;

import org.mmarini.Tuple2;
import org.mmarini.routes.model2.AdaptiveSimulator;
import org.mmarini.routes.model2.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static java.lang.Math.min;

class AdaptiveSimulatorTest {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveSimulatorTest.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Test started");
        Simulator<Double> sim = AdaptiveSimulator.create(0.0,
                        AdaptiveSimulatorTest::nextEvent,
                        Double::valueOf)
                .setEventInterval(Duration.ofMillis(100));
        sim.setSpeed(10)
                .doOnSuccess(t -> logger.info("speed setted at {}", t))
                .subscribe();
        sim.setOnEvent(t -> logger.info("event {}", t));
        sim.setOnSpeed(t -> logger.info("speed {}", t));
        sim.start()
                .doOnSuccess(t -> logger.info("started at {}", t))
                .subscribe();
        Thread.sleep(1000);
        sim.stop()
                .doOnSuccess(t -> logger.info("stopped at {}", t))
                .blockingGet();
        logger.info("Test finished");
    }

    private static Tuple2<Double, Double> nextEvent(double t, double dt) {
        double dt1 = min(0.1, dt);
        double result = t + dt1;
        logger.info("step {}", result);
        return Tuple2.of(t + dt1, dt1);
    }

}