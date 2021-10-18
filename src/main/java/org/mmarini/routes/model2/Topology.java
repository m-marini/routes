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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The topology of a map with nodes, sites and edges
 */
public class Topology {
    /**
     * Returns a topology
     *
     * @param sites the site list
     * @param nodes the node list
     * @param edges the edge list
     */
    public static Topology create(List<SiteNode> sites,
                           List<MapNode> nodes,
                           List<MapEdge> edges) {
        // Sort nodes
        ArrayList<MapNode> sortedNode = new ArrayList<>(nodes);
        sortedNode.sort((a, b) -> {
            return a instanceof SiteNode
                    ? b instanceof SiteNode
                    ? 0 // SiteNode, SiteNode
                    : -1 // SiteNode, MapNode
                    : b instanceof SiteNode
                    ? 1 // MapNode, SiteNode
                    : 0; // MapNode, MapNode
        });
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
        assert sites != null;
        assert nodes != null;
        assert edges != null;
        assert entryEdgesByNode != null;
        this.sites = sites;
        this.nodes = nodes;
        this.edges = edges;
        this.entryEdgesByNode = entryEdgesByNode;
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
}
