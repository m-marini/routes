package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Optional;

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
			+ (DISTANCE_10 - VEHICLE_LENGTH) * INTERVAL_2 / (INTERVAL_2 + REACTION_TIME);
	private static final double SPEED_10 = 10.0;

	@Test
	public void test() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getEdge().isPresent(), equalTo(false));
		assertThat(v.getEdgeLocation(), equalTo(0.0));
		assertThat(v.getEdgeEntryTime(), equalTo(0.0));
	}

	@Test
	public void testMoveFarNext() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setEdge(Optional.of(edge))
				.setEdgeLocation(DISTANCE_10).move(INTERVAL_2, Optional.of(DISTANCE_80));
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getEdgeLocation(), equalTo(DISTANCE_30));
	}

	@Test
	public void testMoveFirstAfterEnd() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setEdge(Optional.of(edge))
				.setEdgeLocation(DISTANCE_90).move(INTERVAL_2, Optional.empty());
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_1));
		assertThat(m.getElem1().getEdgeLocation(), equalTo(DISTANCE_100));
	}

	@Test
	public void testMoveFirstBeforeEnd() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setEdge(Optional.of(edge))
				.setEdgeLocation(DISTANCE_10).move(INTERVAL_2, Optional.empty());
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getEdgeLocation(), equalTo(DISTANCE_30));
	}

	@Test
	public void testMoveFirstToEnd() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setEdge(Optional.of(edge))
				.setEdgeLocation(DISTANCE_80).move(INTERVAL_2, Optional.empty());
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getEdgeLocation(), equalTo(DISTANCE_100));
	}

	@Test
	public void testMoveNearNext() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Tuple2<Vehicle, Double> m = Vehicle.create(departure, destination).setEdge(Optional.of(edge))
				.setEdgeLocation(DISTANCE_10).move(INTERVAL_2, Optional.of(DISTANCE_10));
		assertThat(m, notNullValue());
		assertThat(m.getElem1(), notNullValue());
		assertThat(m.getElem2().doubleValue(), equalTo(INTERVAL_2));
		assertThat(m.getElem1().getEdgeLocation(), equalTo(DISTANCE_SAFE));
	}

	@Test
	public void testSetEdge() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(departure, destination);
		final Vehicle v = Vehicle.create(departure, destination).setEdge(Optional.of(edge));
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getEdge().isPresent(), equalTo(true));
		assertThat(v.getEdge().get(), equalTo(edge));
		assertThat(v.getEdgeLocation(), equalTo(0.0));
		assertThat(v.getEdgeEntryTime(), equalTo(0.0));
	}

	@Test
	public void testSetEdgeEntryTime() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(departure, destination);
		final Vehicle v = Vehicle.create(departure, destination).setEdge(Optional.of(edge)).setEdgeEntryTime(10.0);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getEdge().isPresent(), equalTo(true));
		assertThat(v.getEdge().get(), equalTo(edge));
		assertThat(v.getEdgeLocation(), equalTo(0.0));
		assertThat(v.getEdgeEntryTime(), equalTo(10.0));
	}

	@Test
	public void testSetEdgeLocation() {
		final MapNode departure = MapNode.create(0, 0);
		final MapNode destination = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(departure, destination);
		final Vehicle v = Vehicle.create(departure, destination).setEdge(Optional.of(edge)).setEdgeLocation(10);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getEdge().isPresent(), equalTo(true));
		assertThat(v.getEdge().get(), equalTo(edge));
		assertThat(v.getEdgeLocation(), equalTo(10.0));
		assertThat(v.getEdgeEntryTime(), equalTo(0.0));
	}
}
