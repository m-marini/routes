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
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;
import static org.mmarini.routes.model2.Constants.gridPoint;

/**
 * @author marco.marini@mmarini.org
 */
public class CrossNode implements MapNode {

    /**
     * Returns a map node at a location
     *
     * @param x coordinate
     * @param y y coordinate
     */
    public static CrossNode createNode(double x, double y) {
        return new CrossNode(gridPoint(x, y));
    }

    private final Point2D location;

    /**
     * Create a node
     *
     * @param location the location of node
     */
    public CrossNode(Point2D location) {
        this.location = requireNonNull(location);
    }

    @Override
    public <T> T apply(final MapElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Returns the square of distance of the node from a point
     *
     * @param point the point
     */
    @Override
    public double distanceSqFrom(final Point2D point) {
        return location.distanceSq(point);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossNode mapNode = (CrossNode) o;
        return location.equals(mapNode.location);
    }

    @Override
    public Point2D getLocation() {
        return location;
    }

    @Override
    public CrossNode setLocation(Point2D location) {
        return new CrossNode(location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public boolean isSameLocation(MapNode node) {
        return location.equals(node.getLocation());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrossNode.class.getSimpleName() + "[", "]")
                .add("" + location.getX())
                .add("" + location.getY())
                .toString();
    }
}
