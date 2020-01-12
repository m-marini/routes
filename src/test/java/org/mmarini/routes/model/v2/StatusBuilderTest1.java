package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.routes.model.v2.TestUtils.genArguments;
import static org.mmarini.routes.model.v2.TestUtils.genDouble;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class StatusBuilderTest1 extends AbstractStatusBuilderTest {

	static DoubleStream timeRange() {
		return genArguments().mapToDouble(i -> genDouble(i, 10, 20));
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
		final StatusBuilder builder1 = createBuilder(time, time + 5, (traffic, i) -> traffic);
		final StatusBuilder builder = builder1
				.setInitialStatus(builder1.getInitialStatus().setRandom(new Random(1234)));

		final SimulationStatus result = builder.build();
		assertNotNull(result);

		final Set<EdgeTraffic> traffics1 = result.getTraffic();

		final EdgeTraffic e0 = traffic(traffics1, 0).get();
		assertThat(e0.getVehicles(), hasSize(1));
		assertThat(e0.getLast().getLocation(), closeTo(50, 1e-3));

		final EdgeTraffic e1 = traffic(traffics1, 1).get();
		assertThat(e1.getVehicles(), hasSize(3));
		assertThat(e1.getVehicles().get(0).getLocation(), closeTo(27.083, 1e-3));
		assertThat(e1.getVehicles().get(1).getLocation(), closeTo(37.5, 1e-3));
		assertThat(e1.getVehicles().get(2).getLocation(), closeTo(50, 1e-3));

		final EdgeTraffic e2 = traffic(traffics1, 2).get();
		assertThat(e2.getVehicles(), hasSize(1));
		assertThat(e2.getLast().getLocation(), closeTo(50, 1e-3));
	}

	@Test
	public void findCandidate() {
		final double time = 10;
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime, (edge, i) -> {
			switch (i) {
			case 1:
			case 3:
				return edge.setTime(edge.getTime() + 0.1);
			default:
				return edge.setTime(edge.getTime() + 0.5);
			}
		});
		final Set<EdgeTraffic> result = builder.findCandidates();
		assertNotNull(result);
		assertThat(result, hasSize(2));
		assertThat(result, hasItem(traffic(1)));
		assertThat(result, hasItem(traffic(3)));
	}

	@Test
	public void getMinimumTime() {
		final double time = 10;
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime,
				(edge, i) -> edge.setTime(edge.getTime() + i * 0.1));
		final double result = builder.getMinimumTime();
		assertThat(result, closeTo(time, 1e-3));
	}

	@Test
	public void getMinimumTimeEmpty() {
		final StatusBuilder builder = StatusBuilder.create(SimulationStatus.create(), 10);
		final double result = builder.getMinimumTime();
		assertThat(result, closeTo(10.0, 1e-3));
	}

	@Test
	public void getNextEdge() {
		final double time = 10;
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime,
				(edge, i) -> i == 0 ? edge.setVehicles(List.of(Vehicle.create(site(0), site(1)))) : edge);
		final Optional<EdgeTraffic> result = builder.getNextTraffic(traffic(0));
		assertNotNull(result);
		assertTrue(result.isPresent());
		assertThat(trafficIndex(result.get()), equalTo(4));
	}

	@Test
	public void getNextEdgeEmpty() {
		final double time = 10;
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime, (a, b) -> a);
		final Optional<EdgeTraffic> result = builder.getNextTraffic(traffics.get(0));
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void getNextMinimumTime() {
		final double time = 10;
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime,
				(edge, i) -> edge.setTime(edge.getTime() + i * 0.1));
		final double result = builder.getNextMinimumTime();
		assertThat(result, closeTo(time + 0.1, 1e-3));
	}

	@Test
	public void getNextMinimumTimeEmpty() {
		final StatusBuilder builder = StatusBuilder.create(SimulationStatus.create(), 10);
		final double result = builder.getNextMinimumTime();
		assertThat(result, closeTo(10.0, 1e-3));
	}

	/**
	 * Given a status builder with all traffic times at limitTime except traffic on
	 * edge 0<br>
	 * When invoke isCompleted<br>
	 * Than should result false
	 *
	 * @param time
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isCompletedFalse(final double time) {
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime,
				(edge, i) -> i == 0 ? edge : edge.setTime(limitTime));
		final boolean result = builder.isCompleted();
		assertFalse(result);
	}

	/**
	 * Given a status builder with all traffic times at limitTime<br>
	 * When invoke isCompleted<br>
	 * Than should result true
	 *
	 * @param time
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isCompletedTrue(final double time) {
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime, (edge, i) -> edge.setTime(limitTime));
		final boolean result = builder.isCompleted();
		assertTrue(result);
	}

	/**
	 * Test with vehicles crossing from s0, s1 and s2 with same priority and no
	 * topology priority (stale situation) but in the different instants but with
	 * edge s0 earliest edge
	 *
	 * <pre>
	 *   s0
	 *     \
	 *      \
	 *       0 t=0
	 *        \
	 *        _\|      t=0.25
	 *           n3 <--1--  s1
	 *           ^
	 *           |
	 *           |
	 *           2 t=0.5
	 *           |
	 *           |
	 *
	 *            s2
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isPriorByArrival(final double time) {
		final double limitTime = time + 10;
		final StatusBuilder builder = createBuilder1(time, limitTime,
				// Create sites with modified topology
				() -> {
					return createDefaultSites().stream()
							.map(site -> (site.getX() == 0.0 && site.getY() == 0) ? SiteNode.create(0, -500) : site)
							.collect(Collectors.toList());
				}, this::createDefaultNodes, this::createDefaultEdges, this::createDefaultWeight,
				// Creates traffic with vehicles crossing the end nodes
				(traffic, i) -> {
					switch (i) {
					case 0:
						return traffic
								.setVehicles(List.of(
										Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
								.setTime(time);
					case 1:
						return traffic
								.setVehicles(List.of(
										Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
								.setTime(time + 0.25);
					case 2:
						return traffic
								.setVehicles(List.of(
										Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
								.setTime(time + 0.5);
					default:
						return traffic;
					}
				});
		final boolean result0 = builder.isPrior(traffic(0));
		final boolean result1 = builder.isPrior(traffic(1));
		final boolean result2 = builder.isPrior(traffic(2));
		assertTrue(result0);
		assertFalse(result1);
		assertFalse(result2);
	}

	/**
	 * Test with vehicles crossing from s0, s1 and s2 with same priority and no
	 * topology priority (stale situation) in the same instant but with edge s2
	 * lowest id
	 *
	 * <pre>
	 *   s0
	 *     \
	 *      \
	 *       0 t=0
	 *        \
	 *        _\|      t=0
	 *           n3 <--1--  s1
	 *           ^
	 *           |
	 *           |
	 *           2 t=0
	 *           |
	 *           |
	 *
	 *            s2
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isPriorById(final double time) {
		final double limitTime = time + 10;
		final StatusBuilder builder = createBuilder1(time, limitTime,
				// Create sites with modified topology
				() -> {
					return createDefaultSites().stream()
							.map(site -> (site.getX() == 0.0 && site.getY() == 0) ? SiteNode.create(0, -500) : site)
							.collect(Collectors.toList());
				}, this::createDefaultNodes, this::createDefaultEdges, this::createDefaultWeight,
				// Creates traffic with vehicles crossing the end nodes
				(traffic, i) -> {
					switch (i) {
					case 0:
					case 1:
					case 2:
						return traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
					default:
						return traffic;
					}
				});
		final boolean result0 = builder.isPrior(traffic(0));
		final boolean result1 = builder.isPrior(traffic(1));
		final boolean result2 = builder.isPrior(traffic(2));
		final boolean result3 = builder.isPrior(traffic(3));
		assertFalse(result0);
		assertFalse(result1);
		assertTrue(result2);
		assertFalse(result3);

		assertThat(traffic(2).compareTo(traffic(0)), lessThan(0));
		assertThat(traffic(2).compareTo(traffic(1)), lessThan(0));
	}

	/**
	 * Test with vehicles crossing from s0 and s2 in the same instant but with edge
	 * s0 with higher priority against edge s2
	 *
	 * <pre>
	 *     p=1
	 * s0  --0--> n3
	 *            ^
	 *            |
	 *            |
	 *            2 p=0
	 *            |
	 *            |
	 *
	 *            s2
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isPriorByPriotrity(final double time) {
		final double limitTime = time + 10;
		final StatusBuilder builder = createBuilder1(time, limitTime, this::createDefaultSites,
				this::createDefaultNodes,
				// Creates edges with modified priority
				() -> {
					return this.createDefaultEdges().stream().map(edge -> {
						if (allNodeIndex(edge.getBegin()) == 0 && allNodeIndex(edge.getEnd()) == 3) {
							return edge.setPriority(1);
						} else {
							return edge;
						}
					}).collect(Collectors.toList());
				}, this::createDefaultWeight,
				// Creates traffic with vehicle crossing the end node
				(traffic, i) -> {
					switch (i) {
					case 0:
						return traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
					case 2:
						return traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
					default:
						return traffic;
					}
				});
		final boolean result0 = builder.isPrior(traffic(0));
		final boolean result1 = builder.isPrior(traffic(2));
		assertTrue(result0);
		assertFalse(result1);
	}

	/**
	 * Test with vehicles crossing from s0 and s2 in the same instant but with edge
	 * s0 less direction priority against edge s2
	 *
	 * <pre>
	 * s0  --0--> n3
	 *            ^
	 *            |
	 *            |
	 *            2
	 *            |
	 *            |
	 *
	 *            s2
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isPriorDirection(final double time) {
		final double limitTime = time + 10;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 0:
				return traffic.setTime(time).setVehicles(
						List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
			case 2:
				return traffic.setTime(time).setVehicles(
						List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
			default:
				return traffic;
			}
		});
		final boolean result0 = builder.isPrior(traffic(0));
		final boolean result1 = builder.isPrior(traffic(2));
		assertFalse(result0);
		assertTrue(result1);
	}

	/**
	 * Test with vehicles crossing from s0 and s2 but in instants distant more than
	 * reaction time
	 *
	 * <pre>
	 *     t=1
	 * s0  --0--> n3
	 *            ^
	 *            |
	 *            |
	 *            2 t=2.1
	 *            |
	 *            |
	 *
	 *            s2
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void isPriorTimeNoConflict(final double time) {
		final double limitTime = time + 10;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 0:
				return traffic
						.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
						.setTime(time + 1);
			case 2:
				return traffic
						.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
						.setTime(time + 2.1);
			default:
				return traffic;
			}
		});
		final boolean result = builder.isPrior(traffic(0));
		assertTrue(result);
	}

	/**
	 * Given a returning vehicle arrived at departure<br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be removed
	 *
	 * <pre>
	 *       t=0
	 * s0 <--3--  n3  --4--> s1
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleDeparture(final double time) {

		final StatusBuilder builder = createBuilder(time, time,
				(traffic, i) -> i == 3 ? traffic.setVehicles(List.of(
						Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength()).setReturning(true)))
						: traffic);
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(3));
		assertNotNull(result);

		result.getTraffics().forEach(traffic -> {
			assertThat(traffic.getVehicles(), empty());
		});
	}

	/**
	 * Given a returning vehicle arrived at destination<br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be in the next edge returning<br>
	 *
	 * <pre>
	 * s0 <--3--  n3 <--1--  s1
	 * s0         n3  --4--> s1
	 *                 t=0
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleDestination(final double time) {

		final StatusBuilder builder = createBuilder(time, time,
				(traffic, i) -> i == 4
						? traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
						: traffic);
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(4));
		assertNotNull(result);

		final Optional<EdgeTraffic> traffic4 = traffic(result, 4);
		assertTrue(traffic4.isPresent());
		assertThat(traffic4.get().getVehicles(), empty());

		final Optional<EdgeTraffic> traffic1 = traffic(result, 1);
		assertTrue(traffic1.isPresent());
		assertThat(traffic1.get().getVehicles(), hasSize(1));

		final Vehicle v1 = traffic1.get().getLast();
		assertNotNull(v1);
		assertTrue(v1.isReturning());
		assertThat(v1.getLocation(), equalTo(0.0));
		assertThat(v1.getEdgeEntryTime(), equalTo(time));
	}

	/**
	 * Given a vehicle at end of edge at end simulation time<br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be at begin of the next edge<br>
	 * And the entry time of vehicle should be the end simulation
	 *
	 * <pre>
	 *       t=0        t=0
	 * s0  --0--> n3  --4--> s1
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleEndInstant(final double time) {

		final StatusBuilder builder = createBuilder(time, time,
				(traffic, i) -> i == 0
						? traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
						: traffic);
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(0));
		assertNotNull(result);

		final Optional<EdgeTraffic> traffic0 = traffic(result, 0);
		assertTrue(traffic0.isPresent());
		assertThat(traffic0.get().getVehicles(), empty());

		final Optional<EdgeTraffic> traffic4 = traffic(result, 4);
		assertTrue(traffic4.isPresent());
		assertThat(traffic4.get().getVehicles(), hasSize(1));

		final Vehicle v4 = traffic4.get().getLast();
		assertNotNull(v4);
		assertThat(v4.getLocation(), equalTo(0.0));
		assertThat(v4.getEdgeEntryTime(), equalTo(time));
	}

	/**
	 * Given a vehicle at end of edge at 1 second before end simulation<br>
	 * And the next edge at the end of simulation<br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be at a distance of max speed for 1 second of the
	 * next edge<br>
	 * And the entry time of vehicle should be the exit time of previous edge
	 *
	 * <pre>
	 *       t=1        t=2
	 * s0  --0--> n3  --4--> s1
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleMidInstant(final double time) {
		final double limitTime = time + 2;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 0:
				return traffic
						.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
						.setTime(limitTime - 1);
			case 4:
				return traffic.setTime(limitTime);
			default:
				return traffic;
			}
		});
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(0));
		assertNotNull(result);

		final Optional<EdgeTraffic> traffic0 = traffic(result, 0);
		assertTrue(traffic0.isPresent());
		assertThat(traffic0.get().getVehicles(), empty());

		final Optional<EdgeTraffic> traffic4 = traffic(result, 4);
		assertTrue(traffic4.isPresent());
		assertThat(traffic4.get().getVehicles(), hasSize(1));

		final Vehicle v4 = traffic4.get().getLast();
		assertNotNull(v4);
		assertThat(v4.getLocation(), equalTo(10.0));
		assertThat(v4.getEdgeEntryTime(), closeTo(time + 1, 1e-3));
	}

	/**
	 * Given a vehicle at end of edge at 5 seconds before end simulation<br>
	 * And the next edge at the end of simulation with a vehicle at 20m <br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be at a distance of max speed for 5 second of the
	 * next edge<br>
	 * And the entry time of vehicle should be the exit time of previous edge
	 *
	 * <pre>
	 *       t=1        t=6
	 * s0  --0--> n3  --4--> s1
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleMidInstantWithNextVehicle(final double time) {
		final double limitTime = time + 6;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 0:
				return traffic
						.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())))
						.setTime(time + 1);
			case 4:
				return traffic.setVehicles(List.of(Vehicle.create(site(0), site(1)).setLocation(20)))
						.setTime(limitTime);
			default:
				return traffic;
			}
		});
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(0));
		assertNotNull(result);

		final Optional<EdgeTraffic> traffic0 = traffic(result, 0);
		assertTrue(traffic0.isPresent());
		assertThat(traffic0.get().getVehicles(), empty());

		final Optional<EdgeTraffic> traffic4 = traffic(result, 4);
		assertTrue(traffic4.isPresent());
		assertThat(traffic4.get().getVehicles(), hasSize(2));

		final Vehicle v = traffic4.get().getVehicles().get(0);
		assertThat(v, equalTo(traffic(0).getLast()));
		assertThat(v.getLocation(), closeTo(12.5, 1e-3));
		assertThat(v.getEdgeEntryTime(), equalTo(time + 1));
	}

	/**
	 * Given a vehicle at end of edge<br>
	 * And no next edge<br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be removed
	 *
	 * <pre>
	 *       t=0        t=0
	 * s0  --0--> n3 <--1--  s1
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleNoWay(final double time) {
		final double limitTime = time + 6;
		final StatusBuilder builder = createBuilder1(time, limitTime, this::createDefaultSites,
				this::createDefaultNodes, () -> {
					return createDefaultEdges().stream()
							.filter(edge -> !(edge.getBegin().equals(allNode(3)) && edge.getEnd().equals(allNode(1))))
							.collect(Collectors.toList());
				}, this::createDefaultWeight, (traffic, i) -> {
					switch (i) {
					case 0:
						return traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
					default:
						return traffic;
					}
				});
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(0));
		assertNotNull(result);

		result.getTraffics().forEach(traffic -> {
			assertThat(traffic.getVehicles(), empty());
		});
	}

	/**
	 * Given a vehicle at destination<br>
	 * And no next edge<br>
	 * When move the vehicle to next edge<br>
	 * Than the vehicle should be removed
	 *
	 * <pre>
	 * s0  --0--> n3
	 * s0 <--2--  n3  --3--> s1
	 *           ^
	 *           |  |
	 *           |  |
	 *           1  4
	 *           |  |
	 *           |  |
	 *              v
	 *            s2
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void moveVehicleNoWayAtDestination(final double time) {
		final double limitTime = time + 6;
		final StatusBuilder builder = createBuilder1(time, limitTime, this::createDefaultSites,
				this::createDefaultNodes, () -> {
					return createDefaultEdges().stream()
							.filter(edge -> !(edge.getBegin().equals(allNode(1)) && edge.getEnd().equals(allNode(3))))
							.collect(Collectors.toList());
				}, this::createDefaultWeight, (traffic, i) -> {
					switch (i) {
					case 3:
						return traffic.setVehicles(
								List.of(Vehicle.create(site(0), site(1)).setLocation(traffic.getEdge().getLength())));
					default:
						return traffic;
					}
				});
		final StatusBuilder result = builder.moveLastVehicleAt(traffic(3));
		assertNotNull(result);

		result.getTraffics().forEach(traffic -> {
			assertThat(traffic.getVehicles(), empty());
		});
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
	public void simulationProcess(final double time) {
		final double limitTime = time + 0.4;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 0:
				return traffic.setVehicles(List.of(Vehicle.create(site(0), site(1)).setLocation(498)));
			case 2:
				return traffic.setVehicles(List.of(Vehicle.create(site(0), site(1)).setLocation(498)));
			default:
				return traffic;
			}
		});

		final StatusBuilder result = StatusBuilder.simulationProcess(builder);

		assertNotNull(result);

		final Vehicle v0 = traffic(0).getLast();
		final Vehicle v2 = traffic(2).getLast();

		final EdgeTraffic nt0 = traffic(result, 0).get();
		assertThat(nt0.getTime(), equalTo(limitTime));
		assertThat(nt0.getVehicles(), hasSize(1));
		assertThat(nt0.getLast(), equalTo(v0));
		assertThat(nt0.getLast().getLocation(), equalTo(500.0));

		final EdgeTraffic nt2 = traffic(result, 2).get();
		assertThat(nt2.getTime(), equalTo(limitTime));
		assertThat(nt2.getVehicles(), empty());

		final EdgeTraffic nt4 = traffic(result, 4).get();
		assertThat(nt4.getTime(), equalTo(limitTime));
		assertThat(nt4.getVehicles(), hasSize(1));
		assertThat(nt4.getLast(), equalTo(v2));
		assertThat(nt4.getLast().getLocation(), closeTo(2.0, 1e-3));
	}

	/**
	 * <pre>
	 * Given an initial status at time t
	 * And a vehicle from 0 to 1 in edge 4 at 495 m
	 * And a builder to time t + 1 s
	 * When simulationProcess
	 * Than the vehicle should be at edge 1 at 5.0 m
	 * And all the other edges should be at t+1 time
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void simulationProcess7(final double time) {
		final double limitTime = time + 1;

		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 4:
				return traffic.setVehicles(List.of(Vehicle.create(site(0), site(1)).setLocation(495)));
			default:
				return traffic;
			}
		});

		final StatusBuilder result = StatusBuilder.simulationProcess(builder);

		assertNotNull(result);
		final Set<EdgeTraffic> tr = result.getTraffics();
		assertNotNull(tr);

		final EdgeTraffic nt0 = traffic(result, 0).get();
		assertThat(nt0.getTime(), equalTo(limitTime));

		final EdgeTraffic nt1 = traffic(result, 1).get();
		assertThat(nt1.getTime(), equalTo(limitTime));
		assertThat(nt1.getVehicles(), hasSize(1));
		assertThat(nt1.getLast(), equalTo(traffic(4).getLast()));
		assertTrue(nt1.getLast().isReturning());
		assertThat(nt1.getLast().getLocation(), closeTo(5.0, 1e-3));

		final EdgeTraffic nt2 = traffic(result, 2).get();
		assertThat(nt2.getTime(), equalTo(limitTime));

		final EdgeTraffic nt3 = traffic(result, 3).get();
		assertThat(nt3.getTime(), equalTo(limitTime));

		final EdgeTraffic nt4 = traffic(result, 4).get();
		assertThat(nt4.getTime(), equalTo(limitTime));

		final EdgeTraffic nt5 = traffic(result, 5).get();
		assertThat(nt5.getTime(), equalTo(limitTime));
	}

	/**
	 * <pre>
	 * Given an initial status at time t
	 * And a vehicle from 0 to 1 in edge 4 at 495 m
	 * And busy traffic on edge 1
	 * And a builder to time t + 10 s
	 * When simulationProcess
	 * Than the vehicle should be at edge 4 at 500 m
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void simulationProcessBusyDestination(final double time) {
		final double limitTime = time + 0.5;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 1: {
				final List<Vehicle> vs = IntStream.range(0, 995)
						.mapToObj(j -> Vehicle.create(site(0), site(1)).setReturning(true).setLocation(j + 5))
						.collect(Collectors.toList());
				return traffic.setVehicles(vs).setTime(limitTime);
			}
			case 4: {
				final List<Vehicle> vs = List.of(Vehicle.create(site(0), site(1)).setLocation(495));
				return traffic.setVehicles(vs);
			}
			default:
				return traffic;
			}
		});

		final StatusBuilder result = StatusBuilder.simulationProcess(builder);
		assertNotNull(result);

		final EdgeTraffic nt4 = traffic(result, 4).get();
		assertThat(nt4.getTime(), equalTo(limitTime));
		assertThat(nt4.getVehicles(), hasSize(1));

		final Vehicle v = nt4.getLast();
		assertThat(v, equalTo(traffic(4).getLast()));
	}

	/**
	 * Given an initial status at time t without any vehicle<br>
	 * And a builder to time t + 1 s<br>
	 * When simulationProcess<br>
	 * Than no all traffic should be at time t+1<br>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void simulationProcessEmpty(final double time) {
		final double limitTime = time + 1;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> traffic);

		final StatusBuilder result = StatusBuilder.simulationProcess(builder);
		assertNotNull(result);

		final Set<EdgeTraffic> traffics = result.getTraffics();
		assertThat(traffics, hasSize(6));
		traffics.forEach(traffic -> {
			assertThat(traffic.getTime(), equalTo(limitTime));
		});
	}

	/**
	 * <pre>
	 * Given an initial status at time t
	 * And a vehicle from 0 to 1 in edge 4 at 495 m
	 * And a builder to time t + 0.5 s
	 * When simulationProcess
	 * Than the vehicle should be at edge 1 at 5.0 m
	 * And all the other edges should be at t+1 time
	 * </pre>
	 */
	@ParameterizedTest
	@MethodSource("timeRange")
	public void simulationProcessStale(final double time) {
		final double limitTime = time + 0.5;
		final StatusBuilder builder = createBuilder(time, limitTime, (traffic, i) -> {
			switch (i) {
			case 0: {
				final List<Vehicle> vs = IntStream.range(0, 995)
						.mapToObj(j -> Vehicle.create(site(0), site(1)).setReturning(true).setLocation(j + 5))
						.collect(Collectors.toList());
				return traffic.setVehicles(vs);
			}
			case 3: {
				final List<Vehicle> vs = IntStream.range(0, 995)
						.mapToObj(j -> Vehicle.create(site(1), site(0)).setReturning(true).setLocation(j + 5))
						.collect(Collectors.toList());
				return traffic.setVehicles(vs);
			}
			default:
				return traffic;
			}
		});

		final StatusBuilder result = StatusBuilder.simulationProcess(builder);
		assertNotNull(result);

		final Set<EdgeTraffic> traffics = result.getTraffics();
		assertThat(traffics, hasSize(6));
		traffics.forEach(traffic -> {
			assertThat(traffic.getTime(), equalTo(limitTime));
		});
	}
}
