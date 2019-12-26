package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class SimulationStatusTest implements Constants {

	@Test
	public void test() {
		final SimulationStatus s = SimulationStatus.create();
		assertThat(s, notNullValue());
		assertThat(s.getTraffic(), empty());
		assertThat(s.getMap(), notNullValue());
		assertThat(s.getFrequence(), equalTo(DEFAULT_FREQUENCE));
	}

	@Test
	public void testAddWeight() {
		final SiteNode s1 = SiteNode.create(0, 0);
		final SiteNode s2 = SiteNode.create(0, 10);
		final SimulationStatus map = SimulationStatus.create().addWeight(s1, s2, 1);
		assertThat(map.getWeight(s1, s2), equalTo(Optional.<Double>of(1.0)));
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
		assertThat(s.getTraffic(), empty());
		assertThat(s.getMap(), equalTo(map));
	}

	@Test
	public void testSetTraffics() {
		final SiteNode begin = SiteNode.create(0, 0);
		final SiteNode end = SiteNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);
		final GeoMap map = GeoMap.create().setSites(Set.of(begin, end)).add(edge);
		final Set<EdgeTraffic> vehicles = Set
				.of(EdgeTraffic.create(edge).setVehicles(List.of(Vehicle.create(begin, end))));
		final SimulationStatus s = SimulationStatus.create().setTraffics(vehicles);
		assertThat(s, notNullValue());
		assertThat(s.getTraffic(), equalTo(vehicles));
	}
}
