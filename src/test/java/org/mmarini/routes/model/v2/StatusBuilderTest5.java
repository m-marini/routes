package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test with vehicle at inexistent edge
 *
 * @author mmarini
 *
 */
public class StatusBuilderTest5 {

	private SiteNode n1;
	private SiteNode n2;
	private SiteNode n3;
	private MapNode n4;
	private MapEdge e14;
	private MapEdge e34;
	private MapEdge e24;
	private Vehicle v1;
	private Vehicle v2;
	private EdgeTraffic t14;
	private EdgeTraffic t34;
	private EdgeTraffic t24;
	private Set<EdgeTraffic> traffics;
	private StatusBuilder builder;
	private Vehicle v3;

	/**
	 * <pre>
	 * n1 --v1-> n4 <--- n2
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
		e24 = MapEdge.create(n2, n4).setSpeedLimit(10);
		v1 = Vehicle.create(n1, n2).setLocation(500);
		v2 = Vehicle.create(n3, n2).setLocation(500);
		final List<Vehicle> v14 = List.of(v1);
		final List<Vehicle> v34 = List.of(v2);
		t14 = EdgeTraffic.create(e14).setTime(49.5).setVehicles(v14);
		t34 = EdgeTraffic.create(e34).setTime(50.0).setVehicles(v34);
		t24 = EdgeTraffic.create(e24).setTime(55);
		traffics = Set.of(t14, t34, t24);
		builder = StatusBuilder.create().setTraffics(traffics);
	}

	@Test
	public void testMoveVehicleAtCross() {
		final List<EdgeTraffic> crossingEdges = Stream.of(t14, t34)
				.sorted((a, b) -> Double.compare(a.getTime(), b.getTime())).collect(Collectors.toList());
		final StatusBuilder result = builder.moveVehicleAtCross(crossingEdges);
		assertNotNull(result);

		// outcoming edge
		final Optional<EdgeTraffic> newt24 = result.getTraffics().stream().filter(t24::equals).findFirst();
		assertTrue(newt24.isPresent());
		assertThat(newt24.get(), sameInstance(t24));

		// incoming edge
		final Optional<EdgeTraffic> newt34 = result.getTraffics().stream().filter(t34::equals).findFirst();
		assertTrue(newt34.isPresent());
		assertThat(newt34.get().getTime(), equalTo(t34.getTime()));
		assertThat(newt34.get().getVehicles(), empty());

		// idle edge
		final Optional<EdgeTraffic> newt14 = result.getTraffics().stream().filter(t14::equals).findFirst();
		assertTrue(newt14.isPresent());
		assertThat(newt14.get().getTime(), equalTo(t34.getTime()));
		assertThat(newt14.get().getVehicles(), equalTo(t14.getVehicles()));
	}
}
