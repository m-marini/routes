package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.routes.model.v2.TestUtils.genArguments;
import static org.mmarini.routes.model.v2.TestUtils.genDouble;

import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test build status at cross proximity
 */
public class StatusBuilderTest6 extends AbstractStatusBuilderTest {

	static DoubleStream timeRange() {
		return genArguments().mapToDouble(i -> genDouble(i, 0, 60));
	}

	@ParameterizedTest
	@MethodSource("timeRange")

	public void build6(final double time) {
		final double limitTime = time + 0.4;
		final StatusBuilder builder = createBuilder(time, limitTime, this::createVehicles6, this::createTraffic6);

		final SimulationStatus result = builder.build();

		assertNotNull(result);
		final Set<EdgeTraffic> tr = result.getTraffic();
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

	@ParameterizedTest
	@MethodSource("timeRange")
	public void build7(final double time) {
		final double limitTime = time + 1;

		final StatusBuilder builder = createBuilder(time, limitTime, this::createVehicles7, this::createTraffic7);

		final SimulationStatus result = builder.build();

		assertNotNull(result);
		final Set<EdgeTraffic> tr = result.getTraffic();
		assertNotNull(tr);

		final EdgeTraffic nt0 = findEdge(tr, 0).get();
		assertThat(nt0.getTime(), equalTo(limitTime));

		final EdgeTraffic nt1 = findEdge(tr, 1).get();
		assertThat(nt1.getTime(), equalTo(limitTime));

		final EdgeTraffic nt2 = findEdge(tr, 2).get();
		assertThat(nt2.getTime(), equalTo(limitTime));

		final EdgeTraffic nt3 = findEdge(tr, 3).get();
		assertThat(nt3.getTime(), equalTo(limitTime));

		final EdgeTraffic nt4 = findEdge(tr, 4).get();
		assertThat(nt4.getTime(), equalTo(limitTime));

		final EdgeTraffic nt5 = findEdge(tr, 5).get();
		assertThat(nt5.getTime(), equalTo(limitTime));
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
		if (edgeTraffic.getEdge().equals(edges.get(4))) {
			return edgeTraffic.setVehicles(vehicles);
		} else {
			return edgeTraffic;
		}
	}

	List<Vehicle> createVehicles6() {
		final Vehicle v0 = Vehicle.create(sites.get(0), sites.get(1)).setLocation(498);
		final Vehicle v2 = Vehicle.create(sites.get(2), sites.get(1)).setLocation(498);
		return List.of(v0, v2);
	}

	List<Vehicle> createVehicles7() {
		return List.of(Vehicle.create(sites.get(0), sites.get(1)).setLocation(495));
	}
}
