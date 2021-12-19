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
    private final int waitingAtSite;
    private final double totalDelayTime;
    private final SiteNode site;

    /**
     * Create a traffic information record
     *
     * @param site           the site
     * @param vehicleCount   the number of vehicles to site destination
     * @param delayCount     the number of delayed vehicles to site destination
     * @param waitingAtSite  the number of waiting vehicles at site
     * @param totalDelayTime the total delay time to site destination
     */
    public TrafficInfo(SiteNode site, int vehicleCount, int delayCount, int waitingAtSite, double totalDelayTime) {
        this.vehicleCount = vehicleCount;
        this.delayCount = delayCount;
        this.waitingAtSite = waitingAtSite;
        this.totalDelayTime = totalDelayTime;
        this.site = site;
    }

    /**
     * Returns the average delay time to site destination
     */
    public double getAverageDelayTime() {
        return delayCount == 0 ? 0d : totalDelayTime / delayCount;
    }

    /**
     * Returns the number of delayed vehicles to site destination
     */
    public int getDelayCount() {
        return delayCount;
    }

    /**
     * Returns the site
     */
    public SiteNode getSite() {
        return site;
    }

    /**
     * Returns the total delay time to site destination
     */
    public double getTotalDelayTime() {
        return totalDelayTime;
    }

    /**
     * Returns the number of vehicles to site destination
     */
    public int getVehicleCount() {
        return vehicleCount;
    }

    /**
     * Returns the number of waiting vehicles at site
     */
    public int getWaitingAtSite() {
        return waitingAtSite;
    }
}
