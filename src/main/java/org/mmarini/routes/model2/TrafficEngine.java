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

import org.mmarini.Tuple2;

import java.awt.geom.Point2D;
import java.util.Random;

public interface TrafficEngine {

    /**
     * Returns the status with a new edge
     *
     * @param edge the edge
     */
    TrafficEngine addEdge(MapEdge edge);

    /**
     * Returns the status with the new mapModule
     *
     * @param mapModule the mapModule
     * @param location  the mapModule location
     * @param direction the directions
     * @param epsilon   the marginal distance to map existing nodes
     */
    TrafficEngine addModule(final MapModule mapModule, final Point2D location, final Point2D direction, final double epsilon);

    /**
     * Returns the status
     */
    StatusImpl buildStatus();

    /**
     * Returns the status with changed edge
     *
     * @param oldEdge the old edge
     * @param newEdge the new edge
     */
    TrafficEngine changeEdge(MapEdge oldEdge, MapEdge newEdge);

    /**
     * Returns the status with a node changed
     * if the node is a CrossNode it will b changed to SiteNode
     * if the node is a SiteNode it will b changed to CrossNode
     *
     * @param node the node
     */
    TrafficEngine changeNode(MapNode node);

    /**
     * Returns the next status after a time interval
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    Tuple2<TrafficEngine, Double> next(Random random, double dt);

    /**
     * Returns the status with optimized nodes
     */
    TrafficEngine optimizeNodes();

    /**
     * Returns the status with optimized edge speed limit
     */
    TrafficEngine optimizeSpeed(double speedLimit);

    /**
     * Returns the status with randomized path weights
     *
     * @param random    the random generator
     * @param minWeight the minimum weight value
     */
    TrafficEngine randomizeWeights(Random random, double minWeight);

    /**
     * Returns the status without an edge
     *
     * @param edge the edge
     */
    TrafficEngine removeEdge(MapEdge edge);

    /**
     * Returns the status without a MapNode
     *
     * @param node the node
     */
    TrafficEngine removeNode(MapNode node);

    /**
     * Returns the status with changed frequency
     *
     * @param frequency the frequency
     */
    TrafficEngine setFrequency(double frequency);

    /**
     * Returns the status with locations at a given offset
     *
     * @param offset the offset
     */
    TrafficEngine setOffset(Point2D offset);

    /**
     * Returns the status by changing speed limit
     *
     * @param speedLimit the speed limit
     */
    TrafficEngine setSpeedLimit(double speedLimit);

    /**
     * Returns the status with changed weights
     *
     * @param weights the weights
     */
    TrafficEngine setWeights(double[][] weights);
}
