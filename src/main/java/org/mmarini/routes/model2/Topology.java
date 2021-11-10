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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.entriesToMap;
import static org.mmarini.Utils.getValue;
import static org.mmarini.routes.model2.CrossNode.createNode;

/**
 * The topology of a map with nodes, sites and edges
 */
public class Topology {

    /**
     * Returns a topology
     *
     * @param nodes the node list
     * @param edges the edge list
     */
    public static Topology createTopology(List<MapNode> nodes,
                                          List<MapEdge> edges) {
        // Sort nodes
        ArrayList<MapNode> sortedNode = new ArrayList<>(nodes);
        sortedNode.sort((a, b) -> {
            return a instanceof SiteNode
                    ? b instanceof SiteNode
                    ? 0 // SiteNode, SiteNode
                    : -1 // SiteNode, CrossNode
                    : b instanceof SiteNode
                    ? 1 // CrossNode, SiteNode
                    : 0; // CrossNode, CrossNode
        });
        List<SiteNode> sites = nodes.stream()
                .filter(SiteNode.class::isInstance)
                .map(n -> (SiteNode) n)
                .collect(Collectors.toList());
        // creates the map of entry edges by node
        Map<MapNode, List<MapEdge>> entryEdgesByNode = edges.stream()
                .collect(Collectors.groupingBy(MapEdge::getEnd));
        // Sort edges by priority
        entryEdgesByNode.replaceAll((node, list) -> {
            list.sort((a, b) -> -Integer.compare(a.getPriority(), b.getPriority()));
            return list;
        });
        return new Topology(sites, sortedNode, edges, entryEdgesByNode);
    }

    private final List<SiteNode> sites;
    private final List<MapNode> nodes;
    private final List<MapEdge> edges;
    private final Map<MapNode, List<MapEdge>> entryEdgesByNode;

    /**
     * Create a topology
     *
     * @param sites            the sites list
     * @param nodes            the nodes list
     * @param edges            the edge list
     * @param entryEdgesByNode the entry edges by node ascending sorted by priority
     */
    protected Topology(List<SiteNode> sites,
                       List<MapNode> nodes,
                       List<MapEdge> edges,
                       Map<MapNode, List<MapEdge>> entryEdgesByNode) {
        this.sites = requireNonNull(sites);
        this.nodes = requireNonNull(nodes);
        this.edges = requireNonNull(edges);
        this.entryEdgesByNode = requireNonNull(entryEdgesByNode);
    }

    /**
     * Returns the topology with a new edge
     *
     * @param edge the edge
     */
    public Topology addEdge(MapEdge edge) {
        if (edges.contains(edge)) {
            // No changes because edge already exist
            return this;
        } else {
            MapNode begin = edge.getBegin();
            MapNode end = edge.getEnd();
            boolean isBeginNewNode = !nodes.contains(begin);
            boolean isEndNewNode = !nodes.contains(end);
            assert !(isBeginNewNode && begin instanceof SiteNode)
                    : "begin cannot be a new SiteNode";
            assert !(isEndNewNode && end instanceof SiteNode)
                    : "end cannot be a new SiteNode";
            List<MapNode> newNodes = isBeginNewNode || isEndNewNode
                    ? new ArrayList<>(nodes) : nodes;
            if (isBeginNewNode) {
                newNodes.add(begin);
            }
            if (isEndNewNode) {
                newNodes.add(end);
            }
            List<MapEdge> newEdges = new ArrayList<>(edges);
            newEdges.add(edge);
            return createTopology(newNodes, newEdges);
        }
    }

    /**
     * Add a mapModule in the topology
     *
     * @param mapModule the mapModule
     * @param location  the location
     * @param direction the direction
     * @param epsilon   the marginal distance to map existing nodes
     */
    public Topology addModule(MapModule mapModule, Point2D location, Point2D direction, double epsilon) {
        AffineTransform tr = AffineTransform.getTranslateInstance(location.getX(), location.getY());
        tr.rotate(direction.getX(), direction.getY());
        double epsilonSq = epsilon * epsilon;
        Map<MapNode, MapNode> nodeMap = mapModule.getNodes().stream()
                .map(node -> {
                    Point2D pt = tr.transform(node.getLocation(), null);
                    MapNode newNode = findNearestNode(pt)
                            .filter(mapNode ->
                                    mapNode.getLocation().distanceSq(pt) <= epsilonSq
                            ).orElseGet(
                                    () -> createNode(pt.getX(), pt.getY())
                            );
                    return entry(node, newNode);
                }).collect(entriesToMap());
        List<MapEdge> edges = Stream.concat(
                        // concatenates map edges
                        this.edges.stream(),
                        // generates mapModule edges
                        mapModule.getEdges().stream()
                                .flatMap(edge ->
                                        getValue(nodeMap, edge.getBegin())
                                                .map(edge::setBegin)
                                                .stream())
                                .flatMap(edge -> getValue(nodeMap, edge.getEnd())
                                        .map(edge::setEnd)
                                        .stream())
                                .map(edge ->
                                        edge.setSpeedLimit(min(edge.getSpeedLimit(), edge.getSafetySpeed()))))
                .collect(Collectors.toList());
        List<MapNode> nodes = Stream.concat(this.nodes.stream(),
                        nodeMap.values()
                                .stream()
                                .filter(Predicate.not(this.nodes::contains)))
                .collect(Collectors.toList());

        return createTopology(nodes, edges);
    }

