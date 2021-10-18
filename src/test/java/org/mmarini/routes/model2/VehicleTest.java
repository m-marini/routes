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

import java.awt.geom.Point2D;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    static final long SEED = 1234L;
    static final int PRIORITY = 0;
    static final double SPEED_LIMIT = 10.0;
    static final double MIN_DISTANCE = 1.0;
    static final double MAX_DISTANCE = 10000.0;
    static final double RETURNING_PROB = 0.5;
    static final int MIN_COORDINATE = -10000;
    static final int MAX_COORDINATE = 10000;

    static Stream<Arguments> argsForCreate() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
                .uniform(MIN_COORDINATE, MAX_COORDINATE)
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
    void create(int x0, int y0, int x1, int y1) {
        /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = new SiteNode(new Point2D.Double(x0, y0));
        SiteNode dest = new SiteNode(new Point2D.Double(x1, y1));

        /*
        When creating a vehicle
         */
        Vehicle vehicle = Vehicle.create(dep, dest, 0);

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
    }

    @ParameterizedTest
    @MethodSource("argsForCreate")
    void hashAndEquals(int x0, int y0, int x1, int y1) {
         /*
         Given a departure ode
         and a destination node
         */
        SiteNode dep = new SiteNode(new Point2D.Double(x0, y0));
        SiteNode dest = new SiteNode(new Point2D.Double(x1, y1));
        Vehicle vehicle = Vehicle.create(dep, dest, 0);

        // itself
        assertEquals(vehicle, vehicle);

        // null
        assertNotEquals(null, vehicle);

        // wrong class
        assertNotEquals(new Object(), vehicle);

        // new vehicle
        assertNotEquals(Vehicle.create(dep, dest, 0), vehicle);

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
    @MethodSource("argsForProperties")
    void setProperties(int x0, int y0, int x1, int y1, boolean isReturning, double distance) {
        /*
         Given a vehicle with departure node
         and destination node
         and an edge from departure to destination
         */
        SiteNode dep = new SiteNode(new Point2D.Double(x0, y0));
        SiteNode dest = new SiteNode(new Point2D.Double(x1, y1));
        Vehicle vehicle = Vehicle.create(dep, dest, 0);
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