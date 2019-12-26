package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
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

public class StatusBuilderTest3 {

	private SiteNode n1;
	private SiteNode n2;
	private SiteNode n3;
	private MapEdge e13;
	private MapEdge e23;
	private Vehicle v1;
	private Vehicle v2;
	private EdgeTraffic t13;
	private EdgeTraffic t23;
	private Set<EdgeTraffic> traffics;
	private StatusBuilder builder;

	/**
	 * <pre>
	 * n1 --v1-> n3
	 *           ^
	 *           |
	 *           v2
	 *           |
	 *           |
	 *           n2
	 * </pre>
	 */
	@BeforeEach
	public void createCase() {
		n1 = SiteNode.create(0, 0);
		n2 = SiteNode.create(500, 500);
		n3 = SiteNode.create(500, 0);
		e13 = MapEdge.create(n1, n3).setSpeedLimit(10);
		e23 = MapEdge.create(n2, n3).setSpeedLimit(10);
		v1 = Vehicle.create(n1, n2).setLocation(500);
		v2 = Vehicle.create(n3, n2).setLocation(500).setReturning(true);
		final List<Vehicle> v13 = List.of(v1);
		final List<Vehicle> v23 = List.of(v2);
		t13 = EdgeTraffic.create(e13).setTime(49.5).setVehicles(v13);
		t23 = EdgeTraffic.create(e23).setTime(50.0).setVehicles(v23);
		traffics = Set.of(t13, t23);
		builder = StatusBuilder.create().setTraffics(traffics);
	}

	/**
	 * Test move with vehicle returned at departure
	 */
	@Test
	public void testMoveVehicleAtCross() {
		final List<EdgeTraffic> crossingEdges = Stream.of(t13, t23)
				.sorted((a, b) -> Double.compare(a.getTime(), b.getTime())).collect(Collectors.toList());
		final StatusBuilder result = builder.moveVehicleAtCross(crossingEdges);
		assertNotNull(result);

		// incoming edge
		final Optional<EdgeTraffic> newt23 = result.getTraffics().stream().filter(t23::equals).findFirst();
		assertTrue(newt23.isPresent());
		assertThat(newt23.get().getVehicles(), empty());
		assertFalse(newt23.get().getLastTravelTime().isPresent());

		// idle edge
		final Optional<EdgeTraffic> newt13 = result.getTraffics().stream().filter(t13::equals).findFirst();
		assertTrue(newt13.isPresent());
		assertThat(newt13.get(), equalTo(t13));
		assertThat(newt13.get().getTime(), equalTo(t23.getTime()));
	}
}
