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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.MockRandomBuilder;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.StatusImpl.createStatus;
import static org.mmarini.routes.model2.TestUtils.optionalEmpty;
import static org.mmarini.routes.model2.TestUtils.optionalOf;
import static org.mmarini.routes.model2.Topology.createTopology;
import static org.mmarini.routes.model2.Vehicle.createVehicle;

class StatusImplChangeTopTest {
    static final double DELAY = 10;
    static final long SEED = 1234L;
    static final double MAX_COORDS = 1000.;
    static final int NO_SITES = 3;
    static final double SPEED_LIMIT = 10.0;
    static final int PRIORITY = 0;
    static final double RX0 = 0.2;
    static final double RY0 = 0.3;
    static final double RX1 = 0.8;
    static final double RY1 = 0.35;
    static final double RX2 = 0.1;
    static final double RY2 = 0.8;
    static final double X0 = min(min(RX0, RX1), RX2);
    static final double X1 = max(max(RX0, RX1), RX2);
    static final double DX = X1 - X0;
    static final double Y0 = min(min(RY0, RY1), RY2);
    static final double Y1 = max(max(RY0, RY1), RY2);
    static final double DY = Y1 - Y0;
    static final double MIN_MIN_WEIGHT = 0.01;
    static final double MAX_MIN_WEIGHT = 0.9;
    static final double MIN_FREQUENCY = 0.1;
    static final double MAX_FREQUENCY = 10;
    static final double MIN_RANDOM = 0.0;
    static final double MAX_RANDOM = 1 - 1e-10;
    static final double MIN_MAP_SIZE = 10;
    static final double MAX_MAP_SIZE = 10000;
    static final double MIN_SPEED_LIMIT = 1;
    static final double MAX_SPEED_LIMIT = 30;

    static Stream<Arguments> mapProfileArgs() {
        return ArgumentGenerator.create(SEED)
                .exponential(MIN_MAP_SIZE, MAX_MAP_SIZE)
                .exponential(MIN_MAP_SIZE, MAX_MAP_SIZE)
                .exponential(MIN_MIN_WEIGHT, MAX_MIN_WEIGHT)
                .uniform(MIN_FREQUENCY, MAX_FREQUENCY)
                .uniform(MIN_RANDOM, MAX_RANDOM)
                .uniform(MIN_RANDOM, MAX_RANDOM)
                .uniform(MIN_RANDOM, MAX_RANDOM)
                .uniform(MIN_RANDOM, MAX_RANDOM)
                .uniform(MIN_RANDOM, MAX_RANDOM)
                .uniform(MIN_RANDOM, MAX_RANDOM)
                .generate();
    }

    static Stream<Arguments> speedArgs() {
        return ArgumentGenerator.create(SEED)
                .exponential(MIN_SPEED_LIMIT, MAX_SPEED_LIMIT)
                .generate();
    }

    static Stream<Arguments> xyArgs() {
        return ArgumentGenerator.create(SEED)
                .uniform(-MAX_COORDS, MAX_COORDS)
                .uniform(-MAX_COORDS, MAX_COORDS)
                .generate();
    }

    @Test
    void addEdgeNewNode() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        /*
        And 3 vehicles in the edge
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v1 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(30);
        Vehicle v2 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge12)
                .setDistance(40);
        /*
        And the corresponding status
        with not default transit time
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge12)
                ), 0,
                List.of(v0, v1, v2), SPEED_LIMIT, 0)
                .setEdgeTravelTimes(edge01, edge01.getTransitTime() + DELAY)
                .setEdgeTravelTimes(edge12, edge12.getTransitTime() + DELAY);
        /*
        And a new edge
        3 --> 4
         */
        CrossNode node3 = createNode(0, 10);
        CrossNode node4 = createNode(100, 10);
        MapEdge edge34 = new MapEdge(node3, node4, SPEED_LIMIT, PRIORITY);

        /*
        When adding the new edge
         */
        StatusImpl result = status.addEdge(edge34);

