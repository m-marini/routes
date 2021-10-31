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
import org.mmarini.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.Routes.createRoutes;
import static org.mmarini.routes.model2.SiteNode.createSite;

class RoutesTest {

    static final double EDGE01_TIME = 2.0;
    static final double EDGE02_TIME = 6.0;
    static final double EDGE12_TIME = 3.0;
    static final double EDGE20_TIME = 4.0;

    @Test
    void createMap() {
        /*
        Given the topology
        v--------- 4 ---------
        0 -- 2 --> 1 -- 3 --> 2
         --------- 6 ---------^
         */
        SiteNode node0 = createSite(0, 0);
        CrossNode node1 = createNode(10, 0);
        SiteNode node2 = createSite(20, 0);
        MapEdge edge01 = new MapEdge(node0, node1, 10, 0);
        MapEdge edge02 = new MapEdge(node0, node2, 10, 0);
        MapEdge edge12 = new MapEdge(node1, node2, 10, 0);
        MapEdge edge20 = new MapEdge(node2, node0, 10, 0);
        List<MapEdge> edges = List.of(edge01, edge02, edge12, edge20);
        Map<MapEdge, Double> times = Map.of(edge01, EDGE01_TIME, edge02, EDGE02_TIME, edge12, EDGE12_TIME, edge20, EDGE20_TIME);

        /*
        When generating the next node closure matrix
         */
        Map<Tuple2<MapNode, MapNode>, MapEdge> result = Routes.computeRoutes(edges, times);

        /*
         * Then should result the next node closure matrix
         */
        assertNotNull(result);
        assertThat(result.size(), equalTo(6));
        assertThat(result.get(Tuple2.of(node0, node0)), equalTo(edge01));
        assertThat(result.get(Tuple2.of(node0, node2)), equalTo(edge01));

        assertThat(result.get(Tuple2.of(node1, node0)), equalTo(edge12));
        assertThat(result.get(Tuple2.of(node1, node2)), equalTo(edge12));

        assertThat(result.get(Tuple2.of(node2, node0)), equalTo(edge20));
        assertThat(result.get(Tuple2.of(node2, node2)), equalTo(edge20));
    }

    @Test
    void createPathMapDisjoint() {
        /*
        Given the topology
        v--- 4 ---
        0 -- 2 --> 1 -- 3 --> 2
         --------- 6 ---------^
         And the corresponding previous node closure matrix
         */
        SiteNode node0 = createSite(0, 0);
        CrossNode node1 = createNode(10, 0);
        SiteNode node2 = createSite(20, 0);
        List<MapNode> nodes = List.of(node0, node1, node2);
        MapEdge edge01 = new MapEdge(node0, node1, 10, 0);
        MapEdge edge02 = new MapEdge(node0, node2, 10, 0);
        MapEdge edge12 = new MapEdge(node1, node2, 10, 0);
        MapEdge edge10 = new MapEdge(node1, node0, 10, 0);
        MapEdge[][] edgeMatrix = new MapEdge[][]{
                {null, edge01, edge02},
                {edge10, null, edge12},
                {null, null, null}
        };
        int[][] nextMatrix = new int[][]{
                {1, 1, 1},
                {0, 0, 2},
                {-1, -1, -1},
        };

        /*
        When generating the next node closure matrix
         */
        Map<Tuple2<MapNode, MapNode>, MapEdge> result = createRoutes(nodes, edgeMatrix, nextMatrix);

        /*
         * Then should result the next node closure matrix
         */
        assertThat(result.size(), equalTo(4));
        assertThat(result.get(Tuple2.of(node0, node0)), equalTo(edge01));
        assertThat(result.get(Tuple2.of(node0, node2)), equalTo(edge01));

        assertThat(result.get(Tuple2.of(node1, node0)), equalTo(edge10));
        assertThat(result.get(Tuple2.of(node1, node2)), equalTo(edge12));
    }

