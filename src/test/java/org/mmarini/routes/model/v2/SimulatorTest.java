package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.reactivex.rxjava3.core.Single;

/**
 * Test build status at cross proximity
 */
public class SimulatorTest {
	/**
	 * @return
	 */
	private Simulator createSimulator() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(1000, 0);
		final Set<MapEdge> edges = Set.of(MapEdge.create(s0, s1), MapEdge.create(s1, s0));
		final GeoMap map = GeoMap.create(edges, GeoMap.buildWeights(Set.of(s0, s1), (a, b) -> 0.5));
		final Traffic status = Traffic.create(map);
		final Simulator s = new Simulator().setSimulationStatus(status);
		return s;
	}

	@Test
	public void start() {
		final Simulator s = createSimulator();
		s.start();
		final Traffic result = s.getOutput().blockingFirst();
//		s.stop();
		assertThat(result.getTime(), equalTo(0.0));
	}

	@Test
	public void start2() {
		final Simulator s = createSimulator();
		s.start();
		final Simulator result = s.start();
		assertThat(result, sameInstance(s));
	}

	@Test
	public void startstop() {
		final Simulator s = createSimulator();
		s.start().stop();
		final Traffic result = s.getOutput().blockingFirst();
		assertThat(result.getTime(), equalTo(0.0));
	}

	@Test
	public void stop() {
		final Simulator s = createSimulator();
		final Single<Traffic> result = s.stop();
		final Traffic x = result.blockingGet();
		assertNotNull(x);
	}
}
