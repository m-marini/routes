package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.routes.model.v2.TestUtils.genArguments;
import static org.mmarini.routes.model.v2.TestUtils.genDouble;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test build status at cross proximity
 */
public class StatusBuilderTest6 extends AbstractStatusBuilderTest {

	static DoubleStream timeRange() {
		return genArguments().mapToDouble(i -> genDouble(i, 0, 60));
	}

	/**
	 * <pre>
	 * Given an initial status at time and a constant random seed
	 * And a builder to time t + 5 s
	 * When build the status
	 * Than a vehicle should be at edge 0 at 50m
	 * Than 3 vehicles should be at edge 2 at 27.083 37.5 50m
	 * Than a vehicle should be at edge 4 at 50m
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void build(final double time) {
		final StatusBuilder builder1 = createBuilder(time, time + 5, () -> List.of(), Function.identity());
		final StatusBuilder builder = builder1
				.setInitialStatus(builder1.getInitialStatus().setRandom(new Random(1234)));

		final SimulationStatus result = builder.build();
		assertNotNull(result);

		final Set<EdgeTraffic> traffics1 = result.getTraffic();

		final EdgeTraffic e0 = findEdge(traffics1, 0).get();
		assertThat(e0.getVehicles(), hasSize(1));
		assertThat(e0.getLast().getLocation(), closeTo(50, 1e-3));

		final EdgeTraffic e1 = findEdge(traffics1, 1).get();
		assertThat(e1.getVehicles(), hasSize(3));
		assertThat(e1.getVehicles().get(0).getLocation(), closeTo(27.083, 1e-3));
		assertThat(e1.getVehicles().get(1).getLocation(), closeTo(37.5, 1e-3));
		assertThat(e1.getVehicles().get(2).getLocation(), closeTo(50, 1e-3));

		final EdgeTraffic e2 = findEdge(traffics1, 2).get();
		assertThat(e2.getVehicles(), hasSize(1));
		assertThat(e2.getLast().getLocation(), closeTo(50, 1e-3));
	}

	EdgeTraffic createTraffic6(final EdgeTraffic edgeTraffic) {
		switch (edges.indexOf(edgeTraffic.getEdge())) {
		case 0:
			return edgeTraffic.setVehicles(List.of(vehicles.get(0)));
		case 2:
			return edgeTraffic.setVehicles(List.of(vehicles.get(1)));
		default:
			return edgeTraffic;
		}
	}

	EdgeTraffic createTraffic7(final EdgeTraffic edgeTraffic) {
		if (edges.indexOf(edgeTraffic.getEdge()) == 4) {
			return edgeTraffic.setVehicles(vehicles);
		} else {
			return edgeTraffic;
		}
	}

	/**
	 * <pre>
	 * Given an initial status at time 0s and a constant random seed
	 * And a builder to time 5s
	 * When build the status
	 * Than a vehicle should be at edge 0 at 50m
	 * Than 3 vehicles should be at edge 2 at 27.083 37.5 50m
	 * Than a vehicle should be at edge 4 at 50m
	 * </pre>
	 */
	@Test
	void createVehicles() {
		final StatusBuilder builder1 = createBuilder(0, 5, () -> List.of(), Function.identity());
		final StatusBuilder builder2 = builder1
				.setInitialStatus(builder1.getInitialStatus().setRandom(new Random(1234)));
		final Set<EdgeTraffic> tr = builder2.getTraffics().stream().map(e -> e.setTime(5)).collect(Collectors.toSet());
		final StatusBuilder builder = builder2.setTraffics(tr);

		final StatusBuilder result = builder.createVehicles();
		assertNotNull(result);

		final Set<EdgeTraffic> traffics1 = result.getTraffics();

		final EdgeTraffic e0 = findEdge(traffics1, 0).get();
		assertThat(e0.getVehicles(), hasSize(1));
		assertThat(e0.getLast().getLocation(), closeTo(50, 1e-3));

		final EdgeTraffic e1 = findEdge(traffics1, 1).get();
		assertThat(e1.getVehicles(), hasSize(3));
		assertThat(e1.getVehicles().get(0).getLocation(), closeTo(27.083, 1e-3));
		assertThat(e1.getVehicles().get(1).getLocation(), closeTo(37.5, 1e-3));
		assertThat(e1.getVehicles().get(2).getLocation(), closeTo(50, 1e-3));

		final EdgeTraffic e2 = findEdge(traffics1, 2).get();
		assertThat(e2.getVehicles(), hasSize(1));
		assertThat(e2.getLast().getLocation(), closeTo(50, 1e-3));
	}

	List<Vehicle> createVehicles6() {
		final Vehicle v0 = Vehicle.create(sites.get(0), sites.get(1)).setLocation(498);
		final Vehicle v2 = Vehicle.create(sites.get(2), sites.get(1)).setLocation(498);
		return List.of(v0, v2);
	}

