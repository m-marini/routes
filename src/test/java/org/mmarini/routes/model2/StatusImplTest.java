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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.MockRandomBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.routes.model2.TestUtils.optionalEmpty;
import static org.mmarini.routes.model2.TestUtils.optionalOf;

class StatusImplTest {

    public static final double MIN_FREQUENCY = 0.1;
    public static final double MAX_FREQUENCY = 1.0;
    static final double MIN_DT = 0.1;
    static final double MAX_DT = 0.5;
    static final long SEED = 1234L;
    static final double MIN_TIME = 0.0;
    static final double MAX_TIME = 10;
    static final double SPEED_LIMIT = 10.0;
    static final int PRIORITY = 0;
    static final int X1 = 0;
    static final int Y1 = 0;
    static final int Y2 = 100;
    static final int X2 = 100;
    static final double DISTANCE10 = 10;
    static final int HIGH_PRIORITY = 1;

    static Stream<Arguments> argForTimeAndElapsed() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .uniform(MIN_TIME, MAX_TIME)
                .generate();
    }

    static Stream<Arguments> argForTimeDtFreq() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .exponential(MIN_DT, MAX_DT)
                .exponential(MIN_FREQUENCY, MAX_FREQUENCY)
                .generate();
    }

    static Stream<Arguments> argsForDt() {
        return ArgumentGenerator.create(SEED)
                .exponential(MIN_DT, MAX_DT)
                .generate();
    }

    static Stream<Arguments> time() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .generate().limit(1);
    }

    static Stream<Arguments> times() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .exponential(MIN_DT, MAX_DT)
                .generate();
    }

    @ParameterizedTest
    @MethodSource("time")
    void applyTimeInterval(double time) {
        /*
        Given a topology of
        0 --1--> 1 -----> 2
          <-----   <--0--
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        /*
        And a vehicle 0 returning and arriving to departure (edge10, 49.5)
        And a vehicle 1  arriving to the destination and then returning (edge12. 49.5)
        And a vehicle 2 crossing a free edge (edge01, 49.5)
        And a vehicle 3 crossing a busy edge (edge21, 49.5)
        And a vehicle 4 running the edge (edge10, 30)
        And a vehicle 5 braking for next vehicle (edge10, 20)
         */
        Vehicle v0 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setReturning(true)
                .setDistance(49.5);
        Vehicle v1 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge12)
                .setDistance(49.5);
        Vehicle v2 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(49.5);
        Vehicle v3 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge21)
                .setDistance(49.5);
        Vehicle v4 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setDistance(30);
        Vehicle v5 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setDistance(20);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time,
                List.of(v0, v1, v2, v3, v4, v5), 0);
        Random random = new MockRandomBuilder()
                .nextDouble(0) // No vehicle creation probability
                .nextDouble(0) // No vehicle creation probability
                .build();

        /*
        When handling vehicles
         */
        status.applyTimeInterval(random, MIN_DT);

        // Then vehicle 0 returning and arriving to departure (edge10, 49.5) should be removed
        assertThat(status.getVehicles(), not(hasItem(v0)));
        // And a vehicle 1 arriving to the destination and then returning (edge12. 49.5) should bo at (edge21, 0.5)
        assertTrue(v1.isReturning());
        assertThat(v1.getCurrentEdge(), optionalOf(edge21));
        assertThat(v1.getDistance(), equalTo(0.5));
        // And a vehicle 2 crossing a free edge (edge01, 49.5) should be at edge12,0.5
        assertThat(v2.getCurrentEdge(), optionalOf(edge12));
        assertThat(v2.getDistance(), equalTo(0.5));
        // And a vehicle 3 crossing a busy edge (edge21, 49.5) should be at edge21, 50
        assertThat(v3.getCurrentEdge(), optionalOf(edge21));
        assertThat(v3.getDistance(), equalTo(50.0));
        // And a vehicle 4 running the edge (edge10, 30) should be at edge10, 31
        assertThat(v4.getCurrentEdge(), optionalOf(edge10));
        assertThat(v4.getDistance(), equalTo(31.0));
        // And a vehicle 5 braking for next vehicle (edge10, 20) should be at edge21, 20.5
        assertThat(v5.getCurrentEdge(), optionalOf(edge10));
        assertThat(v5.getDistance(), closeTo(20.5, 0.1));
    }

    /*
    SafetyDistance is 10 m/s * 1 s + 5 m = 15 m
    Max movement is 10 m/s * 0.1 s = 1 m
     */
    @ParameterizedTest
    @CsvSource(value = {
            "10, 20, 0.1, 1, 0.1, false", // next faraway delta = 1m
            "10, 10, 0.1, 0.5, 0.1, false", // next near delta = (10-5)/ (1+1/0.1) = 0.5m
    })
    void computeVehicleMovement(double d1,
                                double dd2,
                                double dt,
                                double expectedPosition,
                                double expectedTime,
                                boolean expectedAtEdgeEnd) {
        /*
        Given a status with 2 vehicle at a distance dd2 each other
         */
        double d2 = d1 + dd2;
        SiteNode site1 = SiteNode.createSite(X1, Y1);
        SiteNode site2 = SiteNode.createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle1 = Vehicle.create(site1, site2, 0)
                .setDistance(d1)
                .setCurrentEdge(edge1);
        Vehicle vehicle2 = Vehicle.create(site1, site2, 0)
                .setCurrentEdge(edge1)
                .setDistance(d2);
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(site1, site2),
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1, vehicle2), 0);

        /*
        When computing location of vehicle1 after dt time interval
         */
        StatusImpl.VehicleMovement result = status.computeVehicleMovement(vehicle1, dt);

        /*
        Then the movement should refer to the vehicle
         */
        assertThat(result.getVehicle(), equalTo(vehicle1));
        /* And should have the expected movement */
        assertThat(result.getDs(), closeTo(expectedPosition, 0.1));
        /* And should have the expected elapsed time */
        assertThat(result.getDt(), closeTo(expectedTime, 0.1));
        /* And should have the end edge predicate */
        assertThat(result.isAtEdgeEnd(), equalTo(expectedAtEdgeEnd));
    }

    @ParameterizedTest
    @MethodSource("argsForDt")
    void computeVehicleMovementExiting(double dt) {
        /*
        Given a status with a vehicle without any edge
         */
        SiteNode site1 = SiteNode.createSite(X1, Y1);
        SiteNode site2 = SiteNode.createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        double edgeLength = edge1.getLength();
        double distance = edgeLength - MIN_DT * SPEED_LIMIT + 0.01;
        Vehicle vehicle1 = Vehicle.create(site1, site2, 0)
                .setDistance(distance)
                .setCurrentEdge(edge1);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(site1, site2),
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1), 0);
        /*
        When computing location of vehicle1 after dt time interval
         */
        StatusImpl.VehicleMovement result = status.computeVehicleMovement(vehicle1, dt);

        /*
        Then the movement should refer to the vehicle
         */
        assertThat(result.getVehicle(), equalTo(vehicle1));
        /* And should have the expected movement */
        assertThat(result.getDs(), closeTo(edgeLength - distance, 0.1));
        /* And should have the expected elapsed time */
        assertThat(result.getDt(), closeTo((edgeLength - distance) / SPEED_LIMIT, 0.01));
        /* And should have the end edge predicate */
        assertTrue(result.isAtEdgeEnd());
    }

    @ParameterizedTest
    @MethodSource("argsForDt")
    void computeVehicleMovementNoEdge(double dt) {
        /*
        Given a status with a vehicle without any edge
         */
        SiteNode site1 = SiteNode.createSite(X1, Y1);
        SiteNode site2 = SiteNode.createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle1 = Vehicle.create(site1, site2, 0);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(site1, site2),
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1), 0);
        /*
        When computing location of vehicle1 after dt time interval
         */
        StatusImpl.VehicleMovement result = status.computeVehicleMovement(vehicle1, dt);

        /*
        Then the movement should refer to the vehicle
         */
        assertThat(result.getVehicle(), equalTo(vehicle1));
        /* And should have the expected movement */
        assertThat(result.getDs(), closeTo(0, 0.1));
        /* And should have the expected elapsed time */
        assertThat(result.getDt(), closeTo(dt, 0.01));
        /* And should have the end edge predicate */
        assertFalse(result.isAtEdgeEnd());
    }

    /*
   SafetyDistance is 10 m/s * 1 s + 5 m = 15 m
   Max movement is 10 m/s * 0.1 s = 1 m
    */
    @Test
    void computeVehicleMovements() {
        /*
        Given a topology of
        0 --1--> 1 ----> 2
          <----   <--0--
        And two vehicle at edge01 at distance 25, 49.5
        And two vehicle at edge10 at distance 20,30,
        And two vehicle at edge12 at distance 10
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(49.5);
        Vehicle v100 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(20);
        Vehicle v101 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(30);
        Vehicle v120 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge12).setDistance(10);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v010, v011, v100, v101, v120), 0);

        /*
        When checking edges availability
         */
        List<StatusImpl.VehicleMovement> result = status.computeVehicleMovements(status.getLastVehicles(), MIN_DT).collect(Collectors.toList());

        /*
        Then it should result the next vehicle function
         */
        assertNotNull(result);
        assertThat(result, hasSize(3));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("vehicle", equalTo(v011)),
                        hasProperty("dt", closeTo(0.05, 1e-3))
                )));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("vehicle", equalTo(v101)),
                        hasProperty("dt", closeTo(0.1, 1e-3))
                )));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("vehicle", equalTo(v120)),
                        hasProperty("dt", closeTo(0.1, 1e-3))
                )));
    }

    @Test
    void copy() {
        /*
        Given a status with the topology
        1 --edge1--> 2
        And a vehicle at edge1,0
         */
        SiteNode site1 = SiteNode.createSite(X1, Y1);
        SiteNode site2 = SiteNode.createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle = Vehicle.create(site1, site2, 0)
                .setCurrentEdge(edge1);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(site1, site2),
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle), 0);

        /*
        When copying the status
         */
        StatusImpl result = status.copy();

        /*
        Then should result a new status with the copied vehicles
         */
        assertThat(result, not(sameInstance(status)));
        assertThat(result.getSites(), sameInstance(status.getSites()));
        assertThat(result.getNodes(), sameInstance(status.getNodes()));
        assertThat(result.getEdges(), sameInstance(status.getEdges()));
        assertThat(result.getVehicles(), not(sameInstance(status.getVehicles())));
        assertThat(result.getVehicles().get(0), not(sameInstance(status.getVehicles().get(0))));
        assertThat(result.getVehicles().get(0), equalTo(status.getVehicles().get(0)));
    }

    @Test
    void create() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And 3 vehicle for edge01 and 2 vehicle for edge21
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);

        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v012 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(50);
        Vehicle v210 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        Vehicle v211 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(10);

        /*
        When creating the status
         */
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v012, v011, v211, v210, v010), 0);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.getNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.getNextVehicle(v011), equalTo(Optional.of(v012)));
        assertThat(status.getNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.getNextVehicle(v210), equalTo(Optional.of(v211)));
        assertThat(status.getNextVehicle(v211), equalTo(Optional.empty()));

        assertThat(status.getEdgeTransitTime(edge01), equalTo(Optional.of(edge01.getTransitTime())));
        assertThat(status.getEdgeTransitTime(edge10), equalTo(Optional.of(edge10.getTransitTime())));
        assertThat(status.getEdgeTransitTime(edge12), equalTo(Optional.of(edge12.getTransitTime())));
        assertThat(status.getEdgeTransitTime(edge21), equalTo(Optional.of(edge21.getTransitTime())));
    }

    @ParameterizedTest
    @MethodSource("argForTimeDtFreq")
    void createVehicles(double time, double dt, double freq) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        3 --->
          <---
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        SiteNode node3 = SiteNode.createSite(100, 100);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge13 = new MapEdge(node1, node3, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge31 = new MapEdge(node3, node1, SPEED_LIMIT, PRIORITY);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2, node3),
                        List.of(node0, node2, node1, node3),
                        List.of(edge01, edge10, edge12, edge13, edge21, edge31)
                ), time,
                List.of(), freq);
        Random random = new MockRandomBuilder()
                .nextDouble(0) // No vehicle creation at site 0
                .nextDouble(0.9999, 0) // 1 vehicles creation at site 2
                .nextDouble(0) // destination veichle 1
                .nextDouble(0.9999, 0.9999, 0) // 2 vehicles creation at site 3
                .nextDouble(0.9999, 0) // destinations vehicle 2, 3
                .build();

        /*
        When handling vehicles
         */
        List<Vehicle> result = status.createVehicles(random, dt);

        // Then vehicle 0 returning and arriving to departure (edge10, 49.5) should be removed
        assertThat(result, contains(
                allOf(
                        hasProperty("departure", equalTo(node2)),
                        hasProperty("destination", equalTo(node0))),
                allOf(
                        hasProperty("departure", equalTo(node3)),
                        hasProperty("destination", equalTo(node2))),
                allOf(
                        hasProperty("departure", equalTo(node3)),
                        hasProperty("destination", equalTo(node0))
                )));
    }

    @ParameterizedTest
    @MethodSource("time")
    void entryVehicleFromEdge(double time) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And 2 vehicle for edge01 and 1 vehicle for edge21
        And a new vehicle entering into edge01
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(10);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v210 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time,
                List.of(v010, v011, v210),
                0);
        Vehicle v012 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);

        /*
        When entry the vehicle v012 into the edge01
         */
        status.enterVehicleToEdge(v012);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.getNextVehicle(v012), equalTo(Optional.of(v010)));
        assertThat(status.getNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.getNextVehicle(v011), equalTo(Optional.empty()));

        assertThat(status.getVehicles(edge01), contains(v012, v010, v011));
        assertThat(v012.getEdgeEntryTime(), equalTo(time));
    }

    @ParameterizedTest
    @MethodSource("time")
    void entryVehicleFromEmptyEdge(double time) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And 2 vehicle for edge01 and 1 vehicle for edge21
        And a new vehicle entering into edge01
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time, List.of(), 0);

        Vehicle v012 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);

        /*
        When entry the vehicle v012 into the edge01
         */
        status.enterVehicleToEdge(v012);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.getNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.getVehicles(edge01), contains(v012));
        assertThat(v012.getEdgeEntryTime(), equalTo(time));
    }

    @ParameterizedTest
    @MethodSource("times")
    void exitVehicleFromEdge(double time, double dt) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And 3 vehicle for edge01 and 2 vehicle for edge21
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v012 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(50).setEdgeEntryTime(time);
        Vehicle v210 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        Vehicle v211 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(10);
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time + dt,
                List.of(v012, v011, v211, v210, v010), 0);

        /*
        When exit the vehicle v012 from the edge
         */
        status.exitVehicleFromEdge(v012);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.getNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.getNextVehicle(v011), equalTo(Optional.empty()));
        assertThat(status.getNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.getNextVehicle(v210), equalTo(Optional.of(v211)));
        assertThat(status.getNextVehicle(v211), equalTo(Optional.empty()));

        assertThat(status.getVehicles(edge01), not(contains(v012)));
        status.getEdgeTransitTime(edge01).ifPresentOrElse(
                t ->
                        assertThat(t, closeTo(dt, 1e-3)),
                () -> fail("transit time empty"));
    }

    @ParameterizedTest
    @MethodSource("times")
    void exitVehicleFromEdgeLastVehicle(double time, double dt) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And 3 vehicle for edge01 and 1 vehicle for edge21
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v012 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(50).setEdgeEntryTime(time);
        Vehicle v210 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time + dt,
                List.of(v012, v011, v210, v010), 0);

        /*
        When exit the vehicle v012 from the edge
         */
        status.exitVehicleFromEdge(v210);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.getNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.getNextVehicle(v011), equalTo(Optional.of(v012)));
        assertThat(status.getNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.getNextVehicle(v210), equalTo(Optional.empty()));

        assertThat(status.getVehicles(edge21), empty());
        status.getEdgeTransitTime(edge01).ifPresentOrElse(
                t ->
                        assertThat(t, closeTo(50 / SPEED_LIMIT, 1e-3)),
                () -> fail("transit time empty"));
    }

    /*
   SafetyDistance is 10 m/s * 1 s + 5 m = 15 m
   Max movement is 10 m/s * 0.1 s = 1 m
    */
    @Test
    void getFirstExitingVehicle() {
        /*
        Given a topology of
        0 --1--> 1 ----> 2
          <----   <--0--
        And two vehicle at edge01 at distance 25, 49.5
        And two vehicle at edge10 at distance 20,30,
        And two vehicle at edge12 at distance 10
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(49.5);
        Vehicle v100 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(20);
        Vehicle v101 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(30);
        Vehicle v120 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge12).setDistance(10);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v010, v011, v100, v101, v120), 0);

        /*
        When checking edges availability
         */
        Optional<StatusImpl.VehicleMovement> result = status.getFirstExitingVehicle(MIN_DT);

        /*
        Then it should result the next vehicle function
         */
        assertNotNull(result);
        assertThat(result, optionalOf(allOf(
                hasProperty("vehicle", equalTo(v011)),
                hasProperty("dt", closeTo(0.05, 1e-3))
        )));
    }

    @Test
    void getNextVehicle() {
        /*
        Given a status with 2 vehicle
         */
        SiteNode site1 = SiteNode.createSite(X1, Y1);
        SiteNode site2 = SiteNode.createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle1 = Vehicle.create(site1, site2, 0);
        vehicle1.setCurrentEdge(edge1);
        Vehicle vehicle2 = Vehicle.create(site1, site2, 0);
        vehicle2.setCurrentEdge(edge1);
        vehicle2.setDistance(DISTANCE10);
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(site1, site2),
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1, vehicle2), 0);
        /*
        When finding the next vehicles of the two vehicles
         */
        Optional<Vehicle> next1 = status.getNextVehicle(vehicle1);
        Optional<Vehicle> next2 = status.getNextVehicle(vehicle2);

        /*
        Then the next1 should be vehicle2
         */
        assertTrue(next1.isPresent());
        assertThat(next1.get(), equalTo(vehicle2));
        /*
        And the next2 should be empty
         */
        assertTrue(next2.isEmpty());
    }

    @Test
    void getWaitingVehicles() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And a vehicle without edge
        And a vehicle running in the edge
        And a vehicle at the end of edge
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle vNoEdge = Vehicle.create(node0, node2, 0);
        Vehicle v011 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v012 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(edge01.getLength());

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(vNoEdge, v012, v011), 0);

        /*
        When getting waiting vehicles
         */
        List<Vehicle> result = status.getWaitingVehicles();

        /*
        Then it should result the next vehicle function
         */
        assertThat(result, containsInAnyOrder(vNoEdge, v012));
        assertThat(result, hasSize(2));
    }

    @ParameterizedTest
    @MethodSource("argForTimeAndElapsed")
    void handleWaitingVehicles(double time, double elapsed) {
        /*
        Given a topology of
        0 ---> 1 ---> 2    3
          <---   <---
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node3 = SiteNode.createSite(100, 100);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        /*
        And vehicle0 at entry of edge01 (busy edge)
        And vehicle1 waiting on site3 to any without path
        And vehicle2 waiting on site0 to site 2 with busy path (edge01)
        And vehicle3 waiting on site2 to site 0 with available path (edge21)
        And vehicle4 waiting on edge12 to site3 without path
        And vehicle5 waiting on edge10 to site2 with busy path (edge01)
        And vehicle6 waiting on edge21 to site0 with available path (edge10)
        And vehicle7 at entry of edge01 at middle of edge
         */
        Vehicle v0 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge01);
        Vehicle v1 = Vehicle.create(node3, node0, 0);
        Vehicle v2 = Vehicle.create(node0, node2, 0);
        Vehicle v3 = Vehicle.create(node0, node2, 0)
                .setReturning(true);
        Vehicle v4 = Vehicle.create(node0, node3, 0)
                .setCurrentEdge(edge12)
                .setDistance(edge12.getLength());
        Vehicle v5 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setDistance(edge10.getLength());
        Vehicle v6 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge21)
                .setDistance(edge21.getLength())
                .setReturning(true)
                .setEdgeEntryTime(time - elapsed);
        Vehicle v7 = Vehicle.create(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setDistance(edge10.getLength() / 2)
                .setReturning(true);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2, node3),
                        List.of(node0, node2, node1, node3),
                        List.of(edge01, edge10, edge12, edge21)
                ), time,
                List.of(v0, v1, v2, v3, v4, v5, v6, v7), 0);

        /*
        When handling vehicles
         */
        status.handleWaitingVehicles();

        // Then the vehicle1 should be removed
        assertThat(status.getVehicles(), not(contains(v1)));
        // And the vehicle2 should stay
        assertThat(v2.getCurrentEdge(), optionalEmpty());
        // And the vehicle3 should go
        assertThat(v3.getCurrentEdge(), optionalOf(edge21));
        assertThat(v3.getDistance(), equalTo(0.0));
        // Then the vehicle4 should be removed
        assertThat(status.getVehicles(), not(contains(v4)));
        // And the vehicle5 should stay
        assertThat(v5.getCurrentEdge(), optionalOf(edge10));
        assertThat(v5.getDistance(), equalTo(edge10.getLength()));
        // And the vehicle6 should go
        assertThat(v6.getCurrentEdge(), optionalOf(edge10));
        assertThat(v6.getDistance(), equalTo(0.0));
        assertThat(v6.getEdgeEntryTime(), equalTo(time));
        assertThat(status.getEdgeTransitTime(edge21), optionalOf(closeTo(elapsed, 0.01)));
        assertThat(status.getNextVehicle(v6), optionalOf(v7));
    }

    /*
   SafetyDistance is 10 m/s * 1 s + 5 m = 15 m
   Max movement is 10 m/s * 0.1 s = 1 m
    */
    @Test
    void isCrossFreeBusy() {
        /*
        Given a topology of
        0 --1--> 1 ----> 2
          <----   <--0--
        And a vehicle at edge01 at distance = 50-15 m (incoming vehicle)
        And a vehicle at edge21 to site0 at distance = edge length
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(50 - 15);
        Vehicle v210 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(edge21.getLength());
        List<Vehicle> vehicles = List.of(v010, v210);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                vehicles, 0);

        /*
        When checking edges availability
         */
        boolean result10 = status.isCrossFree(edge21, edge12);

        /*
        Then it should result the next vehicle function
         */
        assertFalse(result10);
    }

    /*
   SafetyDistance is 10 m/s * 1 s + 5 m = 15 m
   Max movement is 10 m/s * 0.1 s = 1 m
    */
    @Test
    void isCrossFreeSamePriorityBusy() {
        /*
        Given a topology of
        0 --1--> 1 ----> 2
          <----   <--0--
        And a vehicle at edge01 at distance = 50-15 m (incoming vehicle)
        And a vehicle at edge21 to site0 at distance = edge length
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(50 - 15);
        Vehicle v210 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge21).setDistance(edge21.getLength());
        List<Vehicle> vehicles = List.of(v010, v210);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                vehicles, 0);

        /*
        When checking edges availability
         */
        boolean result10 = status.isCrossFree(edge21, edge10);

        /*
        Then it should result the next vehicle function
         */
        assertTrue(result10);
    }

    @Test
    void isEdgeAvailableEntryOnly() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And a vehicle at edge01 at distance = 5 m (edge busy)
        And a vehicle at edge10 at distance > 5 m (edge available)
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(5);
        Vehicle v100 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(5.1);
        List<Vehicle> vehicles = List.of(v010, v100);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                vehicles, 0);

        /*
        When checking edges availability
         */
        boolean result01 = status.isEdgeAvailable(edge01);
        boolean result10 = status.isEdgeAvailable(edge10);
        boolean result12 = status.isEdgeAvailable(edge12);
        boolean result21 = status.isEdgeAvailable(edge21);

        /*
        Then it should result the next vehicle function
         */
        assertFalse(result01);
        assertTrue(result10);
        assertTrue(result12);
        assertTrue(result21);
    }

    /*
   SafetyDistance is 10 m/s * 1 s + 5 m = 15 m
   Max movement is 10 m/s * 0.1 s = 1 m
    */
    @Test
    void moveVehicles() {
        /*
        Given a topology of
        0 --1--> 1 ----> 2
          <----   <--0--
        And two vehicle at edge01 at distance 10, 49 (at edge exit)
        And two vehicle at edge10 at distance 20,30, (10 m of distance)
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(10);
        Vehicle v011 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge01).setDistance(49);
        Vehicle v100 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(20);
        Vehicle v101 = Vehicle.create(node0, node2, 0).setCurrentEdge(edge10).setDistance(30);

        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v010, v011, v100, v101), 0);

        /*
        When checking edges availability
         */
        status.moveVehicles(MIN_DT);

        /*
        Then the vehicles should result moved
         */
        assertThat(v010.getDistance(), closeTo(10.0 + 1, 0.1));
        assertThat(v011.getDistance(), closeTo(49.0 + 1, 0.1));
        assertThat(v100.getDistance(), closeTo(20 + (10.0 - 5) / (1 + 1 / MIN_DT), 0.1));
        assertThat(v101.getDistance(), closeTo(30.0 + 1, 0.1));
    }
}