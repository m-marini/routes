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
 * @version $Id: SimContextImpl.java,v 1.6 2010/10/19 20:33:00 marco Exp $
 */
public class SimContextImpl implements SimContext {
    private double time;

    private Simulator simulator;

    /**
     *
     */
    public SimContextImpl() {
    }

    /**
     * @see org.mmarini.routes.model.SimContext#findNextEdge(org.mmarini.routes.model.MapNode,
     * org.mmarini.routes.model.MapNode)
     */
    @Override
    public MapEdge findNextEdge(final MapNode from, final MapNode to) {
        return getSimulator().findNextEdge(from, to);
    }

    /**
     * @return the simulator
     */
    private Simulator getSimulator() {
        return simulator;
    }

    /**
     * @param simulator the simulator to set
     */
    public void setSimulator(final Simulator simulator) {
        this.simulator = simulator;
    }

    /**
     * @see org.mmarini.routes.model.SimContext#getTime()
     */
    @Override
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(final double time) {
        this.time = time;
    }

    /**
     * @see org.mmarini.routes.model.SimContext#removeVeicle(Vehicle)
     */
    @Override
    public void removeVeicle(final Vehicle veicle) {
        getSimulator().remove(veicle);
    }
}