    /**
     * Returns the topology with a changed node
     *
     * @param node the changing node
     */
    public Topology changeNode(MapNode node) {
        MapNode newNode = node instanceof SiteNode
                ? new CrossNode(node.getLocation())
                : new SiteNode(node.getLocation());
        List<MapNode> newNodes = nodes.stream()
                .filter(Predicate.not(node::equals))
                .collect(Collectors.toList());
        newNodes.add(newNode);
        List<MapEdge> newEdges = edges.stream()
                .map(edge -> {
                    if (edge.getBegin().isSameLocation(node)) {
                        return edge.setBegin(newNode);
                    } else if (edge.getEnd().isSameLocation(node)) {
                        return edge.setEnd(newNode);
                    } else {
                        return edge;
                    }
                })
                .collect(Collectors.toList());
        return createTopology(newNodes, newEdges);
    }

    /**
     * Returns the map of nodes from this to another topology
     *
     * @param other the other topology
     */
    public Map<MapEdge, MapEdge> createEdgeMap(Topology other) {
        return edges.stream()
                .flatMap(edge ->
                        other.edges.stream()
                                .filter(edge::isSameLocation)
                                .findAny()
                                .stream()
                                .map(otherEdge ->
                                        entry(edge, otherEdge))
                )
                .collect(entriesToMap());
    }

    /**
     * Returns the map of nodes from this to another topology
     *
     * @param other the other topology
     */
    public Map<MapNode, MapNode> createNodeMap(Topology other) {
        return nodes.stream()
                .flatMap(node ->
                        other.nodes.stream()
                                .filter(node::isSameLocation)
                                .findAny()
                                .stream()
                                .map(otherNode ->
                                        entry(node, otherNode)))
                .collect(entriesToMap());
    }

    /**
     * Returns the map of nodes from this to another topology
     *
     * @param other the other topology
     */
    public Map<SiteNode, SiteNode> createSiteMap(Topology other) {
        return sites.stream()
                .flatMap(site ->
                        other.sites.stream()
                                .filter(site::isSameLocation)
                                .findAny()
                                .stream()
                                .map(otherSite ->
                                        entry(site, otherSite))
                )
                .collect(entriesToMap());
    }

    /**
     * Returns the nearest node to a point
     *
     * @param point the point
     */
    Optional<MapNode> findNearestNode(Point2D point) {
        return nodes.stream().reduce((a, b) ->
                a.getLocation().distanceSq(point) <= b.getLocation().distanceSq(point)
                        ? a : b
        );
    }

    /**
     * Returns the list of edges
     */
    public List<MapEdge> getEdges() {
        return edges;
    }

    /**
     * Returns the incoming edges at a given node.
     * The list is sorted ascending by priority
     *
     * @param node the node
     */
    public List<MapEdge> getIncomeEdges(MapNode node) {
        List<MapEdge> list = entryEdgesByNode.get(node);
        return list != null ? list : List.of();
    }

    /**
     * Returns the incoming edges crossing an incoming edge and with higher priority.
     * The list is sorted ascending by priority
     *
     * @param incomingEdge the incoming edge
     */
    public List<MapEdge> getIncomeEdges(MapEdge incomingEdge) {
        return getIncomeEdges(incomingEdge.getEnd())
                .stream()
                .filter(edge -> !edge.equals(incomingEdge))
                .takeWhile(edge -> edge.getPriority() > incomingEdge.getPriority())
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of nodes
     */
    public List<MapNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the list of sites
     */
    public List<SiteNode> getSites() {
        return sites;
    }

    /**
     * Returns the optimized topology
     *
     * @param maxSpeed the maximum speed limit for the edges
     */
    public Topology optimize(double maxSpeed) {
        // Filter effective nodes
        List<MapNode> nodes = this.nodes.stream().filter(
                node -> node instanceof SiteNode ||
                        edges.stream().anyMatch(edge -> edge.isCrossingNode(node))
        ).collect(Collectors.toList());

        // Remap edges
        List<MapEdge> edges = this.edges.stream()
                .map(edge -> edge.setSpeedLimit(min(maxSpeed, edge.getSafetySpeed())))
                .collect(Collectors.toList());

        return createTopology(nodes, edges);
    }


    /**
     * Returns the topology without an edge
     *
     * @param edge the edge
     */
    public Topology removeEdge(MapEdge edge) {
        List<MapEdge> newEdges = edges.stream()
                .filter(Predicate.not(e -> e.equals(edge)))
                .collect(Collectors.toList());
        return createTopology(nodes, newEdges);
    }

    /**
     * Returns the topology without a node
     *
     * @param node the node
     */
    public Topology removeNode(MapNode node) {
        List<MapNode> newNodes = nodes.stream()
                .filter(Predicate.not(node::equals))
                .collect(Collectors.toList());
        List<MapEdge> newEdges = edges.stream()
                .filter(edge ->
                        !(edge.getBegin().equals(node) || edge.getEnd().equals(node)))
                .collect(Collectors.toList());
        return createTopology(newNodes, newEdges);
    }
}
