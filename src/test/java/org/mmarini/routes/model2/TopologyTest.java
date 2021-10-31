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

import java.awt.geom.Point2D;
import java.util.List;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.Topology.createTopology;

class TopologyTest {

    static final double SPEED_LIMIT = 10;
    static final int LOW_PRIORITY = 0;
    static final int HIGH_PRIORITY = 1;

    @Test
    void addEdgeWithNewNodes() {
        /*
        Given sites, nodes, edges of the topology
        0 --1--> 1 <--0-- 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2),
                List.of(edge01, edge21));
        /*
        And an edge with 2 new nodes
         */
        CrossNode node3 = createNode(50, 10);
        CrossNode node4 = createNode(50, 14);
        MapEdge edge32 = new MapEdge(node3, node4, SPEED_LIMIT, LOW_PRIORITY);

        /*
        When adding an edge
         */
        Topology result = topology.addEdge(edge32);

        // Then should return the new edge
        assertThat(result.getSites(), equalTo(topology.getSites()));
        assertThat(result.getNodes(), containsInAnyOrder(
                node0, node1, node2, node3, node4
        ));
        assertThat(result.getEdges(), containsInAnyOrder(
                edge01, edge21, edge32
        ));
    }

    @Test
    void addEdgeWithoutNewNode() {
        /*
        Given sites, nodes, edges of the topology
        0 --1--> 1 <--0-- 2
        3 --0-->
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(0, 100);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge31 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2, node3),
                List.of(edge01, edge21, edge31));
        MapEdge edge32 = new MapEdge(node3, node2, SPEED_LIMIT, LOW_PRIORITY);

        /*
        When adding an edge
         */
        Topology result = topology.addEdge(edge32);

        // Then should return the new edge
        assertThat(result.getSites(), equalTo(topology.getSites()));
        assertThat(result.getNodes(), equalTo(topology.getNodes()));
        assertThat(result.getEdges(), containsInAnyOrder(
                edge01, edge21, edge31, edge32
        ));
    }

