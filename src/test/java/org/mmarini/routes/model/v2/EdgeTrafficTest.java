package org.mmarini.routes.model.v2;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.routes.model.v2.TestUtils.genArguments;
import static org.mmarini.routes.model.v2.TestUtils.genDouble;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.routes.model.Constants;

public class EdgeTrafficTest implements Constants {
	private static final double SPEED = 10.0;

	private static final double EXPECTED_DEFAULT_TIME = 10.0 * sqrt(2) / DEFAULT_SPEED_LIMIT_KMH / KMH_TO_MPS;

	static DoubleStream locationRange() {
		return genArguments().mapToDouble(i -> genDouble(i, 0, 100));
	}

	static Stream<Arguments> locationsRange() {
		final Stream<Arguments> result = genArguments()
				.mapToObj(i -> Arguments.of(genDouble(i, 0, 100), genDouble(i, 0, 100)));
		return result;
	}

	static Stream<Arguments> prioritiesRange() {
		final Stream<Arguments> result = IntStream.range(0, 10).mapToObj(Integer::valueOf)
				.flatMap(i -> IntStream.range(0, 10).mapToObj(j -> Arguments.of(i, j + 1)));
		return result;
	}

	static IntStream priorityRange() {
		return genArguments(10);
	}

	static DoubleStream timeRange() {
		return genArguments().mapToDouble(i -> genDouble(i, 1, 10));
	}

