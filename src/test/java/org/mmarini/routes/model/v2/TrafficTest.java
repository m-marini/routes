package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class TrafficTest implements Constants {

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n2
	 * s0 <-- 2 -- s1
	 * </pre>
	 *
	 * When addEdge s1 -- 3 --> n2<br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n2
	 * s0 <-- 2 -- s1 -- 3 --> n2
	 * </pre>
	 *
	 * and traffic on s1--> n2
	 */
	@Test
	public void addEdge() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(100, 0);
		final MapNode n2 = MapNode.create(200, 0);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(n2, s1);
		final MapEdge e2 = MapEdge.create(s1, s0);
		final MapEdge e3 = MapEdge.create(s1, n2);
		final Set<MapNode> sites = Set.of(s0, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Traffics status = Traffics.create(map);

		final Traffics result = status.addEdge(e3);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), containsInAnyOrder(s0, s1));

		assertThat(rMap.getNodes(), containsInAnyOrder(s0, s1, n2));

		final Set<EdgeTraffic> rTraffics = result.getTraffics();
		assertThat(rTraffics, hasSize(4));
		assertThat(rTraffics, hasItem(hasProperty("edge", equalTo(e3))));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- s1
	 * s0 <-- 2 -- s2 -- 3 --> s1
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to s1 at 0m<br>
	 * And from s0 to s2 at 10m<br>
	 * And from s1 to s0 at 20m<br>
	 * And from s1 to s2 at 30m<br>
	 * And from s2 to s0 at 40m<br>
	 * And from s2 to s1 at 50m<br>
	 *
	 * When change edge e3<br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- n1
	 * s0 <-- 2 -- s2 -- 3 --> n1
	 * </pre>
	 *
	 * And the traffic with the same 6 vehicles
	 */
	@Test
	public void changeEdge() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final MapNode s2 = MapNode.create(100, 0);
		final Set<MapNode> sites = Set.of(s0, s1, s2);
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s0, s2).setLocation(10);
			final Vehicle v2 = Vehicle.create(s1, s0).setLocation(20);
			final Vehicle v3 = Vehicle.create(s1, s2).setLocation(30);
			final Vehicle v4 = Vehicle.create(s2, s0).setLocation(40);
			final Vehicle v5 = Vehicle.create(s2, s1).setLocation(50);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1, v2, v3, v4, v5));
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);

		final MapEdge e31 = e3.setPriority(1).setSpeedLimit(20);

		final Traffics result = status.change(e31);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), containsInAnyOrder(s0, s1, s2));

		assertThat(rMap.getNodes(), containsInAnyOrder(s0, s1, s2));

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(e0));
		assertThat(rMap.getEdges(), hasItem(e1));
		assertThat(rMap.getEdges(), hasItem(e2));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("id", equalTo(e31.getId())),
				hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(20.0)))));