    @Test
    void createPathMapTest() {
        /*
        Given the topology
        v--------- 4 ---------
        0 -- 2 --> 1 -- 3 --> 2
         --------- 6 ---------^
         And the corresponding previous node closure matrix
         */
        SiteNode node0 = createSite(0, 0);
        CrossNode node1 = createNode(10, 0);
        SiteNode node2 = createSite(20, 0);
        List<MapNode> nodes = List.of(node0, node1, node2);
        MapEdge edge01 = new MapEdge(node0, node1, 10, 0);
        MapEdge edge02 = new MapEdge(node0, node2, 10, 0);
        MapEdge edge12 = new MapEdge(node1, node2, 10, 0);
        MapEdge edge20 = new MapEdge(node2, node0, 10, 0);
        MapEdge[][] edgeMatrix = new MapEdge[][]{
                {null, edge01, edge02},
                {null, null, edge12},
                {edge20, null, null}
        };
        int[][] nextMatrix = new int[][]{
                {1, 1, 1},
                {2, 2, 2},
                {0, 0, 0},
        };

        /*
        When generating the next node closure matrix
         */
        Map<Tuple2<MapNode, MapNode>, MapEdge> result = createRoutes(nodes, edgeMatrix, nextMatrix);

        /*
         * Then should result the next node closure matrix
         */
        assertThat(result.size(), equalTo(6));
        assertThat(result.get(Tuple2.of(node0, node0)), equalTo(edge01));
        assertThat(result.get(Tuple2.of(node0, node2)), equalTo(edge01));

        assertThat(result.get(Tuple2.of(node1, node0)), equalTo(edge12));
        assertThat(result.get(Tuple2.of(node1, node2)), equalTo(edge12));

        assertThat(result.get(Tuple2.of(node2, node0)), equalTo(edge20));
        assertThat(result.get(Tuple2.of(node2, node2)), equalTo(edge20));
    }

    @Test
    void floydWarshall() {
        /*
        Given an adjacent matrix corresponding to the graph
        v--------- 4 ---------
        0 -- 2 --> 1 -- 3 --> 2
         --------- 6 ---------^
         */
        double[][] timeMatrix = new double[3][3];
        for (double[] time : timeMatrix) {
            Arrays.fill(time, Double.POSITIVE_INFINITY);
        }
        int[][] previousMatrix = new int[3][3];
        for (int[] prev : previousMatrix) {
            Arrays.fill(prev, -1);
        }
        timeMatrix[0][1] = 2;
        previousMatrix[0][1] = 0;
        timeMatrix[0][2] = 6;
        previousMatrix[0][2] = 0;
        timeMatrix[1][2] = 3;
        previousMatrix[1][2] = 1;
        timeMatrix[2][0] = 4;
        previousMatrix[2][0] = 2;

        /*
        When applying floyd warshall algorithm
         */
        int[][] result = Routes.floydWarshall(previousMatrix, timeMatrix);

        /*
         * Then should result the right connection matrix
         */
        assertThat(result, equalTo(new int[][]{
                {2, 0, 1},
                {2, 0, 1},
                {2, 0, 1},
        }));
    }

    @Test
    void nextMatrix() {
        /*
        Given the previous node closure matrix corresponding to the graph
        v--------- 4 ---------
        0 -- 2 --> 1 -- 3 --> 2
         --------- 6 ---------^
         */
        int[][] previousMatrix = new int[][]{
                {2, 0, 1},
                {2, 0, 1},
                {2, 0, 1},
        };

        /*
        When generating the next node closure matrix
         */
        int[][] result = Routes.nextMatrix(previousMatrix);

        /*
         * Then should result the next node closure matrix
         */
        assertThat(result, equalTo(new int[][]{
                {1, 1, 1},
                {2, 2, 2},
                {0, 0, 0},
        }));
    }

    @Test
    void nextMatrixDisjoint() {
        /*
        Given the previous node closure matrix corresponding to the graph
        v-- 4 -----
        0 -- 2 --> 1 -- 3 --> 2
         --------- 6 ---------^
         */
        int[][] previousMatrix = new int[][]{
                {1, 0, 1},
                {1, 0, 1},
                {-1, -1, -1},
        };

        /*
        When generating the next node closure matrix
         */
        int[][] result = Routes.nextMatrix(previousMatrix);

        /*
         * Then should result the next node closure matrix
         */
        assertThat(result, equalTo(new int[][]{
                {1, 1, 1},
                {0, 0, 2},
                {-1, -1, -1},
        }));
    }
}