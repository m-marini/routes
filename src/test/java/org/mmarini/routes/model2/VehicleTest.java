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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.TestUtils.optionalEmpty;
import static org.mmarini.routes.model2.Vehicle.createVehicle;

class VehicleTest {

    static final long SEED = 1234L;
    static final int PRIORITY = 0;
    static final double SPEED_LIMIT = 10.0;
    static final double MIN_DISTANCE = 1.0;
    static final double MAX_DISTANCE = 10000.0;
    static final double RETURNING_PROB = 0.5;
    static final int MIN_COORDINATE = -10000;
    static final int MAX_COORDINATE = 10000;
    private static final double MAX_TIME = 1000;
    private static final double MIN_TIME = 0;

    static Stream<Arguments> argsForCreate() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_TIME, MAX_TIME)
                .generate();
    }

    static Stream<Arguments> argsForProperties() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .nextBoolean(RETURNING_PROB)
                .exponential(MIN_DISTANCE, MAX_DISTANCE)
                .generate()
                .filter(args -> !(args.get()[0].equals(args.get()[2])
                        && args.get()[1].equals(args.get()[3])));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void copy(int x0, int y0, int x1, int y1) {
        /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x1, y1);
        Vehicle vehicle = createVehicle(dep, dest, 0);

        /*
        When copying
         */
        Vehicle result = vehicle.copy();

        /*
        Then should return vehicle
        with the departure and destination nodes
        and default properties
         */
        assertNotNull(result);
        assertThat(result, not(sameInstance(vehicle)));
        assertThat(result, equalTo(vehicle));
        assertFalse(result.getCurrentEdge().isPresent());
        assertThat(result.getDeparture(), equalTo(dep));
        assertThat(result.getDestination(), equalTo(dest));
        assertThat(result.getDistance(), equalTo(0.0));
        assertFalse(result.isReturning());
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void create(int x0, int y0, int x1, int y1, double time) {
        /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x1, y1);

        /*
        When creating a vehicle
         */
        Vehicle vehicle = createVehicle(dep, dest, time);

        /*
        Then should return vehicle
        with the departure and destination nodes
        and default properties
         */
        assertNotNull(vehicle);
        assertThat(vehicle.getId().toString(), matchesPattern("[abcdefABCDEF\\d]{8}-[abcdefABCDEF\\d]{4}-[abcdefABCDEF\\d]{4}-[abcdefABCDEF\\d]{4}-[abcdefABCDEF\\d]{12}"));
        assertFalse(vehicle.getCurrentEdge().isPresent());
        assertThat(vehicle.getDeparture(), equalTo(dep));
        assertThat(vehicle.getDestination(), equalTo(dest));
        assertThat(vehicle.getDistance(), equalTo(0.0));
        assertFalse(vehicle.isReturning());
        assertThat(vehicle.getCreationTime(), equalTo(time));
        assertThat(vehicle.getDirection(), optionalEmpty());
        assertThat(vehicle.getLocation(), optionalEmpty());
        assertThat(vehicle, hasToString(
                matchesPattern("Vehicle\\[id=[\\dabcdef]{8}-[\\dabcdef]{4}-[\\dabcdef]{4}-[\\dabcdef]{4}-[\\dabcdef]{12}\\]")));
        assertThat(vehicle.getEdgeEntryTime(), equalTo(0.0));
        assertFalse(vehicle.isCrossingNode(dep));
        assertTrue(vehicle.isRelatedToNode(dep));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void hashAndEquals(int x0, int y0, int x1, int y1) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x1, y1);
        Vehicle vehicle = createVehicle(dep, dest, 0);

        // itself
        assertEquals(vehicle, vehicle);

        // null
        assertNotEquals(null, vehicle);

        // wrong class
        assertNotEquals(new Object(), vehicle);

        // new vehicle
        assertNotEquals(createVehicle(dep, dest, 0), vehicle);

        // same id
        assertEquals(
                new Vehicle(vehicle.getId(), dep, dest, 0, null, MAX_DISTANCE, true, 0),
                vehicle);

        // hashCode
        assertThat(vehicle.hashCode(),
                equalTo(
                        new Vehicle(
                                vehicle.getId(), dep, dest, 0, null, MAX_DISTANCE, true, 0)
                                .hashCode()));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void isTransitingEdge(int x0, int y0, int x1, int y1) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x0 + 10, y1 + 10);
        MapEdge edge0 = new MapEdge(dep, dest, SPEED_LIMIT, PRIORITY);
        MapEdge edge1 = new MapEdge(dest, dep, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle = createVehicle(dep, dest, 0).setCurrentEdge(edge0);

        /*
        Than is transiting
         */
        assertTrue(vehicle.isTransitingEdge(edge0));
        assertFalse(vehicle.isTransitingEdge(edge1));
        assertTrue(vehicle.isCrossingNode(dep));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void isTransitingEdgeWhenStop(int x0, int y0, int x1, int y1) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x0 + 10, y1 + 10);
        MapEdge edge0 = new MapEdge(dep, dest, SPEED_LIMIT, PRIORITY);
        MapEdge edge1 = new MapEdge(dest, dep, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle = createVehicle(dep, dest, 0);

        /*
        Than is transiting
         */
        assertFalse(vehicle.isTransitingEdge(edge0));
        assertFalse(vehicle.isTransitingEdge(edge1));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void setDeparture(int x0, int y0, int x1, int y1) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x0 + 10, y1 + 10);
        MapEdge edge0 = new MapEdge(dep, dest, SPEED_LIMIT, PRIORITY);
        MapEdge edge1 = new MapEdge(dest, dep, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle = createVehicle(dep, dest, 0).setCurrentEdge(edge0);
        final SiteNode site = createSite(x0 + 10, y0 + 10);

        /*
        When changeding the departure
         */
        Vehicle result = vehicle.setDeparture(site);

        /*
        Than vehicle should be equal
         */
        assertThat(result, equalTo(vehicle));
        assertThat(result, hasProperty("departure", equalTo(site)));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void setDestination(int x0, int y0, int x1, int y1) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x0 + 10, y1 + 10);
        MapEdge edge0 = new MapEdge(dep, dest, SPEED_LIMIT, PRIORITY);
        MapEdge edge1 = new MapEdge(dest, dep, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle = createVehicle(dep, dest, 0).setCurrentEdge(edge0);
        final SiteNode site = createSite(x0 + 10, y0 + 10);

        /*
        When changeding the departure
         */
        Vehicle result = vehicle.setDestination(site);

        /*
        Than vehicle should be equal
         */
        assertThat(result, equalTo(vehicle));
        assertThat(result, hasProperty("destination", equalTo(site)));
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void setEdgeEntryTime(int x0, int y0, int x1, int y1, double time) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x0 + 10, y1 + 10);
        MapEdge edge0 = new MapEdge(dep, dest, SPEED_LIMIT, PRIORITY);
        MapEdge edge1 = new MapEdge(dest, dep, SPEED_LIMIT, PRIORITY);
        Vehicle vehicle = createVehicle(dep, dest, time).setCurrentEdge(edge0);
        final SiteNode site = createSite(x0 + 10, y0 + 10);

        /*
        When changeding the departure
         */
        Vehicle result = vehicle.setEdgeEntryTime(time + 10);

        /*
        Than vehicle should be equal
         */
        assertThat(result, sameInstance(vehicle));
        assertThat(result, hasProperty("edgeEntryTime", equalTo(time + 10)));
    }

    @ParameterizedTest
    @MethodSource("argsForProperties")
    void setProperties(int x0, int y0, int x1, int y1, boolean isReturning, double distance) {
        /*
         Given a vehicle with departure node
         and destination node
         and an edge from departure to destination
         */
        SiteNode dep = createSite(x0, y0);
        SiteNode dest = createSite(x1, y1);
        Vehicle vehicle = createVehicle(dep, dest, 0);
        MapEdge edge = new MapEdge(dep, dest, SPEED_LIMIT, PRIORITY);

        /*
        When setting properties
         */
        vehicle.setCurrentEdge(edge);
        vehicle.setDistance(distance);
        vehicle.setReturning(isReturning);

        /*
        Then should return vehicle
        with the departure and destination nodes
        and set properties
         */
        assertNotNull(vehicle);
        assertThat(vehicle.getId().toString(), matchesPattern("[abcdefABCDEF\\d]{8}-[abcdefABCDEF\\d]{4}-[abcdefABCDEF\\d]{4}-[abcdefABCDEF\\d]{4}-[abcdefABCDEF\\d]{12}"));
        assertEquals(vehicle.getDeparture(), dep);
        assertEquals(vehicle.getDestination(), dest);
        assertTrue(vehicle.getCurrentEdge().isPresent());
        assertEquals(vehicle.getCurrentEdge().get(), edge);
        assertEquals(vehicle.getDistance(), distance);
        assertEquals(vehicle.isReturning(), isReturning);
        assertThat(vehicle.getCurrentDestination(), equalTo(isReturning ? dep : dest));
    }

}