	List<Vehicle> createVehicles7() {
		return List.of(Vehicle.create(sites.get(0), sites.get(1)).setLocation(495));
	}

	@Test
	void createVehiclesBusy() {
		final StatusBuilder builder1 = createBuilder(0, 5, () -> List.of(), Function.identity());
		final StatusBuilder builder2 = builder1
				.setInitialStatus(builder1.getInitialStatus().setRandom(new Random(1234)));
		final Set<EdgeTraffic> tr = builder2.getTraffics().stream()
				.map(e -> e.setTime(5).setVehicles(List.of(Vehicle.create(sites.get(0), sites.get(1)))))
				.collect(Collectors.toSet());
		final StatusBuilder builder = builder2.setTraffics(tr);

		final StatusBuilder result = builder.createVehicles();
		assertNotNull(result);

		final Set<EdgeTraffic> traffics1 = result.getTraffics();

		final EdgeTraffic e0 = findEdge(traffics1, 0).get();
		assertThat(e0.getVehicles(), hasSize(1));

		final EdgeTraffic e1 = findEdge(traffics1, 1).get();
		assertThat(e1.getVehicles(), hasSize(1));

		final EdgeTraffic e2 = findEdge(traffics1, 2).get();
		assertThat(e2.getVehicles(), hasSize(1));
	}

	/**
	 * <pre>
	 * Given an initial status at time t
	 * And a vehicle in edge 0 at 498 m
	 * And a vehicle in edge 2 at 498 m
	 * And a builder to time t + 0.4 s
	 * When moveAllVehicles
	 * Than the vehicle at edge 2 should be at edge 4 at 2.0 m
	 * And the vehicle at edge 0 should be at 500 m
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveAllVehicles6(final double time) {
		final double limitTime = time + 0.4;
		final StatusBuilder builder = createBuilder(time, limitTime, this::createVehicles6, this::createTraffic6);

		final StatusBuilder result = builder.moveAllVehicles();

		assertNotNull(result);
		final Set<EdgeTraffic> tr = result.getTraffics();
		assertNotNull(tr);

		final EdgeTraffic nt0 = findEdge(tr, 0).get();

		assertThat(nt0.getTime(), equalTo(limitTime));
		assertThat(nt0.getVehicles(), hasSize(1));
		assertThat(nt0.getVehicles(), hasItem(vehicles.get(0)));
		assertThat(nt0.getLast().getLocation(), equalTo(500.0));

		final EdgeTraffic nt2 = findEdge(tr, 2).get();
		assertThat(nt2.getTime(), equalTo(limitTime));
		assertThat(nt2.getVehicles(), empty());

		final EdgeTraffic nt4 = findEdge(tr, 4).get();
		assertThat(nt4.getTime(), equalTo(limitTime));
		assertThat(nt4.getVehicles(), hasSize(1));
		assertThat(nt4.getVehicles(), hasItem(vehicles.get(1)));
		assertThat(nt4.getLast().getLocation(), closeTo(2.0, 1e-3));

	}

	/**
	 * <pre>
	 * Given an initial status at time t
	 * And a vehicle from 0 to 1 in edge 4 at 495 m
	 * And a builder to time t + 1 s
	 * When moveAllVehicles
	 * Than the vehicle should be at edge 1 at 5.0 m
	 * And all the other edges should be at t+1 time
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveAllVehicles7(final double time) {
		final double limitTime = time + 1;

		final StatusBuilder builder = createBuilder(time, limitTime, this::createVehicles7, this::createTraffic7);

		final StatusBuilder result = builder.moveAllVehicles();

		assertNotNull(result);
		final Set<EdgeTraffic> tr = result.getTraffics();
		assertNotNull(tr);

		final EdgeTraffic nt0 = findEdge(tr, 0).get();
		assertThat(nt0.getTime(), equalTo(limitTime));

		final EdgeTraffic nt1 = findEdge(tr, 1).get();
		assertThat(nt1.getTime(), equalTo(limitTime));
		assertThat(nt1.getVehicles(), hasSize(1));
		assertTrue(nt1.getLast().isReturning());
		assertThat(nt1.getLast().getLocation(), closeTo(5.0, 1e-3));

		final EdgeTraffic nt2 = findEdge(tr, 2).get();
		assertThat(nt2.getTime(), equalTo(limitTime));

		final EdgeTraffic nt3 = findEdge(tr, 3).get();
		assertThat(nt3.getTime(), equalTo(limitTime));

		final EdgeTraffic nt4 = findEdge(tr, 4).get();
		assertThat(nt4.getTime(), equalTo(limitTime));

		final EdgeTraffic nt5 = findEdge(tr, 5).get();
		assertThat(nt5.getTime(), equalTo(limitTime));
	}
}
