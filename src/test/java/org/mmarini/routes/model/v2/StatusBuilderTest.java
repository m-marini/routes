package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatusBuilderTest {

	private SiteNode n1;
	private SiteNode n2;
	private SiteNode n3;
	private MapNode n4;
	private MapEdge e14;
	private MapEdge e34;
	private MapEdge e42;
	private Vehicle v1;
	private Vehicle v2;
	private EdgeTraffic t14;
	private EdgeTraffic t34;
	private EdgeTraffic t42;
	private Set<EdgeTraffic> traffics;
	private StatusBuilder builder;

	/**
	 * <pre>
	 * n1 --v1-> n4 --> n2
	 *           ^
	 *           |
	 *           v2
	 *           |
	 *           |
	 *           n3
	 * </pre>
	 */
	@BeforeEach
	public void createCase() {
		n1 = SiteNode.create(0, 0);
		n2 = SiteNode.create(1000, 0);
		n3 = SiteNode.create(500, 500);
		n4 = MapNode.create(500, 0);
		e14 = MapEdge.create(n1, n4).setSpeedLimit(10);
		e34 = MapEdge.create(n3, n4).setSpeedLimit(10);
		e42 = MapEdge.create(n4, n2).setSpeedLimit(10);
		v1 = Vehicle.create(n1, n2).setLocation(500);
		v2 = Vehicle.create(n3, n2).setLocation(500);
		final List<Vehicle> v14 = List.of(v1);
		final List<Vehicle> v34 = List.of(v2);
		t14 = EdgeTraffic.create(e14).setTime(49.5).setVehicles(v14);
		t34 = EdgeTraffic.create(e34).setTime(50.0).setVehicles(v34);
		t42 = EdgeTraffic.create(e42).setTime(55);
		traffics = Set.of(t14, t34, t42);
		builder = StatusBuilder.create().setTraffics(traffics);
	}

	@Test
	public void testBuildTrafficStats() {
		final TrafficStats result = builder.buildTrafficStats();
		assertNotNull(result);

		final Optional<MapNode> n = result.prevNode(n1, n2);
		assertTrue(n.isPresent());
		assertThat(n.get(), equalTo(n4));
	}

	@Test
	public void testCreate() {
		final StatusBuilder result = StatusBuilder.create();
		assertThat(result, notNullValue());
		assertThat(result.getTraffics(), empty());
	}

	@Test
	public void testMoveVehicle() {
		final StatusBuilder result = builder.moveVehicle(t34, t42);
		assertNotNull(result);

		final Optional<EdgeTraffic> newt42 = result.getTraffics().stream().filter(t42::equals).findFirst();
		assertTrue(newt42.isPresent());
		assertThat(newt42.get().getVehicles(), hasSize(1));
		assertThat(newt42.get().getVehicles(), hasItem(v2));
		final Vehicle newv2 = newt42.get().getVehicles().get(0);
		assertThat(newv2.getEdgeEntryTime(), equalTo(t34.getTime()));
		assertThat(newv2.getLocation(), equalTo(50.0));

		final Optional<EdgeTraffic> newt34 = result.getTraffics().stream().filter(t34::equals).findFirst();
		assertTrue(newt34.isPresent());
		assertThat(newt34.get().getVehicles(), empty());
		assertFalse(newt34.get().getLastTravelTime().isPresent());

		final Optional<EdgeTraffic> newt14 = result.getTraffics().stream().filter(t14::equals).findFirst();
		assertTrue(newt14.isPresent());
		assertThat(newt14.get(), sameInstance(t14));
	}

	@Test
	public void testMoveVehicleAtCross() {
		final List<EdgeTraffic> crossingEdges = Stream.of(t14, t34)
				.sorted((a, b) -> Double.compare(a.getTime(), b.getTime())).collect(Collectors.toList());
		final StatusBuilder result = builder.moveVehicleAtCross(crossingEdges);
		assertNotNull(result);

		// outcoming edge
		final Optional<EdgeTraffic> newt42 = result.getTraffics().stream().filter(t42::equals).findFirst();
		assertTrue(newt42.isPresent());
		assertThat(newt42.get().getVehicles(), hasSize(1));
		assertThat(newt42.get().getVehicles(), hasItem(v2));
		final Vehicle newv2 = newt42.get().getVehicles().get(0);
		assertThat(newv2.getEdgeEntryTime(), equalTo(t34.getTime()));
		assertThat(newv2.getLocation(), equalTo(50.0));

		// incoming edge
		final Optional<EdgeTraffic> newt34 = result.getTraffics().stream().filter(t34::equals).findFirst();
		assertTrue(newt34.isPresent());
		assertThat(newt34.get().getVehicles(), empty());
		assertFalse(newt34.get().getLastTravelTime().isPresent());

		// idle edge
		final Optional<EdgeTraffic> newt14 = result.getTraffics().stream().filter(t14::equals).findFirst();
		assertTrue(newt14.isPresent());
		assertThat(newt14.get(), equalTo(t14));
		assertThat(newt14.get().getTime(), equalTo(t34.getTime()));
	}

	/**
	 * <pre>
	 *  -- e1 ->O
	 *          ^
	 *          |
	 *          e2
	 *          |
	 * </pre>
	 */
	@Test
	public void testPriorityNoConflict() {
		final SiteNode node1 = SiteNode.create(0, 0);
		final SiteNode node2 = SiteNode.create(100, 0);
		final SiteNode node3 = SiteNode.create(100, 100);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Vehicle v1 = Vehicle.create(node1, node2).setLocation(100);
		final Vehicle v2 = Vehicle.create(node1, node2).setLocation(100);
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(List.of(v1)).setTime(1.0);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(List.of(v2)).setTime(2.1);

		final List<EdgeTraffic> list = Stream.of(ev1, ev2).sorted((a, b) -> Double.compare(a.getTime(), b.getTime()))
				.collect(Collectors.toList());

		final EdgeTraffic result = StatusBuilder.selectByPriority(list);
		assertThat(result, notNullValue());
		assertThat(result, sameInstance(ev1));
	}

	/**
	 * <pre>
	 * \
	 *  e1
	 *   \|
	 *   -O<-- e3 --
	 *    ^
	 *    |
	 *    e2
	 *    |
	 * </pre>
	 */
	@Test
	public void testSelectByArrival() {
		final SiteNode node1 = SiteNode.create(0, 0);
		final SiteNode node2 = SiteNode.create(100, 100);
		final SiteNode node3 = SiteNode.create(100, 200);
		final SiteNode node4 = SiteNode.create(200, 100);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final MapEdge edge3 = MapEdge.create(node4, node2);
		final Vehicle v1 = Vehicle.create(node1, node2).setLocation(100 * Math.sqrt(2));
		final Vehicle v2 = Vehicle.create(node1, node2).setLocation(100);
		final Vehicle v3 = Vehicle.create(node1, node2).setLocation(100);
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(List.of(v1)).setTime(1.5);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(List.of(v2)).setTime(1.75);
		final EdgeTraffic ev3 = EdgeTraffic.create(edge3).setVehicles(List.of(v3)).setTime(1.0);

		final List<EdgeTraffic> list = Stream.of(ev1, ev2, ev3)
				.sorted((a, b) -> Double.compare(a.getTime(), b.getTime())).collect(Collectors.toList());

		final EdgeTraffic result = StatusBuilder.selectByPriority(list);
		assertThat(result, notNullValue());
		assertThat(result, sameInstance(ev3));
	}

	/**
	 * <pre>
	 *  -- e1 ->O
	 *          ^
	 *          |
	 *          e2
	 *          |
	 * </pre>
	 */
	@Test
	public void testSelectByPriority() {
		final SiteNode node1 = SiteNode.create(0, 0);
		final SiteNode node2 = SiteNode.create(100, 0);
		final SiteNode node3 = SiteNode.create(100, 100);
		final MapEdge edge1 = MapEdge.create(node1, node2).setPriority(1);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Vehicle v1 = Vehicle.create(node1, node2).setLocation(100);
		final Vehicle v2 = Vehicle.create(node1, node2).setLocation(100);
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(List.of(v1)).setTime(1.0);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(List.of(v2)).setTime(1.5);

		final List<EdgeTraffic> list = Stream.of(ev1, ev2).sorted((a, b) -> Double.compare(a.getTime(), b.getTime()))
				.collect(Collectors.toList());

		final EdgeTraffic result = StatusBuilder.selectByPriority(list);
		assertThat(result, notNullValue());
		assertThat(result, sameInstance(ev1));
	}

	/**
	 * <pre>
	 *  -- e1 ->O
	 *          ^
	 *          |
	 *          e2
	 *          |
	 * </pre>
	 */
	@Test
	public void testSelectByRightDirection() {
		final SiteNode node1 = SiteNode.create(0, 0);
		final SiteNode node2 = SiteNode.create(100, 0);
		final SiteNode node3 = SiteNode.create(100, 100);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Vehicle v1 = Vehicle.create(node1, node2).setLocation(100);
		final Vehicle v2 = Vehicle.create(node1, node2).setLocation(100);
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(List.of(v1)).setTime(1.0);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(List.of(v2)).setTime(1.5);

		final List<EdgeTraffic> list = Stream.of(ev1, ev2).sorted((a, b) -> Double.compare(a.getTime(), b.getTime()))
				.collect(Collectors.toList());

		final EdgeTraffic result = StatusBuilder.selectByPriority(list);
		assertThat(result, notNullValue());
		assertThat(result, sameInstance(ev2));
	}

	@Test
	public void testSetTraffic() {
		final MapEdge edge = MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10));
		final EdgeTraffic et = EdgeTraffic.create(edge);
		final Set<EdgeTraffic> traffics = Set.of(et);
		final StatusBuilder result = StatusBuilder.create().setTraffics(traffics);
		assertThat(result, notNullValue());
		assertThat(result.getTraffics(), sameInstance(traffics));
	}
}
