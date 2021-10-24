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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;

public class FrequencyMeter {
    private static final long MAX_FPS_MILLIS = 300L;
    private static final double SECS_PER_MILLIS = 1e-3;

    /**
     * Returns a default frequency meter with 300 ms of sample interval
     */
    public static FrequencyMeter create() {
        return create(MAX_FPS_MILLIS);
    }

    /**
     * Returns a default frequency meter
     *
     * @param maxFpsMillis the maximum interval of samples
     */
    public static FrequencyMeter create(long maxFpsMillis) {
        return new FrequencyMeter(maxFpsMillis);
    }

    private final PublishProcessor<Double> processor;
    private final long maxFpsMillis;
    private long counter;
    private long startInstant;

    /**
     * Creates a frequency meter
     *
     * @param maxFpsMillis the maximum interval of samples
     */
    protected FrequencyMeter(long maxFpsMillis) {
        this.maxFpsMillis = maxFpsMillis;
        this.processor = PublishProcessor.create();
        reset();
    }

    /**
     * Returns the Flowable of frequency
     */
    public Flowable<Double> getFlowable() {
        return processor;
    }

    /**
     * Reset the frequency meter
     */
    public void reset() {
        counter = 0;
        startInstant = System.currentTimeMillis();
    }

    /**
     * Perform a tick
     */
    public void tick() {
        long nowMs = System.currentTimeMillis();
        counter++;
        // Computes fps
        long frameElapsed = nowMs - startInstant;
        if (frameElapsed > maxFpsMillis) {
            double fps = (double) counter / frameElapsed / SECS_PER_MILLIS;
            counter = 0;
            startInstant = nowMs;
            processor.onNext(fps);
        }
    }
}
