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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TopologyTest {

    private static final double SPEED_LIMIT = 10;
    private static final int LOW_PRIORITY = 0;
    private static final int HIGH_PRIORITY = 1;

    @Test
    void create() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 ---> 2
          <---   <---
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        List<SiteNode> sites = List.of(node0, node2);
        List<MapNode> nodes = List.of(node0, node1, node2);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        List<MapEdge> edges = List.of(edge01, edge10, edge12, edge21);

        /*
        When creating the topology
         */
        Topology result = Topology.create(sites, nodes, edges);

        // Then should returns a topology
        assertNotNull(result);
        // And site list
        assertThat(result.getSites(), containsInAnyOrder(node0, node2));
        // And node list
        assertThat(result.getNodes(), contains(node0, node2, node1));
        // And edge list
        assertThat(result.getEdges(), containsInAnyOrder(edge21, edge01, edge10, edge12));
        // And incoming edge for each node
        assertThat(result.getIncomeEdges(node0), contains(edge10));
        assertThat(result.getIncomeEdges(node1), contains(edge01, edge21));
        assertThat(result.getIncomeEdges(node2), contains(edge12));
    }

    @Test
    void getIncomeEdges() {
        /*
        Given sites, nodes, edges of the topology
        0 --1--> 1 <--0-- 2
        3 --0-->
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        SiteNode node3 = SiteNode.createSite(0, 100);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge31 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = Topology.create(
                List.of(node0, node2, node3),
                List.of(node0, node1, node2, node3),
                List.of(edge01, edge21, edge31));

        /*
        When getting incoming edges
         */
        List<MapEdge> list01 = topology.getIncomeEdges(edge01);
        List<MapEdge> list21 = topology.getIncomeEdges(edge21);
        List<MapEdge> list31 = topology.getIncomeEdges(edge31);

        // Then should returns a list
        assertNotNull(list01);
        assertThat(list01, empty());
        assertNotNull(list21);
        assertThat(list21, contains(edge01));
        assertNotNull(list31);
        assertThat(list31, contains(edge01));
    }
}