//				hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(20.0)))));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(6));
			assertThat(traffic.getVehicles().get(0).getLocation(), equalTo(0.0));
			assertThat(traffic.getVehicles().get(1).getLocation(), equalTo(10.0));
			assertThat(traffic.getVehicles().get(2).getLocation(), equalTo(20.0));
			assertThat(traffic.getVehicles().get(3).getLocation(), equalTo(30.0));
			assertThat(traffic.getVehicles().get(4).getLocation(), equalTo(40.0));
			assertThat(traffic.getVehicles().get(5).getLocation(), equalTo(50.0));
		});
	}

	/**
	 * Change not existing node
	 *
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> n2 <-- 1 -- s1
	 * s0 <-- 2 -- n2 -- 3 --> s1
	 * </pre>
	 *
	 * And two vehicles in each edge traffic from s0 to s1<br>
	 * And from s1 to s0<br>
	 *
	 * When changeNode n3 <br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> n2 <-- 1 -- s1
	 * s0 <-- 2 -- n2 -- 3 --> s1
	 * </pre>
	 *
	 * And no vehicles in any edge traffic
	 */
	@Test
	public void changeNodeNotExisting() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final MapNode n2 = MapNode.create(100, 0);
		final Set<MapNode> sites = Set.of(s0, s1);
		final MapEdge e0 = MapEdge.create(s0, n2);
		final MapEdge e1 = MapEdge.create(s1, n2);
		final MapEdge e2 = MapEdge.create(n2, s0);
		final MapEdge e3 = MapEdge.create(n2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights).setFrequence(DEFAULT_FREQUENCE);

		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final List<Vehicle> vehicles = List.of(Vehicle.create(s0, s1), Vehicle.create(s1, s0).setLocation(10));
			return EdgeTraffic.create(edge).setVehicles(vehicles);
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);
		final MapNode n3 = MapNode.create(10, 10);

		final Traffics result = status.changeNode(n3, (a, b) -> 1);

		assertNotNull(result);
		assertThat(result, sameInstance(status));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> n2 <-- 1 -- s1
	 * s0 <-- 2 -- n2 -- 3 --> s1
	 * </pre>
	 *
	 * And two vehicles in each edge traffic from s0 to s1<br>
	 * And from s1 to s0 at 10m<br>
	 *
	 * When changeNode s1<br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> n2 <-- 1 -- n1
	 * s0 <-- 2 -- n2 -- 3 --> n1
	 * </pre>
	 *
	 * And a vehicle from s1 to s0 in any edge traffic at 10m
	 */
	@Test
	public void changeNodetoOneSite() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final Set<MapNode> sites = Set.of(s0, s1);
		final MapNode n2 = MapNode.create(100, 0);
		final MapEdge e0 = MapEdge.create(s0, n2);
		final MapEdge e1 = MapEdge.create(s1, n2);
		final MapEdge e2 = MapEdge.create(n2, s0);
		final MapEdge e3 = MapEdge.create(n2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream()
				.map(edge -> EdgeTraffic.create(edge)
						.setVehicles(List.of(Vehicle.create(s0, s1), Vehicle.create(s1, s0).setLocation(10))))
				.collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);

		final Traffics result = status.changeNode(s1, (a, b) -> 1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		final MapNode n1 = MapNode.create(200, 0);
		assertThat(rMap.getSites(), containsInAnyOrder(s0));
		assertThat(rMap.getNodes(), containsInAnyOrder(s0, n1, n2));

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(n2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n1)), hasProperty("end", equalTo(n2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n2)), hasProperty("end", equalTo(n1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n2)), hasProperty("end", equalTo(s0)))));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(1));
			assertThat(traffic.getVehicles(), hasItem(hasProperty("location", equalTo(10.0))));
		});
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n2
	 * s0 <-- 2 -- s1 -- 3 --> n2
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to s1 at 0m<br>
	 * And from s1 to s0 at 10m<br>
	 *
	 * When changeNode n2<br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- s2
	 * s0 <-- 2 -- s1 -- 3 --> s2
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s1 at 0m<br>
	 * And from s1 to s0 at 10m <br>
	 */
	@Test
	public void changeNodeToSite() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(100, 0);
		final MapNode n2 = MapNode.create(200, 0);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(n2, s1);
		final MapEdge e2 = MapEdge.create(s1, s0);
		final MapEdge e3 = MapEdge.create(s1, n2);
		final Set<MapNode> sites = Set.of(s0, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s1, s0).setLocation(10);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1));
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);

		final Traffics result = status.changeNode(n2, (a, b) -> 1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		final MapNode s2 = MapNode.create(200, 0);
		assertThat(rMap.getSites(), hasSize(3));
		assertThat(rMap.getSites(), hasItem(s0));
		assertThat(rMap.getSites(), hasItem(s1));
		assertThat(rMap.getSites(), hasItem(s2));

		assertThat(rMap.getNodes(), containsInAnyOrder(s0, s1, s2));

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s0)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s1)))));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(2));
			assertThat(traffic.getVehicles().get(0).getLocation(), equalTo(0.0));
			assertThat(traffic.getVehicles().get(1).getLocation(), equalTo(10.0));
		});
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- s1
	 * s0 <-- 2 -- s2 -- 3 --> s1
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to s1 at 0m<br>
	 * And from s0 to s2 at 10m<br>
	 * And from s1 to s0 at 20m<br>
	 * And from s1 to s2 at 30m<br>
	 * And from s2 to s0 at 40m<br>
	 * And from s2 to s1 at 50m<br>
	 *
	 * When changeNode s1<br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- n1
	 * s0 <-- 2 -- s2 -- 3 --> n1
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s2 at 10m<br>
	 * And from s2 to s0 at 40m <br>
	 */
	@Test
	public void changeSiteToNode() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final MapNode s2 = MapNode.create(100, 0);
		final Set<MapNode> sites = Set.of(s0, s1, s2);
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s0, s2).setLocation(10);
			final Vehicle v2 = Vehicle.create(s1, s0).setLocation(20);
			final Vehicle v3 = Vehicle.create(s1, s2).setLocation(30);
			final Vehicle v4 = Vehicle.create(s2, s0).setLocation(40);
			final Vehicle v5 = Vehicle.create(s2, s1).setLocation(50);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1, v2, v3, v4, v5));
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);

		final Traffics result = status.changeNode(s1, (a, b) -> 1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), containsInAnyOrder(s0, s2));

		final MapNode n1 = MapNode.create(200, 0);
		assertThat(rMap.getNodes(), containsInAnyOrder(s0, n1, s2));

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(e0));
		assertThat(rMap.getEdges(), hasItem(e1));
		assertThat(rMap.getEdges(), hasItem(e2));
		assertThat(rMap.getEdges(), hasItem(e3));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(4));
			assertThat(traffic.getVehicles().get(0).getLocation(), equalTo(10.0));
			assertThat(traffic.getVehicles().get(1).getLocation(), equalTo(20.0));
			assertThat(traffic.getVehicles().get(2).getLocation(), equalTo(30.0));
			assertThat(traffic.getVehicles().get(3).getLocation(), equalTo(40.0));
		});
	}

	@Test
	public void create() {
		final Traffics result = Traffics.create();
		assertNotNull(result);
		assertThat(result.getTraffics(), empty());
		assertNotNull(result.getMap());
	}

	/**
	 * Given a map
	 *
	 * <pre>
	 * s0 -- 0 --> n2 <-- 1 -- s1
	 * s0 <-- 2 -- n2 -- 3 --> s1
	 * </pre>
	 *
	 * When create a simulation status
	 *
	 * Then the status should have empty traffic in the edge
	 *
	 */
	@Test
	public void createMap() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final MapNode n2 = MapNode.create(100, 0);
		final Set<MapNode> sites = Set.of(s0, s1);
		final MapEdge e0 = MapEdge.create(s0, n2);
		final MapEdge e1 = MapEdge.create(s1, n2);
		final MapEdge e2 = MapEdge.create(n2, s0);
		final MapEdge e3 = MapEdge.create(n2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights).setFrequence(DEFAULT_FREQUENCE);

		final Traffics result = Traffics.create(map);

		assertNotNull(result);
		assertThat(result.getTraffics(), hasSize(4));

		result.getTraffics().forEach(traffic -> {
			assertThat(traffic.getVehicles(), empty());
		});
	}

	@Test
	public void getTime() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setVehicles(List.of(Vehicle.create(begin, end)))
				.setTime(10);
		final Set<EdgeTraffic> vehicles = Set.of(traffic);
		final Traffics s = Traffics.create().setTraffics(vehicles);

		final double result = s.getTime();

		assertThat(result, equalTo(10.0));
	}

	@Test
	public void nextPoisson() {
		final Traffics s = Traffics.create().setRandom(new Random(1234));
		int tot = 0;
		final int n = 100000;
		final double lambda = 10;
		for (int i = 0; i < n; i++) {
			tot += s.nextPoison(lambda);
		}
		assertThat((double) tot / n, closeTo(lambda, lambda * 0.01));
	}

	/**
	 * Given a map profile with 3 sites<br>
	 * and minWeight=0.5<br>
	 * and size 100m x 100m<br>
	 * and a random generator with 6 values for coordinates<br>
	 * and 6 values for weights
	 *
	 * When create a random simulation status
	 *
	 * Then the status should have 3 sites<br>
	 * and 6 weights
	 */
	@Test
	public void random() {
		final MapProfile profile = new MapProfile(3, 100, 100, 0.5, 1);
		final Random random = MockRandomBuilder.range(1, 3 * 2).concat(MockRandomBuilder.range(1, 6)).build();

		final Traffics result = Traffics.random(profile, random);

		assertNotNull(result);
		assertThat(result.getTraffics(), empty());

		final GeoMap map = result.getMap();
		assertThat(map.getSites(), hasSize(3));
		map.getSites().forEach(s -> {
			assertThat(s.getX(), greaterThanOrEqualTo(0.0));
			assertThat(s.getX(), lessThanOrEqualTo(100.0));
			assertThat(s.getY(), greaterThanOrEqualTo(0.0));
			assertThat(s.getY(), lessThanOrEqualTo(100.0));
		});

		final Map<Tuple2<MapNode, MapNode>, Double> weights = result.getMap().getWeights();
		assertThat(weights.size(), equalTo(6));
		assertThat(weights, hasValue(0.5));
		assertThat(weights, hasValue(0.5 + 1.0 / 12));
		assertThat(weights, hasValue(0.5 + 2.0 / 12));
		assertThat(weights, hasValue(0.5 + 3.0 / 12));
		assertThat(weights, hasValue(0.5 + 4.0 / 12));
		assertThat(weights, hasValue(0.5 + 5.0 / 12));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> n2 <-- 1 -- s1
	 * s0 <-- 2 -- n2 -- 3 --> s1
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to s1 at 0m<br>
	 * And from s0 to s2 at 10m<br>
	 * And from s1 to s0 at 20m<br>
	 * And from s1 to s2 at 30m<br>
	 * And from s2 to s0 at 40m<br>
	 * And from s2 to s1 at 50m<br>
	 *
	 * When remove e0<br>
	 *
	 * Then the map should be
	 *
	 * <pre>
	 *             s2 <-- 1 -- n0
	 * s0 <-- 2 -- s2 -- 3 --> n0
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s2 at 10m<br>
	 * And from s2 to s0 at 40m <br>
	 */
	@Test
	public void removeAnEdge() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode n1 = MapNode.create(200, 0);
		final MapNode s2 = MapNode.create(100, 0);
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(n1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, n1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Set<MapNode> sites = Set.of(s0, s2);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);

		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, n1);
			final Vehicle v1 = Vehicle.create(s0, s2).setLocation(10);
			final Vehicle v2 = Vehicle.create(n1, s0).setLocation(20);
			final Vehicle v3 = Vehicle.create(n1, s2).setLocation(30);
			final Vehicle v4 = Vehicle.create(s2, s0).setLocation(40);
			final Vehicle v5 = Vehicle.create(s2, n1).setLocation(50);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1, v2, v3, v4, v5));
		}).collect(Collectors.toSet());

		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);

		final Traffics result = status.removeEdge(e0);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), containsInAnyOrder(s0, s2));

		assertThat(rMap.getNodes(), containsInAnyOrder(s0, n1, s2));

		assertThat(rMap.getEdges(), hasSize(3));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n1)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(n1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s0)))));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(3));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n2
	 * s0 <-- 2 -- s1 -- 3 --> n2
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to n1 at 0m<br>
	 * And from s1 to s0 at 10m<br>
	 *
	 * When removeNode n2<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s1
	 * s0 <-- 2 -- s1
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s1 at 0m<br>
	 * And from s1 to s0 at 10m <br>
	 */
	@Test
	public void removeNode() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final Set<MapNode> sites = Set.of(s0, s1);
		final MapNode n0 = MapNode.create(100, 0);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(n0, s1);
		final MapEdge e2 = MapEdge.create(s1, s0);
		final MapEdge e3 = MapEdge.create(s1, n0);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s1, s0).setLocation(10);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1));
		}).collect(Collectors.toSet());
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics);

		final Traffics result = status.removeNode(n0);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), hasSize(2));
		assertThat(rMap.getSites(), hasItems(s0, s1));

		assertThat(rMap.getNodes(), containsInAnyOrder(s0, s1));

		assertThat(rMap.getEdges(), hasSize(2));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s0)))));

		final Set<EdgeTraffic> rTraffics = result.getTraffics();
		assertThat(rTraffics, hasSize(2));
		rTraffics.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(2));
			assertThat(traffic.getVehicles(), hasItem(allOf(hasProperty("departure", equalTo(s0)),
					hasProperty("destination", equalTo(s1)), hasProperty("location", equalTo(0.0)))));
			assertThat(traffic.getVehicles(), hasItem(allOf(hasProperty("departure", equalTo(s1)),
					hasProperty("destination", equalTo(s0)), hasProperty("location", equalTo(10.0)))));
		});
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n2
	 * s0 <-- 2 -- s1 -- 3 --> n2
	 * </pre>
	 *
	 * When removeNode n3<br>
	 *
	 * Then the status should be the same
	 */
	@Test
	public void removeNodeNotExist() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final MapNode n2 = MapNode.create(100, 0);
		final MapNode n3 = MapNode.create(10, 10);
		final Set<MapNode> sites = Set.of(s0, s1);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(n2, s1);
		final MapEdge e2 = MapEdge.create(s1, s0);
		final MapEdge e3 = MapEdge.create(s1, n2);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s1, s0).setLocation(10);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1));
		}).collect(Collectors.toSet());
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics);

		final Traffics result = status.removeNode(n3);
		assertThat(result, sameInstance(status));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- s1
	 * s0 <-- 2 -- s2 -- 3 --> s1
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to s1 at 0m<br>
	 * And from s0 to s2 at 10m<br>
	 * And from s1 to s0 at 20m<br>
	 * And from s1 to s2 at 30m<br>
	 * And from s2 to s0 at 40m<br>
	 * And from s2 to s1 at 50m<br>
	 * When removeNode s1<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s2
	 * s0 <-- 1 -- s2
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s2 at 10m<br>
	 * And from s2 to s0 at 40m <br>
	 */
	@Test
	public void removeSite() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(200, 0);
		final MapNode s2 = MapNode.create(100, 0);
		final Set<MapNode> sites = Set.of(s0, s1, s2);
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s0, s2).setLocation(10);
			final Vehicle v2 = Vehicle.create(s1, s0).setLocation(20);
			final Vehicle v3 = Vehicle.create(s1, s2).setLocation(30);
			final Vehicle v4 = Vehicle.create(s2, s0).setLocation(40);
			final Vehicle v5 = Vehicle.create(s2, s1).setLocation(50);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1, v2, v3, v4, v5));
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(traffics).setRandom(random);

		final Traffics result = status.removeNode(s1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), hasSize(2));
		assertThat(rMap.getSites(), containsInAnyOrder(s0, s2));

		assertThat(rMap.getNodes(), containsInAnyOrder(s0, s2));

		assertThat(rMap.getEdges(), hasSize(2));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s0)))));

		final Set<EdgeTraffic> rTraffics = result.getTraffics();
		assertThat(rTraffics, hasSize(2));
		rTraffics.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(2));
			assertThat(traffic.getVehicles(), hasItem(allOf(hasProperty("departure", equalTo(s0)),
					hasProperty("destination", equalTo(s2)), hasProperty("location", equalTo(10.0)))));
			assertThat(traffic.getVehicles(), hasItem(allOf(hasProperty("departure", equalTo(s2)),
					hasProperty("destination", equalTo(s0)), hasProperty("location", equalTo(40.0)))));
		});

	}

	@Test
	public void setGeoMap() {
		final GeoMap map = GeoMap.create(Set.of(), MapNode.create(0, 0));
		final Traffics s = Traffics.create().setGeoMap(map);
		assertNotNull(s);
		assertThat(s.getTraffics(), empty());
		assertThat(s.getMap(), sameInstance(map));
	}

	@Test
	public void setTraffics() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Set<EdgeTraffic> vehicles = Set
				.of(EdgeTraffic.create(edge).setVehicles(List.of(Vehicle.create(begin, end))));
		final Traffics status = Traffics.create();

		final Traffics result = status.setTraffics(vehicles);

		assertNotNull(result);

		assertThat(result.getTraffics(), equalTo(vehicles));
	}
}
