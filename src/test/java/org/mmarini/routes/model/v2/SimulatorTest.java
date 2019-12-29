package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Test build status at cross proximity
 */
public class SimulatorTest {
	@Test
	public void start() {
		final Simulator s = new Simulator();
		final SiteNode s0 = SiteNode.create(0, 0);
		final SiteNode s1 = SiteNode.create(1000, 0);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final Set<MapEdge> edges = Set.of(MapEdge.create(s0, s1), MapEdge.create(s1, s0));
		final GeoMap map = GeoMap.create().setSites(sites).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setTraffics(traffics);
		s.setSimulationStatus(status).start();
		final SimulationStatus result = s.getOutput().blockingFirst();
		s.stop();

		assertThat(result.getTime(), greaterThan(0.0));
	}
}
