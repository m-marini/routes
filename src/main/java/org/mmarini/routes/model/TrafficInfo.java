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
 */
public class TrafficInfo {
    private int veicleCount;
    private int delayCount;
    private double totalDelayTime;
    private SiteNode destination;

    /**
     *
     */
    public TrafficInfo() {
    }

    /**
     * Get the average delay time.
     *
     * @return the average time
     */
    public double getAverageDelayTime() {
        final int ct = getDelayCount();
        if (ct == 0) {
            return 0.;
        }
        return getTotalDelayTime() / ct;
    }

    /**
     * @return the delayCount
     */
    public int getDelayCount() {
        return delayCount;
    }

    /**
     * @param delayCount the delayCount to set
     */
    public void setDelayCount(final int delayCount) {
        this.delayCount = delayCount;
    }

    /**
     * @return the destination
     */
    public SiteNode getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(final SiteNode destination) {
        this.destination = destination;
    }

    /**
     * @return the totalDelayTime
     */
    public double getTotalDelayTime() {
        return totalDelayTime;
    }

    /**
     * @param totalDelayTime the totalDelayTime to set
     */
    public void setTotalDelayTime(final double totalDelayTime) {
        this.totalDelayTime = totalDelayTime;
    }

    /**
     * @return the vehicleCount
     */
    public int getVeicleCount() {
        return veicleCount;
    }

    /**
     * @param veicleCount the veicleCount to set
     */
    public void setVeicleCount(final int veicleCount) {
        this.veicleCount = veicleCount;
    }

}
