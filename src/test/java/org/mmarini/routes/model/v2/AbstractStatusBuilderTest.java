package org.mmarini.routes.model.v2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
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
	List<MapNode> nodes;
	List<MapEdge> edges;
	List<EdgeTraffic> traffics;
	GeoMap map;
	private Map<Tuple2<MapNode, MapNode>, Double> weights;

	/**
	 *
	 * @param time
	 * @param limitTime
	 * @param nodesBuilder
	 * @param siteBuilder
	 * @param edgesBuilder
	 * @param weightBuilder
	 * @param trafficTransformer
	 * @return
	 */
	TrafficBuilder createBuilder(final double time, final double limitTime,
			final Supplier<Stream<MapNode>> nodesBuilder, final Supplier<IntStream> siteBuilder,
			final Supplier<Stream<MapEdge>> edgesBuilder, final ToDoubleBiFunction<MapNode, MapNode> weightBuilder,
			final BiFunction<EdgeTraffic, Integer, EdgeTraffic> trafficTransformer) {
		nodes = nodesBuilder.get().collect(Collectors.toList());
		final Set<MapNode> sites = siteBuilder.get().mapToObj(nodes::get).collect(Collectors.toSet());
		edges = edgesBuilder.get().collect(Collectors.toList());
		weights = GeoMap.buildWeights(sites, weightBuilder);
		if (sites.size() == 1) {
			final MapNode site = sites.stream().findAny().get();
			map = GeoMap.create(Set.copyOf(edges), site);
		} else {
			map = GeoMap.create(Set.copyOf(edges), weights);
		}

		traffics = IntStream.range(0, edges.size()).mapToObj(i -> {
			final MapEdge edge = edges.get(i);
			final EdgeTraffic template = EdgeTraffic.create(edge).setTime(time);
			final EdgeTraffic result = trafficTransformer.apply(template, i);
			return result;
		}).collect(Collectors.toList());

		final Traffics status = Traffics.create().setGeoMap(map).setTraffics(Set.copyOf(traffics));
		return TrafficBuilder.create(status, limitTime);
	}

	/**
	 *
	 * @param time
	 * @param limitTime
	 * @param trafficTransformer
	 * @return
	 */
	TrafficBuilder createDefaultBuilder(final double time, final double limitTime,
			final BiFunction<EdgeTraffic, Integer, EdgeTraffic> trafficTransformer) {
		return createBuilder(time, limitTime, this::createDefaultNodes, this::createDefaultSites,
				this::createDefaultEdges, this::createDefaultWeight, trafficTransformer);
	}

	Stream<MapEdge> createDefaultEdges() {
		return Stream.of(MapEdge.create(node(0), node(3)).setSpeedLimit(10),
				MapEdge.create(node(1), node(3)).setSpeedLimit(10), MapEdge.create(node(2), node(3)).setSpeedLimit(10),
				MapEdge.create(node(3), node(0)).setSpeedLimit(10), MapEdge.create(node(3), node(1)).setSpeedLimit(10),
				MapEdge.create(node(3), node(2)).setSpeedLimit(10));
	}

	Stream<MapNode> createDefaultNodes() {
		return Stream.of(MapNode.create(0, 0), MapNode.create(1000, 0), MapNode.create(500, 500),
				MapNode.create(500, 0));
	}

	IntStream createDefaultSites() {
		return IntStream.range(0, 3);
	}

	double createDefaultWeight(final MapNode from, final MapNode to) {
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

	Optional<EdgeTraffic> traffic(final Collection<EdgeTraffic> traffics, final int idx) {
		return traffics.parallelStream().filter(et -> et.equals(traffic(idx))).findFirst();
	}

	EdgeTraffic traffic(final int i) {
		final EdgeTraffic result = traffics.get(i);
		return result;
	}

	Optional<EdgeTraffic> traffic(final TrafficBuilder builder, final int idx) {
		return traffic(builder.getTraffics(), idx);
	}

	int trafficIndex(final EdgeTraffic traffic) {
		final int result = traffics.indexOf(traffic);
		return result;
	}
}
