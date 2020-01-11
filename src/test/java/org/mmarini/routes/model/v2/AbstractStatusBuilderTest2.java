package org.mmarini.routes.model.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;

/**
 * Test build status at cross proximity
 */
public abstract class AbstractStatusBuilderTest2 {

	List<SiteNode> sites;
	List<MapNode> nodes;
	List<MapNode> nodesByIndex;
	List<MapEdge> edges;
	List<EdgeTraffic> traffics;
	GeoMap map;
	private Map<Tuple2<SiteNode, SiteNode>, Double> weights;

	MapNode allNode(final int i) {
		return nodesByIndex.get(i);
	}

	int allNodeIndex(final MapNode node) {
		return nodesByIndex.indexOf(node);
	}

	StatusBuilder createBuilder(final double time, final double limitTime,
			final BiFunction<EdgeTraffic, Integer, EdgeTraffic> trafficTransformer) {
		traffics = IntStream.range(0, edges.size()).mapToObj(i -> {
			final MapEdge edge = edges.get(i);
			final EdgeTraffic et = EdgeTraffic.create(edge).setTime(time);
			final EdgeTraffic result = trafficTransformer.apply(et, i);
			return result;
		}).collect(Collectors.toList());
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setTraffics(Set.copyOf(traffics))
				.setWeights(weights);
		return StatusBuilder.create(status, limitTime);
	}

	/**
	 * <pre>
	 * s0  --0--> n3 <--1--  s1
	 * s0 <--3--  n3  --4--> s1
	 *           ^
	 *           |  |
	 *           |  |
	 *           2  3
	 *           |  |
	 *           |  |
	 *              v
	 *            s2
	 * </pre>
	 */
	@BeforeEach
	public void createCase() {
		sites = List.of(SiteNode.create(0, 0), SiteNode.create(1000, 0), SiteNode.create(500, 500));
		nodes = List.of(MapNode.create(500, 0));
		edges = List.of(MapEdge.create(sites.get(0), nodes.get(0)).setSpeedLimit(10),
				MapEdge.create(sites.get(1), nodes.get(0)).setSpeedLimit(10),
				MapEdge.create(sites.get(2), nodes.get(0)).setSpeedLimit(10),
				MapEdge.create(nodes.get(0), sites.get(0)).setSpeedLimit(10),
				MapEdge.create(nodes.get(0), sites.get(1)).setSpeedLimit(10),
				MapEdge.create(nodes.get(0), sites.get(2)).setSpeedLimit(10));
		nodesByIndex = new ArrayList<>(sites);
		nodesByIndex.addAll(nodes);
		weights = sites.parallelStream().flatMap(from -> sites.parallelStream().map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), x -> 1.0));
		map = GeoMap.create().setSites(Set.copyOf(sites)).setNodes(Set.copyOf(nodes)).setEdges(Set.copyOf(edges));
	}

	MapEdge edge(final int i) {
		return edges.get(i);
	}

	int edgeIndex(final MapEdge edge) {
		return edges.indexOf(edge);
	}

	MapNode node(final int i) {
		return nodes.get(i);
	}

	int nodeIndex(final MapNode node) {
		return nodes.indexOf(node);
	}

	SiteNode site(final int i) {
		return sites.get(i);
	}

	int siteIndex(final SiteNode site) {
		return sites.indexOf(site);
	}

	/**
	 *
	 * @param traffics
	 * @param idx
	 * @return
	 */
	Optional<EdgeTraffic> traffic(final Collection<EdgeTraffic> traffics, final int idx) {
		return traffics.parallelStream().filter(et -> et.equals(traffic(idx))).findFirst();
	}

	EdgeTraffic traffic(final int i) {
		return traffics.get(i);
	}

	int trafficIndex(final EdgeTraffic traffic) {
		return traffics.indexOf(traffic);
	}
}
