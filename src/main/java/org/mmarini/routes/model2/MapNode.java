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

/**
 * @author marco.marini@mmarini.org
 */
public class MapNode implements MapElement {
    /**
     * Returns a map node at a location
     *
     * @param x coordinate
     * @param y y coordinate
     */
    public static MapNode createNode(double x, double y) {
        return new MapNode(new Point2D.Double(x, y));
    }

    private final Point2D location;

    /**
     * Create a node
     *
     * @param location the location of node
     */
    public MapNode(Point2D location) {
        assert location != null;
        this.location = location;
    }

    @Override
    public <T> T apply(final MapElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapNode mapNode = (MapNode) o;
        return location.equals(mapNode.location);
    }

    /**
     * Returns the square of distance of the node from a point
     *
     * @param point the point
     */
    public double getDistanceSq(final Point2D point) {
        return location.distanceSq(point);
    }

    /**
     * Returns the location of node
     */
    public Point2D getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MapNode.class.getSimpleName() + "[", "]")
                .add("" + location.getX())
                .add("" + location.getY())
                .toString();
    }
}