	@Test
	public void compareDifferentDirection() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 10);
		final MapNode n = MapNode.create(10, 0);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(s0, n));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(s1, n));

		final int result12 = et1.compareDirection(et2);
		final int result21 = et2.compareDirection(et1);

		assertThat(result12, lessThan(0));
		assertThat(result21, greaterThan(0));
	}

	@ParameterizedTest
	@MethodSource("prioritiesRange")
	public void compareDifferentPriority(final int priority, final int delta) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setPriority(priority));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(begin, end).setPriority(priority + delta));

		final int result12 = et1.comparePriority(et2);
		final int result21 = et2.comparePriority(et1);

		assertThat(result12, lessThan(0));
		assertThat(result21, greaterThan(0));
	}

	@ParameterizedTest
	@MethodSource("priorityRange")
	public void compareEqualPriority(final int priority) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setPriority(priority));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(begin, end).setPriority(priority));

		final int result = et1.comparePriority(et2);
		assertThat(result, equalTo(0));
	}

	@ParameterizedTest
	@MethodSource("locationsRange")
	public void compareExit(final double location1, final double location2) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(100, 0);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(location1);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(location2);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setSpeedLimit(10))
				.setVehicles(List.of(v1));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(end, begin).setSpeedLimit(10))
				.setVehicles(List.of(v2));

		final int expected = Double.compare(location1, location2);

		final int result12 = et1.compareExitTime(et2);
		final int result21 = et2.compareExitTime(et1);

		if (expected < 0) {
			assertThat(result12, greaterThan(0));
			assertThat(result21, lessThan(0));
		} else if (expected > 0) {
			assertThat(result12, lessThan(0));
			assertThat(result21, greaterThan(0));
		} else {
			assertThat(result12, equalTo(0));
			assertThat(result21, equalTo(0));
		}
	}

	@Test
	public void compareExitNoVehicles() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(100, 0);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setSpeedLimit(10));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(end, begin).setSpeedLimit(10));

		final int result12 = et1.compareExitTime(et2);
		final int result21 = et2.compareExitTime(et1);

		assertThat(result12, equalTo(0));
		assertThat(result21, equalTo(0));
	}

	@Test
	public void compareExitOneNoVehicles() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(100, 0);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setSpeedLimit(10))
				.setVehicles(List.of(Vehicle.create(begin, end)));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(end, begin).setSpeedLimit(10));

		final int result12 = et1.compareExitTime(et2);
		final int result21 = et2.compareExitTime(et1);

		assertThat(result12, lessThan(0));
		assertThat(result21, greaterThan(0));
	}

	@Test
	public void compareSameDirection() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end));
		final EdgeTraffic et2 = EdgeTraffic.create(MapEdge.create(end, begin));
		final int result12 = et1.compareDirection(et2);
		final int result21 = et2.compareDirection(et1);
		assertThat(result12, equalTo(0));
		assertThat(result21, equalTo(0));
	}

	@Test
	public void compareTo() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 0);
		final EdgeTraffic traffic0 = EdgeTraffic.create(MapEdge.create(node0, node1));
		final EdgeTraffic traffic1 = EdgeTraffic.create(MapEdge.create(node1, node0));

		final int result01 = traffic0.compareTo(traffic1);
		final int result10 = traffic1.compareTo(traffic0);

		assertThat(result10, lessThan(0));
		assertThat(result01, greaterThan(0));
	}

	@Test
	public void create() {
		final MapEdge edge = MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10));
		final EdgeTraffic result = EdgeTraffic.create(edge);
		assertThat(result, notNullValue());
		assertFalse(result.getLastTravelTime().isPresent());
		assertThat(result.getTravelTime(), closeTo(EXPECTED_DEFAULT_TIME, EXPECTED_DEFAULT_TIME * 1e-3));
		assertThat(result.getEdge(), sameInstance(edge));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getExitTime(final double location) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(100, 0);
		final Vehicle v = Vehicle.create(begin, end).setLocation(location);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setSpeedLimit(10))
				.setVehicles(List.of(v));

		final OptionalDouble result = et1.getExitTime();
		assertNotNull(result);
		assertTrue(result.isPresent());
		assertThat(result.getAsDouble(), closeTo((100 - location) / 10, 1e-3));
	}

	@Test
	public void getExitTimeEmpty() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(100, 0);
		final EdgeTraffic et1 = EdgeTraffic.create(MapEdge.create(begin, end).setSpeedLimit(10));

		final OptionalDouble result = et1.getExitTime();
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void getTrafficCongestion0() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 60);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final EdgeTraffic et = EdgeTraffic.create(edge);

		final double result = et.getTrafficCongestion();

		assertThat(result, closeTo(0, 1e-3));
	}

	@Test
	public void getTrafficCongestion03() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 60);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final List<Vehicle> vehicles = IntStream.range(0, 3)
				.mapToObj(i -> Vehicle.create(begin, end).setLocation(i * 5)).collect(Collectors.toList());
		final EdgeTraffic et = EdgeTraffic.create(edge).setVehicles(vehicles);

		final double result = et.getTrafficCongestion();

		assertThat(result, closeTo(0.3, 1e-3));
	}

	@Test
	public void getTrafficCongestion05() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 60);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final List<Vehicle> vehicles = IntStream.range(0, 5)
				.mapToObj(i -> Vehicle.create(begin, end).setLocation(i * 5)).collect(Collectors.toList());
		final EdgeTraffic et = EdgeTraffic.create(edge).setVehicles(vehicles);

		final double result = et.getTrafficCongestion();

		assertThat(result, closeTo(0.5, 1e-3));
	}

	@Test
	public void getTrafficCongestion075() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 60);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final List<Vehicle> vehicles = IntStream.range(0, 9)
				.mapToObj(i -> Vehicle.create(begin, end).setLocation(i * 5)).collect(Collectors.toList());
		final EdgeTraffic et = EdgeTraffic.create(edge).setVehicles(vehicles);

		final double result = et.getTrafficCongestion();

		final double expected = Math.pow(0.5, 0.25) * 0.5 + 0.5;

		assertThat(result, closeTo(expected, 1e-3));
	}

	@Test
	public void getTrafficCongestion1() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 60);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final List<Vehicle> vehicles = IntStream.range(0, 13)
				.mapToObj(i -> Vehicle.create(begin, end).setLocation(i * 5)).collect(Collectors.toList());
		final EdgeTraffic et = EdgeTraffic.create(edge).setVehicles(vehicles);

		final double result = et.getTrafficCongestion();

		assertThat(result, closeTo(1, 1e-3));
	}

	/**
	 * <pre>
	 *  0 --> 1
	 *        ^
	 *        |
	 *        |
	 *        2
	 * </pre>
	 */
	@Test
	public void isAllFromLeft() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 0);
		final MapNode node2 = MapNode.create(0, 10);
		final EdgeTraffic edge0 = EdgeTraffic.create(MapEdge.create(node0, node1));
		final EdgeTraffic edge1 = EdgeTraffic.create(MapEdge.create(node2, node1));
		final Set<EdgeTraffic> traffics = Set.of(edge0, edge1);

		assertFalse(edge0.isAllFromLeft(traffics));
		assertTrue(edge1.isAllFromLeft(traffics));
	}

	@Test
	public void isCrossing() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 0);
		final MapNode node2 = MapNode.create(0, 10);
		final EdgeTraffic edge = EdgeTraffic.create(MapEdge.create(node0, node1));
		final EdgeTraffic other = EdgeTraffic.create(MapEdge.create(node2, node1));

		final boolean result = edge.isCrossing(other);
		assertTrue(result);
	}

	@Test
	public void move1() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(10.0);
		final List<Vehicle> list = List.of(v2, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setVehicles(list);

		final EdgeTraffic result = traffic.moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(2.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, hasSize(2));

		final Vehicle v11 = vehicles.get(1);
		assertThat(v11.getLocation(), equalTo(60.0));

		final Vehicle v21 = vehicles.get(0);
		assertThat(v21.getLocation(), equalTo(30.0));

	}

	@Test
	public void moveBeforeTime() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(10.0);
		final List<Vehicle> list = List.of(v2, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setVehicles(list).setTime(2.0);

		final EdgeTraffic result = traffic.moveVehicles(1.0);

		assertThat(result, notNullValue());
		assertThat(result, sameInstance(result));
	}

	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveToTime(final double time) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final Vehicle v0 = Vehicle.create(begin, end).setLocation(10.0);
		final List<Vehicle> list = List.of(v0, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setVehicles(list).setTime(time);

		final EdgeTraffic result = traffic.moveToTime(time + 1);

		assertNotNull(result);
		assertThat(result.getTime(), equalTo(time + 1));

		assertThat(result.getVehicles(), hasSize(2));
		final Vehicle rv0 = result.getVehicles().get(0);
		final Vehicle rv1 = result.getVehicles().get(1);

		assertThat(rv0, equalTo(v0));
		assertThat(rv0.getLocation(), closeTo(20.0, 1e-3));

		assertThat(rv1, equalTo(v1));
		assertThat(rv1.getLocation(), closeTo(50.0, 1e-3));
	}

	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveToTimeBefore(final double time) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(10.0);
		final List<Vehicle> list = List.of(v2, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setVehicles(list).setTime(time);

		final EdgeTraffic result = traffic.moveToTime(time - 1);

		assertNotNull(result);
		assertThat(result, sameInstance(traffic));

	}

	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveToTimeEmpty(final double time) {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setTime(time);

		final EdgeTraffic result = traffic.moveToTime(time + 1);

		assertNotNull(result);
		assertThat(result.getTime(), equalTo(time + 1));

	}

	@Test
	public void removeLastNotEmpty() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end).setLocation(10);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(0);
		final List<Vehicle> vehicles = List.of(v1, v);
		final EdgeTraffic result = EdgeTraffic.create(edge).setTime(5).setVehicles(vehicles).removeLast();
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), hasSize(1));
		assertThat(result.getTravelTime(), equalTo(5.0));
	}

	@Test
	public void removeLastToEmpty() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end).setLocation(10);
		final List<Vehicle> vehicles = List.of(v);
		final EdgeTraffic result = EdgeTraffic.create(edge).setTime(5).setVehicles(vehicles).removeLast();
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), empty());
		assertThat(result.getTravelTime(), equalTo(1.0));
	}

	@Test
	public void removeNotLast() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end).setLocation(9);
		final List<Vehicle> vehicles = List.of(v);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setTime(5).setVehicles(vehicles);

		final EdgeTraffic result = traffic.removeLast();

		assertThat(result, notNullValue());
		assertThat(result, sameInstance(traffic));
	}

	@Test
	public void setEdge() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(0, 100);
		final MapNode node2 = MapNode.create(0, 70);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final Vehicle v0 = Vehicle.create(node0, node1).setLocation(10.0);
		final Vehicle v1 = Vehicle.create(node0, node1).setLocation(60.0);
		final List<Vehicle> list = List.of(v0, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge0).setVehicles(list).setTime(2.0);

		final EdgeTraffic result = traffic.setEdge(edge1);

		assertNotNull(result);
		assertThat(result.getEdge(), sameInstance(edge1));
		assertThat(result.getVehicles(), sameInstance(list));
	}

	@Test
	public void setEdgeEmpty() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(0, 100);
		final MapNode node2 = MapNode.create(0, 70);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final EdgeTraffic traffic = EdgeTraffic.create(edge0);

		final EdgeTraffic result = traffic.setEdge(edge1);

		assertNotNull(result);
		assertThat(result.getEdge(), sameInstance(edge1));
		assertThat(result.getVehicles(), empty());
	}

	@Test
	public void setEdgeShrink() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(0, 100);
		final MapNode node2 = MapNode.create(0, 50);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final Vehicle v0 = Vehicle.create(node0, node1).setLocation(10.0);
		final Vehicle v1 = Vehicle.create(node0, node1).setLocation(60.0);
		final List<Vehicle> list = List.of(v0, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge0).setVehicles(list).setTime(2.0);

		final EdgeTraffic result = traffic.setEdge(edge1);

		assertNotNull(result);
		assertThat(result.getEdge(), sameInstance(edge1));

		assertThat(result.getVehicles(), hasSize(2));
		final Vehicle rv0 = result.getVehicles().get(0);
		final Vehicle rv1 = result.getVehicles().get(1);

		assertThat(rv0, equalTo(v0));
		assertThat(rv0.getLocation(), equalTo(10.0));
		assertThat(rv1, equalTo(v1));
		assertThat(rv1.getLocation(), equalTo(50.0));
	}

	@Test
	public void setEdgeShrinkAll() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(0, 100);
		final MapNode node2 = MapNode.create(0, 12);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final Vehicle v0 = Vehicle.create(node0, node1).setLocation(10.0);
		final Vehicle v1 = Vehicle.create(node0, node1).setLocation(60.0);
		final List<Vehicle> list = List.of(v0, v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge0).setVehicles(list).setTime(2.0);

		final EdgeTraffic result = traffic.setEdge(edge1);

		assertNotNull(result);
		assertThat(result.getEdge(), sameInstance(edge1));

		assertThat(result.getVehicles(), hasSize(2));
		final Vehicle rv0 = result.getVehicles().get(0);
		final Vehicle rv1 = result.getVehicles().get(1);

		assertThat(rv0, equalTo(v0));
		assertThat(rv0.getLocation(), equalTo(7.0));
		assertThat(rv1, equalTo(v1));
		assertThat(rv1.getLocation(), equalTo(12.0));
	}

	@Test
	public void setEdgeShrinkAndCut() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(0, 100);
		final MapNode node2 = MapNode.create(0, 9);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final Vehicle v0 = Vehicle.create(node0, node1).setLocation(5.0);
		final Vehicle v1 = Vehicle.create(node0, node1).setLocation(15.0);
		final Vehicle v2 = Vehicle.create(node0, node1).setLocation(60.0);
		final List<Vehicle> list = List.of(v0, v1, v2);
		final EdgeTraffic traffic = EdgeTraffic.create(edge0).setVehicles(list).setTime(2.0);

		final EdgeTraffic result = traffic.setEdge(edge1);

		assertNotNull(result);
		assertThat(result.getEdge(), sameInstance(edge1));

		assertThat(result.getVehicles(), hasSize(2));
		final Vehicle rv0 = result.getVehicles().get(0);
		final Vehicle rv1 = result.getVehicles().get(1);

		assertThat(rv0, equalTo(v1));
		assertThat(rv0.getLocation(), equalTo(4.0));
		assertThat(rv1, equalTo(v2));
		assertThat(rv1.getLocation(), equalTo(9.0));
	}

	@Test
	public void setLastTravelTime() {
		final EdgeTraffic s = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)))
				.setLastTravelTime(OptionalDouble.of(10.0));
		assertThat(s, notNullValue());
		assertTrue(s.getLastTravelTime().isPresent());
		assertThat(s.getLastTravelTime().getAsDouble(), equalTo(10.0));
		assertThat(s.getTravelTime(), equalTo(10.0));
	}

	@Test
	public void testAddVehicle() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(6);
		final List<Vehicle> vehicles = List.of(v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setTime(10).setVehicles(vehicles);

		final EdgeTraffic result = traffic.addVehicle(v, 5.0);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), hasSize(2));
		assertThat(result.getLast().get(), equalTo(v1));
		assertThat(result.getVehicles().get(0), equalTo(v));
		assertThat(result.getVehicles().get(0).getLocation(), closeTo(0.833, 1e-3));
		assertThat(result.getVehicles().get(0).getEdgeEntryTime(), equalTo(5.0));
	}

	@Test
	public void testAddVehicleEmpty() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end);
		final EdgeTraffic result = EdgeTraffic.create(edge).setTime(10).addVehicle(v, 5.0);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), hasSize(1));
		assertThat(result.getLast().get(), equalTo(v));
		assertThat(result.getLast().get().getLocation(), equalTo(50.0));
	}

	@Test
	public void testCreate() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final EdgeTraffic result = EdgeTraffic.create(edge);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), notNullValue());
		assertThat(result.getVehicles(), empty());
	}

	@Test
	public void testEquals() {
		final EdgeTraffic s1 = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		final EdgeTraffic s11 = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		final EdgeTraffic s12 = s1.setLastTravelTime(OptionalDouble.of(100));
		final EdgeTraffic s13 = s1.setLastTravelTime(OptionalDouble.of(100)).setTime(100);
		final EdgeTraffic s14 = s1.setLastTravelTime(OptionalDouble.of(100)).setVehicles(Collections.emptyList());
		final EdgeTraffic s2 = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 20)));
		final EdgeTraffic s3 = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 10), MapNode.create(10, 10)));

		assertThat(s1, notNullValue());
		assertThat(s11, notNullValue());
		assertThat(s12, notNullValue());
		assertThat(s13, notNullValue());
		assertThat(s14, notNullValue());
		assertThat(s2, notNullValue());
		assertThat(s3, notNullValue());

		assertNotSame(s1, s11);
		assertNotSame(s1, s12);
		assertNotSame(s1, s13);
		assertNotSame(s1, s14);
		assertNotSame(s1, s2);
		assertNotSame(s1, s3);

		assertFalse(s1.equals(null));
		assertFalse(s1.equals(new Object()));
		assertFalse(s1.equals(s2));
		assertFalse(s1.equals(s3));

		assertTrue(s1.equals(s1));
		assertTrue(s1.equals(s11));
		assertTrue(s1.equals(s12));
		assertTrue(s1.equals(s13));
		assertTrue(s1.equals(s14));

		assertTrue(s11.equals(s1));
		assertTrue(s11.equals(s11));
		assertTrue(s11.equals(s12));
		assertTrue(s11.equals(s13));
		assertTrue(s11.equals(s14));

		assertTrue(s12.equals(s1));
		assertTrue(s12.equals(s11));
		assertTrue(s12.equals(s12));
		assertTrue(s12.equals(s13));
		assertTrue(s12.equals(s14));

		assertTrue(s13.equals(s1));
		assertTrue(s13.equals(s11));
		assertTrue(s13.equals(s12));
		assertTrue(s13.equals(s13));
		assertTrue(s13.equals(s14));

		assertTrue(s14.equals(s1));
		assertTrue(s14.equals(s11));
		assertTrue(s14.equals(s12));
		assertTrue(s14.equals(s13));
		assertTrue(s14.equals(s14));
	}

	@Test
	public void testGetLast() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end);
		final List<Vehicle> vehicles = List.of(v);
		final Vehicle result = EdgeTraffic.create(edge).setVehicles(vehicles).getLast().get();
		assertThat(result, notNullValue());
		assertThat(result, equalTo(v));
	}

	@Test
	public void testHashCode() {
		final EdgeTraffic s1 = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		final EdgeTraffic s11 = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		final EdgeTraffic s12 = s1.setLastTravelTime(OptionalDouble.of(100));
		final EdgeTraffic s13 = s1.setLastTravelTime(OptionalDouble.of(100)).setTime(100);
		final EdgeTraffic s14 = s1.setLastTravelTime(OptionalDouble.of(100)).setVehicles(Collections.emptyList());

		assertThat(s1, notNullValue());
		assertThat(s11, notNullValue());
		assertThat(s12, notNullValue());
		assertThat(s13, notNullValue());
		assertThat(s14, notNullValue());

		assertNotSame(s1, s11);
		assertNotSame(s1, s12);
		assertNotSame(s1, s13);
		assertNotSame(s1, s14);

		assertEquals(s1.hashCode(), s11.hashCode());
		assertEquals(s1.hashCode(), s12.hashCode());
		assertEquals(s1.hashCode(), s13.hashCode());
		assertEquals(s1.hashCode(), s14.hashCode());
	}

	@Test
	public void testIsBusyFalse() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end).setLocation(VEHICLE_LENGTH + 1e-3);
		final List<Vehicle> vehicles = List.of(v);
		final boolean result = EdgeTraffic.create(edge).setVehicles(vehicles).isBusy();
		assertFalse(result);
	}

	@Test
	public void testIsBusyFalse1() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final boolean result = EdgeTraffic.create(edge).isBusy();
		assertFalse(result);
	}

	@Test
	public void testIsBusyTrue() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end);
		final List<Vehicle> vehicles = List.of(v);
		final boolean result = EdgeTraffic.create(edge).setVehicles(vehicles).isBusy();
		assertTrue(result);
	}

	@Test
	public void testMove2() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100.0);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(5);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(0);
		final List<Vehicle> list = List.of(v2, v1);

		final EdgeTraffic result = EdgeTraffic.create(edge).setVehicles(list).moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(2.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, hasSize(2));

		final Vehicle v11 = vehicles.get(1);
		assertThat(v11.getLocation(), equalTo(5 + 20.0));

		final Vehicle v21 = vehicles.get(0);
		assertThat(v21.getLocation(), equalTo(40.0 / 3));

	}

	@Test
	public void testmove3() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100.0);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(110);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(105);
		final List<Vehicle> list = List.of(v2, v1);

		final EdgeTraffic result = EdgeTraffic.create(edge).setVehicles(list).moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(0.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, hasSize(2));

		final Vehicle v11 = vehicles.get(1);
		assertThat(v11.getLocation(), equalTo(100.0));

		final Vehicle v21 = vehicles.get(0);
		assertThat(v21.getLocation(), equalTo(95.0));

	}

	/** test move on empty edge */
	@Test
	public void testMove4() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);

		final EdgeTraffic result = EdgeTraffic.create(edge).moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(2.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, empty());
	}

	@Test
	public void testMove5() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final List<Vehicle> list = List.of(v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setVehicles(list);

		final EdgeTraffic result = traffic.moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(2.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, hasSize(1));

		final Vehicle v11 = vehicles.get(0);
		assertThat(v11.getLocation(), equalTo(60.0));
	}

	@Test
	public void testSetVehicles() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end);
		final List<Vehicle> vehicles = List.of(v);
		final EdgeTraffic result = EdgeTraffic.create(edge).setVehicles(vehicles);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), notNullValue());
		assertThat(result.getVehicles(), hasSize(1));
		assertThat(result.getVehicles(), hasItem(v));
	}

	@Test
	public void testToString() {
		final EdgeTraffic s = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		assertThat(s, hasToString("EdgeTraffic [590864a2-f026-3db9-9edc-cfc9b60fe90b]"));
	}
}
