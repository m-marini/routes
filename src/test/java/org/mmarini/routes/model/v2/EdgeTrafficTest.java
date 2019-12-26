package org.mmarini.routes.model.v2;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class EdgeTrafficTest implements Constants {
	private static final double SPEED = 10.0;

	private static final double EXPECTED_DEFAULT_TIME = 10.0 * sqrt(2) / DEFAULT_SPEED_LIMIT_KMH / KMH_TO_MPS;

	@Test
	public void test() {
		final EdgeTraffic s = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		assertThat(s, notNullValue());
		assertFalse(s.getLastTravelTime().isPresent());
		assertThat(s.getTravelTime(), closeTo(EXPECTED_DEFAULT_TIME, EXPECTED_DEFAULT_TIME * 1e-3));
	}

	@Test
	public void testAddVehicle() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(6);
		final List<Vehicle> vehicles = List.of(v1);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setTime(10).setVehicles(vehicles);

		final EdgeTraffic result = traffic.addVehicle(v, 5.0);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), hasSize(2));
		assertThat(result.getLast(), equalTo(v1));
		assertThat(result.getVehicles().get(0), equalTo(v));
		assertThat(result.getVehicles().get(0).getLocation(), closeTo(0.833, 1e-3));
		assertThat(result.getVehicles().get(0).getEdgeEntryTime(), equalTo(5.0));
	}

	@Test
	public void testAddVehicleEmpty() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end);
		final EdgeTraffic result = EdgeTraffic.create(edge).setTime(10).addVehicle(v, 5.0);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), hasSize(1));
		assertThat(result.getLast(), equalTo(v));
		assertThat(result.getLast().getLocation(), equalTo(50.0));
	}

	@Test
	public void testCreate() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
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
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end);
		final List<Vehicle> vehicles = List.of(v);
		final Vehicle result = EdgeTraffic.create(edge).setVehicles(vehicles).getLast();
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
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end).setLocation(VEHICLE_LENGTH + 1e-3);
		final List<Vehicle> vehicles = List.of(v);
		final boolean result = EdgeTraffic.create(edge).setVehicles(vehicles).isBusy();
		assertFalse(result);
	}

	@Test
	public void testIsBusyFalse1() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final boolean result = EdgeTraffic.create(edge).isBusy();
		assertFalse(result);
	}

	@Test
	public void testIsBusyTrue() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end);
		final List<Vehicle> vehicles = List.of(v);
		final boolean result = EdgeTraffic.create(edge).setVehicles(vehicles).isBusy();
		assertTrue(result);
	}

	@Test
	public void testMove1() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final Vehicle v2 = Vehicle.create(begin, end).setLocation(10.0);
		final List<Vehicle> list = List.of(v2, v1);

		final EdgeTraffic result = EdgeTraffic.create(edge).setVehicles(list).moveVehicles(2.0);

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
	public void testMove2() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100.0);
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
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100.0);
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
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);

		final EdgeTraffic result = EdgeTraffic.create(edge).moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(2.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, empty());
	}

	@Test
	public void testMove5() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 100);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(SPEED);
		final Vehicle v1 = Vehicle.create(begin, end).setLocation(40.0);
		final List<Vehicle> list = List.of(v1);

		final EdgeTraffic result = EdgeTraffic.create(edge).setVehicles(list).moveVehicles(2.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(2.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, hasSize(1));

		final Vehicle v11 = vehicles.get(0);
		assertThat(v11.getLocation(), equalTo(60.0));
	}

	@Test
	public void testRemoveLastNotEmpty() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
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
	public void testRemoveLastToEmpty() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final Vehicle v = Vehicle.create(begin, end).setLocation(10);
		final List<Vehicle> vehicles = List.of(v);
		final EdgeTraffic result = EdgeTraffic.create(edge).setTime(5).setVehicles(vehicles).removeLast();
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), empty());
		assertThat(result.getTravelTime(), equalTo(1.0));
	}

	@Test
	public void testSetLastTravelTime() {
		final EdgeTraffic s = EdgeTraffic.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)))
				.setLastTravelTime(OptionalDouble.of(10.0));
		assertThat(s, notNullValue());
		assertTrue(s.getLastTravelTime().isPresent());
		assertThat(s.getLastTravelTime().getAsDouble(), equalTo(10.0));
		assertThat(s.getTravelTime(), equalTo(10.0));
	}

	@Test
	public void testSetVehicles() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
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
