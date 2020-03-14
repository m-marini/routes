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
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Planner of route computes the fastest route from a node source to a target
 * node and information about travel time.
 */
public class RoutePlanner {

	/** Returns an empty planner. */
	public static RoutePlanner create() {
		return new RoutePlanner(Collections.emptySet());
	}

	private final Set<EdgeTraffic> edgeTraffics;
	private final List<MapNode> nodes;
	private final int[][] connectionMatrix;
	private final double[][] timeMatrix;
	private final double[][] minTimeMatrix;

	/**
	 * Creates a planner for the edge traffic.
	 *
	 * @param edgeStats the edge traffic
	 */
	public RoutePlanner(final Set<EdgeTraffic> edgeStats) {
		this.edgeTraffics = edgeStats;
		this.nodes = createNodes();
		final int n = nodes.size();
		this.connectionMatrix = new int[n][n];
		this.timeMatrix = new double[n][n];
		this.minTimeMatrix = new double[n][n];
		createMatrices();
	}

	/**
	 * Creates the travel time matrix and the route matrix with Floyd-Warshall
	 * algorithm.
	 *
	 * @return the route planner
	 */
	private RoutePlanner createMatrices() {
		final int n = nodes.size();
		for (int i = 0; i < n; i++) {
			Arrays.fill(timeMatrix[i], Double.POSITIVE_INFINITY);
			Arrays.fill(minTimeMatrix[i], Double.POSITIVE_INFINITY);
			timeMatrix[i][i] = 0;
			minTimeMatrix[i][i] = 0;
		}
		for (int i = 0; i < n; i++) {
			Arrays.fill(connectionMatrix[i], -1);
			connectionMatrix[i][i] = i;
		}

		for (final EdgeTraffic s : edgeTraffics) {
			final int i = nodes.indexOf(s.getEdge().getBegin());
			final int j = nodes.indexOf(s.getEdge().getEnd());
			timeMatrix[i][j] = s.getTravelTime();
			minTimeMatrix[i][j] = s.getEdge().getTransitTime();
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
					final double mt = minTimeMatrix[i][k] + minTimeMatrix[k][j];
					if (mt < minTimeMatrix[i][j]) {
						minTimeMatrix[i][j] = mt;
					}
				}
			}
		}

		return this;
	}

	/** Returns the list of nodes. */
	List<MapNode> createNodes() {
		final Set<MapNode> begins = edgeTraffics.parallelStream().map(s -> s.getEdge().getBegin())
				.collect(Collectors.toSet());
		final Set<MapNode> ends = edgeTraffics.parallelStream().map(s -> s.getEdge().getEnd())
				.collect(Collectors.toSet());
		begins.addAll(ends);
		final List<MapNode> result = List.copyOf(begins);
		return result;
	}

	/** Returns the connection matrix. */
	int[][] getConnectionMatrix() {
		return connectionMatrix;
	}

	/** Returns the edge traffic. */
	public Set<EdgeTraffic> getEdgeTraffics() {
		return edgeTraffics;
	}

	/**
	 * Returns the minimum travel time between two nodes.
	 * <p>
	 * The time is computed considering only the limit speed of edges. Returns an
	 * empty value if no connection exists between the two nodes
	 * <p/>
	 *
	 * @param from the starting node
	 * @param to   the destination node
	 */
	public OptionalDouble getMinTime(final MapNode from, final MapNode to) {
		final int i = nodes.indexOf(from);
		final int j = nodes.indexOf(to);
		return i >= 0 && j >= 0 && i != j && minTimeMatrix[i][j] != Double.POSITIVE_INFINITY
				? OptionalDouble.of(minTimeMatrix[i][j])
				: OptionalDouble.empty();
	}

	/** Returns the list of nodes. */
	public List<MapNode> getNodes() {
		return nodes;
	}

	/**
	 * Returns the expected travel time between two nodes.
	 * <p>
	 * The time is computed considering the current traffics in the edge. Returns an
	 * empty value if no connection exists between the two nodes
	 * <p/>
	 *
	 * @param from the starting node
	 * @param to   the destination node
	 */
	public OptionalDouble getTime(final MapNode from, final MapNode to) {
		final int i = nodes.indexOf(from);
		final int j = nodes.indexOf(to);
		return i >= 0 && j >= 0 && i != j && timeMatrix[i][j] != Double.POSITIVE_INFINITY
				? OptionalDouble.of(timeMatrix[i][j])
				: OptionalDouble.empty();
	}

	/** Returns the tme matrix. */
	double[][] getTimeMatrix() {
		return timeMatrix;
	}

	/**
	 * Returns the next edge from a node to a given node.
	 * <p>
	 * Returns an empty edge if no connection exists between the two node
	 * </p>
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
	 * Returns the previous node in the route from a node to a given node.
	 * <p>
	 * Returns an empty node if no connection exists between the two node
	 * </p>
	 *
	 * @param from source node
	 * @param to   destination node
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
	 * Returns the planner with a given edge traffic.
	 *
	 * @param edgeStats the edge traffics
	 */
	public RoutePlanner setEdgeStats(final Set<EdgeTraffic> edgeStats) {
		return new RoutePlanner(edgeStats);
	}
}
