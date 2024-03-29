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

public interface UIConstants {
    long FPS = 60;
    long NANOSPS = 1000000000L;
    long MAX_FPS_MILLIS = 300L;
    double SECS_PER_MILLIS = 1e-3;
    int CURSOR_SELECTION_PRECISION = 10;
    double MAX_PRECISION_DISTANCE = 3;
    double SCALE_FACTOR = Math.pow(2, 1D / 4);
    //        Math.sqrt(2);

    double FREQUENCY1 = 2;
    double FREQUENCY2 = 4;
    double FREQUENCY3 = 8;
    double FREQUENCY4 = 14;
    double FREQUENCY5 = 28;

    double DIFFICULT_FREQUENCY = FREQUENCY5 / 60;
    double NORMAL_FREQUENCY = FREQUENCY3 / 60;
    double EASY_FREQUENCY = FREQUENCY1 / 60;

    /**
     * Returns the precision distance for a scale
     *
     * @param scale the scale
     */
    static double computePrecisionDistance(double scale) {
        return CURSOR_SELECTION_PRECISION / scale;
    }

}
