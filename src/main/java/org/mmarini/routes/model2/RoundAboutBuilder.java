/*
 *
 * Copyright (c) 2023 Marco Marini, marco.marini@mmarini.org
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
 */

package org.mmarini.routes.model2;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static java.lang.String.format;
import static org.mmarini.routes.model2.Constants.*;

/**
 *
 */
public class RoundAboutBuilder implements ModuleBuilder {
    public static final double TRACK_SIZE = 6d;
    public static final double Y0 = TRACK_SIZE * 1.5;
    public static final double Y1 = TRACK_SIZE * 0.5;
    public static final double Y2 = -TRACK_SIZE * 0.5;
    public static final double Y3 = -TRACK_SIZE * 1.5;

    static MapEdge createEdge(MapNode from, MapNode to, int priority) {
        double distance = to.getLocation().distance(from.getLocation());
        double speed = min(computeSafetySpeed(distance), DEFAULT_SPEED_LIMIT_MPS);
        return new MapEdge(from, to, speed, priority);
    }

    private final int numEntries;
    private final double edgeLength;
    private final MapNode[] nodes;

    public RoundAboutBuilder(int numEntries, double edgeLength) {
        if ((numEntries % 2) != 0) {
            throw new IllegalArgumentException(format("numEntries must be even (%d)", numEntries));
        }
        this.numEntries = numEntries;
        this.edgeLength = edgeLength;
        this.nodes = createNodes();
    }

    @Override
    public MapModule build() {
        List<MapEdge> edges = createEdges().collect(Collectors.toList());
        return new MapModule(edges);
    }

    Stream<MapNode> createCircleNodes(double radius) {
        return IntStream.range(0, getRoundEdgeNum()).mapToObj(i -> {
            double angle = i * getEdgeAngle();
            double x = radius * cos(angle);
            double y = -radius * sin(angle);
            return new CrossNode(new Point2D.Double(x, y));
        });
    }

    Stream<MapEdge> createCrossEdge(int i) {
        int opposite = i + numEntries / 2;
        return Stream.of(
                createEdge(getEntryNode(opposite, 3), getEntryNode(i, 0), 1),
                createEdge(getEntryNode(opposite, 2), getEntryNode(i, 1), 1),
                createEdge(getEntryNode(i, 2), getEntryNode(opposite, 1), 1),
                createEdge(getEntryNode(i, 3), getEntryNode(opposite, 0), 1));
    }

    Stream<MapEdge> createCrossEdges() {
        return IntStream.range(0, numEntries / 2).boxed().flatMap(this::createCrossEdge);
    }

    Stream<MapEdge> createEdges() {
        return Stream.of(
                createRoundEdges(this::createInnerEdge),
                createRoundEdges(this::createOuterEdge),
                createCrossEdges(),
                createJunctionEdges()
        ).flatMap(Function.identity());
    }

    Stream<MapNode> createEntriesNodes() {
        return IntStream.range(0, numEntries).boxed()
                .flatMap(this::createEntryNodes);
    }

    Stream<MapNode> createEntryNodes(int i) {
        double radius = getOuterRadius();
        double angle = getEdgeAngle();
        double x1 = radius * cos(angle);
        double yr = radius * sin(angle);
        double dyr = yr - Y0;
        double x2 = sqrt(edgeLength * edgeLength - dyr * dyr);
        double x = x1 + x2;
        double rotation = 2 * PI * i / numEntries;
        AffineTransform tr = AffineTransform.getRotateInstance(-rotation);
        return DoubleStream.of(Y0, Y1, Y2, Y3).mapToObj(y ->
                createTransformedNode(x, y, tr)
        );
    }

    MapEdge createInnerEdge(int i) {
        return createEdge(getInnerNode(i), getInnerNode(i + 1), 1);
    }

    Stream<MapEdge> createJunctionEdge(int i) {
        int ii = i * 2;
        MapNode ib = getInnerNode(ii - 1);
        MapNode ob = getOuterNode(ii - 1);
        MapNode ie = getInnerNode(ii + 1);
        MapNode oe = getOuterNode(ii + 1);
        MapNode e0 = getEntryNode(i, 0);
        MapNode e1 = getEntryNode(i, 1);
        MapNode b2 = getEntryNode(i, 2);
        MapNode b3 = getEntryNode(i, 3);
        return Stream.of(
                createEdge(ib, e0, 0),
                createEdge(ob, e0, 0),
                createEdge(ib, e1, 0),
                createEdge(ob, e1, 0),
                createEdge(b2, ie, 0),
                createEdge(b2, oe, 0),
                createEdge(b3, ie, 0),
                createEdge(b3, oe, 0)
        );
    }

    private Stream<MapEdge> createJunctionEdges() {
        return IntStream.range(0, numEntries).boxed()
                .flatMap(this::createJunctionEdge);
    }

    MapNode[] createNodes() {
        return Stream.of(
                        createCircleNodes(getInnerRadius()),
                        createCircleNodes(getOuterRadius()),
                        createEntriesNodes())
                .flatMap(Function.identity())
                .toArray(MapNode[]::new);
    }

    MapEdge createOuterEdge(int i) {
        return createEdge(getOuterNode(i), getOuterNode(i + 1), 1);
    }

    Stream<MapEdge> createRoundEdges(IntFunction<MapEdge> mapper) {
        int n = getRoundEdgeNum();
        return IntStream.range(0, n).mapToObj(mapper);
    }

    private MapNode createTransformedNode(double x, double y, AffineTransform tr) {
        Point2D.Double point = new Point2D.Double(x, y);
        tr.transform(point, point);
        return new CrossNode(gridPoint(point));
    }

    double getEdgeAngle() {
        return PI / numEntries;
    }

    /**
     * Returns entry node
     *
     * @param entryIndex the entry index (-numEntries ... numEntries)
     * @param nodeIndex  the node index (0-3)
     */
    MapNode getEntryNode(int entryIndex, int nodeIndex) {
        int index = ((entryIndex + numEntries) % numEntries) * 4 + nodeIndex + numEntries * 4;
        return nodes[index];
    }

    /**
     * Returns the inner node at a specific index.
     * <p>
     * Index 0 starts with most left nodes than clock counter wise
     * </p>
     *
     * @param i index (-numEntries .. numEntries)
     */
    MapNode getInnerNode(int i) {
        int n = 2 * numEntries;
        return nodes[(i + n) % n];
    }

    /**
     * Returns the inner radius
     */
    double getInnerRadius() {
        double angle = getEdgeAngle() / 2;
        return edgeLength / sin(angle) / 2;
    }


    /**
     * Returns the outer node at a specific index
     * <p>
     * Index 0 starts with most left nodes than ccw
     * </p>
     *
     * @param i index (-numEntries .. numEntries)
     */
    MapNode getOuterNode(int i) {
        int n = numEntries * 2;
        return nodes[(i + n) % n + n];
    }


    /**
     * Returns the outer radius
     */
    double getOuterRadius() {
        return getInnerRadius() + TRACK_SIZE;
    }

    int getRoundEdgeNum() {
        return numEntries * 2;
    }
}
