package org.mmarini.routes.model.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Test build status at cross proximity
 *
 * <pre>
 * s0  --0--> n3 <--1--  s1
 * s0 <--3--  n3  --4--> s1
 *           ^
 *           |  |
 *           |  |
 *           2  5
 *           |  |
 *           |  |
 *              v
 *            s2
 * </pre>
 */
public abstract class AbstractStatusBuilderTest {

	List<SiteNode> sites;
	List<MapNode> nodes;
	List<MapNode> nodesByIndex;
	List<MapEdge> edges;
	List<EdgeTraffic> traffics;
	GeoMap map;
	private Map<Tuple2<SiteNode, SiteNode>, Double> weights;

	MapNode allNode(final int i) {
		final MapNode result = nodesByIndex.get(i);
		return result;
	}

	int allNodeIndex(final MapNode node) {
		final int result = nodesByIndex.indexOf(node);
		return result;
	}

	StatusBuilder createBuilder(final double time, final double limitTime,
			final BiFunction<EdgeTraffic, Integer, EdgeTraffic> trafficTransformer) {
		return createBuilder1(time, limitTime, this::createDefaultSites, this::createDefaultNodes,
				this::createDefaultEdges, this::createDefaultWeight, trafficTransformer);
	}

	StatusBuilder createBuilder1(final double time, final double limitTime, final Supplier<List<SiteNode>> sitesBuilder,
			final Supplier<List<MapNode>> nodesBuilder, final Supplier<List<MapEdge>> edgesBuilder,
			final ToDoubleBiFunction<SiteNode, SiteNode> weightBuilder,
			final BiFunction<EdgeTraffic, Integer, EdgeTraffic> trafficTransformer) {
		sites = sitesBuilder.get();
		nodes = nodesBuilder.get();
		nodesByIndex = new ArrayList<>(sites);
		nodesByIndex.addAll(nodes);
		edges = edgesBuilder.get();
		map = GeoMap.create().setSites(Set.copyOf(sites)).setNodes(Set.copyOf(nodes)).setEdges(Set.copyOf(edges));

		traffics = IntStream.range(0, edges.size()).mapToObj(i -> {
			final MapEdge edge = edges.get(i);
			final EdgeTraffic template = EdgeTraffic.create(edge).setTime(time);
			final EdgeTraffic result = trafficTransformer.apply(template, i);
			return result;
		}).collect(Collectors.toList());
		final Stream<Tuple2<SiteNode, SiteNode>> stream = sites.parallelStream()
				.flatMap(from -> sites.parallelStream().map(to -> new Tuple2<>(from, to)));
		weights = stream.collect(Collectors.toMap(Function.identity(), tuple -> {
			final double result = weightBuilder.applyAsDouble(tuple.getElem1(), tuple.getElem2());
			return result;
		}));

		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setTraffics(Set.copyOf(traffics))
				.setWeights(weights);
		return StatusBuilder.create(status, limitTime);

	}

	List<MapEdge> createDefaultEdges() {
		return List.of(MapEdge.create(allNode(0), allNode(3)).setSpeedLimit(10),
				MapEdge.create(allNode(1), allNode(3)).setSpeedLimit(10),
				MapEdge.create(allNode(2), allNode(3)).setSpeedLimit(10),
				MapEdge.create(allNode(3), allNode(0)).setSpeedLimit(10),
				MapEdge.create(allNode(3), allNode(1)).setSpeedLimit(10),
				MapEdge.create(allNode(3), allNode(2)).setSpeedLimit(10));
	}

	List<MapNode> createDefaultNodes() {
		return List.of(MapNode.create(500, 0));
	}

	List<SiteNode> createDefaultSites() {
		return List.of(SiteNode.create(0, 0), SiteNode.create(1000, 0), SiteNode.create(500, 500));
	}

	double createDefaultWeight(final SiteNode from, final SiteNode to) {
		return 1;
	}

	MapEdge edge(final int i) {
		final MapEdge result = edges.get(i);
		return result;
	}

	int edgeIndex(final MapEdge edge) {
		final int result = edges.indexOf(edge);
		return result;
	}

	MapNode node(final int i) {
		final MapNode result = nodes.get(i);
		return result;
	}

	int nodeIndex(final MapNode node) {
		final int result = nodes.indexOf(node);
		return result;
	}

	SiteNode site(final int i) {
		final SiteNode result = sites.get(i);
		return result;
	}

	int siteIndex(final SiteNode site) {
		final int result = sites.indexOf(site);
		return result;
	}

	Optional<EdgeTraffic> traffic(final Collection<EdgeTraffic> traffics, final int idx) {
		return traffics.parallelStream().filter(et -> et.equals(traffic(idx))).findFirst();
	}

	EdgeTraffic traffic(final int i) {
		final EdgeTraffic result = traffics.get(i);
		return result;
	}

	Optional<EdgeTraffic> traffic(final StatusBuilder builder, final int idx) {
		return traffic(builder.getTraffics(), idx);
	}

	int trafficIndex(final EdgeTraffic traffic) {
		final int result = traffics.indexOf(traffic);
		return result;
	}
}
