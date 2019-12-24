package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.OptionalDouble;

import org.junit.Test;
import org.mmarini.routes.model.Constants;

public class VehicleTest implements Constants {

	private static final double INTERVAL_1 = 1.0;
	private static final double INTERVAL_2 = 2.0;
	private static final double DISTANCE_10 = 10.0;
	private static final double DISTANCE_30 = 30.0;
	private static final double DISTANCE_90 = 90.0;
	private static final double DISTANCE_80 = 80.0;
	private static final double DISTANCE_100 = 100.0;
	private static final double DISTANCE_SAFE = DISTANCE_10
			+ (25.0 - VEHICLE_LENGTH - DISTANCE_10) * INTERVAL_2 / (INTERVAL_2 + REACTION_TIME);
	private static final double SPEED_10 = 10.0;

	@Test
	public void test() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getLocation(), equalTo(0.0));
		assertThat(v.getEdgeEntryTime(), equalTo(0.0));
	}

	@Test
	public void testMoveFarNext() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> result = Vehicle.create(departure, destination).setLocation(DISTANCE_10)
				.move(edge, INTERVAL_2, OptionalDouble.of(DISTANCE_80));
		assertThat(result, notNullValue());
		assertThat(result.getElem1(), notNullValue());
		assertThat(result.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(result.getElem1().getLocation(), equalTo(DISTANCE_30));
	}

	@Test
	public void testMoveFirstAfterEnd() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setLocation(DISTANCE_90).move(edge,
				INTERVAL_2, OptionalDouble.empty());
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_1));
		assertThat(m.getElem1().getLocation(), equalTo(DISTANCE_100));
	}

	@Test
	public void testMoveFirstBeforeEnd() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setLocation(DISTANCE_10).move(edge,
				INTERVAL_2, OptionalDouble.empty());
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getLocation(), equalTo(DISTANCE_30));
	}

	@Test
	public void testMoveFirstToEnd() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setLocation(DISTANCE_80).move(edge,
				INTERVAL_2, OptionalDouble.empty());
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getLocation(), equalTo(DISTANCE_100));
	}

	@Test
	public void testMoveNearNext() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setLocation(DISTANCE_10).move(edge,
				INTERVAL_2, OptionalDouble.of(DISTANCE_10 + VEHICLE_LENGTH + DISTANCE_10));
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getLocation(), equalTo(DISTANCE_SAFE));
	}

	@Test
	public void testMoveOver() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setLocation(85).move(edge, 1,
				OptionalDouble.of(100.0));
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(1.0));
		assertThat(m.getElem1().getLocation(), equalTo(95.0));
	}

	@Test
	public void testSetEdgeEntryTime() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination).setEdgeEntryTime(10.0);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getLocation(), equalTo(0.0));
		assertThat(v.getEdgeEntryTime(), equalTo(10.0));
	}

	@Test
	public void testSetLocation() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination).setLocation(10);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getLocation(), equalTo(10.0));
		assertThat(v.getEdgeEntryTime(), equalTo(0.0));
	}
}
