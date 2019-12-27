package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatusBuilderTest2 {

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
	private Vehicle v3;

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
		v3 = Vehicle.create(n3, n2).setLocation(100);
		final List<Vehicle> v14 = List.of(v1);
		final List<Vehicle> v34 = List.of(v3, v2);
		t14 = EdgeTraffic.create(e14).setTime(49.5).setVehicles(v14);
		t34 = EdgeTraffic.create(e34).setTime(50.0).setVehicles(v34);
		t42 = EdgeTraffic.create(e42).setTime(55);
		traffics = Set.of(t14, t34, t42);
		builder = StatusBuilder.create().setTraffics(traffics);
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
		assertThat(newt34.get().getVehicles(), hasSize(1));
		assertThat(newt34.get().getVehicles().get(0), equalTo(v3));
		assertTrue(newt34.get().getLastTravelTime().isPresent());
		assertThat(newt34.get().getLastTravelTime().getAsDouble(), equalTo(t34.getTime() - v1.getEdgeEntryTime()));

		final Optional<EdgeTraffic> newt14 = result.getTraffics().stream().filter(t14::equals).findFirst();
		assertTrue(newt14.isPresent());
		assertThat(newt14.get(), sameInstance(t14));
	}
}
