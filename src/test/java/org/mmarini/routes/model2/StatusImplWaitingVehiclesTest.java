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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.StatusImpl.createStatus;
import static org.mmarini.routes.model2.TestUtils.optionalEmpty;
import static org.mmarini.routes.model2.TestUtils.optionalOf;
import static org.mmarini.routes.model2.Topology.createTopology;
import static org.mmarini.routes.model2.Vehicle.createVehicle;

class StatusImplWaitingVehiclesTest {

    static final int X0 = 0;
    static final int Y0 = 0;
    static final int X1 = 50;
    static final int Y1 = 0;
    static final int X2 = 100;
    static final int Y2 = 0;
    static final int X3 = 100;
    static final int Y3 = 100;
    static final long SEED = 1234L;
    static final double MIN_TIME = 0.0;
    static final double MAX_TIME = 10;
    static final double SPEED_LIMIT = 10.0;
    static final int PRIORITY = 0;

    static Stream<Arguments> argForTimeAndElapsed() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .uniform(MIN_TIME, MAX_TIME)
                .generate();
    }

    SiteNode node0;
    CrossNode node1;
    SiteNode node2;
    SiteNode node3;
    MapEdge edge01;
    MapEdge edge10;
    MapEdge edge12;
    MapEdge edge21;
    Topology topology;


    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles1AtSiteWithNoPath(double time, double elapsed) {
        /*
        Given a topology of
        0 ---> 1 ---> 2    3/v1
          <---   <---
        And vehicle1 waiting on site3 to any without path
         */
        Vehicle v1 = createVehicle(node3, node0, 0);
        StatusImpl status = createStatus(topology, time,
                List.of(v1), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle1 should be removed
        assertThat(status.getVehicles(), not(contains(v1)));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles2AtSiteBusyPath(double time, double elapsed) {
        /*
        Given a topology of
        0/v1 v0---> 1 ---> 2    3
             <-----   <---
        And vehicle0 at entry of edge01 (busy edge)
        And vehicle2 waiting on site0 to site 2 with busy path (edge01)
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01);
        Vehicle v2 = createVehicle(node0, node2, 0);

        StatusImpl status = createStatus(topology, time,
                List.of(v0, v2), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle2 should stay
        assertThat(v2.getCurrentEdge(), optionalEmpty());
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles3AtSiteAvailablePath(double time, double elapsed) {
        /*
        Given a topology of
        0 ---> 1 ------> 2/v3    3
          <---   <-v6---
        And vehicle3 waiting on site2 to site 0 with available path (edge21)
         */
        Vehicle v3 = createVehicle(node0, node2, 0)
                .setReturning(true);
        Vehicle v6 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge21)
                .setDistance(edge21.getLength() - 10)
                .setReturning(true)
                .setEdgeEntryTime(time - elapsed);
        StatusImpl status = createStatus(topology, time,
                List.of(v3, v6), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle3 should go
        assertThat(v3.getCurrentEdge(), optionalOf(edge21));
        assertThat(v3.getDistance(), equalTo(0.0));
        assertThat(status.getNextVehicle(v3), optionalOf(v6));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles4AtEdgeWithoutPath(double time, double elapsed) {
        /*
        Given a topology of
        0 ---> 1 ---v4> 2    3
          <---   <-----
        And vehicle4 waiting on edge12 to site3 without path
         */
        Vehicle v4 = createVehicle(node0, node3, 0)
                .setCurrentEdge(edge12)
                .setDistance(edge12.getLength());

        StatusImpl status = createStatus(topology, time,
                List.of(v4), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle4 should be removed
        assertThat(status.getVehicles(), not(contains(v4)));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles5AtEdgeBusyPath(double time, double elapsed) {
        /*
        Given a topology of
        0 v0---> 1 ---> 2    3
          <v5---   <---
         */
        /*
        And vehicle0 at entry of edge01 (busy edge)
        And vehicle5 waiting on edge10 to site2 with busy path (edge01)
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01);
        Vehicle v5 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setDistance(edge10.getLength());

        StatusImpl status = createStatus(topology, time,
                List.of(v0, v5), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle5 should stay
        assertThat(v5.getCurrentEdge(), optionalOf(edge10));
        assertThat(v5.getDistance(), equalTo(edge10.getLength()));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles6AtEdgeAvailablePath(double time, double elapsed) {
        /*
        Given a topology of
        0 ------> 1 -------> 2    3
          <-v7---   <v6---v5
        And vehicle6 waiting on edge21 to site0 with available path (edge10)
         */
        Vehicle v5 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge21)
                .setReturning(true);
        Vehicle v6 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge21)
                .setDistance(edge21.getLength())
                .setReturning(true)
                .setEdgeEntryTime(time - elapsed);
        Vehicle v7 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setDistance(edge10.getLength() - 10)
                .setReturning(true);

        StatusImpl status = createStatus(topology, time,
                List.of(v5, v6, v7), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle6 should go
        assertThat(v6.getCurrentEdge(), optionalOf(edge10));
        assertThat(v6.getDistance(), equalTo(0.0));
        assertThat(v6.getEdgeEntryTime(), equalTo(time));
        assertThat(status.getNextVehicle(v6), optionalOf(v7));
        assertThat(status.getEdgeTransitTime(edge21), closeTo(elapsed, 0.01));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehiclest7AtDestination(double time, double elapsed) {
        /*
        Given a topology of
        0 ---> 1 ---v7> 2    3
          <---   <-----
        And vehicle6 waiting on edge21 to site0 with available path (edge10)
         */
        Vehicle v7 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge12)
                .setDistance(edge12.getLength());

        StatusImpl status = createStatus(topology, time,
                List.of(v7), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle7 should return
        assertTrue(v7.isReturning());
        assertThat(v7.getCurrentEdge(), optionalOf(edge21));
        assertThat(v7.getDistance(), equalTo(0.0));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehiclest8ReturnToDeparterue(double time, double elapsed) {
        /*
        Given a topology of
        0 ---> 1 ---v7> 2    3
          <---   <-----
        And vehicle8 waiting on edge12 returning site2
         */
        Vehicle v8 = createVehicle(node2, node0, 0)
                .setCurrentEdge(edge12)
                .setReturning(true)
                .setDistance(edge12.getLength());

        StatusImpl status = createStatus(topology, time,
                List.of(v8), SPEED_LIMIT, 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle7 should be removed
        assertThat(status.getVehicles(), not(contains(v8)));
    }

    @BeforeEach
    void init() {
        node0 = createSite(X0, Y0);
        node3 = createSite(X3, Y3);
        node2 = createSite(X2, Y2);
        node1 = createNode(X1, Y1);
        edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        topology = createTopology(
                List.of(node0, node2, node1, node3),
                List.of(edge01, edge10, edge12, edge21)
        );
    }
}