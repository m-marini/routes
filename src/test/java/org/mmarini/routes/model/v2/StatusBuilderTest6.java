package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test build status at cross proximity
 */
public class StatusBuilderTest6 {

	private static final double TIME = 50.2;
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
		v1 = Vehicle.create(n1, n2).setLocation(498);
		v2 = Vehicle.create(n3, n2).setLocation(498);
		final List<Vehicle> v14 = List.of(v1);
		final List<Vehicle> v34 = List.of(v2);
		t14 = EdgeTraffic.create(e14).setTime(49.8).setVehicles(v14);
		t34 = EdgeTraffic.create(e34).setTime(49.8).setVehicles(v34);
		t42 = EdgeTraffic.create(e42).setTime(49.8);
		traffics = Set.of(t14, t34, t42);
		final GeoMap map = GeoMap.create().setSites(Set.of(n1, n2, n3)).setNodes(Set.of(n1))
				.setEdges(Set.of(e14, e34, e42));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setTraffics(traffics);
		builder = StatusBuilder.create(status, TIME);
	}

	@Test
	public void testBuild() {
		final SimulationStatus result = builder.build();
		assertNotNull(result);
		final Set<EdgeTraffic> tr = result.getTraffic();
		assertNotNull(tr);

		final EdgeTraffic nt14 = tr.stream().filter(et -> et.equals(t14)).findFirst().get();
		assertThat(nt14.getTime(), equalTo(TIME));
		assertThat(nt14.getVehicles(), hasSize(1));
		assertThat(nt14.getVehicles(), hasItem(v1));
		assertThat(nt14.getLast().getLocation(), equalTo(500.0));

		final EdgeTraffic nt34 = tr.stream().filter(et -> et.equals(t34)).findFirst().get();
		assertThat(nt34.getTime(), equalTo(TIME));
		assertThat(nt34.getVehicles(), empty());

		final EdgeTraffic nt42 = tr.stream().filter(et -> et.equals(t42)).findFirst().get();
		assertThat(nt42.getTime(), equalTo(TIME));
		assertThat(nt42.getVehicles(), hasSize(1));
		assertThat(nt42.getVehicles(), hasItem(v2));
		assertThat(nt42.getLast().getLocation(), closeTo(2.0, 1e-3));

	}
}
