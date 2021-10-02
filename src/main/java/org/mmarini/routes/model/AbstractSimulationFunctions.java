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

/**
 * @author marco.marini@mmarini.org
 * @version $Id: AbstractSimulationFunctions.java,v 1.2 2009/01/05 17:04:20
 * marco Exp $
 */
public abstract class AbstractSimulationFunctions implements Constants {
    private static final AbstractSimulationFunctions instance = new LinearSimulationFunctions();

    /**
     * @return the instance
     */
    public static AbstractSimulationFunctions createInstance() {
        return instance;
    }

    /**
     *
     */
    protected AbstractSimulationFunctions() {
    }

    /**
     * @param speed
     * @return
     */
    public abstract double computeDistanceBySpeed(double speed);

    /**
     * @param distance
     * @return
     */
    public abstract double computeSpeed(double distance);

    /**
     * @param distance
     * @param veicleCount
     * @return
     */
    public abstract double computeSpeedByVeicles(double distance, int veicleCount);

    /**
     * @param distance
     * @param veicleCount
     * @param speedLimit
     * @return
     */
    public abstract double computeTrafficLevel(double distance, int veicleCount, double speedLimit);

    /**
     * @param distance
     * @param speedLimit
     * @param veicleCount
     * @return
     */
    public abstract double computeTransitTime(double distance, double speedLimit, int veicleCount);

    /**
     * @param time
     * @param length
     * @return
     */
    public abstract double computeVeicleMovement(double time, double length);
}
