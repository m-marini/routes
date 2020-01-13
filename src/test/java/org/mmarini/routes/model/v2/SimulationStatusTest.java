package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class SimulationStatusTest implements Constants {

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> n0 <-- 1 -- s1
	 * s0 <-- 2 -- n0 -- 3 --> s1
	 * </pre>
	 *
	 * And two vehicles in each edge traffic from s0 to s1<br>
	 * And from s1 to s0<br>
	 * When changeNode no node<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> n0 <-- 1 -- n1
	 * s0 <-- 2 -- n0 -- 3 --> n1
	 * </pre>
	 *
	 * And no vehicles in any edge traffic
	 */
	@Test
	public void changeNodeNoNode() {
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final MapNode n0 = MapNode.create(100, 0);
		final Set<MapNode> nodes = Set.of(n0);
		final MapEdge e0 = MapEdge.create(s0, n0);
		final MapEdge e1 = MapEdge.create(s1, n0);
		final MapEdge e2 = MapEdge.create(n0, s0);
		final MapEdge e3 = MapEdge.create(n0, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream()
				.map(edge -> EdgeTraffic.create(edge)
						.setVehicles(List.of(Vehicle.create(s0, s1), Vehicle.create(s1, s0).setLocation(10))))
				.collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final MapNode s2 = MapNode.create(10, 10);
		final SimulationStatus result = status.changeNode(s2);
		assertNotNull(result);
		assertThat(result, sameInstance(status));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> n0 <-- 1 -- s1
	 * s0 <-- 2 -- n0 -- 3 --> s1
	 * </pre>
	 *
	 * And two vehicles in each edge traffic from s0 to s1<br>
	 * And from s1 to s0<br>
	 * When changeNode no site<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> n0 <-- 1 -- n1
	 * s0 <-- 2 -- n0 -- 3 --> n1
	 * </pre>
	 *
	 * And no vehicles in any edge traffic
	 */
	@Test
	public void changeNodeNoSite() {
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final MapNode n0 = MapNode.create(100, 0);
		final Set<MapNode> nodes = Set.of(n0);
		final MapEdge e0 = MapEdge.create(s0, n0);
		final MapEdge e1 = MapEdge.create(s1, n0);
		final MapEdge e2 = MapEdge.create(n0, s0);
		final MapEdge e3 = MapEdge.create(n0, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream()
				.map(edge -> EdgeTraffic.create(edge)
						.setVehicles(List.of(Vehicle.create(s0, s1), Vehicle.create(s1, s0).setLocation(10))))
				.collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SiteNode s2 = SiteNode.create(10, 10);
		final SimulationStatus result = status.changeNode(s2);
		assertNotNull(result);
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
	 * When changeNode s1<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- n0
	 * s0 <-- 2 -- s2 -- 3 --> n0
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s2 at 10m<br>
	 * And from s2 to s0 at 40m <br>
	 */
	@Test
	public void changeNodeToNode() {
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final SiteNode s2 = SiteNode.create(100, 0);
		final Set<SiteNode> sites = Set.of(s0, s1, s2);
		final Set<MapNode> nodes = Set.of();
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
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
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SimulationStatus result = status.changeNode(s1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), contains(s0, s2));

		final MapNode n0 = MapNode.create(200, 0);
		assertThat(rMap.getNodes(), contains(n0));

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n0)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(n0)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s0)))));

		final Map<Tuple2<SiteNode, SiteNode>, Double> rW = result.getWeights();
		assertThat(rW.keySet(), hasSize(2));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(2));
			assertThat(traffic.getVehicles().get(0).getLocation(), equalTo(10.0));
			assertThat(traffic.getVehicles().get(1).getLocation(), equalTo(40.0));
		});
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> n0 <-- 1 -- s1
	 * s0 <-- 2 -- n0 -- 3 --> s1
	 * </pre>
	 *
	 * And two vehicles in each edge traffic from s0 to s1<br>
	 * And from s1 to s0<br>
	 * When changeNode s1<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> n0 <-- 1 -- n1
	 * s0 <-- 2 -- n0 -- 3 --> n1
	 * </pre>
	 *
	 * And no vehicles in any edge traffic
	 */
	@Test
	public void changeNodetoOneSite() {
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final MapNode n0 = MapNode.create(100, 0);
		final Set<MapNode> nodes = Set.of(n0);
		final MapEdge e0 = MapEdge.create(s0, n0);
		final MapEdge e1 = MapEdge.create(s1, n0);
		final MapEdge e2 = MapEdge.create(n0, s0);
		final MapEdge e3 = MapEdge.create(n0, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream()
				.map(edge -> EdgeTraffic.create(edge)
						.setVehicles(List.of(Vehicle.create(s0, s1), Vehicle.create(s1, s0).setLocation(10))))
				.collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SimulationStatus result = status.changeNode(s1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), contains(s0));
		final MapNode n1 = MapNode.create(200, 0);
		assertThat(rMap.getNodes(), contains(n0, n1));

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(n0)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n1)), hasProperty("end", equalTo(n0)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n0)), hasProperty("end", equalTo(n1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(n0)), hasProperty("end", equalTo(s0)))));

		final Map<Tuple2<SiteNode, SiteNode>, Double> rW = result.getWeights();
		assertThat(rW.keySet(), hasSize(0));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> assertThat(traffic.getVehicles(), empty()));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n0
	 * s0 <-- 2 -- s1 -- 3 --> n0
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to s1 at 0m<br>
	 * And from s1 to s0 at 10m<br>
	 * When changeNode n0<br>
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
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(100, 0);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final MapNode n0 = MapNode.create(200, 0);
		final Set<MapNode> nodes = Set.of(n0);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(n0, s1);
		final MapEdge e2 = MapEdge.create(s1, s0);
		final MapEdge e3 = MapEdge.create(s1, n0);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s1, s0).setLocation(10);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1));
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SimulationStatus result = status.changeNode(n0);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		final SiteNode s2 = SiteNode.create(200, 0);
		assertThat(rMap.getSites(), hasSize(3));
		assertThat(rMap.getSites(), hasItem(s0));
		assertThat(rMap.getSites(), hasItem(s1));
		assertThat(rMap.getSites(), hasItem(s2));

		assertThat(rMap.getNodes(), empty());

		assertThat(rMap.getEdges(), hasSize(4));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s0)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s1)))));

		final Map<Tuple2<SiteNode, SiteNode>, Double> rW = result.getWeights();
		assertThat(rW.keySet(), hasSize(6));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(4));
		rTraffic.forEach(traffic -> {
			assertThat(traffic.getVehicles(), hasSize(2));
			assertThat(traffic.getVehicles().get(0).getLocation(), equalTo(0.0));
			assertThat(traffic.getVehicles().get(1).getLocation(), equalTo(10.0));
		});
	}

	@Test
	public void nextPoisson() {
		final SimulationStatus s = SimulationStatus.create().setRandom(new Random(1234));
		int tot = 0;
		final int n = 100000;
		final double lambda = 10;
		for (int i = 0; i < n; i++) {
			tot += s.nextPoison(lambda);
		}
		assertThat((double) tot / n, closeTo(lambda, lambda * 0.01));
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
	 * When changeNode s1<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s2 <-- 1 -- n0
	 * s0 <-- 2 -- s2 -- 3 --> n0
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s2 at 10m<br>
	 * And from s2 to s0 at 40m <br>
	 */
	@Test
	public void removeAnEdge() {
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final SiteNode s2 = SiteNode.create(100, 0);
		final Set<SiteNode> sites = Set.of(s0, s1, s2);
		final Set<MapNode> nodes = Set.of();
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
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
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SimulationStatus result = status.removeEdge(e0);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), sameInstance(sites));

		assertThat(rMap.getNodes(), empty());

		assertThat(rMap.getEdges(), hasSize(3));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s0)))));

		final Map<Tuple2<SiteNode, SiteNode>, Double> rW = result.getWeights();
		assertThat(rW, sameInstance(weights));

		final Set<EdgeTraffic> rTraffic = result.getTraffics();
		assertThat(rTraffic, hasSize(3));
	}

	/**
	 * Given a simulation status with a map
	 *
	 * <pre>
	 * s0 -- 0 --> s1 <-- 1 -- n0
	 * s0 <-- 2 -- s1 -- 3 --> n0
	 * </pre>
	 *
	 * And 6 vehicles in each edge traffic from s0 to n1 at 0m<br>
	 * And from s1 to s0 at 10m<br>
	 * When removeNode s1<br>
	 * Then the map should be
	 *
	 * <pre>
	 * s0 -- 0 --> s1
	 * s0 <-- 1 -- s1
	 * </pre>
	 *
	 * And 2 vehicles in any edge traffic from s0 to s1 at 0m<br>
	 * And from s1 to s0 at 10m <br>
	 */
	@Test
	public void removeNode() {
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final MapNode n0 = MapNode.create(100, 0);
		final Set<MapNode> nodes = Set.of(n0);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(n0, s1);
		final MapEdge e2 = MapEdge.create(s1, s0);
		final MapEdge e3 = MapEdge.create(s1, n0);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream().map(edge -> {
			final Vehicle v0 = Vehicle.create(s0, s1);
			final Vehicle v1 = Vehicle.create(s1, s0).setLocation(10);
			return EdgeTraffic.create(edge).setVehicles(List.of(v0, v1));
		}).collect(Collectors.toSet());
		final Random random = new Random(1234);
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SimulationStatus result = status.removeNode(n0);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), hasSize(2));
		assertThat(rMap.getSites(), hasItems(s0, s1));

		assertThat(rMap.getNodes(), empty());

		assertThat(rMap.getEdges(), hasSize(2));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s1)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s1)), hasProperty("end", equalTo(s0)))));

		final Map<Tuple2<SiteNode, SiteNode>, Double> rW = result.getWeights();
		assertThat(rW.keySet(), hasSize(2));
		assertThat(rW, hasKey(new Tuple2<>(s0, s1)));
		assertThat(rW, hasKey(new Tuple2<>(s1, s0)));

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
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(200, 0);
		final SiteNode s2 = SiteNode.create(100, 0);
		final Set<SiteNode> sites = Set.of(s0, s1, s2);
		final Set<MapNode> nodes = Set.of();
		final MapEdge e0 = MapEdge.create(s0, s2);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final MapEdge e2 = MapEdge.create(s2, s0);
		final MapEdge e3 = MapEdge.create(s2, s1);
		final Set<MapEdge> edges = Set.of(e0, e1, e2, e3);
		final GeoMap map = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
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
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = sites.parallelStream()
				.flatMap(
						from -> sites.parallelStream().filter(to -> !from.equals(to)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> 1.0));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setFrequence(DEFAULT_FREQUENCE)
				.setTraffics(traffics).setRandom(random).setWeights(weights);

		final SimulationStatus result = status.removeNode(s1);
		assertNotNull(result);

		final GeoMap rMap = result.getMap();
		assertThat(rMap.getSites(), hasSize(2));
		assertThat(rMap.getSites(), hasItems(s0, s2));

		assertThat(rMap.getNodes(), empty());

		assertThat(rMap.getEdges(), hasSize(2));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s0)), hasProperty("end", equalTo(s2)))));
		assertThat(rMap.getEdges(), hasItem(allOf(hasProperty("begin", equalTo(s2)), hasProperty("end", equalTo(s0)))));

		final Map<Tuple2<SiteNode, SiteNode>, Double> rW = result.getWeights();
		assertThat(rW.keySet(), hasSize(2));
		assertThat(rW, hasKey(new Tuple2<>(s0, s2)));
		assertThat(rW, hasKey(new Tuple2<>(s2, s0)));

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
	public void test() {
		final SimulationStatus s = SimulationStatus.create();
		assertThat(s, notNullValue());
		assertThat(s.getTraffics(), empty());
		assertThat(s.getMap(), notNullValue());
		assertThat(s.getFrequence(), equalTo(DEFAULT_FREQUENCE));
	}

	@Test
	public void testAddWeight() {
		final SiteNode s1 = SiteNode.create(0, 0);
		final SiteNode s2 = SiteNode.create(0, 10);
		final SimulationStatus map = SimulationStatus.create().addWeight(s1, s2, 1);
		assertThat(map.getWeight(s1, s2), equalTo(1.0));
	}

	@Test
	public void testSetFrequence() {
		final SimulationStatus s = SimulationStatus.create().setFrequence(2.0);
		assertThat(s.getFrequence(), equalTo(2.0));
	}

	@Test
	public void testSetGeoMap() {
		final GeoMap map = GeoMap.create().add(SiteNode.create(0, 0));
		final SimulationStatus s = SimulationStatus.create().setGeoMap(map);
		assertThat(s, notNullValue());
		assertThat(s.getTraffics(), empty());
		assertThat(s.getMap(), equalTo(map));
	}

	@Test
	public void testSetTraffics() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final Set<EdgeTraffic> vehicles = Set
				.of(EdgeTraffic.create(edge).setVehicles(List.of(Vehicle.create(begin, end))));
		final SimulationStatus s = SimulationStatus.create().setTraffics(vehicles);
		assertThat(s, notNullValue());
		assertThat(s.getTraffics(), equalTo(vehicles));
	}
}
