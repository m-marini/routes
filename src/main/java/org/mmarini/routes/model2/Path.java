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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.mmarini.Utils.toMap;
import static org.mmarini.Utils.zipWithIndex;

/**
 *
 */
public class Path {
    static Map<Path, MapEdge> create(List<MapNode> nodes, List<MapEdge> edges, Map<MapEdge, Double> edgeTravelTimes) {
        Map<MapNode, Integer> indexByNode = zipWithIndex(nodes)
                .map(entry -> entry(entry.getValue(), entry.getKey()))
                .collect(toMap());
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
        /*
        zipWithIndex(edges).forEach(entry -> {
            int idx = entry.getKey();
            MapEdge edge = entry.getValue();
            int i = indexByNode.get(edge.getBegin());
            int j = indexByNode.get(edge.getEnd());
            travelMatrix[i][j] = edgeTravelTimes.get(edge);
            previousMatrix[i][j] = i;
            edgeMatrix[i][j] = edge;
        });
         */

        // Computes the optimal paths
        int[][] nextMatrix = nextMatrix(floydWarshall(previousMatrix, travelMatrix));

        // Convert to path map
        return createPathMap(nodes, edgeMatrix, nextMatrix);
    }

    static Map<Path, MapEdge> createPathMap(List<MapNode> nodes, MapEdge[][] edgeMatrix, int[][] nextMatrix) {
        int n = nodes.size();
        Stream.Builder<Entry<Path, MapEdge>> builder = Stream.builder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                MapNode to = nodes.get(j);
                int next = nextMatrix[i][j];
                if (next >= 0 && to instanceof SiteNode) {
                    MapNode from = nodes.get(i);
                    builder.add(entry(new Path(from, to), edgeMatrix[i][next]));
                }
            }
        }
        return builder.build().collect(toMap());
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

    public static int[][] nextMatrix(int[][] previousMatrix) {
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

    private final MapNode from;
    private final MapNode to;

    Path(MapNode from, MapNode to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return from.equals(path.from) && to.equals(path.to);
    }

    public MapNode getFrom() {
        return from;
    }

    public MapNode getTo() {
        return to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