    @Test
    void addModule() {
                /*
        Given a topology of
        0 ---> 1 ---> 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, HIGH_PRIORITY);
        /*
        And the corresponding status
         */
        Topology topology = createTopology(
                List.of(node0, node2, node1),
                List.of(edge01, edge12)
        );
        /*
        And a module
            2
            |
            |
            v
            0 ---> 1
         */
        CrossNode moduleNode0 = createNode(0, 0);
        CrossNode moduleNode1 = createNode(10, 0);
        CrossNode moduleNode2 = createNode(0, 10);
        MapEdge moduleEdge01 = new MapEdge(moduleNode0, moduleNode1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge moduleEdge20 = new MapEdge(moduleNode2, moduleNode0, SPEED_LIMIT, LOW_PRIORITY);
        Module module = new Module(List.of(moduleEdge01, moduleEdge20));

        /*
        When adding the module
         */
        Topology result = topology.addModule(module,
                new Point2D.Double(51, 0),
                new Point2D.Double(1, 1),
                2);

        /*
        Than the topology should be
           4       3
            \     ^
             \   /
              v /
        0 ---> 1 ---> 2
         */
        assertNotNull(result);
        assertThat(result.getSites(), contains(node0, node2));
        CrossNode node3 = createNode(51 + 10 * sqrt(0.5), 10 * sqrt(0.5));
        CrossNode node4 = createNode(51 - 10 * sqrt(0.5), 10 * sqrt(0.5));
        MapEdge edge13 = new MapEdge(node1, node3, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge41 = new MapEdge(node4, node1, SPEED_LIMIT, LOW_PRIORITY);
        assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3, node4));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge12, edge13, edge41));
    }

    @Test
    void changeMapNode() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 <--- 2
        3 --->
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(0, 10);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge31 = new MapEdge(node3, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2, node3),
                List.of(edge01, edge21, edge31));

        /*
        When changing a node
         */
        Topology result = topology.changeNode(node1);

        /*
        Then should return
        0 ---> 4 <--- 2
        3 --->
         */
        SiteNode node4 = createSite(50, 0);
        MapEdge edge04 = new MapEdge(node0, node4, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge24 = new MapEdge(node2, node4, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge34 = new MapEdge(node3, node4, SPEED_LIMIT, LOW_PRIORITY);
        assertNotNull(result);
        assertThat(result.getSites(), containsInAnyOrder(node0, node2, node4, node3));
        assertThat(result.getNodes(), containsInAnyOrder(
                node0, node2, node3, node4
        ));
        assertThat(result.getEdges(), containsInAnyOrder(edge04, edge24, edge34));
    }

    @Test
    void changeSiteNode() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 <--- 2
        3 --->
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(0, 10);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge31 = new MapEdge(node3, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2, node3),
                List.of(edge01, edge21, edge31));

        /*
        When changing a node
         */
        Topology result = topology.changeNode(node3);

        /*
        Then should return
        0 ---> 1 <--- 2
        4 --->
         */
        CrossNode node4 = createNode(0, 10);
        MapEdge edge41 = new MapEdge(node4, node1, SPEED_LIMIT, LOW_PRIORITY);
        assertNotNull(result);
        assertThat(result.getSites(), containsInAnyOrder(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(
                node0, node2, node1, node4
        ));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge21, edge41));
    }

    @Test
    void create() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 ---> 2
          <---   <---
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        List<MapNode> nodes = List.of(node0, node1, node2);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        List<MapEdge> edges = List.of(edge01, edge10, edge12, edge21);

        /*
        When creating the topology
         */
        Topology result = createTopology(nodes, edges);

        // Then should return a topology
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
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(0, 100);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge31 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2, node3),
                List.of(edge01, edge21, edge31));

        /*
        When getting incoming edges
         */
        List<MapEdge> list01 = topology.getIncomeEdges(edge01);
        List<MapEdge> list21 = topology.getIncomeEdges(edge21);
        List<MapEdge> list31 = topology.getIncomeEdges(edge31);

        // Then should return a list
        assertNotNull(list01);
        assertThat(list01, empty());
        assertNotNull(list21);
        assertThat(list21, contains(edge01));
        assertNotNull(list31);
        assertThat(list31, contains(edge01));
    }

    @Test
    void removeEdge() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 <--- 2
          <---   --->
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2),
                List.of(edge01, edge10, edge12, edge21));

        /*
        When removing an edge
         */
        Topology result = topology.removeEdge(edge10);

        // Then should return the new edge
        assertThat(result.getSites(), equalTo(topology.getSites()));
        assertThat(result.getNodes(), equalTo(topology.getNodes()));
        assertThat(result.getEdges(), containsInAnyOrder(
                edge01, edge12, edge21
        ));
    }

    @Test
    void removeMapNode() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 <--- 2
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2),
                List.of(edge01, edge21));

        /*
        When removing a node
         */
        Topology result = topology.removeNode(node1);

        // Then should return the new edge
        assertNotNull(result);
        assertThat(result.getSites(), equalTo(topology.getSites()));
        assertThat(result.getNodes(), containsInAnyOrder(
                node0, node2
        ));
        assertThat(result.getEdges(), empty());
    }

    @Test
    void removeSite() {
        /*
        Given sites, nodes, edges of the topology
        0 ---> 1 <--- 2
        3 --->
         */
        SiteNode node0 = createSite(0, 0);
        SiteNode node2 = createSite(100, 0);
        SiteNode node3 = createSite(0, 10);
        CrossNode node1 = createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, HIGH_PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, LOW_PRIORITY);
        MapEdge edge31 = new MapEdge(node3, node1, SPEED_LIMIT, LOW_PRIORITY);
        Topology topology = createTopology(
                List.of(node0, node1, node2, node3),
                List.of(edge01, edge21, edge31));

        /*
        When removing a node
         */
        Topology result = topology.removeNode(node3);

        // Then should return the new edge
        assertNotNull(result);
        assertThat(result.getSites(), containsInAnyOrder(node0, node2));
        assertThat(result.getNodes(), containsInAnyOrder(
                node0, node2, node1
        ));
        assertThat(result.getEdges(), containsInAnyOrder(edge01, edge21));
    }
}