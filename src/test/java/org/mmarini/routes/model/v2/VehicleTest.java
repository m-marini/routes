package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.routes.model.v2.TestUtils.genArguments;
import static org.mmarini.routes.model.v2.TestUtils.genDouble;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

	static Stream<Arguments> location() {
		return genArguments().mapToObj(i -> {
			final double location = genDouble(i, 0, 100);
			final double time = genDouble(i, 0, 60);
			return Arguments.of(location, time);
		});
	}

	static DoubleStream valueRange_0_10() {
		return genArguments().mapToDouble(i -> genDouble(i, 0, 10));
	}

	@Test
	public void compareTo() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v1 = Vehicle.create(departure, destination);
		final Vehicle v2 = Vehicle.create(departure, destination);

		final int result12 = v1.compareTo(v2);
		final int result21 = v2.compareTo(v1);

		assertThat(Math.signum(result21), equalTo(-Math.signum(result12)));
	}

	@Test
	public void getName() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination);
		final String result = v.getName();
		assertNotNull(result);
		assertThat(result, matchesPattern(".{8}-.{4}-.{4}-.{4}-.{12}"));
	}

	@Test
	public void getShortName() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination);
		final String result = v.getShortName();
		assertNotNull(result);
		assertThat(result, matchesPattern(".{6}"));
	}

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
		assertThat(v.getId(), hasToString(matchesPattern(".{8}-.{4}-.{4}-.{4}-.{12}")));
	}

	@Test
	public void testEquals() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v1 = Vehicle.create(departure, destination);
		final Vehicle v11 = v1.setLocation(10);
		final Vehicle v12 = v1.setEdgeEntryTime(10);
		final Vehicle v13 = v1.setReturning(true);

		final Vehicle v2 = Vehicle.create(departure, destination);

		assertThat(v1, notNullValue());
		assertThat(v2, notNullValue());

		assertFalse(v1.equals(null));
		assertFalse(v1.equals(new Object()));
		assertFalse(v2.equals(v1));
		assertFalse(v1.equals(v2));

		assertTrue(v1.equals(v1));
		assertTrue(v1.equals(v11));
		assertTrue(v1.equals(v12));
		assertTrue(v1.equals(v13));
		assertTrue(v11.equals(v1));
		assertTrue(v11.equals(v11));
		assertTrue(v11.equals(v12));
		assertTrue(v11.equals(v13));
		assertTrue(v12.equals(v1));
		assertTrue(v12.equals(v11));
		assertTrue(v12.equals(v12));
		assertTrue(v12.equals(v13));
		assertTrue(v13.equals(v1));
		assertTrue(v13.equals(v11));
		assertTrue(v13.equals(v12));
		assertTrue(v13.equals(v13));

		assertThat(v1.compareTo(v1), equalTo(0));
		assertThat(v1.compareTo(v11), equalTo(0));
		assertThat(v1.compareTo(v12), equalTo(0));
		assertThat(v1.compareTo(v13), equalTo(0));
		assertThat(v11.compareTo(v1), equalTo(0));
		assertThat(v11.compareTo(v11), equalTo(0));
		assertThat(v11.compareTo(v12), equalTo(0));
		assertThat(v11.compareTo(v13), equalTo(0));
		assertThat(v12.compareTo(v1), equalTo(0));
		assertThat(v12.compareTo(v11), equalTo(0));
		assertThat(v12.compareTo(v12), equalTo(0));
		assertThat(v12.compareTo(v13), equalTo(0));
		assertThat(v13.compareTo(v1), equalTo(0));
		assertThat(v13.compareTo(v11), equalTo(0));
		assertThat(v13.compareTo(v12), equalTo(0));
		assertThat(v13.compareTo(v13), equalTo(0));
	}

	@Test
	public void testHashcode() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v1 = Vehicle.create(departure, destination);
		final Vehicle v11 = v1.setLocation(10);
		final Vehicle v12 = v1.setEdgeEntryTime(10);
		final Vehicle v13 = v1.setReturning(true);

		assertThat(v1, notNullValue());

		assertThat(v1.hashCode(), equalTo(v11.hashCode()));
		assertThat(v1.hashCode(), equalTo(v12.hashCode()));
		assertThat(v1.hashCode(), equalTo(v13.hashCode()));
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

	@ParameterizedTest(name = "{index} ==> location=''{0}''")
	@MethodSource("location")
	public void testMoveLastToEnd(final double location) {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Vehicle vehicle = Vehicle.create(departure, destination).setLocation(location);

		final double time = (DISTANCE_100 - location) / SPEED_10;
		final Tuple2<Vehicle, Double> m = vehicle.move(edge, time, OptionalDouble.empty());

		assertThat(m, notNullValue());
		final Vehicle elem1 = m.getElem1();
		assertThat(elem1, notNullValue());
		assertThat(elem1.getLocation(), equalTo(DISTANCE_100));
		assertThat(m.getElem2().doubleValue(), equalTo(time));
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

	/**
	 * Given a vehicle at 85 m<br>
	 * And a edge of 100 m<br>
	 * And speed of 10 m/sbr> When move the vehicle for 1 second in the edge with a
	 * next vehicle at 100 m<br>
	 * Than the vehicle should move for 1 sec<br>
	 * And at 90 m
	 *
	 */
	@Test
	public void testMoveOver() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(DISTANCE_100, 0);
		final MapEdge edge = MapEdge.create(departure, destination).setSpeedLimit(SPEED_10);
		final Vehicle vehicle = Vehicle.create(departure, destination).setLocation(85);
		final Tuple2<Vehicle, Double> result = vehicle.move(edge, 1, OptionalDouble.of(100.0));
		assertThat(result, notNullValue());
		assertThat(result.getElem1(), notNullValue());
		assertThat(result.getElem2().doubleValue(), equalTo(1.0));
		assertThat(result.getElem1().getLocation(), equalTo(90.0));
	}

	@ParameterizedTest(name = "{index} ==> edgeEntryTime=''{0}''")
	@MethodSource("valueRange_0_10")
	public void testSetEdgeEntryTime(final double edgeEntryTime) {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination).setEdgeEntryTime(edgeEntryTime);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getLocation(), equalTo(0.0));
		assertThat(v.getEdgeEntryTime(), equalTo(edgeEntryTime));
	}

	@ParameterizedTest(name = "{index} ==> location=''{0}''")
	@MethodSource("valueRange_0_10")
	public void testSetLocation(final double location) {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 0);
		final Vehicle v = Vehicle.create(departure, destination).setLocation(location);
		assertThat(v, notNullValue());
		assertThat(v.getDeparture(), equalTo(departure));
		assertThat(v.getDestination(), equalTo(destination));
		assertThat(v.getLocation(), equalTo(location));
		assertThat(v.getEdgeEntryTime(), equalTo(0.0));
	}

	@Test
	public void testToString() {
		final SiteNode departure = SiteNode.create(0, 0);
		final SiteNode destination = SiteNode.create(10, 10);
		final Vehicle v = Vehicle.create(departure, destination);
		assertThat(v, notNullValue());
		assertThat(v, hasToString(matchesPattern("Vehicle \\[.{8}-.{4}-.{4}-.{4}-.{12}\\]")));
	}

}
