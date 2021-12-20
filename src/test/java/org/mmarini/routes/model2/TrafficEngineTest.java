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
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.TestUtils.optionalDoubleOf;
import static org.mmarini.routes.model2.TestUtils.optionalOf;
import static org.mmarini.routes.model2.Topology.createTopology;
import static org.mmarini.routes.model2.TrafficEngineImpl.createEngine;
import static org.mmarini.routes.model2.Vehicle.createVehicle;

class TrafficEngineTest {

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
    static final double MIN_DELAY_TIME = 0.1;
    static final double MAX_DELAY_TIME = 10d;
    private static final int MAX_VEHICLES = 1000;

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

    static Stream<Arguments> argsFrequencies() {
        return ArgumentGenerator.create(SEED)
                .exponential(0.1, 10)
                .exponential(0.1, 10)
                .generate();
    }

    static Stream<Arguments> argsGetPathFrequencies() {
        return ArgumentGenerator.create(SEED)
                .exponential(0.1, 10)
                .exponential(0.01, 1)
                .exponential(0.01, 1)
                .exponential(0.01, 1)
                .exponential(0.01, 1)
                .exponential(0.01, 1)
                .exponential(0.01, 1)
                .exponential(MIN_DELAY_TIME, MAX_DELAY_TIME)
                .exponential(MIN_DELAY_TIME, MAX_DELAY_TIME)
                .generate();
    }

