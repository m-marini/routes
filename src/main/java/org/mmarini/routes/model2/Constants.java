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

public interface Constants {
    double REACTION_TIME = 1;
    double VEHICLE_LENGTH = 5;

    /**
     * Returns the movement distance when vehicle is braking
     * (brakingDistance - VEHICLE_LENGTH) / (1 + REACTION_TIME / dt)
     *
     * @param brakingDistance the breaking distance
     * @param dt              the time interval
     */
    static double brakingMovement(double brakingDistance, double dt) {
        return (brakingDistance - VEHICLE_LENGTH) / (1 + REACTION_TIME / dt);
    }

    /**
     * Returns the safety distance for a vehicle
     *
     * @param speed the vehicle speed
     */
    static double computeSafetyDistance(final double speed) {
        return REACTION_TIME * speed + VEHICLE_LENGTH;
    }
}
