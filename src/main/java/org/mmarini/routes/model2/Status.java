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

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;

public interface Status {

    /**
     * Returns the status with a new edge
     *
     * @param edge the edge
     */
    Status addEdge(MapEdge edge);

    /**
     * Returns the status with the new module
     *
     * @param module    the module
     * @param location  the module location
     * @param direction the directions
     * @param epsilon   the marginal distance to map existing nodes
     */
    Status addModule(final Module module, final Point2D location, final Point2D direction, final double epsilon);

    /**
     * Returns the status with changed edge
     *
     * @param oldEdge the old edge
     * @param newEdge the new edge
     */
    Status changeEdge(MapEdge oldEdge, MapEdge newEdge);

    /**
     * Returns the status with a node changed
     * if the node is a CrossNode it will b changed to SiteNode
     * if the node is a SiteNode it will b changed to CrossNode
     *
     * @param node the node
     */
    Status changeNode(MapNode node);

    /**
     * Returns the edges
     */
    List<MapEdge> getEdges();

    /**
     * Returns the frequency of vehicle generation (#/s)
     */
    double getFrequency();

    /**
     * Returns the status with changed frequency
     *
     * @param frequency the frequency
     */
    Status setFrequency(double frequency);

    /**
     * Returns the nodes
     */
    List<MapNode> getNodes();

    /**
     * Returns the path frequencies
     */
    DoubleMatrix<SiteNode> getPathFrequencies();

    /**
     * Returns the sites
     */
    List<SiteNode> getSites();

    /**
     * Returns the speed limit
     */
    double getSpeedLimit();

    /**
     * Returns the status by changing speed limit
     *
     * @param speedLimit the speed limit
     */
    Status setSpeedLimit(double speedLimit);

    /**
     * Returns the simulation time
     */
    double getTime();

    /**
     * Returns the traffic info list
     */
    List<TrafficInfo> getTrafficInfo();

    /**
     * Returns the vehicles
     */
    List<Vehicle> getVehicles();

    /**
     * Returns the weight Matrix
     */
    DoubleMatrix<SiteNode> getWeightMatrix();

    /**
     * Returns the next status after a time interval
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    Status next(Random random, double dt);

    /**
     * Returns the status with optimized edge speed limit
     */
    Status optimize();

    /**
     * Returns the status with randomized path weights
     *
     * @param random    the random generator
     * @param minWeight the minimum weight value
     */
    Status randomizeWeights(Random random, double minWeight);

    /**
     * Returns the status without an edge
     *
     * @param edge the edge
     */
    Status removeEdge(MapEdge edge);

    /**
     * Returns the status without a MapNode
     *
     * @param node the node
     */
    Status removeNode(MapNode node);

    /**
     * Returns the status with locations at a given offset
     *
     * @param offset the offset
     */
    Status setOffset(Point2D offset);

    /**
     * Returns the status with changed weights
     *
     * @param weights the weights
     */
    Status setWeights(double[][] weights);
}
