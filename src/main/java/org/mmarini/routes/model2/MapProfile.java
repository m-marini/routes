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
 * @author Marco
 */
public class MapProfile {
    private final int siteCount;
    private final double width;
    private final double height;
    private final double minWeight;
    private final double frequency;

    /**
     * @param siteCount
     * @param width
     * @param height
     * @param minWeight
     * @param frequency
     */
    public MapProfile(int siteCount, double width, double height, double minWeight, double frequency) {
        this.siteCount = siteCount;
        this.width = width;
        this.height = height;
        this.minWeight = minWeight;
        this.frequency = frequency;
    }

    /**
     * @return the frequence
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @return the minWeight
     */
    public double getMinWeight() {
        return minWeight;
    }

    /**
     * @return the siteCount
     */
    public int getSiteCount() {
        return siteCount;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }
}
