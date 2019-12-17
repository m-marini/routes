package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mmarini.routes.model.Constants;

public class TrafficStatsTest implements Constants {

	private static final double TIME_10M = 10.0 / DEFAULT_SPEED_LIMIT_KMH / KMH_TO_MPS;
	private static final double TIME_20M = TIME_10M * 2;
	private static final double TIME_30M = TIME_10M * 3;

	@Test
	public void test() {
		final TrafficStats s = TrafficStats.create();
		assertThat(s, notNullValue());
		assertThat(s.getEdgeStats(), empty());
	}

	/**
	 * <pre>
	 * 1 <--> 2
	 *        ^
	 *        |
	 *        v
	 * 3 <--> 4
	 * </pre>
	 *
	 */
	@Test
	public void testCreateConnectionMatrix() {
		final MapNode node1 = MapNode.create(0, 0);
		final MapNode node2 = MapNode.create(10, 0);
		final MapNode node3 = MapNode.create(0, 10);
		final MapNode node4 = MapNode.create(10, 10);
		final MapEdge edge12 = MapEdge.create(node1, node2);
		final MapEdge edge21 = MapEdge.create(node2, node1);
		final MapEdge edge24 = MapEdge.create(node2, node4);
		final MapEdge edge34 = MapEdge.create(node3, node4);
		final MapEdge edge42 = MapEdge.create(node4, node2);
		final MapEdge edge43 = MapEdge.create(node4, node3);
		final Set<MapEdge> edges = new HashSet<>(Arrays.asList(edge12, edge21, edge24, edge34, edge42, edge43));

		final Set<EdgeStats> stats = edges.stream().map(EdgeStats::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertThat(s, notNullValue());
		assertThat(s.getEdgeStats(), equalTo(stats));
		final List<MapNode> nods = s.getNodes();
		final int n1 = nods.indexOf(node1);
		final int n2 = nods.indexOf(node2);
		final int n3 = nods.indexOf(node3);
		final int n4 = nods.indexOf(node4);
		final int[][] m = s.getConnectionMatrix();

		assertThat(m[n1][n1], equalTo(n1));
		assertThat(m[n1][n2], equalTo(n1));
		assertThat(m[n1][n3], equalTo(n4));
		assertThat(m[n1][n4], equalTo(n2));

		assertThat(m[n2][n1], equalTo(n2));
		assertThat(m[n2][n2], equalTo(n2));
		assertThat(m[n2][n3], equalTo(n4));
		assertThat(m[n2][n4], equalTo(n2));

		assertThat(m[n3][n1], equalTo(n2));
		assertThat(m[n3][n2], equalTo(n4));
		assertThat(m[n3][n3], equalTo(n3));
		assertThat(m[n3][n4], equalTo(n3));

		assertThat(m[n4][n1], equalTo(n2));
		assertThat(m[n4][n2], equalTo(n4));
		assertThat(m[n4][n3], equalTo(n4));
		assertThat(m[n4][n4], equalTo(n4));
	}

	/**
	 * <pre>
	 * 1 <--> 2
	 *        ^
	 *        |
	 *        v
	 * 3 <--> 4
	 * </pre>
	 *
	 */
	@Test
	public void testCreateTimeMatrices() {
		final MapNode node1 = MapNode.create(0, 0);
		final MapNode node2 = MapNode.create(10, 0);
		final MapNode node3 = MapNode.create(0, 10);
		final MapNode node4 = MapNode.create(10, 10);
		final MapEdge edge12 = MapEdge.create(node1, node2);
		final MapEdge edge21 = MapEdge.create(node2, node1);
		final MapEdge edge24 = MapEdge.create(node2, node4);
		final MapEdge edge34 = MapEdge.create(node3, node4);
		final MapEdge edge42 = MapEdge.create(node4, node2);
		final MapEdge edge43 = MapEdge.create(node4, node3);
		final Set<MapEdge> edges = new HashSet<>(Arrays.asList(edge12, edge21, edge24, edge34, edge42, edge43));

		final Set<EdgeStats> stats = edges.stream().map(EdgeStats::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertThat(s, notNullValue());
		assertThat(s.getEdgeStats(), equalTo(stats));
		final List<MapNode> nods = s.getNodes();
		final int n1 = nods.indexOf(node1);
		final int n2 = nods.indexOf(node2);
		final int n3 = nods.indexOf(node3);
		final int n4 = nods.indexOf(node4);
		final double[][] m = s.getTimeMatrix();

		assertThat(m[n1][n1], equalTo(0.0));
		assertThat(m[n1][n2], closeTo(TIME_10M, TIME_10M * 1e-3));
		assertThat(m[n1][n3], closeTo(TIME_30M, TIME_10M * 1e-3));
		assertThat(m[n1][n4], closeTo(TIME_20M, TIME_20M * 1e-3));

		assertThat(m[n2][n1], closeTo(TIME_10M, TIME_10M * 1e-3));
		assertThat(m[n2][n2], equalTo(0.0));
		assertThat(m[n2][n3], closeTo(TIME_20M, TIME_20M * 1e-3));
		assertThat(m[n2][n4], closeTo(TIME_10M, TIME_10M * 1e-3));

		assertThat(m[n3][n1], closeTo(TIME_30M, TIME_10M * 1e-3));
		assertThat(m[n3][n2], closeTo(TIME_20M, TIME_20M * 1e-3));
		assertThat(m[n3][n3], equalTo(0.0));
		assertThat(m[n3][n4], closeTo(TIME_10M, TIME_30M * 1e-3));

		assertThat(m[n4][n1], closeTo(TIME_20M, TIME_20M * 1e-3));
		assertThat(m[n4][n2], closeTo(TIME_10M, TIME_10M * 1e-3));
		assertThat(m[n4][n3], closeTo(TIME_10M, TIME_30M * 1e-3));
		assertThat(m[n4][n4], equalTo(0.0));
	}

	@Test
	public void testgCreateNodes() {
		final MapNode node1 = MapNode.create(0, 0);
		final MapNode node2 = MapNode.create(10, 0);
		final MapNode node3 = MapNode.create(0, 10);
		final MapNode node4 = MapNode.create(10, 10);
		final Set<MapNode> nodes = new HashSet<>(Arrays.asList(node1, node2, node3, node4));
		final MapEdge edge12 = MapEdge.create(node1, node2);
		final MapEdge edge13 = MapEdge.create(node1, node3);
		final MapEdge edge21 = MapEdge.create(node2, node1);
		final MapEdge edge24 = MapEdge.create(node2, node4);
		final MapEdge edge31 = MapEdge.create(node3, node1);
		final MapEdge edge42 = MapEdge.create(node4, node2);
		final Set<MapEdge> edges = new HashSet<>(Arrays.asList(edge12, edge13, edge21, edge24, edge31, edge42));

		final Set<EdgeStats> stats = edges.stream().map(EdgeStats::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertThat(s, notNullValue());
		assertThat(s.getEdgeStats(), equalTo(stats));
		assertThat(s.getNodes(), containsInAnyOrder(nodes.toArray()));
	}

	/**
	 * <pre>
	 * 1 <--> 2
	 *        ^
	 *        |
	 *        v
	 * 3 <--> 4
	 * </pre>
	 *
	 */
	@Test
	public void testNextNode() {
		final MapNode node1 = MapNode.create(0, 0);
		final MapNode node2 = MapNode.create(10, 0);
		final MapNode node3 = MapNode.create(0, 10);
		final MapNode node4 = MapNode.create(10, 10);
		final MapEdge edge12 = MapEdge.create(node1, node2);
		final MapEdge edge21 = MapEdge.create(node2, node1);
		final MapEdge edge24 = MapEdge.create(node2, node4);
		final MapEdge edge34 = MapEdge.create(node3, node4);
		final MapEdge edge42 = MapEdge.create(node4, node2);
		final MapEdge edge43 = MapEdge.create(node4, node3);
		final Set<MapEdge> edges = new HashSet<>(Arrays.asList(edge12, edge21, edge24, edge34, edge42, edge43));

		final Set<EdgeStats> stats = edges.stream().map(EdgeStats::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertThat(s.nextNode(node1, node3), equalTo(Optional.of(node4)));
		assertThat(s.nextNode(node1, node4), equalTo(Optional.of(node2)));
		assertThat(s.nextNode(node2, node3), equalTo(Optional.of(node4)));
		assertThat(s.nextNode(node3, node1), equalTo(Optional.of(node2)));
		assertThat(s.nextNode(node3, node2), equalTo(Optional.of(node4)));
		assertThat(s.nextNode(node4, node1), equalTo(Optional.of(node2)));
	}

	@Test
	public void testSetEdgeStats() {
		final Set<EdgeStats> edgeStats = new HashSet<>();
		final TrafficStats s = TrafficStats.create().setEdgeStats(edgeStats);
		assertThat(s, notNullValue());
		assertThat(s.getEdgeStats(), equalTo(edgeStats));
	}
}
