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
package org.mmarini.routes.model;

import static java.lang.Math.*;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: LinearSimulationFunctions.java,v 1.2 2009/01/05 17:04:20 marco
 * Exp $
 */
public class LinearSimulationFunctions extends AbstractSimulationFunctions {

    private static final double MIN_SPEED_LIMIT = 5 / 3.6;

    /**
     *
     */
    protected LinearSimulationFunctions() {
    }

    /**
     * @param distance
     * @param speedLimit
     * @param veicleCount
     * @return
     */
    private double calculateCurrentSpeed(final double distance, final double speedLimit, final int veicleCount) {
        if (veicleCount < 1) {
            return speedLimit;
        }
        final double maxSpeed = (distance / veicleCount - VEICLE_LENGTH) / REACTION_TIME;
        return min(maxSpeed, speedLimit);
    }

    /**
     * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeDistanceBySpeed(double)
     */
    @Override
    public double computeDistanceBySpeed(final double speed) {
        return REACTION_TIME * speed + VEICLE_LENGTH;
        // return ENERGY * speed * speed + REACTION
        // * speed + VEICLE_LENGTH;
    }

    /**
     *
     */
    @Override
    public double computeSpeed(final double distance) {
        return max(distance / REACTION_TIME, MIN_SPEED_LIMIT);
    }

    /**
     * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeSpeedByVeicles(double,
     * int)
     */
    @Override
    public double computeSpeedByVeicles(final double distance, final int veicleCount) {
        return max((distance - VEICLE_LENGTH) / REACTION_TIME, 0f);
    }

    @Override
    public double computeTrafficLevel(final double distance, final int veicleCount, final double speedLimit) {
        if (veicleCount == 0) {
            return 0;
        }
        final int maxVeicles = (int) floor(distance / VEICLE_LENGTH);
        final int optVeicles = (int) floor(distance / computeDistanceBySpeed(speedLimit));
        final double tl = optVeicles > 0 && veicleCount <= optVeicles
                ? (double) veicleCount / optVeicles * 0.5
                // (veicleCount - optVeicles) / (maxVeicles - optVeicles) * 0.5 + 0.5;
                : ((double) (veicleCount + maxVeicles - 2 * optVeicles) / (maxVeicles - optVeicles)) * 0.5;
        return max(0, min(tl, 1));
    }

    @Override
    public double computeTransitTime(final double distance, final double speedLimit, final int veicleCount) {
        final double speed = calculateCurrentSpeed(distance, speedLimit, veicleCount);
        return distance / speed;
    }

    @Override
    public double computeVeicleMovement(final double time, final double length) {
        // double kr = REACTION;
        // double vl = VEICLE_LENGTH;
        // double ke = ENERGY;
        // double det = Math.pow(kr + t, 2) - 4 * kr * (vl - l);
        // return (double) ((Math.sqrt(det) - kr - t) / (2f * ke) * t);
        return (length - VEICLE_LENGTH) / (1 + REACTION_TIME / time);
    }
}
