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

/**
 * @author marco.marini@mmarini.org
 */
public class TrafficInfo {
    private final int vehicleCount;
    private final int delayCount;
    private final double totalDelayTime;
    private final SiteNode destination;

    /**
     * Create a traffic information record
     *
     * @param destination    the destination
     * @param vehicleCount   the number of vehicles
     * @param delayCount     the number of delayed vehicles
     * @param totalDelayTime the total delay time
     */
    public TrafficInfo(SiteNode destination, int vehicleCount, int delayCount, double totalDelayTime) {
        this.vehicleCount = vehicleCount;
        this.delayCount = delayCount;
        this.totalDelayTime = totalDelayTime;
        this.destination = destination;
    }

    /**
     * Returns the average delay time.
     */
    public double getAverageDelayTime() {
        return delayCount == 0 ? 0d : totalDelayTime / delayCount;
    }

    /**
     * Returns the number of delayed vehicles
     */
    public int getDelayCount() {
        return delayCount;
    }

    /**
     * Returns the destination
     */
    public SiteNode getDestination() {
        return destination;
    }

    /**
     * Returns the total delay time
     */
    public double getTotalDelayTime() {
        return totalDelayTime;
    }

    /**
     * Returns the number of vehicles
     */
    public int getVehicleCount() {
        return vehicleCount;
    }
}
