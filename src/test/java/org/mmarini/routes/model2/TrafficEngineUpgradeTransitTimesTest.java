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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.Topology.createTopology;
import static org.mmarini.routes.model2.TrafficEngineImpl.createEngine;
import static org.mmarini.routes.model2.Vehicle.createVehicle;

class TrafficEngineUpgradeTransitTimesTest {

    static final double SPEED_LIMIT = 10.0;
    static final int PRIORITY = 0;
    static final int MAX_VEHICLES = 1000;

    static final double V0_TIME = 10;
    static final double V1_TIME = 5;

    static final double TIME = 100;

    @Test
    void updateTransitTime() {
        /*
        Given a traffic engine
        and a vehicle in an edge with a time in edge greater than last transit time
        and a vehicle in an edge with a time in edge less than last transit time
        and an edge without vehicles
         */
        /*
        Topology:
        0 --v0--> 1 --v1--> 2
          <------   <------
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        double edgeTransitTime = 50d / SPEED_LIMIT;
        double lowTime = edgeTransitTime * 0.5;
        double greatTime = edgeTransitTime * 1.5;
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setEdgeEntryTime(TIME - greatTime)
                .setDistance(10);
        Vehicle v1 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge12)
                .setEdgeEntryTime(TIME - lowTime)
                .setDistance(49);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), TIME,
                List.of(v0, v1), SPEED_LIMIT, 0);

        /*
        When upgrading the traffic times 
         */
        TrafficEngineImpl result = status.updateTransitTime();

        /*
        Then the traffic time of edge with first vehicle should be the time in edge of the vehicle  
         */
        assertThat(result.findEdgeTransitTime(edge01), closeTo(greatTime, 0.1));

        /*
        And the traffic time of edge with second vehicle should be not change  
         */
        assertThat(result.findEdgeTransitTime(edge12), closeTo(edgeTransitTime, 0.1));

        /*
        And the traffic time of empty edge should be not change   
         */
        assertThat(result.findEdgeTransitTime(edge10), closeTo(edgeTransitTime, 0.1));
        assertThat(result.findEdgeTransitTime(edge21), closeTo(edgeTransitTime, 0.1));
    }
}