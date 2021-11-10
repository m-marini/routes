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
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class MapModule {
    private final List<MapEdge> edges;

    public MapModule(List<MapEdge> edges) {
        this.edges = edges;
    }

    /**
     * Returns the bound rectangle
     */
    public Rectangle2D getBound() {
        List<Point2D> pts = getNodes().stream().map(MapNode::getLocation).collect(Collectors.toList());
        double x0 = pts.stream().mapToDouble(Point2D::getX).min().orElse(0);
        double y0 = pts.stream().mapToDouble(Point2D::getY).min().orElse(0);
        double x1 = pts.stream().mapToDouble(Point2D::getX).max().orElse(0);
        double y1 = pts.stream().mapToDouble(Point2D::getY).max().orElse(0);
        return new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0);
    }

    /**
     * Returns the edges
     */
    public List<MapEdge> getEdges() {
        return edges;
    }

    /**
     * Returns the set of node
     */
    Set<MapNode> getNodes() {
        return getEdges().stream()
                .flatMap(mapEdge -> Stream.of(mapEdge.getBegin(), mapEdge.getEnd()))
                .collect(Collectors.toSet());
    }
}
