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

import org.mmarini.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mmarini.Tuple2.swap;
import static org.mmarini.Utils.zipWithIndex;

/**
 *
 */
public interface Routes {

    static Map<Tuple2<MapNode, MapNode>, MapEdge> computeRoutes(List<MapEdge> edges, Map<MapEdge, Double> edgeTravelTimes) {
        List<MapNode> nodes = edges.stream()
                .flatMap(edge -> Stream.of(edge.getBegin(), edge.getEnd()))
                .distinct()
                .collect(Collectors.toList());

        Map<MapNode, Integer> indexByNode = zipWithIndex(nodes)
                .map(swap())
                .collect(Tuple2.toMap());
        int n = nodes.size();
        // Create initial adjacent matrices
        int[][] previousMatrix = new int[n][n];
        for (int[] matrix : previousMatrix) {
            Arrays.fill(matrix, -1);
        }
        double[][] travelMatrix = new double[n][n];
        for (double[] matrix : travelMatrix) {
            Arrays.fill(matrix, Double.POSITIVE_INFINITY);
        }
        MapEdge[][] edgeMatrix = new MapEdge[n][n];
        edges.forEach(edge -> {
            int i = indexByNode.get(edge.getBegin());
            int j = indexByNode.get(edge.getEnd());
            travelMatrix[i][j] = edgeTravelTimes.get(edge);
            previousMatrix[i][j] = i;
            edgeMatrix[i][j] = edge;
        });

        // Computes the optimal paths
        int[][] nextMatrix = nextMatrix(floydWarshall(previousMatrix, travelMatrix));

        // Convert to path map
        return createRoutes(nodes, edgeMatrix, nextMatrix);
    }

    static Map<Tuple2<MapNode, MapNode>, MapEdge> createRoutes(List<MapNode> nodes, MapEdge[][] edgeMatrix, int[][] nextMatrix) {
        int n = nodes.size();
        Stream.Builder<Tuple2<Tuple2<MapNode, MapNode>, MapEdge>> builder = Stream.builder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                MapNode to = nodes.get(j);
                int next = nextMatrix[i][j];
                if (next >= 0 && to instanceof SiteNode) {
                    MapNode from = nodes.get(i);
                    builder.add(Tuple2.of(Tuple2.of(from, to), edgeMatrix[i][next]));
                }
            }
        }
        return builder.build().collect(Tuple2.toMap());
    }

    /**
     * Returns the previous node closure matrix
     *
     * @param previousNode the previous node adjacent matrix
     * @param travelTime   the adjacent matrix travel time
     */
    static int[][] floydWarshall(int[][] previousNode, double[][] travelTime) {
        int n = previousNode.length;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (travelTime[i][j] > travelTime[i][k] + travelTime[k][j]) {
                        travelTime[i][j] = travelTime[i][k] + travelTime[k][j];
                        previousNode[i][j] = previousNode[k][j];
                    }
                }
            }
        }
        return previousNode;
    }

    static int[][] nextMatrix(int[][] previousMatrix) {
        int n = previousMatrix.length;
        int[][] nextMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int k = j;
                for (; ; ) {
                    int prev = previousMatrix[i][k];
                    if (prev < 0) {
                        nextMatrix[i][j] = -1;
                        break;
                    } else if (prev == i) {
                        nextMatrix[i][j] = k;
                        break;
                    } else {
                        k = prev;
                    }
                }
            }
        }
        return nextMatrix;
    }
}
