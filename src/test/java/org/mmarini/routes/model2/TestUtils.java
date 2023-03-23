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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.*;

public class TestUtils {

    private static boolean edgeAt(Object o, Point2D begin, Point2D end) {
        if (!(o instanceof MapEdge)) {
            return false;
        }
        MapEdge oEdge = (MapEdge) o;
        return oEdge.getBeginLocation().equals(begin)
                && oEdge.getEndLocation().equals(end);
    }

    static Matcher<MapEdge> edgeAt(MapEdge expectedEdge) {
        requireNonNull(expectedEdge);
        return edgeAt(expectedEdge.getBeginLocation(), expectedEdge.getEndLocation());
    }

    static Matcher<MapEdge> edgeAt(Point2D begin, Point2D end) {
        requireNonNull(begin);
        requireNonNull(end);
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("edge at ")
                        .appendValue(begin)
                        .appendText(" -> ")
                        .appendValue(end);
            }

            @Override
            public boolean matches(Object o) {
                return edgeAt(o, begin, end);
            }
        };
    }

    static Optional<MapEdge> findEdge(Collection<MapEdge> edges, MapEdge edge) {
        return edges.stream()
                .filter(edge::isSameLocation)
                .findAny();
    }

    static Optional<MapNode> findNode(Collection<MapNode> nodes, MapNode node) {
        return nodes.stream()
                .filter(node::isSameLocation)
                .findAny();
    }

    static Matcher<MapNode> isCrossNode() {
        return isA(CrossNode.class);
    }

    static Matcher<MapNode> isSiteNode() {
        return isA(SiteNode.class);
    }

    private static boolean nodeAt(Object o, Point2D expectedLocation) {
        if (o == null || !(o instanceof MapNode)) {
            return false;
        }
        MapNode oNode = (MapNode) o;
        return oNode.getLocation().equals(expectedLocation);
    }

    static Matcher<MapNode> nodeAt(Point2D expectedLocation) {
        requireNonNull(expectedLocation);
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("node at ")
                        .appendValue(expectedLocation);
            }

            @Override
            public boolean matches(Object o) {
                return nodeAt(o, expectedLocation);
            }
        };
    }

    static Matcher<MapNode> nodeAt(MapNode expectedNode) {
        requireNonNull(expectedNode);
        return nodeAt(expectedNode.getLocation());
    }

    static Matcher<MapNode> nodeCloseTo(double x, double y, double epsilon) {
        return allOf(
                isA(MapNode.class),
                hasProperty("location",
                        pointCloseTo(x, y, epsilon)));
    }

    static Matcher<OptionalDouble> optionalDoubleOf(double item) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(item);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof OptionalDouble)) {
                    return false;
                }
                OptionalDouble opt = (OptionalDouble) o;
                if (opt.isEmpty()) {
                    return false;
                } else {
                    return opt.getAsDouble() == item;
                }
            }
        };
    }

    static Matcher<OptionalDouble> optionalDoubleOf(Matcher<Double> matcher) {
        requireNonNull(matcher);
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(matcher);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof OptionalDouble)) {
                    return false;
                }
                OptionalDouble opt = (OptionalDouble) o;
                if (opt.isEmpty()) {
                    return false;
                } else {
                    return matcher.matches(opt.getAsDouble());
                }
            }
        };
    }

    static <T> Matcher<Optional<T>> optionalEmpty() {
        return equalTo(Optional.empty());
    }

    static <T> Matcher<Optional<T>> optionalOf(Matcher<T> matcher) {
        requireNonNull(matcher);
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(matcher);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Optional)) {
                    return false;
                }
                Optional<?> opt = (Optional<?>) o;
                if (opt.isEmpty()) {
                    return false;
                } else {
                    return matcher.matches(opt.get());
                }
            }
        };
    }

    static <T> Matcher<Optional<T>> optionalOf(T item) {
        requireNonNull(item);
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(item);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Optional)) {
                    return false;
                }
                Optional<?> opt = (Optional<?>) o;
                if (opt.isEmpty()) {
                    return false;
                } else {
                    return opt.get().equals(item);
                }
            }
        };
    }

    static Matcher<Point2D> pointCloseTo(double x, double y, double epsilon) {
        return pointCloseTo(new Point2D.Double(x, y), epsilon);
    }

    static Matcher<Point2D> pointCloseTo(Point2D expectedPoint, double epsilon) {
        requireNonNull(expectedPoint);
        return new BaseMatcher<>() {
            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("was ")
                        .appendValue(item);
                if (item instanceof Point2D) {
                    description.appendText(" within a distance of ")
                            .appendValue(expectedPoint.distance((Point2D) item));
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expectedPoint)
                        .appendText(" within a distance of ")
                        .appendValue(epsilon);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Point2D)) {
                    return false;
                }
                Point2D point = (Point2D) o;
                double dist = point.distance(expectedPoint);
                return dist <= epsilon;
            }
        };
    }

    static Matcher<Vehicle> vehicleAt(Matcher<MapEdge> edge, Matcher<Double> distance) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Vehicle at edge ");
                edge.describeTo(description);
                description.appendText(", ");
                distance.describeTo(description);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Vehicle)) {
                    return false;
                }
                Vehicle oVehicle = (Vehicle) o;
                return oVehicle.getCurrentEdge().filter(edge::matches).isPresent()
                        && distance.matches(oVehicle.getDistance());
            }
        };
    }

    static Matcher<Vehicle> vehicleAt(Matcher<MapEdge> edge) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Vehicle at ");
                edge.describeTo(description);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Vehicle)) {
                    return false;
                }
                Vehicle oVehicle = (Vehicle) o;
                return oVehicle.getCurrentEdge().filter(edge::matches).isPresent();
            }
        };
    }

    static Optional<Vehicle> vehicleById(Collection<Vehicle> vehicles, UUID id) {
        requireNonNull(vehicles);
        requireNonNull(id);
        return vehicles.stream().filter(v -> v.getId().equals(id)).findAny();
    }

    static Optional<Vehicle> vehicleById(Collection<Vehicle> vehicles, Vehicle vehicle) {
        requireNonNull(vehicle);
        return vehicleById(vehicles, vehicle.getId());
    }

    static Matcher<Vehicle> vehicleId(Vehicle expectedVehicle) {
        requireNonNull(expectedVehicle);
        return vehicleId(expectedVehicle.getId());
    }

    static Matcher<Vehicle> vehicleId(UUID expectedId) {
        requireNonNull(expectedId);
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Vehicle ").
                        appendValue(expectedId);
            }

            @Override
            public boolean matches(Object o) {
                return vehicleId(o, expectedId);
            }
        };
    }

    private static boolean vehicleId(Object o, UUID expectedId) {
        if (!(o instanceof Vehicle)) {
            return false;
        }
        Vehicle oVehicle = (Vehicle) o;
        return oVehicle.getId().equals(expectedId);
    }
}
