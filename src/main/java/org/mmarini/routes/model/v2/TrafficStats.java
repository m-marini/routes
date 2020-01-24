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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TrafficStats {
	private static final Logger logger = LoggerFactory.getLogger(TrafficStats.class);

	/**
	 *
	 * @return
	 */
	public static TrafficStats create() {
		return new TrafficStats(Collections.emptySet());
	}

	private final Set<EdgeTraffic> edgeTraffics;
	private final List<MapNode> nodes;
	private final int[][] connectionMatrix;
	private final double[][] timeMatrix;

	/**
	 *
	 * @param edgeStats
	 */
	public TrafficStats(final Set<EdgeTraffic> edgeStats) {
		this.edgeTraffics = edgeStats;
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

		for (final EdgeTraffic s : edgeTraffics) {
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
		final Set<MapNode> begins = edgeTraffics.parallelStream().map(s -> s.getEdge().getBegin())
				.collect(Collectors.toSet());
		final Set<MapNode> ends = edgeTraffics.parallelStream().map(s -> s.getEdge().getEnd())
				.collect(Collectors.toSet());
		begins.addAll(ends);
		final List<MapNode> result = List.copyOf(begins);
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
	public Set<EdgeTraffic> getEdgeTraffics() {
		return edgeTraffics;
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
	 * Returns the next edge from a node to a given node
	 *
	 * @param from from node
	 * @param to   to node
	 */
	public Optional<EdgeTraffic> nextEdge(final MapNode from, final MapNode to) {
		MapNode end = to;
		Optional<MapNode> prev = Optional.empty();
		for (;;) {
			prev = prevNode(from, end);
			if (prev.isEmpty()) {
				// No path
				return Optional.empty();
			}
			final MapNode pe = prev.get();
			if (pe.equals(from)) {
				break;
			}
			end = pe;
		}
		// Found from -> end
		final MapNode finalEnd = end;
		final Optional<EdgeTraffic> result = edgeTraffics.stream()
				.filter(et -> et.getEdge().getBegin().equals(from) && et.getEdge().getEnd().equals(finalEnd))
				.findFirst();
		return result;
	}

	/**
	 *
	 * @param node2
	 * @param node4
	 * @return
	 */
	public Optional<MapNode> prevNode(final MapNode from, final MapNode to) {
		final int i = nodes.indexOf(from);
		final int j = nodes.indexOf(to);
		if (i >= 0 && j >= 0) {
			final int k = connectionMatrix[i][j];
			final Optional<MapNode> result = k >= 0 ? Optional.of(nodes.get(k)) : Optional.empty();
			return result;
		} else {
			return Optional.empty();
		}
	}

	/**
	 *
	 * @param edgeStats
	 * @return
	 */
	public TrafficStats setEdgeStats(final Set<EdgeTraffic> edgeStats) {
		return new TrafficStats(edgeStats);
	}
}
