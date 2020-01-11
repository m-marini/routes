package org.mmarini.routes.model.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;

/**
 * Test build status at cross proximity
 */
public abstract class AbstractStatusBuilderTest1 {

	List<SiteNode> sites;
	List<MapNode> nodes;
	List<MapNode> nodesByIndex;
	List<MapEdge> edges;
	List<EdgeTraffic> traffics;
	GeoMap map;
	List<Vehicle> vehicles;
	private Map<Tuple2<SiteNode, SiteNode>, Double> weights;

	StatusBuilder createBuilder(final double time, final double limitTime,
			final Supplier<List<Vehicle>> vehiclesBuilder,
			final Function<EdgeTraffic, EdgeTraffic> trafficTransformer) {
		vehicles = vehiclesBuilder.get();
		traffics = edges.stream().map(e -> EdgeTraffic.create(e).setTime(time)).map(trafficTransformer)
				.collect(Collectors.toList());
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

	/**
	 *
	 * @param traffics
	 * @param idx
	 * @return
	 */
	Optional<EdgeTraffic> findEdge(final Collection<EdgeTraffic> traffics, final int idx) {
		return traffics.parallelStream().filter(et -> et.getEdge().equals(edges.get(idx))).findFirst();
	}
}
