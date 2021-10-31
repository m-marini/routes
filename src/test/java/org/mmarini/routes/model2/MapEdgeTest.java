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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Point2D;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mmarini.routes.model2.Constants.REACTION_TIME;
import static org.mmarini.routes.model2.Constants.VEHICLE_LENGTH;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.TestUtils.pointCloseTo;

class MapEdgeTest {

    static final long SEED = 1234L;
    static final int MIN_COORDINATE_VALUE = -2000;
    static final int MAX_COORDINATE_VALUE = 2000 + 1;
    static final double MIN_SPEED_LIMIT = 10;
    static final double MAX_SPEED_LIMITS = 50;
    static final int MIN_PRIORITY = 0;
    static final int MAX_PRIORITY = 5;
    static final double MIN_DISTANCE = -10000;
    static final double MAX_DISTANCE = 10000;

    static Stream<Arguments> edges() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .exponential(MIN_SPEED_LIMIT, MAX_SPEED_LIMITS)
                .uniform(MIN_PRIORITY, MAX_PRIORITY)
                .generate()
                .filter(args ->
                        !(args.get()[0].equals(args.get()[2])
                                && args.get()[1].equals(args.get()[3])));
    }

    static Stream<Arguments> edgesWithDistance() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .exponential(MIN_SPEED_LIMIT, MAX_SPEED_LIMITS)
                .uniform(MIN_PRIORITY, MAX_PRIORITY)
                .uniform(MIN_DISTANCE, MAX_DISTANCE)
                .generate()
                .filter(args ->
                        !(args.get()[0].equals(args.get()[2])
                                && args.get()[1].equals(args.get()[3])));

    }

    @ParameterizedTest
    @CsvSource(value = {
            "0,0, 100,0, 0,0, 0,0",
            "0,0, 100,0, 0,10, 0,0",
            "0,0, 100,0, 5,-10, 5,0",
            "0,0, 100,0, 10,-10, 10,0",
            "0,0, 100,100, -10,10, 0,0",
            "0,0, 100,100, 10,-10, 0,0",
            "0,0, 100,100, 40,60, 50,50",
            "10,10, 20,20, 60,60, 20,20",
            "10,10, 20,20, 0,0, 10,10",
            "10,10, 20,20, -10,10, 10,10",
    })
    void closerFrom(int x0, int y0, int x1, int y1, int x2, int y2, double ex, double ey) {
        /*
        Given an edge
        with a beginning node
        and an end node
        and a speed limit
        and a priority
        And a point in the space
         */
        CrossNode begin = createNode(x0, y0);
        CrossNode end = createNode(x1, y1);
        MapEdge edge = new MapEdge(begin, end, MIN_SPEED_LIMIT, MIN_PRIORITY);
        Point2D point = new Point2D.Double(x2, y2);

        // When computing the closer edge point from a point
        Point2D result = edge.closerFrom(point);

        // Then should return the expected value of ka
        assertThat(result, pointCloseTo(new Point2D.Double(ex, ey), 0.1));
    }

    @ParameterizedTest
    @MethodSource("edges")
    void create(int x0, int y0, int x1, int y1, double speed, int priority) {
        /*
        Given an edge
        with a beginning node
        and an end node
        and a speed limit
        and a priority
         */
        CrossNode begin = createNode(x0, y0);
        CrossNode end = createNode(x1, y1);
        MapEdge edge = new MapEdge(begin, end, speed, priority);

        /*
         When getting properties
         And applying visitor
         */
        MapNode resultBegin = edge.getBegin();
        Point2D resultBeginLoc = edge.getBeginLocation();
        MapNode resultEnd = edge.getEnd();
        Point2D resultEndLoc = edge.getEndLocation();
        double resultSpeedLimit = edge.getSpeedLimit();
        int resultPriority = edge.getPriority();
        Point2D vector = edge.getVector();
        double length = edge.getLength();
        double transitTime = edge.getTransitTime();
        double safetyDistance = edge.getSafetyDistance();
        MapNode visitor = edge.apply(new MapElementVisitorAdapter<>() {
            @Override
            public MapNode visit(MapEdge edge) {
                return edge.getBegin();
            }
        });

        //Then should return the beginning node
        assertThat(resultBegin, equalTo(begin));
        //Then should return the end node
        assertThat(resultEnd, equalTo(end));
        // And the speed limit
        assertThat(resultSpeedLimit, equalTo(speed));
        // And the priority
        assertThat(resultPriority, equalTo(priority));
        // And the beginning location
        assertThat(resultBeginLoc, equalTo(new Point2D.Double(x0, y0)));
        // And the end location
        assertThat(resultEndLoc, equalTo(new Point2D.Double(x1, y1)));
        // And the length
        double distance = sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        assertThat(length, equalTo(distance));
        // And the vector
        assertThat(vector, equalTo(new Point2D.Double(x1 - x0, y1 - y0)));
        // And the transit time
        assertThat(transitTime, equalTo(distance / speed));
        // And the safety distance
        assertThat(safetyDistance, equalTo(REACTION_TIME * speed + VEHICLE_LENGTH));
        // And the visitor
        assertThat(visitor, equalTo(begin));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0,0, 100,0, 0,0, 0.0",
            "0,0, 100,0, 0,10, 10.0",
            "0,0, 100,0, 5,-10, 10.0",
            "0,0, 100,0,10, -10, 10.0",
            "0,0, 100,100, -10,10, 14.1",
            "0,0, 100,100, 10,-10, 14.1",
            "0,0, 100,100, 40,60, 14.1",
            "10,10, 20,20, 60,60, 56.6",
            "10,10, 20,20, 0,0, 14.1",
            "10,10, 20,20, -10,10, 20",
    })
    void distanceFrom(int x0, int y0, int x1, int y1, int x2, int y2, double expectedDistance) {
        /*
        Given an edge
        with a beginning node
        and an end node
        and a speed limit
        and a priority
        And a point in the space
         */
        CrossNode begin = createNode(x0, y0);
        CrossNode end = createNode(x1, y1);
        MapEdge edge = new MapEdge(begin, end, MIN_SPEED_LIMIT, MIN_PRIORITY);
        Point2D point = new Point2D.Double(x2, y2);

        // When computing distance from a point
        double result = edge.distanceFrom(point);

        // Then should return the expected value of ka
        assertThat(result, closeTo(expectedDistance, 0.1));
    }

    @ParameterizedTest
    @MethodSource("edges")
    void equalsAndHash(int x0, int y0, int x1, int y1, double speed, int priority) {
        /*
        Given an edge
        with a beginning node
        and an end node
        and a speed limit
        and a priority
         */
        CrossNode begin = createNode(x0, y0);
        CrossNode end = createNode(x1, y1);
        MapEdge edge = new MapEdge(begin, end, speed, priority);

        // itself
        assertEquals(edge, edge);

        // null
        assertNotEquals(null, edge);

        // wrong class
        assertNotEquals(new Object(), edge);

        // different begin
        assertNotEquals(
                new MapEdge(
                        createNode(x0 + 1, y0 - 1),
                        end, speed, priority),
                edge);

        // different end
        assertNotEquals(
                new MapEdge(
                        begin,
                        createNode(x1 + 1, y1 - 1),
                        speed, priority),
                edge);

        // same begin and end
        assertEquals(
                new MapEdge(begin, end, speed + 1, priority + 1),
                edge);

        // hashCode
        assertEquals(
                new MapEdge(begin, end, speed + 1, priority + 1).hashCode(),
                edge.hashCode());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0,0, 100,0, 0,0, 0.0",
            "0,0, 100,0, 0,10, 0.0",
            "0,0, 100,0, 5,-10, 5.0",
            "0,0, 100,0,10, -10, 10.0",
            "0,0, 100,100, -10,10, 0.0",
            "0,0, 100,100, 10,-10, 0.0",
            "0,0, 100,100, 40,60, 70.7",
            "10,10, 20,20, 60,60, 70.7",
            "10,10, 20,20, 0,0, -14.1",
            "10,10, 20,20, -10,10, -14.1",
    })
    void ka(int x0, int y0, int x1, int y1, int x2, int y2, double expectedKa) {
        /*
        Given an edge
        with a beginning node
        and an end node
        and a speed limit
        and a priority
        And a point in the space
         */
        CrossNode begin = createNode(x0, y0);
        CrossNode end = createNode(x1, y1);
        MapEdge edge = new MapEdge(begin, end, MIN_SPEED_LIMIT, MIN_PRIORITY);
        Point2D point = new Point2D.Double(x2, y2);

        // When computing ka factor
        double result = edge.ka(point);

        // Then should return the expected value of ka
        assertThat(result, closeTo(expectedKa, 0.1));
    }

    @ParameterizedTest
    @MethodSource("edgesWithDistance")
    void locationAt(int x0, int y0, int x1, int y1, double speed, int priority, double distance) {
        /*
        Given an edge
        with a beginning node
        and an end node
        and a speed limit
        and a priority
        And a distance from the beginning
         */
        CrossNode begin = createNode(x0, y0);
        CrossNode end = createNode(x1, y1);
        MapEdge edge = new MapEdge(begin, end, speed, priority);

        /*
         * When computing location at
         */
        Point2D result = edge.locationAt(distance);

        //Then should return the beginning node
        double length = sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        assertThat(result, equalTo(new Point2D.Double(
                distance / length * (x1 - x0) + x0,
                distance / length * (y1 - y0) + y0
        )));
    }
}