    static Stream<Arguments> time() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .generate();
    }

    static Stream<Arguments> times() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_TIME, MAX_TIME)
                .exponential(MIN_DT, MAX_DT)
                .generate();
    }

    @ParameterizedTest
    @MethodSource("time")
    void applyTimeIntervalVehicleEndingTravel(double time) {
        /*
        Given a topology of
        0 ------> 1 ---> 2
          <-v0---   <---
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        /*
        And a vehicle 0 returning and arriving to departure (edge10, 49.5)
         */
        Vehicle v0 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge10)
                .setReturning(true)
                .setDistance(49.5);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time,
                List.of(v0), SPEED_LIMIT, 0);
        Random random = new MockRandomBuilder()
                .nextDouble(0) // No vehicle creation probability
                .nextDouble(0) // No vehicle creation probability
                .build();

        /*
        When handling vehicles
         */
        status.applyTimeInterval(random, MIN_DT);

        // Then vehicle 0 returning and arriving to departure (edge10, 49.5) should be removed
        assertThat(status.findVehicles(), not(hasItem(v0)));
    }

    @ParameterizedTest
    @MethodSource("argsFrequencies")
    void changeFrequency(double f1, double f2) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And the status
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(), SPEED_LIMIT, f1);
        /*
        When changing frequency
         */
        TrafficEngineImpl result = status.setFrequency(f2);
        /*
        Then it should result the new frequency
         */
        assertNotNull(result);
        assertThat(result.getFrequency(), equalTo(f2));
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
        SiteNode site1 = createSite(X1, Y1);
        SiteNode site2 = createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle1 = createVehicle(site1, site2, 0)
                .setDistance(d1)
                .setCurrentEdge(edge1);
        Vehicle vehicle2 = createVehicle(site1, site2, 0)
                .setCurrentEdge(edge1)
                .setDistance(d2);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1, vehicle2), SPEED_LIMIT, 0);

        /*
        When computing location of vehicle1 after dt time interval
         */
        TrafficEngineImpl.VehicleMovement result = status.computeVehicleMovement(vehicle1, dt);

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
        SiteNode site1 = createSite(X1, Y1);
        SiteNode site2 = createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        double edgeLength = edge1.getLength();
        double distance = edgeLength - MIN_DT * SPEED_LIMIT + 0.01;
        Vehicle vehicle1 = createVehicle(site1, site2, 0)
                .setDistance(distance)
                .setCurrentEdge(edge1);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1), SPEED_LIMIT, 0);
        /*
        When computing location of vehicle1 after dt time interval
         */
        TrafficEngineImpl.VehicleMovement result = status.computeVehicleMovement(vehicle1, dt);

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
        SiteNode site1 = createSite(X1, Y1);
        SiteNode site2 = createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle1 = createVehicle(site1, site2, 0);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1), SPEED_LIMIT, 0);
        /*
        When computing location of vehicle1 after dt time interval
         */
        TrafficEngineImpl.VehicleMovement result = status.computeVehicleMovement(vehicle1, dt);

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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(49.5);
        Vehicle v100 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(20);
        Vehicle v101 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(30);
        Vehicle v120 = createVehicle(node0, node2, 0).setCurrentEdge(edge12).setDistance(10);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v010, v011, v100, v101, v120), SPEED_LIMIT, 0);

        /*
        When checking edges availability
         */
        List<TrafficEngineImpl.VehicleMovement> result = status.computeVehicleMovements(status.getLastVehicles(), MIN_DT).collect(Collectors.toList());

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
    void create() {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And 3 vehicle for edge01 and 2 vehicle for edge21
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);

        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v012 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(50);
        Vehicle v210 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        Vehicle v211 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(10);

        /*
        When creating the status
         */
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v012, v011, v211, v210, v010), SPEED_LIMIT, 0);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.findNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.findNextVehicle(v011), equalTo(Optional.of(v012)));
        assertThat(status.findNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.findNextVehicle(v210), equalTo(Optional.of(v211)));
        assertThat(status.findNextVehicle(v211), equalTo(Optional.empty()));

        assertThat(status.findEdgeTransitTime(edge01), equalTo(edge01.getTransitTime()));
        assertThat(status.findEdgeTransitTime(edge10), equalTo(edge10.getTransitTime()));
        assertThat(status.findEdgeTransitTime(edge12), equalTo(edge12.getTransitTime()));
        assertThat(status.findEdgeTransitTime(edge21), equalTo(edge21.getTransitTime()));
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(100, 100);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge13 = new MapEdge(node1, node3, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge31 = new MapEdge(node3, node1, SPEED_LIMIT, PRIORITY);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1, node3),
                        List.of(edge01, edge10, edge12, edge13, edge21, edge31)
                ), time,
                List.of(), SPEED_LIMIT, freq);
        Random random = new MockRandomBuilder()
                .nextDouble(0) // No vehicle creation at site 0
                .nextDouble(0.9999, 0) // 1 vehicles creation at site 2
                .nextDouble(0) // destination vehicle 1
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(10);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v210 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time,
                List.of(v010, v011, v210),
                SPEED_LIMIT, 0);
        Vehicle v012 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);

        /*
        When entry the vehicle v012 into the edge01
         */
        status.enterVehicleToEdge(v012);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.findNextVehicle(v012), equalTo(Optional.of(v010)));
        assertThat(status.findNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.findNextVehicle(v011), equalTo(Optional.empty()));

        assertThat(status.findVehicles(edge01), contains(v012, v010, v011));
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time, List.of(), SPEED_LIMIT, 0);

        Vehicle v012 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);

        /*
        When entry the vehicle v012 into the edge01
         */
        status.enterVehicleToEdge(v012);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.findNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.findVehicles(edge01), contains(v012));
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v012 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(50).setEdgeEntryTime(time);
        Vehicle v210 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        Vehicle v211 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(10);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time + dt,
                List.of(v012, v011, v211, v210, v010), SPEED_LIMIT, 0);

        /*
        When exit the vehicle v012 from the edge
         */
        status.exitVehicleFromEdge(v012);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.findNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.findNextVehicle(v011), equalTo(Optional.empty()));
        assertThat(status.findNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.findNextVehicle(v210), equalTo(Optional.of(v211)));
        assertThat(status.findNextVehicle(v211), equalTo(Optional.empty()));

        assertThat(status.findVehicles(edge01), not(contains(v012)));
        assertThat(status.findEdgeTransitTime(edge01), closeTo(dt, 1e-3));
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(0);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v012 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(50).setEdgeEntryTime(time);
        Vehicle v210 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(0);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), time + dt,
                List.of(v012, v011, v210, v010), SPEED_LIMIT, 0);

        /*
        When exit the vehicle v012 from the edge
         */
        status.exitVehicleFromEdge(v210);

        /*
        Then it should result the next vehicle function
         */
        assertThat(status.findNextVehicle(v010), equalTo(Optional.of(v011)));
        assertThat(status.findNextVehicle(v011), equalTo(Optional.of(v012)));
        assertThat(status.findNextVehicle(v012), equalTo(Optional.empty()));

        assertThat(status.findNextVehicle(v210), equalTo(Optional.empty()));

        assertThat(status.findVehicles(edge21), empty());
        assertThat(status.findEdgeTransitTime(edge01), closeTo(50 / SPEED_LIMIT, 1e-3));
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(20);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(49.5);
        Vehicle v100 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(20);
        Vehicle v101 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(30);
        Vehicle v120 = createVehicle(node0, node2, 0).setCurrentEdge(edge12).setDistance(10);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v010, v011, v100, v101, v120), SPEED_LIMIT, 0);

        /*
        When checking edges availability
         */
        Optional<TrafficEngineImpl.VehicleMovement> result = status.findFirstExitingVehicle(MIN_DT);

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
        SiteNode site1 = createSite(X1, Y1);
        SiteNode site2 = createSite(X2, Y2);
        MapEdge edge1 = new MapEdge(site1, site2, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle1 = createVehicle(site1, site2, 0);
        vehicle1.setCurrentEdge(edge1);
        Vehicle vehicle2 = createVehicle(site1, site2, 0);
        vehicle2.setCurrentEdge(edge1);
        vehicle2.setDistance(DISTANCE10);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(site1, site2),
                        List.of(edge1)
                ), 0,
                List.of(vehicle1, vehicle2), SPEED_LIMIT, 0);
        /*
        When finding the next vehicles of the two vehicles
         */
        Optional<Vehicle> next1 = status.findNextVehicle(vehicle1);
        Optional<Vehicle> next2 = status.findNextVehicle(vehicle2);

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

    @ParameterizedTest
    @MethodSource("argsGetPathFrequencies")
    void getPathFrequencies(double frequency,
                            double w01, double w02,
                            double w10, double w12,
                            double w20, double w21
    ) {
        /*
        Given a topology of
        0 ---> 1 ---> 2
          <---   <---
        And the status
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node1 = createSite(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node1, node2),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(), SPEED_LIMIT, frequency)
                .setWeights(new double[][]{
                        {0, w01, w02},
                        {w10, 0, w12},
                        {w20, w21, 0}
                });
        /*
        When getting path frequencies
         */
        DoubleMatrix<SiteNode> result = status.getPathFrequencies();
        /*
        Then it should result the frequencies for each path
        f01 = f10 = (w01/(w0
        1+w02)+w10/(w10+w12))*frequency
        f02 = f20 = (w02/(w01+w02)+w20/(w20+w21))*frequency
        f12 = f21 = (w12/(w10+w12)+w21/(w20+w21))*frequency
         */
        double f01 = (w01 / (w01 + w02) + w10 / (w10 + w12)) * frequency;
        double f02 = (w02 / (w01 + w02) + w20 / (w20 + w21)) * frequency;
        double f12 = (w12 / (w10 + w12) + w21 / (w20 + w21)) * frequency;
        assertNotNull(result);

        assertThat(result.getValue(node0, node0), optionalDoubleOf(0.0));
        assertThat(result.getValue(node0, node1), optionalDoubleOf(closeTo(f01, 1e-6)));
        assertThat(result.getValue(node0, node2), optionalDoubleOf(closeTo(f02, 1e-6)));
        assertThat(result.getValue(node1, node0), optionalDoubleOf(closeTo(f01, 1e-6)));
        assertThat(result.getValue(node1, node1), optionalDoubleOf(0.0));
        assertThat(result.getValue(node1, node2), optionalDoubleOf(closeTo(f12, 1e-6)));
        assertThat(result.getValue(node2, node0), optionalDoubleOf(closeTo(f02, 1e-6)));
        assertThat(result.getValue(node2, node1), optionalDoubleOf(closeTo(f12, 1e-6)));
        assertThat(result.getValue(node2, node2), optionalDoubleOf(0.0));

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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle vNoEdge = createVehicle(node0, node2, 0);
        Vehicle v011 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(20);
        Vehicle v012 = createVehicle(node0, node2, 0)
                .setCurrentEdge(edge01)
                .setDistance(edge01.getLength());

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(vNoEdge, v012, v011), SPEED_LIMIT, 0);

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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(50 - 15);
        Vehicle v210 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(edge21.getLength());
        List<Vehicle> vehicles = List.of(v010, v210);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                vehicles, SPEED_LIMIT, 0);

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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(50 - 15);
        Vehicle v210 = createVehicle(node0, node2, 0).setCurrentEdge(edge21).setDistance(edge21.getLength());
        List<Vehicle> vehicles = List.of(v010, v210);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                vehicles, SPEED_LIMIT, 0);

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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(5);
        Vehicle v100 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(5.1);
        List<Vehicle> vehicles = List.of(v010, v100);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                vehicles, SPEED_LIMIT, 0);

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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        Vehicle v010 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(10);
        Vehicle v011 = createVehicle(node0, node2, 0).setCurrentEdge(edge01).setDistance(49);
        Vehicle v100 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(20);
        Vehicle v101 = createVehicle(node0, node2, 0).setCurrentEdge(edge10).setDistance(30);

        TrafficEngineImpl status = createEngine(
                MAX_VEHICLES, createTopology(
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(v010, v011, v100, v101), SPEED_LIMIT, 0);

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