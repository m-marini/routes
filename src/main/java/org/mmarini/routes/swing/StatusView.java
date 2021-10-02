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

package org.mmarini.routes.swing;

import org.mmarini.routes.model.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StatusView {
    public static final Color DEFAULT_NODE_COLOR = Color.LIGHT_GRAY;
    private static final double DEFAULT_MAP_SIZE = 5000;
    private static final Rectangle2D DEFAULT_MAP_BOUND = new Rectangle2D.Double(0, 0, DEFAULT_MAP_SIZE,
            DEFAULT_MAP_SIZE);


    private final List<MapNode> nodes;
    private final List<SiteNode> sites;
    private final List<MapEdge> edges;
    private final List<NodeView> nodeViews;
    private final List<EdgeView> edgesViews;
    private final List<Vehicle> vehicles;
    private final Map<MapNode, NodeView> viewByNode;
    private final Map<MapEdge, EdgeView> viewByEdge;
    private final List<TrafficInfo> trafficInfo;
    private final List<Path> paths;
    private final RouteInfos routeInfos;

    public StatusView(List<MapNode> nodes,
                      List<SiteNode> sites,
                      List<MapEdge> edges,
                      List<Vehicle> vehicles,
                      List<TrafficInfo> trafficInfo,
                      List<Path> paths,
                      RouteInfos routeInfos,
                      List<NodeView> nodeViews,
                      List<EdgeView> edgesViews,
                      Map<MapNode, NodeView> viewByNode,
                      Map<MapEdge, EdgeView> viewByEdge) {
        this.nodes = nodes;
        this.sites = sites;
        this.nodeViews = nodeViews;
        this.edges = edges;
        this.paths = paths;
        this.trafficInfo = trafficInfo;
        this.routeInfos = routeInfos;
        this.edgesViews = edgesViews;
        this.vehicles = vehicles;
        this.viewByNode = viewByNode;
        this.viewByEdge = viewByEdge;
    }

    /**
     *
     */
    public Rectangle2D computeMapBound() {
        Rectangle2D.Double bound = new Rectangle2D.Double();
        if (nodes.isEmpty()) {
            bound.setFrame(DEFAULT_MAP_BOUND);
        } else if (nodes.size() == 1) {
            final Point2D location = nodes.get(0).getLocation();
            bound.setFrameFromCenter(location.getX(), location.getY(), DEFAULT_MAP_SIZE * 0.5, DEFAULT_MAP_SIZE * 0.5);
        } else {
            final Point2D location = nodes.get(0).getLocation();
            bound.setFrame(location.getX(), location.getY(), 0, 0);
            for (final MapNode node : nodes) {
                bound.add(node.getLocation());
            }
        }
        return bound;
    }

    /**
     * @param point
     * @param precision
     * @return
     */
    public Optional<MapEdge> findEdge(final Point2D point, final double precision) {
        double dist = precision * precision;
        MapEdge edge = null;
        for (final MapEdge e : edges) {
            final double d = e.getDistanceSq(point);
            if (d <= dist) {
                dist = d;
                edge = e;
            }
        }
        return Optional.ofNullable(edge);
    }

    /**
     * @param point
     * @param precision
     */
    public Optional<MapElement> findElement(final Point2D point, final double precision) {
        return findNode(point, precision)
                .<MapElement>map(Function.identity())
                .or(() -> findEdge(point, precision));
    }

    /**
     * @param point
     * @param precision
     */
    public Optional<MapNode> findNode(final Point2D point, final double precision) {
        double dist = precision * precision;
        MapNode element = null;
        for (final MapNode n : nodes) {
            final double d = n.getDistanceSq(point);
            if (d <= dist) {
                dist = d;
                element = n;
            }
        }
        return Optional.ofNullable(element);
    }

    public Optional<EdgeView> getEdgeViews(MapEdge edge) {
        return Optional.ofNullable(viewByEdge.get(edge));
    }

    public List<MapEdge> getEdges() {
        return edges;
    }

    public List<EdgeView> getEdgesViews() {
        return edgesViews;
    }

    public Optional<NodeView> getNodeView(MapNode node) {
        return Optional.ofNullable(viewByNode.get(node));
    }

    public List<NodeView> getNodeViews() {
        return nodeViews;
    }

    public List<MapNode> getNodes() {
        return nodes;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public RouteInfos getRouteInfos() {
        return routeInfos;
    }

    public List<SiteNode> getSites() {
        return sites;
    }

    public List<TrafficInfo> getTrafficInfo() {
        return trafficInfo;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public Point2D snapToNode(final Point2D point, final double precision) {
        return findNode(point, precision)
                .map(MapNode::getLocation)
                .orElse(point);
    }
}
