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

import org.mmarini.Tuple2;
import org.mmarini.routes.model2.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.getValue;
import static org.mmarini.Utils.zipWithIndex;

public class StatusView {
    public static final Color DEFAULT_NODE_COLOR = Color.LIGHT_GRAY;
    private static final double DEFAULT_MAP_SIZE = 5000;
    private static final Rectangle2D DEFAULT_MAP_BOUND = new Rectangle2D.Double(0, 0, DEFAULT_MAP_SIZE,
            DEFAULT_MAP_SIZE);
    private static final double NODE_SATURATION = 1;

    /**
     * @param status the status
     */
    public static StatusView createStatusView(Status status) {
        List<MapNode> nodes = status.getNodes();
        List<SiteNode> sites = status.getSites();
        List<MapEdge> edges = status.getEdges();

        // Computes site color map
        int noSites = sites.size();
        SwingUtils util = SwingUtils.getInstance();
        Map<MapNode, Color> colorBySite = zipWithIndex(sites)
                .collect(Collectors.toMap(
                        Tuple2::getV2,
                        entry -> {
                            int i = entry._1;
                            final double value = (double) i / (noSites - 1);
                            return util.computeColor(value, NODE_SATURATION);
                        }));

        // Converts to node view
        String nodeNamePattern = Messages.getString("RouteMediator.nodeNamePattern"); //$NON-NLS-1$
        List<NodeView> nodeViews = zipWithIndex(nodes)
                .map(entry -> {
                    int i = entry._1;
                    MapNode node = entry._2;
                    return new NodeView(
                            MessageFormat.format(nodeNamePattern, i + 1),
                            node,
                            getValue(colorBySite, node).orElse(DEFAULT_NODE_COLOR));
                })
                .collect(Collectors.toList());
        Map<MapNode, NodeView> viewByNode = nodeViews.stream().collect(Collectors.toMap(NodeView::getNode, Function.identity()));

        // Converts to edgeView
        String edgeNamePattern = Messages.getString("RouteMediator.edgeNamePattern"); //$NON-NLS-1$
        List<EdgeView> edgesViews = zipWithIndex(edges)
                .map(entry -> {
                    int i = entry._1;
                    MapEdge edge = entry._2;
                    final String begin = Optional.ofNullable(viewByNode.get(edge.getBegin()))
                            .map(NodeView::getName).orElse("?");
                    final String end = Optional.ofNullable(viewByNode.get(edge.getEnd()))
                            .map(NodeView::getName).orElse("?");
                    final String name = MessageFormat.format(edgeNamePattern, i, begin, end);
                    return new EdgeView(edge, name, begin, end, edge.getPriority(), edge.getSpeedLimit());
                })
                .collect(Collectors.toList());
        Map<MapEdge, EdgeView> viewByEdge = edgesViews.stream()
                .collect(Collectors.toMap(EdgeView::getEdge, Function.identity()));
        List<NodeView> siteViews = nodeViews.stream()
                .filter(node -> node.getNode() instanceof SiteNode)
                .collect(Collectors.toList());
        return new StatusView(status, nodeViews, siteViews, edgesViews, viewByNode, viewByEdge);
    }

    private final Status status;
    private final List<NodeView> nodeViews;
    private final List<NodeView> siteViews;
    private final List<EdgeView> edgesViews;
    private final Map<MapNode, NodeView> viewByNode;
    private final Map<MapEdge, EdgeView> viewByEdge;

    /**
     * @param status     the status
     * @param nodeViews  the node views
     * @param siteViews  the site views
     * @param edgesViews the edge Views
     * @param viewByNode the view by node
     * @param viewByEdge the view by edge
     */
    protected StatusView(Status status,
                         List<NodeView> nodeViews,
                         List<NodeView> siteViews,
                         List<EdgeView> edgesViews,
                         Map<MapNode, NodeView> viewByNode,
                         Map<MapEdge, EdgeView> viewByEdge) {
        this.status = requireNonNull(status);
        this.nodeViews = requireNonNull(nodeViews);
        this.siteViews = requireNonNull(siteViews);
        this.edgesViews = requireNonNull(edgesViews);
        this.viewByNode = requireNonNull(viewByNode);
        this.viewByEdge = requireNonNull(viewByEdge);
    }

    /**
     *
     */
    public Rectangle2D computeMapBound() {
        Rectangle2D.Double bound = new Rectangle2D.Double();
        if (status.getNodes().isEmpty()) {
            bound.setFrame(DEFAULT_MAP_BOUND);
        } else if (status.getNodes().size() == 1) {
            final Point2D location = status.getNodes().get(0).getLocation();
            bound.setFrameFromCenter(location.getX(), location.getY(), DEFAULT_MAP_SIZE * 0.5, DEFAULT_MAP_SIZE * 0.5);
        } else {
            final Point2D location = status.getNodes().get(0).getLocation();
            bound.setFrame(location.getX(), location.getY(), 0, 0);
            for (final MapNode node : status.getNodes()) {
                bound.add(node.getLocation());
            }
        }
        return bound;
    }

    /**
     * @param point     the point
     * @param precision the precision
     */
    public Optional<MapEdge> findEdge(final Point2D point, final double precision) {
        double dist = precision * precision;
        MapEdge edge = null;
        for (final MapEdge e : status.getEdges()) {
            final double d = e.distanceSqFrom(point);
            if (d <= dist) {
                dist = d;
                edge = e;
            }
        }
        return Optional.ofNullable(edge);
    }

    /**
     * @param point     the point
     * @param precision the precision
     */
    public Optional<MapElement> findElement(final Point2D point, final double precision) {
        return findNode(point, precision)
                .<MapElement>map(Function.identity())
                .or(() -> findEdge(point, precision));
    }

    /**
     * @param point     the point
     * @param precision the precision
     */
    public Optional<MapNode> findNode(final Point2D point, final double precision) {
        double dist = precision * precision;
        MapNode element = null;
        for (final MapNode n : status.getNodes()) {
            final double d = n.distanceSqFrom(point);
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
        return status.getEdges();
    }

    public double getEdgesTrafficLevel(MapEdge edge) {
        return status.edgeTrafficLevel(edge);
    }

    public List<EdgeView> getEdgesViews() {
        return edgesViews;
    }

    public DoubleMatrix<NodeView> getFrequencies() {
        return status.getPathFrequencies().map(siteViews, view ->
                view.getNode() instanceof SiteNode ?
                        Optional.of((SiteNode) view.getNode())
                        : Optional.empty());
    }

    public Optional<NodeView> getNodeView(MapNode node) {
        return getValue(viewByNode, node);
    }

    public List<NodeView> getNodeViews() {
        return nodeViews;
    }

    public List<MapNode> getNodes() {
        return status.getNodes();
    }

    public List<SiteNode> getSites() {
        return status.getSites();
    }

    public List<TrafficInfo> getTrafficInfo() {
        return status.getTrafficInfo();
    }

    public List<Vehicle> getVehicles() {
        return status.getVehicles();
    }

    public DoubleMatrix<NodeView> getWeightMatrix() {
        return status.getWeightMatrix().map(siteViews,
                view -> Optional.of((SiteNode) view.getNode()));
    }

    public Point2D snapToNode(final Point2D point, final double precision) {
        return findNode(point, precision)
                .map(MapNode::getLocation)
                .orElse(point);
    }
}
