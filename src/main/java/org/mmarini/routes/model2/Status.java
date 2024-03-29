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

import java.util.List;

public interface Status {

    /**
     * Returns the edge traffic level from 0 min to 1 max
     *
     * @param edge the edge
     */
    double edgeTrafficLevel(MapEdge edge);

    /**
     * Returns the edges
     */
    List<MapEdge> getEdges();

    /**
     * Returns the frequency of vehicle generation (#/s)
     */
    double getFrequency();

    /**
     * Returns the maximum number of vehicles
     */
    int getMaxVehicle();

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
     * Returns the simulation time
     */
    double getTime();

    /**
     * Returns the topology
     */
    Topology getTopology();

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
}
