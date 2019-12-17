//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.model.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class TrafficStats {
//	private static final Logger logger = LoggerFactory.getLogger(TrafficStats.class);

	/**
	 *
	 * @return
	 */
	public static TrafficStats create() {
		return new TrafficStats(Collections.emptySet());
	}

	private final Set<EdgeStats> edgeStats;
	private final List<MapNode> nodes;
	private final int[][] connectionMatrix;

	private final double[][] timeMatrix;

	/**
	 *
	 * @param edgeStats
	 */
	public TrafficStats(final Set<EdgeStats> edgeStats) {
		this.edgeStats = edgeStats;
		this.nodes = createNodes();
		final int n = nodes.size();
		this.connectionMatrix = new int[n][n];
		this.timeMatrix = new double[n][n];
		createMatrices();
	}

	/**
	 *
	 * @return
	 */
	private TrafficStats createMatrices() {
		final int n = nodes.size();
		for (int i = 0; i < n; i++) {
			Arrays.fill(timeMatrix[i], Double.POSITIVE_INFINITY);
			timeMatrix[i][i] = 0;
		}
		for (int i = 0; i < n; i++) {
			Arrays.fill(connectionMatrix[i], -1);
			connectionMatrix[i][i] = i;
		}

		for (final EdgeStats s : edgeStats) {
			final int i = nodes.indexOf(s.getEdge().getBegin());
			final int j = nodes.indexOf(s.getEdge().getEnd());
			timeMatrix[i][j] = s.getTravelTime();
			connectionMatrix[i][j] = i;
		}

		/*
		 * Floyd algorithm
		 */
		for (int k = 0; k < n; ++k) {
			for (int i = 0; i < n; ++i) {
				for (int j = 0; j < n; ++j) {
					final double t = timeMatrix[i][k] + timeMatrix[k][j];
					if (t < timeMatrix[i][j]) {
						timeMatrix[i][j] = t;
						connectionMatrix[i][j] = connectionMatrix[k][j];
					}
				}
			}
		}

		return this;
	}

	/**
	 *
	 * @return
	 */
	List<MapNode> createNodes() {
		final Set<MapNode> begins = edgeStats.parallelStream().map(s -> s.getEdge().getBegin())
				.collect(Collectors.toSet());
		final Set<MapNode> ends = edgeStats.parallelStream().map(s -> s.getEdge().getEnd()).collect(Collectors.toSet());
		begins.addAll(ends);
		final List<MapNode> result = new ArrayList<>(begins);
		return result;
	}

	/**
	 *
	 * @return
	 */
	int[][] getConnectionMatrix() {
		return connectionMatrix;
	}

	/**
	 *
	 * @return
	 */
	public Set<EdgeStats> getEdgeStats() {
		return edgeStats;
	}

	/**
	 *
	 * @return
	 */
	public List<MapNode> getNodes() {
		return nodes;
	}

	/**
	 *
	 * @return
	 */
	double[][] getTimeMatrix() {
		return timeMatrix;
	}

	/**
	 *
	 * @param node2
	 * @param node4
	 * @return
	 */
	public Optional<MapNode> nextNode(final MapNode from, final MapNode to) {
		final int i = nodes.indexOf(from);
		final int j = nodes.indexOf(to);
		final int k = connectionMatrix[i][j];
		final Optional<MapNode> result = k >= 0 ? Optional.of(nodes.get(k)) : Optional.empty();
		return result;
	}

	/**
	 *
	 * @param edgeStats
	 * @return
	 */
	public TrafficStats setEdgeStats(final Set<EdgeStats> edgeStats) {
		return new TrafficStats(edgeStats);
	}
}
