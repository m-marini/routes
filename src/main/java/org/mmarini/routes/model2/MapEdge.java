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

import java.awt.geom.Point2D;
import java.util.StringJoiner;

import static java.lang.Math.floor;
import static java.util.Objects.requireNonNull;
import static org.mmarini.routes.model2.Constants.*;

/**
 * A map edge starting from the beginning node to the end node with speed limit and priority
 *
 * @author marco.marini@mmarini.org
 */
public class MapEdge implements MapElement {
    private final MapNode begin;
    private final MapNode end;
    private final double speedLimit;
    private final int priority;

    /**
     * @param begin      the beginning node
     * @param end        the end node
     * @param speedLimit the speed limit
     * @param priority   the priority
     */
    public MapEdge(MapNode begin, MapNode end, double speedLimit, int priority) {
        this.begin = requireNonNull(begin);
        this.end = requireNonNull(end);
        assert !begin.equals(end);
        this.speedLimit = speedLimit;
        this.priority = priority;
    }

    @Override
    public <T> T apply(final MapElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Returns the closer point in the edge from a given point
     *
     * @param point the point
     */
    Point2D closerFrom(final Point2D point) {
        final double ka = ka(point);
        if (ka <= 0) {
            return begin.getLocation();
        } else if (ka >= getLength()) {
            return end.getLocation();
        } else {
            return locationAt(ka);
        }
    }

    /**
     * Returns the distance from the closer point in the edge
     *
     * @param point the point
     */
    @Override
    public double distanceSqFrom(final Point2D point) {
        return point.distanceSq(closerFrom(point));
    }

    /**
     * Returns the beginning node
     */
    public MapNode getBegin() {
        return begin;
    }

    /**
     * Returns an edge with new beginning node
     *
     * @param begin the beginning node
     */
    public MapEdge setBegin(MapNode begin) {
        return new MapEdge(begin, end, speedLimit, priority);
    }

    /**
     * Returns the beginning location
     */
    public Point2D getBeginLocation() {
        return begin.getLocation();
    }

    /**
     * @return the edgeVector
     */
    public Point2D getDirection() {
        Point2D beginLocation = begin.getLocation();
        Point2D endLocation = end.getLocation();
        return new Point2D.Double(
                endLocation.getX() - beginLocation.getX(),
                endLocation.getY() - beginLocation.getY());
    }

    /**
     * Returns the end node
     */
    public MapNode getEnd() {
        return end;
    }

    /**
     * Returns an edge with new end node
     *
     * @param end the end node
     */
    public MapEdge setEnd(MapNode end) {
        return new MapEdge(begin, end, speedLimit, priority);
    }

    /**
     * Returns the end location
     */
    public Point2D getEndLocation() {
        return end.getLocation();
    }

    /**
     * Returns the length of edge
     */
    public double getLength() {
        return begin.getLocation().distance(end.getLocation());
    }

    /**
     * Returns the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the edge with priority
     *
     * @param priority the priority
     */
    public MapEdge setPriority(int priority) {
        return new MapEdge(begin, end, speedLimit, priority);
    }

    /**
     * Returns the safety distance in the edge
     */
    public double getSafetyDistance() {
        return computeSafetyDistance(speedLimit);
    }

    /**
     * Returns the safety speed in the edge
     * The safety speed is the speed with a safatey distance equal to the length of the edge
     */
    public double getSafetySpeed() {
        return computeSafetySpeed(getLength());
    }

    /**
     * Returns the speedLimit
     */
    public double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * Returns the edge with speed limit
     *
     * @param speedLimit the speed limit
     */
    public MapEdge setSpeedLimit(double speedLimit) {
        return new MapEdge(begin, end, speedLimit, priority);
    }

    /**
     * Returns the traffic level for a given number of vehicles
     *
     * @param vehicleCount the number of vehicles
     */
    public double getTrafficLevel(int vehicleCount) {
        if (vehicleCount == 0) {
            return 0;
        } else {
            double length = getLength();
            int maxVehicles = (int) floor(length / VEHICLE_LENGTH);
            int optimalCount = (int) floor(length / (getSafetyDistance() + VEHICLE_LENGTH));
            if (vehicleCount <= optimalCount) {
                return vehicleCount / optimalCount * 0.5;
            } else {
                /*
                (vehicleCount - optimalCount) / (maxVehicles - optimalCount) * 0.5 + 0.5;
                (vehicleCount - optimalCount) / (maxVehicles - optimalCount) / 2 + 1/2;
                ((vehicleCount - optimalCount) + (maxVehicles - optimalCount))  / (maxVehicles - optimalCount) /2;
                (vehicleCount - optimalCount + maxVehicles - optimalCount)  / (maxVehicles - optimalCount) /2;
                (vehicleCount + maxVehicles - 2 * optimalCount)  / (maxVehicles - optimalCount) /2;
                 */
                int n = vehicleCount + maxVehicles - 2 * optimalCount;
                int d = maxVehicles - optimalCount;
                return 0.5 * n / d;
            }
        }
    }

    /**
     * Returns the transit time: length / speedLimit
     */
    public double getTransitTime() {
        return getLength() / speedLimit;
    }

    /**
     * Returns true if the edge is crossing a node
     *
     * @param node the node
     */
    public boolean isCrossingNode(MapNode node) {
        return begin.equals(node) || end.equals(node);
    }

    /**
     * Retuns true is the edge is located at same location of other
     *
     * @param other the other edge
     */
    public boolean isSameLocation(MapEdge other) {
        return begin.isSameLocation(other.begin) && end.isSameLocation(other.end);
    }

    /**
     * Returns the distance from the beginning and the closer point in the edge direction
     * from a given point
     *
     * @param point the point
     */
    double ka(final Point2D point) {
        final Point2D begin = getBeginLocation();
        final double x0 = begin.getX();
        final double y0 = begin.getY();
        final Point2D ev = getDirection();
        final double xe = ev.getX();
        final double ye = ev.getY();
        final double xp = point.getX() - x0;
        final double yp = point.getY() - y0;
        final double ep = xe * xp + ye * yp;
        return ep / getLength();
    }

    /**
     * Returns the location at a distance from the beginning
     *
     * @param distance the distance
     */
    public Point2D locationAt(final double distance) {
        final Point2D end = getEndLocation();
        final Point2D begin = getBeginLocation();
        final double k = distance / getLength();
        final double x0 = begin.getX();
        final double y0 = begin.getY();
        return new Point2D.Double(
                k * (end.getX() - x0) + x0,
                k * (end.getY() - y0) + y0);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MapEdge.class.getSimpleName() + "[", "]")
                .add(begin.toString())
                .add(end.toString())
                .toString();
    }
}
