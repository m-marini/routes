package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SimulatorTest {

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
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(Arrays.asList(v1)).setTime(1.0);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(Arrays.asList(v2)).setTime(2.1);

		final List<EdgeTraffic> list = Arrays.asList(ev1, ev2);
		Collections.sort(list, (a, b) -> Double.compare(a.getTime(), b.getTime()));

		final EdgeTraffic result = Simulator.selectByPriority(list);
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
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(Arrays.asList(v1)).setTime(1.5);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(Arrays.asList(v2)).setTime(1.75);
		final EdgeTraffic ev3 = EdgeTraffic.create(edge3).setVehicles(Arrays.asList(v3)).setTime(1.0);

		final List<EdgeTraffic> list = Arrays.asList(ev1, ev2, ev3);
		Collections.sort(list, (a, b) -> Double.compare(a.getTime(), b.getTime()));

		final EdgeTraffic result = Simulator.selectByPriority(list);
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
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(Arrays.asList(v1)).setTime(1.0);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(Arrays.asList(v2)).setTime(1.5);

		final List<EdgeTraffic> list = Arrays.asList(ev1, ev2);
		Collections.sort(list, (a, b) -> Double.compare(a.getTime(), b.getTime()));

		final EdgeTraffic result = Simulator.selectByPriority(list);
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
		final EdgeTraffic ev1 = EdgeTraffic.create(edge1).setVehicles(Arrays.asList(v1)).setTime(1.0);
		final EdgeTraffic ev2 = EdgeTraffic.create(edge2).setVehicles(Arrays.asList(v2)).setTime(1.5);

		final List<EdgeTraffic> list = Arrays.asList(ev1, ev2);
		Collections.sort(list, (a, b) -> Double.compare(a.getTime(), b.getTime()));

		final EdgeTraffic result = Simulator.selectByPriority(list);
		assertThat(result, notNullValue());
		assertThat(result, sameInstance(ev2));
	}

}
