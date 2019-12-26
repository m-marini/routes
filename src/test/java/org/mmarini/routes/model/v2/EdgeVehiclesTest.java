package org.mmarini.routes.model.v2;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class EdgeVehiclesTest implements Constants {
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
	public void testIsBusyFalse() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(0, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Vehicle v = Vehicle.create(begin, end).setLocation(VEHICLE_LENGTH + 1e-3);
		final List<Vehicle> vehicles = Arrays.asList(v);
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
		final List<Vehicle> vehicles = Arrays.asList(v);
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
		final List<Vehicle> list = Arrays.asList(v2, v1);

		final EdgeTraffic edgeTraffic = EdgeTraffic.create(edge).setVehicles(list).setTime(1);
		final EdgeTraffic result = edgeTraffic.moveVehicles(3.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(3.0));

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
		final List<Vehicle> list = Arrays.asList(v2, v1);

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
		final List<Vehicle> list = Arrays.asList(v2, v1);

		final EdgeTraffic edgeTraffic = EdgeTraffic.create(edge).setVehicles(list).setTime(1);
		final EdgeTraffic result = edgeTraffic.moveVehicles(3.0);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(1.0));

		final List<Vehicle> vehicles = result.getVehicles();
		assertThat(vehicles, hasSize(2));

		final Vehicle v11 = vehicles.get(1);
		assertThat(v11.getLocation(), equalTo(100.0));

		final Vehicle v21 = vehicles.get(0);
		assertThat(v21.getLocation(), equalTo(95.0));

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
		final List<Vehicle> vehicles = Arrays.asList(v);
		final EdgeTraffic result = EdgeTraffic.create(edge).setVehicles(vehicles);
		assertThat(result, notNullValue());
		assertThat(result.getVehicles(), notNullValue());
		assertThat(result.getVehicles(), hasSize(1));
		assertThat(result.getVehicles(), hasItem(v));
	}

}