        /*
        Than the topology should be
        0 ---> 1 ---> 2
          <----------
         */
        assertNotNull(result);
        assertThat(result.getSites(), contains(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3, node4));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge12, edge34));
        /*
        And the same vehicles in the same edges
         */
        assertThat(result.getVehicles(), containsInAnyOrder(v0, v1, v2));
        assertThat(result.getVehicles(edge01), contains(v0, v1));
        assertThat(result.getVehicles(edge12), contains(v2));
        /*
        And the old transit time plus the new one
         */
        assertThat(result.getEdgeTransitTime(edge01), equalTo(edge01.getTransitTime() + DELAY));
        assertThat(result.getEdgeTransitTime(edge12), equalTo(edge12.getTransitTime() + DELAY));
        assertThat(result.getEdgeTransitTime(edge34), equalTo(edge34.getTransitTime()));
    }

    @Test
    void addEdgeNoNewNode() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        /*
        And 3 vehicles in the edge
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v1 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(30);
        Vehicle v2 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge12)
                .setDistance(40);
        /*
        And the corresponding status
        with not default transit time
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge12)
                ), 0,
                List.of(v0, v1, v2), SPEED_LIMIT, 0)
                .setEdgeTravelTimes(edge01, edge01.getTransitTime() + DELAY)
                .setEdgeTravelTimes(edge12, edge12.getTransitTime() + DELAY);
        /*
        And a new edge
        0 <---------- 2
         */
        MapEdge edge20 = new MapEdge(node2, node0, SPEED_LIMIT, PRIORITY);

        /*
        When adding the new edge
         */
        StatusImpl result = status.addEdge(edge20);

        /*
        Than the topology should be
        0 ---> 1 ---> 2
          <----------
         */
        assertNotNull(result);
        assertThat(result.getSites(), contains(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge12, edge20));
        /*
        And the same vehicles in the same edges
         */
        assertThat(result.getVehicles(), containsInAnyOrder(v0, v1, v2));
        assertThat(result.getVehicles(edge01), contains(v0, v1));
        assertThat(result.getVehicles(edge12), contains(v2));
        /*
        And the old transit time plus the new one
         */
        assertThat(result.getEdgeTransitTime(edge01), equalTo(edge01.getTransitTime() + DELAY));
        assertThat(result.getEdgeTransitTime(edge12), equalTo(edge12.getTransitTime() + DELAY));
        assertThat(result.getEdgeTransitTime(edge20), equalTo(edge20.getTransitTime()));
    }

    @Test
    void addModule() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        /*
        And the corresponding status
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge12)
                ), 0,
                List.of(), SPEED_LIMIT, 0);

        /*
        And a module
            2
            |
            |
            v
            0 ---> 1
         */
        CrossNode moduleNode0 = createNode(0, 0);
        CrossNode moduleNode1 = createNode(10, 0);
        CrossNode moduleNode2 = createNode(0, 10);
        MapEdge moduleEdge01 = new MapEdge(moduleNode0, moduleNode1, SPEED_LIMIT, PRIORITY);
        MapEdge moduleEdge20 = new MapEdge(moduleNode2, moduleNode0, SPEED_LIMIT, PRIORITY);
        Module module = new Module(List.of(moduleEdge01, moduleEdge20));

        /*
        When adding the module
         */
        StatusImpl result = status.addModule(module,
                new Point2D.Double(51, 0),
                new Point2D.Double(1, 1),
                2);

        /*
        Than the topology should be
           4       3
            \     ^
             \   /
              v /
        0 ---> 1 ---> 2
         */
        assertNotNull(result);
        CrossNode expNode3 = createNode(51 + 10 * sqrt(0.5), 10 * sqrt(0.5));
        CrossNode expNode4 = createNode(51 - 10 * sqrt(0.5), 10 * sqrt(0.5));
        MapEdge edge13 = new MapEdge(node1, expNode3, SPEED_LIMIT, moduleEdge01.getPriority());
        MapEdge edge41 = new MapEdge(expNode4, node1, SPEED_LIMIT, moduleEdge20.getPriority());
        assertThat(result.getSites(), contains(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, expNode3, expNode4));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge12, edge13, edge41));
    }

    @Test
    void changeCrossNode() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        /*
        And 2 vehicles edge01
        And 1 vehicle edge12
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v1 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(30);
        Vehicle v2 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge12)
                .setDistance(25);
        /*
        And the related status with a modified transit time for edge01
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge12)
                ), 0,
                List.of(v0, v1, v2), SPEED_LIMIT, 0)
                .setEdgeTravelTimes(edge01, edge01.getTransitTime() + DELAY)
                .setWeights(new double[][]{
                        {0, 2},
                        {3, 0}
                });

        /*
        When change node1
         */
        StatusImpl result = status.changeNode(node1);

        /*
        Then the sites should be node0, node 2, site1
        And nodes should be node0, node 2, site1
        And edges should be newEdge01, newEdge12
         */
        assertNotNull(result);
        SiteNode site1 = createSite(50, 0);
        MapEdge newEdge01 = new MapEdge(node0, site1, SPEED_LIMIT, PRIORITY);
        MapEdge newEdge12 = new MapEdge(site1, node2, SPEED_LIMIT, PRIORITY);
        assertThat(result.getSites(), contains(node0, node2, site1));
        assertThat(result.getNodes(), containsInAnyOrder(node0, site1, node2));
        assertThat(result.getEdges(), containsInAnyOrder(newEdge01, newEdge12));

        /*
        And vehicles v1, v2, v3 should have current edge upgraded
         */
        Vehicle expV0 = v0.copy().setCurrentEdge(newEdge01);
        Vehicle expV1 = v1.copy().setCurrentEdge(newEdge01);
        Vehicle expV2 = v2.copy().setCurrentEdge(newEdge12);
        assertNotNull(result);
        assertThat(result.getVehicles(), containsInAnyOrder(expV0, expV1, expV2));
        /*
        And edge time be the corresponding of previous status
         */
        assertThat(result.getEdgeTransitTime(newEdge01), equalTo(newEdge01.getTransitTime() + DELAY));
        assertThat(result.getEdgeTransitTime(newEdge12), equalTo(newEdge12.getTransitTime()));
        assertThat(result.getEdgeTransitTime(newEdge12), equalTo(newEdge12.getTransitTime()));
        /*
        And vehicle in edge be the corresponding of previous status
         */
        assertThat(result.getVehicles(newEdge01), contains(expV0, expV1));
        assertThat(result.getVehicles(newEdge12), contains(expV2));
        /*
        And next vehicle be the expected
         */
        assertThat(result.getNextVehicle(expV0), optionalOf(equalTo(expV1)));
        assertThat(result.getNextVehicle(expV1), optionalEmpty());
        assertThat(result.getNextVehicle(expV2), optionalEmpty());
        /*
        And next vehicle be the expected
         */
        assertThat(result.getNextVehicle(expV0), optionalOf(equalTo(expV1)));
        assertThat(result.getNextVehicle(expV1), optionalEmpty());
        assertThat(result.getNextVehicle(expV2), optionalEmpty());
        /*
        And weights should be 0 for each new pair
         */
        assertThat(result.getWeightMatrix().getValues(), equalTo(new double[][]{
                {0, 2, 0},
                {3, 0, 0},
                {0, 0, 0}
        }));
    }

    @Test
    void changeEdge() {
        /*
        Given a topology of
        0 -------> 1 ---> 2
               3 ------->
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        CrossNode node3 = createNode(30, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge32 = new MapEdge(node3, node2, SPEED_LIMIT, PRIORITY);
        /*
        And 3 vehicles in the changing edge
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v1 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(30);
        Vehicle v2 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(40);
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node2, node1, node3),
                        List.of(edge01, edge12, edge32)
                ), 0,
                List.of(v0, v1, v2), SPEED_LIMIT, 0);
        /*
        And a new edge edge03
         */
        MapEdge edge03 = new MapEdge(node0, node3, SPEED_LIMIT, PRIORITY);

        /*
        When change the edge
         */
        StatusImpl result = status.changeEdge(edge01, edge03);
        /*
        Then vehicle v2 should be removed
         */
        assertNotNull(result);
        assertThat(result.getVehicles(), containsInAnyOrder(v0, v1));
        assertThat(result.getVehicles(edge03), contains(v0, v1));
        assertThat(result.getNextVehicle(v0), optionalOf(v1));
        assertThat(result.getNextVehicle(v1), optionalEmpty());
        assertThat(result.getEdgeTransitTime(edge03), equalTo(edge03.getTransitTime()));
    }

    @Test
    void changeSiteNode() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
         */
        SiteNode site0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node1 = createSite(50, 0);
        MapEdge edge01 = new MapEdge(site0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        /*
        And 2 vehicles edge01
        And 1 vehicle edge12
         */
        Vehicle v0 = createVehicle(node1, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v1 = createVehicle(node1, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(30);
        Vehicle v2 = createVehicle(site0, node2, 0)
                .setCurrentEdge(edge12)
                .setDistance(25);
        /*
        And the related status with a modified transit time for edge01, edge02
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(site0, node2, node1),
                        List.of(edge01, edge12)
                ), 0,
                List.of(v0, v1, v2), SPEED_LIMIT, 0)
                .setEdgeTravelTimes(edge01, edge01.getTransitTime() + DELAY)
                .setEdgeTravelTimes(edge12, edge12.getTransitTime() + DELAY)
                .setWeights(new double[][]{
                        {0, 1, 2},
                        {3, 0, 4},
                        {5, 6, 0}
                });

        /*
        When change node1
         */
        StatusImpl result = status.changeNode(site0);

        /*
        Then the sites should be node1, node2
        And nodes should be node0, node 2, site1
        And edges should be newEdge01, newEdge12
         */
        assertNotNull(result);
        CrossNode node0 = createNode(0, 0);
        MapEdge newEdge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge newEdge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        assertThat(result.getSites(), contains(node2, node1));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2));
        assertThat(result.getEdges(), containsInAnyOrder(newEdge01, newEdge12));

        /*
        And vehicles v1, v2 should have current edge upgraded
         */
        Vehicle expV0 = v0.copy().setCurrentEdge(newEdge01);
        Vehicle expV1 = v1.copy().setCurrentEdge(newEdge01);
        assertNotNull(result);
        assertThat(result.getVehicles(), containsInAnyOrder(expV0, expV1));
        /*
        And edge time be the corresponding of previous status
         */
        assertThat(result.getEdgeTransitTime(newEdge01), equalTo(newEdge01.getTransitTime() + DELAY));
        assertThat(result.getEdgeTransitTime(newEdge12), equalTo(newEdge12.getTransitTime()));

        /*
        And vehicle in edge be the corresponding of previous status
         */
        assertThat(result.getVehicles(newEdge01), contains(expV0, expV1));
        assertThat(result.getVehicles(newEdge12), empty());
        /*
        And next vehicle be the expected
         */
        assertThat(result.getNextVehicle(expV0), optionalOf(equalTo(expV1)));
        assertThat(result.getNextVehicle(expV1), optionalEmpty());
        /*
        And next vehicle be the expected
         */
        assertThat(result.getNextVehicle(expV0), optionalOf(equalTo(expV1)));
        assertThat(result.getNextVehicle(expV1), optionalEmpty());
        /*
        And weights should be 1 for each pair
         */
        assertThat(result.getWeightMatrix().getValues(), equalTo(new double[][]{
                {0, 4},
                {6, 0},
        }));
    }

    @ParameterizedTest
    @MethodSource("mapProfileArgs")
    void createRandom(double width, double height,
                      double minWeight, double frequency,
                      double rw01, double rw02,
                      double rw10, double rw12,
                      double rw20, double rw21
    ) {
        /*
        Given a map profile
         */
        MapProfile profile = new MapProfile(NO_SITES, width, height, minWeight, frequency);
        /*
        And a random generator generating 5 * 2 values (coordinates)
         */
        Random random = new MockRandomBuilder()
                .nextDouble(RX0, RY0)
                .nextDouble(RX1, RY1)
                .nextDouble(RX2, RY2)
                .nextDouble(rw01, rw02, rw10, rw12, rw20, rw21)
                .build();

        /*
        When creating a random map
         */
        StatusImpl result = StatusImpl.createRandom(random, profile, SPEED_LIMIT);

        /*
        Then the sites should be the 5 sites
        And nodes should be the 5 sites
        And edges should be empty
         */
        assertNotNull(result);
        SiteNode site0 = createSite(
                ((RX0 - X0) / DX - 0.5) * 2 * width,
                ((RY0 - Y0) / DY - 0.5) * 2 * height);
        SiteNode site1 = createSite(
                ((RX1 - X0) / DX - 0.5) * 2 * width,
                ((RY1 - Y0) / DY - 0.5) * 2 * height);
        SiteNode site2 = createSite(
                ((RX2 - X0) / DX - 0.5) * 2 * width,
                ((RY2 - Y0) / DY - 0.5) * 2 * height);

        assertNotNull(result);
        assertThat(result.getSites(), contains(site0, site1, site2));
        assertThat(result.getNodes(), contains(site0, site1, site2));
        assertThat(result.getEdges(), empty());

        /*
        And vehicles should be empty
         */
        assertThat(result.getVehicles(), empty());

        /*
        And weight be ...
         */
        double w01 = rw01 * (1 - minWeight) + minWeight;
        double w02 = rw02 * (1 - minWeight) + minWeight;
        double w10 = rw10 * (1 - minWeight) + minWeight;
        double w12 = rw12 * (1 - minWeight) + minWeight;
        double w20 = rw20 * (1 - minWeight) + minWeight;
        double w21 = rw21 * (1 - minWeight) + minWeight;
        assertThat(result.getWeightMatrix().getValues()[0][0], equalTo(0d));
        assertThat(result.getWeightMatrix().getValues()[0][1], closeTo(w01, 1e-6));
        assertThat(result.getWeightMatrix().getValues()[0][2], closeTo(w02, 1e-6));

        assertThat(result.getWeightMatrix().getValues()[1][0], closeTo(w10, 1e-6));
        assertThat(result.getWeightMatrix().getValues()[1][1], equalTo(0d));
        assertThat(result.getWeightMatrix().getValues()[1][2], closeTo(w12, 1e-6));

        assertThat(result.getWeightMatrix().getValues()[2][0], closeTo(w20, 1e-6));
        assertThat(result.getWeightMatrix().getValues()[2][1], closeTo(w21, 1e-6));
        assertThat(result.getWeightMatrix().getValues()[2][2], equalTo(0d));


        /*
        And frequency should be the selected
         */
        assertThat(result.getFrequency(), equalTo(frequency));
    }

    @ParameterizedTest
    @MethodSource("speedArgs")
    void optimize(double speedLimit) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
                      3
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(10, 0);
        CrossNode node3 = createNode(10, 50);
        CrossNode node1 = createNode(5, 0);
        MapEdge edge01 = new MapEdge(node0, node1, 1, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, 1, PRIORITY);

        /*
        And a vehicle v0 in the removing edge,
            a vehicle v1 in remaining edge and with path to remaining sites
        */
        Vehicle v0 = createVehicle(node0, node2, 0).setCurrentEdge(edge01);
        Vehicle v1 = createVehicle(node0, node2, 0).setCurrentEdge(edge12);
        /*
        And a status
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node1, node2, node3),
                        List.of(edge01, edge12)
                ), 0,
                List.of(v0, v1), speedLimit, 0);

        /*
        When removing node3
         */
        StatusImpl result = status.optimize();

        /*
        Then the topology should be
        0 ---> 1 ---> 2
         */
        assertNotNull(result);
        assertThat(result.getSites(), contains(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2));
        assertThat(result.getEdges(), containsInAnyOrder(
                allOf(
                        hasProperty("begin", equalTo(node0)),
                        hasProperty("end", equalTo(node1)),
                        hasProperty("speedLimit", equalTo(min(speedLimit, edge01.getSafetySpeed())))
                ),
                allOf(
                        hasProperty("begin", equalTo(node1)),
                        hasProperty("end", equalTo(node2)),
                        hasProperty("speedLimit", equalTo(min(speedLimit, edge12.getSafetySpeed())))
                )
        ));

        /*
        And the speed limit should be the safety speed
         */

        /*
        And the vehicles should be v3
         */
        assertThat(result.getVehicles(), contains(v0, v1));
    }

    @Test
    void removeCrossNode() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
               1 ---> 3
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node3 = createNode(100, 50);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge13 = new MapEdge(node1, node3, SPEED_LIMIT, PRIORITY);
        /*
        And a vehicle v0 in the removing edge,
            a vehicle v1 in remaining edge and with path to remaining sites
        */
        Vehicle v0 = createVehicle(node0, node2, 0).setCurrentEdge(edge13);
        Vehicle v1 = createVehicle(node0, node2, 0).setCurrentEdge(edge12);
        /*
        And a status
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node1, node2, node3),
                        List.of(edge01, edge12, edge13)
                ), 0,
                List.of(v0, v1), SPEED_LIMIT, 0);

        /*
        When removing node3
         */
        StatusImpl result = status.removeNode(node3);

        /*
        Then the topology should be
        0 ---> 1 ---> 2
         */
        assertNotNull(result);
        assertThat(result.getSites(), contains(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge12));

        /*
        And the vehicles should be v3
         */
        assertThat(result.getVehicles(), contains(v1));
    }

    // vehicle in a removed edge
    @Test
    void removeEdge() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        /*
        And a vehicle in a removing edge
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setReturning(true)
                .setDistance(10);
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v0), SPEED_LIMIT, 0);

        /*
        When change the topology
         */
        Status result = status.removeEdge(edge10);
        /*
        Then the vehicles should be removed
         */
        assertNotNull(result);
        assertThat(result.getEdges(), not(hasItem(edge10)));
        assertThat(result.getVehicles(), empty());
    }

    @Test
    void removeSiteNode() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
               1 ---> 3
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(100, 50);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge13 = new MapEdge(node1, node3, SPEED_LIMIT, PRIORITY);
        /*
        And a vehicle v0 in the removing edge,
            a vehicle v1 from removing site
            a vehicle v2 to removing site
            a vehicle v3 in remaining edge and with path to remaining sites
        */
        Vehicle v0 = createVehicle(node0, node2, 0).setCurrentEdge(edge13);
        Vehicle v1 = createVehicle(node3, node2, 0).setCurrentEdge(edge01);
        Vehicle v2 = createVehicle(node0, node3, 0).setCurrentEdge(edge12);
        Vehicle v3 = createVehicle(node0, node2, 0).setCurrentEdge(edge12).setDistance(10);
        /*
        And a status
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node1, node2, node3),
                        List.of(edge01, edge12, edge13)
                ), 0,
                List.of(v0, v1, v2, v3), SPEED_LIMIT, 0);

        /*
        When removing node3
         */
        StatusImpl result = status.removeNode(node3);

        /*
        Then the topology should be
        0 ---> 1 ---> 2
         */
        assertNotNull(result);
        assertThat(result.getSites(), contains(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge12));

        /*
        And the vehicles should be v3
         */
        assertThat(result.getVehicles(), contains(v3));
    }

    @ParameterizedTest
    @MethodSource("xyArgs")
    void setOffset(double x, double y) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
               1 ---> 3
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(100, 50);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge13 = new MapEdge(node1, node3, SPEED_LIMIT, PRIORITY);
        /*
        And a vehicle v0 in the removing edge,
            a vehicle v1 from removing site
            a vehicle v2 to removing site
            a vehicle v3 in remaining edge and with path to remaining sites
        */
        Vehicle v0 = createVehicle(node0, node2, 0).setCurrentEdge(edge13);
        Vehicle v1 = createVehicle(node3, node2, 0).setCurrentEdge(edge01);
        Vehicle v2 = createVehicle(node0, node3, 0).setCurrentEdge(edge12);
        Vehicle v3 = createVehicle(node0, node2, 0).setCurrentEdge(edge12).setDistance(10);
        /*
        And a status
         */
        StatusImpl status = createStatus(
                createTopology(
                        List.of(node0, node1, node2, node3),
                        List.of(edge01, edge12, edge13)
                ), 0,
                List.of(v0, v1, v2, v3), SPEED_LIMIT, 0);

        /*
        When setting offset
         */
        StatusImpl result = status.setOffset(new Point2D.Double(x, y));

        /*
        Then the topology should be translated
        0 ---> 1 ---> 2
               1 ---> 3
         */
        assertNotNull(result);
        SiteNode expNode0 = createSite(0 - x, 0 - y);
        SiteNode expNode2 = createSite(100 - x, 0 - y);
        SiteNode expNode3 = createSite(100 - x, 50 - y);
        CrossNode expNode1 = createNode(50 - x, 0 - y);
        MapEdge expEdge01 = new MapEdge(expNode0, expNode1, SPEED_LIMIT, PRIORITY);
        MapEdge expEdge12 = new MapEdge(expNode1, expNode2, SPEED_LIMIT, PRIORITY);
        MapEdge expEdge13 = new MapEdge(expNode1, expNode3, SPEED_LIMIT, PRIORITY);

        assertThat(result.getSites(), contains(expNode0, expNode2, expNode3));
        assertThat(result.getNodes(), containsInAnyOrder(expNode0, expNode1, expNode2, expNode3));
        assertThat(result.getEdges(), containsInAnyOrder(expEdge01, expEdge12, expEdge13));

        /*
        And the vehicles should be v0', v01', v02', v03'
         */
        Vehicle expV0 = v0.setDeparture(expNode0).setDestination(expNode2).setCurrentEdge(expEdge13);
        Vehicle expV1 = v1.setDeparture(expNode3).setDestination(expNode2).setCurrentEdge(expEdge01);
        Vehicle expV2 = v2.setDeparture(expNode0).setDestination(expNode3).setCurrentEdge(expEdge12);
        Vehicle expV3 = v3.setDeparture(expNode0).setDestination(expNode2).setCurrentEdge(expEdge12);
        assertThat(result.getVehicles(), contains(expV0, expV1, expV2, expV3));

        /*
        And vehicles by edge
         */
        assertThat(result.getVehicles(expEdge01), contains(expV1));
        assertThat(result.getVehicles(expEdge12), contains(expV2, expV3));
        assertThat(result.getVehicles(expEdge13), contains(expV0));
    }
}