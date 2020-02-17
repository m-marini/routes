package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class TrafficStatsTest implements Constants {

	private static final double TIME_10M = 10.0 / DEFAULT_SPEED_LIMIT_KMH / KMH_TO_MPS;
	private static final double TIME_20M = TIME_10M * 2;
	private static final double TIME_30M = TIME_10M * 3;

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
	public void getMinTime11() {
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		final OptionalDouble t13 = s.getMinTime(node1, node1);
		assertFalse(t13.isPresent());
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
	public void getMinTime13() {
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		final OptionalDouble t13 = s.getMinTime(node1, node3);
		assertTrue(t13.isPresent());
		assertThat(t13.getAsDouble(), closeTo(30 / (DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS), 1e-3));
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
	public void getTime11() {
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		final OptionalDouble t13 = s.getTime(node1, node1);
		assertFalse(t13.isPresent());
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
	public void getTime13() {
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		final OptionalDouble t13 = s.getTime(node1, node3);
		assertTrue(t13.isPresent());
		assertThat(t13.getAsDouble(), closeTo(30 / (DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS), 1e-3));
	}

	@Test
	public void test() {
		final TrafficStats s = TrafficStats.create();
		assertNotNull(s);
		assertThat(s.getEdgeTraffics(), empty());
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertNotNull(s);
		assertThat(s.getEdgeTraffics(), equalTo(stats));
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertNotNull(s);
		assertThat(s.getEdgeTraffics(), equalTo(stats));
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
		final Set<MapNode> nodes = Set.of(node1, node2, node3, node4);
		final MapEdge edge12 = MapEdge.create(node1, node2);
		final MapEdge edge13 = MapEdge.create(node1, node3);
		final MapEdge edge21 = MapEdge.create(node2, node1);
		final MapEdge edge24 = MapEdge.create(node2, node4);
		final MapEdge edge31 = MapEdge.create(node3, node1);
		final MapEdge edge42 = MapEdge.create(node4, node2);
		final Set<MapEdge> edges = Set.of(edge12, edge13, edge21, edge24, edge31, edge42);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertNotNull(s);
		assertThat(s.getEdgeTraffics(), equalTo(stats));
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
	public void testNextEdge() {
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		final Optional<EdgeTraffic> n13 = s.nextEdge(node1, node3);
		assertTrue(n13.isPresent());
		assertThat(n13.get().getEdge(), equalTo(edge12));

		final Optional<EdgeTraffic> n14 = s.nextEdge(node1, node4);
		assertTrue(n14.isPresent());
		assertThat(n14.get().getEdge(), equalTo(edge12));

		final Optional<EdgeTraffic> n23 = s.nextEdge(node2, node3);
		assertTrue(n23.isPresent());
		assertThat(n23.get().getEdge(), equalTo(edge24));

		final Optional<EdgeTraffic> n31 = s.nextEdge(node3, node1);
		assertTrue(n31.isPresent());
		assertThat(n31.get().getEdge(), equalTo(edge34));

		final Optional<EdgeTraffic> n32 = s.nextEdge(node3, node2);
		assertTrue(n32.isPresent());
		assertThat(n32.get().getEdge(), equalTo(edge34));

		final Optional<EdgeTraffic> n41 = s.nextEdge(node4, node1);
		assertTrue(n41.isPresent());
		assertThat(n41.get().getEdge(), equalTo(edge42));
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
	public void testPrevNode() {
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
		final Set<MapEdge> edges = Set.of(edge12, edge21, edge24, edge34, edge42, edge43);

		final Set<EdgeTraffic> stats = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());

		final TrafficStats s = new TrafficStats(stats);

		assertThat(s.prevNode(node1, node3), equalTo(Optional.of(node4)));
		assertThat(s.prevNode(node1, node4), equalTo(Optional.of(node2)));
		assertThat(s.prevNode(node2, node3), equalTo(Optional.of(node4)));
		assertThat(s.prevNode(node3, node1), equalTo(Optional.of(node2)));
		assertThat(s.prevNode(node3, node2), equalTo(Optional.of(node4)));
		assertThat(s.prevNode(node4, node1), equalTo(Optional.of(node2)));
	}

	@Test
	public void testSetEdgeStats() {
		final Set<EdgeTraffic> edgeStats = Collections.emptySet();
		final TrafficStats s = TrafficStats.create().setEdgeStats(edgeStats);
		assertNotNull(s);
		assertThat(s.getEdgeTraffics(), equalTo(edgeStats));
	}
}
