package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class GeoMapTest implements Constants {

	@Test
	public void addEdge() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(20, 20);

		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);

		final GeoMap map = GeoMap.create(Set.of(edge0));

		final GeoMap result1 = map.add(edge1);
		assertThat(result1.getEdges(), hasItem(edge0));
		assertThat(result1.getEdges(), hasItem(edge1));

		assertThat(result1.getNodes(), hasItem(node0));
		assertThat(result1.getNodes(), hasItem(node1));
		assertThat(result1.getNodes(), hasItem(node2));

		assertTrue(result1.getWeights().isEmpty());

		assertThat(result1.getSites(), empty());
	}

	@Test
	public void addEdgeTwice() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);

		final MapEdge edge0 = MapEdge.create(node0, node1);

		final GeoMap map = GeoMap.create(Set.of(edge0));

		final GeoMap result1 = map.add(edge0);
		assertThat(result1, sameInstance(map));
	}

	@Test
	public void changeNode0() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2));

		final GeoMap result = map.changeNode(node0, (a, b) -> 1.0);
		assertNotNull(result);
		assertThat(result.getSites(), contains(node0));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge0, edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(0));
	}

	@Test
	public void changeNode1() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), node1);

		final GeoMap result = map.changeNode(node0, (a, b) -> 1.0);
		assertNotNull(result);
		assertThat(result.getSites(), containsInAnyOrder(node0, node1));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge0, edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(2));
	}

	@Test
	public void changeNodeNoNode() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapNode node4 = MapNode.create(20, 25);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), node1);

		final GeoMap result = map.changeNode(node4, (a, b) -> 1.0);
		assertNotNull(result);
		assertThat(result, sameInstance(map));
	}

	@Test
	public void changeSite1() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), node0);

		final GeoMap result = map.changeNode(node0, (a, b) -> 1.0);
		assertNotNull(result);
		assertThat(result.getSites(), empty());
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge0, edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(0));
	}

	@Test
	public void changeSite2() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node0, node1);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node1, node0);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k10, 1.0);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), weights);

		final GeoMap result = map.changeNode(node0, (a, b) -> 1.0);
		assertNotNull(result);
		assertThat(result.getSites(), containsInAnyOrder(node1));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge0, edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(0));
	}

	@Test
	public void changeSite3() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node0, node1);
		final Tuple2<MapNode, MapNode> k02 = new Tuple2<>(node0, node2);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node1, node0);
		final Tuple2<MapNode, MapNode> k12 = new Tuple2<>(node1, node2);
		final Tuple2<MapNode, MapNode> k20 = new Tuple2<>(node2, node0);
		final Tuple2<MapNode, MapNode> k21 = new Tuple2<>(node2, node1);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k02, 1.0, k10, 1.0, k12, 1.0, k20, 1.0,
				k21, 1.0);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), weights);

		final GeoMap result = map.changeNode(node0, (a, b) -> 1.0);
		assertNotNull(result);
		assertThat(result.getSites(), containsInAnyOrder(node1, node2));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge0, edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(2));
		assertThat(result.getWeights(), hasKey(k12));
		assertThat(result.getWeights(), hasKey(k21));
	}

	@Test
	public void createEdgesAndSite() {
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node0 = MapNode.create(0, 0);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapNode site0 = MapNode.create(5, 5);
		final GeoMap map = GeoMap.create(Set.of(edge0), site0);

		assertNotNull(map);
		assertTrue(map.getWeights().isEmpty());
		assertThat(map.getSites(), contains(site0));
		assertThat(map.getEdges(), contains(edge0));
		assertThat(map.getNodes(), containsInAnyOrder(site0, node0, node1));
	}

	@Test
	public void createEdgesAndWeights() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapNode node2 = MapNode.create(5, 5);
		final MapNode node3 = MapNode.create(6, 6);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node2, node3);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node3, node2);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k10, 1.0);
		final GeoMap map = GeoMap.create(Set.of(edge0), weights);

		assertNotNull(map);
		assertThat(map.getWeights(), hasEntry(k01, Double.valueOf(1.0)));
		assertThat(map.getWeights(), hasEntry(k10, Double.valueOf(1.0)));
		assertThat(map.getSites(), contains(node2, node3));
		assertThat(map.getEdges(), contains(edge0));
		assertThat(map.getNodes(), containsInAnyOrder(node2, node3, node0, node1));
	}

	@Test
	public void createEdgesOnly() {
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node0 = MapNode.create(0, 0);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge0));

		assertNotNull(map);
		assertThat(map.getSites(), empty());
		assertTrue(map.getWeights().isEmpty());
		assertThat(map.getEdges(), contains(edge0));
		assertThat(map.getNodes(), containsInAnyOrder(node0, node1));
	}

	@Test
	public void createEmpty() {
		final GeoMap result = GeoMap.create();
		assertNotNull(result);
		assertThat(result.getSites(), empty());
		assertThat(result.getNodes(), empty());
		assertThat(result.getEdges(), empty());
		assertTrue(result.getWeights().isEmpty());
		assertThat(result.getFrequence(), equalTo(DEFAULT_FREQUENCE));
	}

	@Test
	public void findNearest() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge0));

		final Optional<MapNode> result = map.findNearst(new Point2D.Double(1, 1), 2);
		assertTrue(result.isPresent());
		assertThat(result.get(), equalTo(node0));

		final Optional<MapNode> result1 = map.findNearst(new Point2D.Double(1, 1), 1);
		assertFalse(result1.isPresent());

		final Optional<MapNode> result2 = map.findNearst(new Point2D.Double(1, 1), 20);
		assertTrue(result2.isPresent());
		assertThat(result2.get(), equalTo(node0));
	}

	@Test
	public void getEdge() {
		final MapNode node0 = MapNode.create(10, 10);
		final MapNode node1 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node1);
		final MapEdge edge2 = MapEdge.create(node1, node0);
		final GeoMap map = GeoMap.create(Set.of(edge0));

		final Optional<MapEdge> result = map.getEdge(edge1);
		assertTrue(result.isPresent());
		assertThat(result.get(), sameInstance(edge0));

		final Optional<MapEdge> result1 = map.getEdge(edge2);
		assertFalse(result1.isPresent());
	}

	@Test
	public void getNode() {
		final MapNode site = MapNode.create(0, 0);
		final MapNode node0 = MapNode.create(10, 10);
		final MapNode node1 = MapNode.create(20, 20);
		final MapEdge edge = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge), site);

		final Optional<MapNode> result0 = map.getNode(MapNode.create(10, 10));
		assertTrue(result0.isPresent());
		assertThat(result0.get(), sameInstance(node0));

		final Optional<MapNode> result1 = map.getNode(MapNode.create(20, 20));
		assertTrue(result1.isPresent());
		assertThat(result1.get(), sameInstance(node1));

		final Optional<MapNode> result2 = map.getNode(MapNode.create(0, 0));
		assertTrue(result2.isPresent());
		assertThat(result2.get(), sameInstance(site));
	}

	@Test
	public void getSite() {
		final MapNode site = MapNode.create(0, 0);
		final MapNode node0 = MapNode.create(10, 10);
		final MapNode node1 = MapNode.create(20, 20);
		final MapEdge edge = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge), site);

		final Optional<MapNode> result0 = map.getSite(MapNode.create(10, 10));
		assertFalse(result0.isPresent());

		final Optional<MapNode> result1 = map.getSite(MapNode.create(20, 20));
		assertFalse(result1.isPresent());

		final Optional<MapNode> result2 = map.getSite(MapNode.create(0, 0));
		assertTrue(result2.isPresent());
		assertThat(result2.get(), sameInstance(site));
	}

	@Test
	public void getWeight() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node0, node1);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node1, node0);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 0.4, k10, 0.7);
		final GeoMap map = GeoMap.create(Set.of(), weights);

		final double result = map.getWeight(node0, node1);

		assertThat(result, equalTo(0.4));
	}

	@Test
	public void random() {
		final Random random = new Random(1234);
		final MapProfile profile = new MapProfile(3, 100, 100, 0.5, 1);

		final GeoMap result = GeoMap.random(profile, random);

		assertNotNull(result);
		assertThat(result.getEdges(), empty());
		assertThat(result.getSites(), hasSize(3));
		final List<MapNode> sites = result.getSites().stream().sorted().collect(Collectors.toList());
		final MapNode s0 = sites.get(0);
		final MapNode s1 = sites.get(1);
		final MapNode s2 = sites.get(2);

		assertThat(result.getNodes(), hasSize(3));
		assertThat(result.getNodes(), containsInAnyOrder(s0, s1, s2));
		assertThat(result.getWeights().size(), equalTo(6));

		assertThat(result.getWeights(), hasKey(new Tuple2<>(s0, s1)));
		assertThat(result.getWeights(), hasKey(new Tuple2<>(s0, s2)));
		assertThat(result.getWeights(), hasKey(new Tuple2<>(s1, s0)));
		assertThat(result.getWeights(), hasKey(new Tuple2<>(s1, s2)));
		assertThat(result.getWeights(), hasKey(new Tuple2<>(s2, s0)));
		assertThat(result.getWeights(), hasKey(new Tuple2<>(s2, s1)));
	}

	@Test
	public void random10() {
		final Random random = new Random(1234);
		final MapProfile profile = new MapProfile(10, 100, 100, 0.5, 1);

		final GeoMap result = GeoMap.random(profile, random);

		assertNotNull(result);
		assertThat(result.getEdges(), empty());
		assertThat(result.getSites(), hasSize(10));

		assertThat(result.getNodes(), hasSize(10));
		assertThat(result.getWeights().size(), equalTo(10 * 10 - 10));
	}

	@Test
	public void randomWeights() {
		final Random random = MockRandomBuilder.range(0, 6).build();
		final Set<MapNode> sites = Set.of(MapNode.create(0, 0), MapNode.create(10, 10), MapNode.create(20, 20));
		final List<MapNode> orderedSites = sites.stream().sorted().collect(Collectors.toList());
		final MapNode n0 = orderedSites.get(0);
		final MapNode n1 = orderedSites.get(1);
		final MapNode n2 = orderedSites.get(2);

		final Map<Tuple2<MapNode, MapNode>, Double> result = GeoMap.buildWeights(sites,
				GeoMap.randomWeight(0.5, random));

		assertNotNull(result);
		assertThat(result.size(), equalTo(6));
		assertThat(result, hasEntry(new Tuple2<>(n0, n1), 0.5));
		assertThat(result, hasEntry(new Tuple2<>(n0, n2), 0.5 + 1.0 / 12.0));
		assertThat(result, hasEntry(new Tuple2<>(n1, n0), 0.5 + 2.0 / 12.0));
		assertThat(result, hasEntry(new Tuple2<>(n1, n2), 0.5 + 3.0 / 12.0));
		assertThat(result, hasEntry(new Tuple2<>(n2, n0), 0.5 + 4.0 / 12.0));
		assertThat(result, hasEntry(new Tuple2<>(n2, n1), 0.5 + 5.0 / 12.0));
	}

	@Test
	public void removeEdge() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge0), node0);

		final GeoMap result = map.remove(edge0);

		assertNotNull(result);
		assertThat(result.getSites(), contains(node0));
		assertThat(result.getNodes(), contains(node0));
		assertThat(result.getEdges(), empty());

		assertThat(result.remove(edge0), sameInstance(result));
	}

	@Test
	public void removeEdge2() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node0);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1), node0);

		final GeoMap result = map.remove(edge0);

		assertNotNull(result);
		assertThat(result.getSites(), contains(node0));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1));
		assertThat(result.getEdges(), contains(edge1));
	}

	@Test
	public void removeNodeFromMapNode() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1), node0);

		final GeoMap result = map.remove(node1);
		assertNotNull(result);
		assertThat(result.getSites(), contains(node0));
		assertThat(result.getEdges(), contains(edge1));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node2));
	}

	@Test
	public void removeNodeNotInMap() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1), node0);

		final GeoMap result = map.remove(MapNode.create(1, 2));
		assertNotNull(result);
		assertThat(result, sameInstance(map));
	}

	@Test
	public void removeSite() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1), node0);

		final GeoMap result = map.remove(node0);
		assertNotNull(result);
		assertThat(result.getSites(), empty());
		assertThat(result.getNodes(), containsInAnyOrder(node1, node2));
		assertThat(result.getEdges(), contains(edge1));
	}

	@Test
	public void removeSites() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node0, node1);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node1, node0);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k10, 1.0);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), weights);

		final GeoMap result = map.remove(node0);
		assertNotNull(result);
		assertThat(result.getSites(), contains(node1));
		assertTrue(result.getWeights().isEmpty());
		assertThat(result.getNodes(), containsInAnyOrder(node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge1, edge2));
	}

	@Test
	public void removeSites3() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node0, node1);
		final Tuple2<MapNode, MapNode> k02 = new Tuple2<>(node0, node2);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node1, node0);
		final Tuple2<MapNode, MapNode> k12 = new Tuple2<>(node1, node2);
		final Tuple2<MapNode, MapNode> k20 = new Tuple2<>(node2, node0);
		final Tuple2<MapNode, MapNode> k21 = new Tuple2<>(node2, node1);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k02, 1.0, k10, 1.0, k12, 1.0, k20, 1.0,
				k21, 1.0);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2), weights);

		final GeoMap result = map.remove(node0);
		assertNotNull(result);
		assertThat(result.getSites(), containsInAnyOrder(node1, node2));
		assertThat(result.getNodes(), containsInAnyOrder(node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(2));
		assertThat(result.getWeights(), hasKey(k12));
		assertThat(result.getWeights(), hasKey(k21));
	}

	@Test
	public void replaceEdge() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(20, 20);

		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final MapEdge edge2 = MapEdge.create(node0, node1).setPriority(1);

		final GeoMap map = GeoMap.create(Set.of(edge0, edge1));

		final GeoMap result1 = map.add(edge2);

		assertThat(result1.getEdges(), hasSize(2));
		assertThat(result1.getEdges(), hasItem(sameInstance(edge1)));
		assertThat(result1.getEdges(), hasItem(sameInstance(edge2)));

		assertThat(result1.getNodes(), hasItem(node0));
		assertThat(result1.getNodes(), hasItem(node1));

		assertTrue(result1.getWeights().isEmpty());

		assertThat(result1.getSites(), empty());
	}

	@Test
	public void replaceEdge1() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(20, 20);

		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node0, node2);
		final MapEdge edge2 = MapEdge.create(node0, node1).setSpeedLimit(10);

		final GeoMap map = GeoMap.create(Set.of(edge0, edge1));

		final GeoMap result1 = map.add(edge2);

		assertThat(result1.getEdges(), hasSize(2));
		assertThat(result1.getEdges(), hasItem(sameInstance(edge1)));
		assertThat(result1.getEdges(), hasItem(sameInstance(edge2)));

		assertThat(result1.getNodes(), hasItem(node0));
		assertThat(result1.getNodes(), hasItem(node1));

		assertTrue(result1.getWeights().isEmpty());

		assertThat(result1.getSites(), empty());
	}

	@Test
	public void setEdge() {
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node0 = MapNode.create(0, 0);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapNode site0 = MapNode.create(5, 5);
		final MapNode site1 = MapNode.create(6, 6);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(site0, site1);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(site1, site0);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k10, 1.0);
		final GeoMap map = GeoMap.create(Set.of(edge0), weights);
		final MapEdge edge1 = MapEdge.create(site0, site1);
		final GeoMap result = map.setEdges(Set.of(edge1));

		assertNotNull(result);
		assertThat(result.getWeights(), hasEntry(k01, Double.valueOf(1.0)));
		assertThat(result.getWeights(), hasEntry(k10, Double.valueOf(1.0)));
		assertThat(result.getSites(), contains(site0, site1));
		assertThat(result.getEdges(), contains(edge1));
		assertThat(result.getNodes(), containsInAnyOrder(site0, site1));
	}

	@Test
	public void setSite() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge0));
		final MapNode node2 = MapNode.create(20, 20);

		final GeoMap result = map.setSite(node2);

		assertNotNull(result);
		assertTrue(result.getWeights().isEmpty());
		assertThat(result.getSites(), contains(node2));
		assertThat(result.getEdges(), contains(edge0));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2));
	}

	@Test
	public void setWeight() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapNode node2 = MapNode.create(10, 20);
		final MapNode node3 = MapNode.create(20, 20);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final MapEdge edge1 = MapEdge.create(node1, node2);
		final MapEdge edge2 = MapEdge.create(node3, node2);
		final GeoMap map = GeoMap.create(Set.of(edge0, edge1, edge2));

		final GeoMap result = map.setWeight(node0, node1, 1);
		assertNotNull(result);
		assertThat(result.getSites(), containsInAnyOrder(node0, node1));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
		assertThat(result.getEdges(), containsInAnyOrder(edge0, edge1, edge2));
		assertThat(result.getWeights().size(), equalTo(1));
	}

	@Test
	public void setWeights() {
		final MapNode node0 = MapNode.create(0, 0);
		final MapNode node1 = MapNode.create(10, 10);
		final MapEdge edge0 = MapEdge.create(node0, node1);
		final GeoMap map = GeoMap.create(Set.of(edge0));
		final MapNode node2 = MapNode.create(5, 5);
		final MapNode node3 = MapNode.create(6, 6);
		final Tuple2<MapNode, MapNode> k01 = new Tuple2<>(node2, node3);
		final Tuple2<MapNode, MapNode> k10 = new Tuple2<>(node3, node2);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = Map.of(k01, 1.0, k10, 1.0);

		final GeoMap result = map.setWeights(weights);

		assertNotNull(result);
		assertThat(result.getWeights(), hasEntry(k01, Double.valueOf(1.0)));
		assertThat(result.getWeights(), hasEntry(k10, Double.valueOf(1.0)));
		assertThat(result.getSites(), contains(node2, node3));
		assertThat(result.getEdges(), contains(edge0));
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1, node2, node3));
	}
}
