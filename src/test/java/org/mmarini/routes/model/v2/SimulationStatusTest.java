package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.mmarini.routes.model.Constants;

public class SimulationStatusTest implements Constants {

	@Test
	public void test() {
		final SimulationStatus s = SimulationStatus.create();
		assertThat(s, notNullValue());
		assertThat(s.getVehicles(), empty());
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
		assertThat(s.getVehicles(), empty());
		assertThat(s.getMap(), equalTo(map));
	}
//
//	@Test
//	public void testSetVeichle() {
//		final Set<EdgeVehicles> vehicles = new HashSet<>();
//		vehicles.add(Vehicle.create(SiteNode.create(0, 0), SiteNode.create(10, 10)));
//		final SimulationStatus s = SimulationStatus.create().setVehicles(vehicles);
//		assertThat(s, notNullValue());
//		assertThat(s.getVehicles(), equalTo(vehicles));
//	}
}
