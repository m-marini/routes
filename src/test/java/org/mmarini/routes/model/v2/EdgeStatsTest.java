package org.mmarini.routes.model.v2;

import static java.lang.Math.sqrt;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.mmarini.routes.model.Constants;

public class EdgeStatsTest implements Constants {

	private static final double EXPECTED_DEFAULT_TIME = 10.0 * sqrt(2) / DEFAULT_SPEED_LIMIT_KMH / KMH_TO_MPS;

	@Test
	public void test() {
		final EdgeStats s = EdgeStats.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)));
		assertThat(s, notNullValue());
		assertFalse(s.getLastTravelTime().isPresent());
		assertThat(s.getVehicleCount(), equalTo(0));
		assertThat(s.getTravelTime(), closeTo(EXPECTED_DEFAULT_TIME, EXPECTED_DEFAULT_TIME * 1e-3));
	}

	@Test
	public void testSetLastTravelTime() {
		final EdgeStats s = EdgeStats.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)))
				.setLastTravelTime(Optional.of(10.0));
		assertThat(s, notNullValue());
		assertTrue(s.getLastTravelTime().isPresent());
		assertThat(s.getLastTravelTime().get(), equalTo(10.0));
		assertThat(s.getVehicleCount(), equalTo(0));
	}

	@Test
	public void testSetVehicleCount() {
		final EdgeStats s = EdgeStats.create(MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10)))
				.setVehicleCount(5);
		assertThat(s, notNullValue());
		assertFalse(s.getLastTravelTime().isPresent());
		assertThat(s.getVehicleCount(), equalTo(5));
		assertThat(s.getTravelTime(), closeTo(EXPECTED_DEFAULT_TIME, 1));
	}

}
