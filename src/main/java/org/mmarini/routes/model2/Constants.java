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

import java.awt.geom.Point2D;

public interface Constants {
    double REACTION_TIME = 1;
    double VEHICLE_LENGTH = 5;
    double PRECISION = 1e-3;
    double KMPHSPM = 3.6;
    double DEFAULT_SPEED_LIMIT_KMH = 130;
    double DEFAULT_FREQUENCY = 8d / 60;
    double DEFAULT_PATH_INTERVAL = 3;
    double DEFAULT_WEIGHT = 1;
    double DEFAULT_SPEED_LIMIT_MPS = DEFAULT_SPEED_LIMIT_KMH / KMPHSPM;
    int DEFAULT_PRIORITY = 0;
    int DEFAULT_MAX_VEHICLES = 8000;


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

    /**
     * Returns the safety speed for an edge
     *
     * @param distance the safety distance
     */
    static double computeSafetySpeed(final double distance) {
        return (distance - VEHICLE_LENGTH) / REACTION_TIME;
    }

    static Point2D gridPoint(double x, double y) {
        double x1 = Math.round(x / PRECISION) * PRECISION;
        double y1 = Math.round(y / PRECISION) * PRECISION;
        return new Point2D.Double(x1, y1);
    }
}
