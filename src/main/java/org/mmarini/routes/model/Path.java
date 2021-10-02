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

/**
 *
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Path.java,v 1.3 2010/10/19 20:33:00 marco Exp $
 *
 */
public class Path implements Cloneable {
    private SiteNode departure;
    private SiteNode destination;
    private double weight;

    public Path(SiteNode departure, SiteNode destination, double weight) {
        this.departure = departure;
        this.destination = destination;
        this.weight = weight;
    }

    /**
     *
     */
    public Path() {
    }

    /**
     *
     * @param path
     */
    public Path(final Path path) {
        departure = path.departure;
        destination = path.destination;
        weight = path.weight;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Path clone() {
        return new Path(this);
    }

    /**
     * @return the departure
     */
    public SiteNode getDeparture() {
        return departure;
    }

    /**
     * @param departure the departure to set
     */
    public void setDeparture(final SiteNode departure) {
        this.departure = departure;
    }

    /**
     * @return the arrival
     */
    public SiteNode getDestination() {
        return destination;
    }

    /**
     * @param arrival the arrival to set
     */
    public void setDestination(final SiteNode arrival) {
        this.destination = arrival;
    }

    /**
     * @return the frequence
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param frequence the frequence to set
     */
    public void setWeight(final double frequence) {
        this.weight = frequence;
    }

}
