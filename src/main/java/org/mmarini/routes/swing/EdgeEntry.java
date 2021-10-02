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

package org.mmarini.routes.swing;

import org.mmarini.routes.model.MapEdge;

/**
 *
 */
public class EdgeEntry {
    private final MapEdge edge;
    private final String name;
    private final String beginName;
    private final String endName;
    private final int priority;
    private final double speedLimit;

    /**
     * @param edge       the edge
     * @param name       the edge name
     * @param beginName  the begin node name
     * @param endName    the end node name
     * @param priority   the priority value
     * @param speedLimit the speed limit
     */
    public EdgeEntry(MapEdge edge, String name, String beginName, String endName, int priority, double speedLimit) {
        this.edge = edge;
        this.name = name;
        this.beginName = beginName;
        this.endName = endName;
        this.priority = priority;
        this.speedLimit = speedLimit;
    }

    public String getBeginName() {
        return beginName;
    }

    public MapEdge getEdge() {
        return edge;
    }

    public String getEndName() {
        return endName;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * @param priority
     */
    public EdgeEntry setPriority(int priority) {
        return new EdgeEntry(edge, name, beginName, endName, priority, speedLimit);
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * @param speedLimit
     */
    public EdgeEntry setSpeedLimit(double speedLimit) {
        return new EdgeEntry(edge, name, beginName, endName, priority, speedLimit);
    }

    @Override
    public String toString() {
        return name;
    }
}
