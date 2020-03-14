package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrafficsBuilderTest {

	private MapNode n1;
	private MapNode n2;
	private MapNode n3;
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
	private TrafficsBuilder builder;

	@Test
	public void create() {
		final TrafficsBuilder result = TrafficsBuilder.create();
		assertThat(result, notNullValue());
		assertThat(result.getTraffics(), empty());
	}

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
		n1 = MapNode.create(0, 0);
		n2 = MapNode.create(1000, 0);
		n3 = MapNode.create(500, 500);
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
		builder = TrafficsBuilder.create().setTraffics(traffics);
	}

	@Test
	public void getTrafficStats() {
		final RoutePlanner result = builder.getRoutePlanner();
		assertNotNull(result);

		final Optional<MapNode> n = result.prevNode(n1, n2);
		assertTrue(n.isPresent());
		assertThat(n.get(), equalTo(n4));
	}

	@Test
	public void nextPoisson() {
		final Random random = new Random(1234);
		int tot = 0;
		final int n = 100000;
		final double lambda = 10;
		for (int i = 0; i < n; i++) {
			tot += TrafficsBuilder.nextPoison(lambda, random);
		}
		assertThat((double) tot / n, closeTo(lambda, lambda * 0.01));
	}

	@Test
	public void setTraffic() {
		final MapEdge edge = MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10));
		final EdgeTraffic et = EdgeTraffic.create(edge);
		final Set<EdgeTraffic> traffics = Set.of(et);
		final TrafficsBuilder result = TrafficsBuilder.create().setTraffics(traffics);
		assertThat(result, notNullValue());
		assertThat(result.getTraffics(), sameInstance(traffics));
	}
}
