//scusa
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.model.v2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder of simulation status.
 * <p>
 * Builds the simulation status starting from an status applying the simulation
 * process for a given time interval
 * </p>
 */
public class TrafficsBuilder implements Constants {

	/** Returns an empty traffics builders. */
	public static TrafficsBuilder create() {
		return new TrafficsBuilder(Traffics.create(), Collections.emptySet(), 0);
	}

	/**
	 * Returns a builder from an initial status for a given instant.
	 *
	 * @param status the status
	 * @param time   the instant
	 */
	public static TrafficsBuilder create(final Traffics status, final double time) {
		return new TrafficsBuilder(status, status.getTraffics(), time);
	}

	/**
	 * Returns a random integer number with Poisson distribution and a given
	 * average.
	 *
	 * @param lambda the average of generated number
	 * @param random the random generator
	 */
	static int nextPoison(final double lambda, final Random random) {
		int k = -1;
		double p = 1;
		final double l = Math.exp(-lambda);
		do {
			++k;
			p *= random.nextDouble();
		} while (p > l);
		return k;
	}

	/**
	 * Returns the new traffic builder applying the simulation process.
	 *
	 * @param builder the initial builder
	 */
	static TrafficsBuilder simulationProcess(final TrafficsBuilder builder) {
		TrafficsBuilder st = builder;
		for (;;) {
			// Move all vehicles in the edges
			st = st.moveVehiclesInAllEdges();

			if (st.isCompleted()) {
				break;
			}

			// Find the edges candidates to vehicle move
			final Set<EdgeTraffic> earliests = st.findCandidates();

			// Filter the priority and the next free to move vehicles
			st.getRoutePlanner();
			final TrafficsBuilder st1 = st;

			final Optional<EdgeTraffic> edg = earliests.parallelStream().filter(ed -> {
				final Optional<EdgeTraffic> next1 = st1.getNextTraffic(ed);
				return next1.map(next -> {
					return !next.isBusy() && st1.isPrior(ed);
				}).orElse(true);
			}).findAny();

			st = edg.map(edge -> {
				// at least an edge with priority and free next edge
				return st1.moveLastVehicleAt(edge);
			}).orElseGet(() -> {
				// no edge with priority and free next edge
				// stop vehicles to next instant
				final double nextTime = st1.getNextMinimumTime();
				final Set<EdgeTraffic> nextTraffics = earliests.parallelStream().map(ed -> ed.moveToTime(nextTime))
						.collect(Collectors.toSet());
				return st1.addTraffics(nextTraffics);
			});
		}
		return st;

	}

	private final Set<EdgeTraffic> traffics;
	private final double time;
	private final Traffics initialStatus;

	private RoutePlanner routePlanner;

	/**
	 * Creates a traffic builder.
	 *
	 * @param status   the current traffic information
	 * @param traffics the current edges traffic
	 * @param time     the target simulation instant
	 */
	protected TrafficsBuilder(final Traffics status, final Set<EdgeTraffic> traffics, final double time) {
		super();
		this.initialStatus = status;
		this.traffics = traffics;
		this.time = time;
	}

	/**
	 * Returns the status builder with new traffics added.
	 *
	 * @param traffics the added traffics
	 */
	private TrafficsBuilder addTraffics(final Collection<EdgeTraffic> traffics) {
		if (traffics.isEmpty()) {
			return this;
		} else {
			final Set<EdgeTraffic> newTraffics = new HashSet<>(this.traffics);
			traffics.forEach(t -> {
				newTraffics.remove(t);
				newTraffics.add(t);
			});
			return setTraffics(newTraffics);
		}
	}

	/**
	 * Returns the builder with a given edge traffic.
	 *
	 * @param edge the edge traffic
	 */
	private TrafficsBuilder addTraffics(final EdgeTraffic edge) {
		final Set<EdgeTraffic> newTraffics = this.traffics.parallelStream().map(traffic -> {
			return edge.equals(traffic) ? edge : traffic;
		}).collect(Collectors.toSet());
		return setTraffics(newTraffics);
	}

	/** Returns the simulation status at the target instant. */
	public Traffics build(final Random random) {
		final TrafficsBuilder finalStatus = simulationProcess(this).createVehicles(random);
		final Traffics resut = finalStatus.initialStatus.setTraffics(finalStatus.traffics);
		return resut;
	}

	/**
	 * Returns the status builder with n new vehicles.
	 *
	 * @param n    number of vehicles
	 * @param from departure
	 * @param to   destination
	 * @param ed   the edge
	 * @param t0   the insertion time
	 */
	TrafficsBuilder createVehicles(final int n, final MapNode from, final MapNode to, final EdgeTraffic ed,
			final double t0) {
		if (n <= 0) {
			return this;
		} else {
			EdgeTraffic newEdge = ed;
			for (int i = 0; i < n && !newEdge.isBusy(); i++) {
				final Vehicle v = Vehicle.create(from, to);
				newEdge = newEdge.addVehicle(v, t0);
			}
			final TrafficsBuilder result = addTraffics(newEdge);
			return result;
		}
	}

	/**
	 * Returns the traffic builder generating random new vehicles.
	 *
	 * @param random the random generator
	 */
	TrafficsBuilder createVehicles(final Random random) {
		final double frequence = initialStatus.getMap().getFrequence();
		final Optional<Double> t0o = initialStatus.getTraffics().parallelStream().findAny().map(EdgeTraffic::getTime);
		final TrafficsBuilder result1 = t0o.map(t0 -> {
			final double dt = time - t0;
			final int noSites = initialStatus.getMap().getSites().size();
			final double lambda0 = frequence * dt / (noSites - 1) / 2;
			final RoutePlanner planner = getRoutePlanner();
			TrafficsBuilder result = this;
			for (final Entry<Tuple2<MapNode, MapNode>, Double> entry : initialStatus.getMap().getWeights().entrySet()) {
				final MapNode from = entry.getKey().get1();
				final MapNode to = entry.getKey().get2();
				if (!from.equals(to)) {
					final Optional<EdgeTraffic> edge = planner.nextEdge(from, to);
					final double weight = entry.getValue();
					final int n = nextPoison(lambda0 * weight, random);
					final TrafficsBuilder builder = result;
					result = edge.map(ed -> {
						final TrafficsBuilder res1 = builder.createVehicles(n, from, to, ed, t0);
						return res1;
					}).orElseGet(() -> {
						return builder;
					});
				}
			}
			return result;
		}).orElse(this);
		return result1;
	}

	/**
	 * Returns the edge candidate of vehicle movement.
	 * <p>
	 * The candidates are the edges with the lowest simulation time
	 * </p>
	 */
	Set<EdgeTraffic> findCandidates() {
		final double minTime = getMinimumTime();
		final Set<EdgeTraffic> result = traffics.parallelStream().filter(et -> et.getTime() == minTime)
				.collect(Collectors.toSet());
		return result;
	}

	/** Returns the initial status. */
	Traffics getInitialStatus() {
		return initialStatus;
	}

	/** Returns the minimum simulation time. */
	double getMinimumTime() {
		final double result = traffics.parallelStream().mapToDouble(EdgeTraffic::getTime).min().orElseGet(() -> time);
		return result;
	}

	/** Returns the next minimum simulation time. */
	double getNextMinimumTime() {
		final double min = getMinimumTime();
		final double result = traffics.parallelStream().mapToDouble(EdgeTraffic::getTime).filter(t -> t != min).min()
				.orElseGet(() -> time);
		return result;
	}

	/**
	 * Returns the next edge for a give edge.
	 *
	 * @param edge edge
	 */
	Optional<EdgeTraffic> getNextTraffic(final EdgeTraffic edge) {
		final RoutePlanner planner = getRoutePlanner();
		final Optional<EdgeTraffic> result1 = edge.getLast().flatMap(vehicle -> {
			final MapNode to = vehicle.getTarget();
			final Optional<EdgeTraffic> result = planner.nextEdge(edge.getEdge().getEnd(), to);
			return result;
		});
		return result1;
	}

	/** Returns the route planner with lazy creation. */
	RoutePlanner getRoutePlanner() {
		if (routePlanner == null) {
			routePlanner = RoutePlanner.create().setEdgeStats(traffics);
		}
		return routePlanner;
	}

	/** Returns the current traffic information. */
	Set<EdgeTraffic> getTraffics() {
		return traffics;
	}

	/**
	 * Returns the incoming edges traffics stream for a given edge.
	 *
	 * @param trafficEdge the edge
	 */
	private Stream<EdgeTraffic> incomingTrafficStream(final EdgeTraffic trafficEdge) {
		return traffics.parallelStream().filter(te -> !te.getVehicles().isEmpty() && te.isCrossing(trafficEdge));
	}

	/** Returns true if not any edge has not completed the simulation. */
	boolean isCompleted() {
		return !traffics.parallelStream().anyMatch(edge -> edge.getTime() < time);
	}

	/**
	 * Returns true if the traffic edge is has priority.
	 *
	 * @param trafficEdge edge
	 */
	boolean isPrior(final EdgeTraffic trafficEdge) {
		final boolean result = trafficEdge.getExitTime().stream().mapToObj(exitTime -> {
			final double limitTime = exitTime + REACTION_TIME;
			// Filter all the crossing traffics within expected exit time within limit
			final Set<EdgeTraffic> xTraffics = incomingTrafficStream(trafficEdge)
					.filter(traffic -> traffic.getExitTime().getAsDouble() <= limitTime).collect(Collectors.toSet());
			if (xTraffics.size() == 1) {
				// no other crossing traffics
				return true;
			}
			// check for higher priority edge
			final boolean exitsHigherPriorityTraffics = xTraffics.parallelStream()
					.anyMatch(traffic -> traffic.comparePriority(trafficEdge) > 0);
			if (exitsHigherPriorityTraffics) {
				return false;
			}
			// filter same priority edges
			final Set<EdgeTraffic> isoPriorities = xTraffics.parallelStream()
					.filter(traffic -> traffic.comparePriority(trafficEdge) == 0).collect(Collectors.toSet());
			if (isoPriorities.size() == 1) {
				// no other crossing traffic with same priority
				return true;
			}
			// Check for traffics coming from right priority
			if (trafficEdge.isAllFromLeft(isoPriorities)) {
				// All traffics are coming from left
				return true;
			}
			// Check for stale
			// Look for traffics from right with absolute priority
			final boolean stale = !isoPriorities.parallelStream()
					.anyMatch(traffic -> traffic.isAllFromLeft(isoPriorities));
			if (!stale) {
				// there is at least an edge with traffic with absolute priority
				return false;
			}

			// Check for arrivals
			final double earliestTime = isoPriorities.parallelStream()
					.flatMapToDouble(traffic -> traffic.getExitTime().stream()).min().getAsDouble();
			final EdgeTraffic priorTraffic = isoPriorities.stream().filter(
					traffic -> traffic.getExitTime().stream().mapToObj(time -> time == earliestTime).findAny().get())
					.sorted().findFirst().get();
			return priorTraffic.equals(trafficEdge);
		}).findAny().orElseGet(
				// no vehicles on traffic edge
				() -> false);
		return result;
	}

	/**
	 * Returns the status builder with last vehicle of the given edge moved to next
	 * edge.
	 *
	 * @param traffic the edge
	 */
	TrafficsBuilder moveLastVehicleAt(final EdgeTraffic traffic) {
		final TrafficsBuilder result = traffic.getLast().map(vehicle -> {
			if (!vehicle.getTarget().equals(traffic.getEdge().getEnd())) {
				final EdgeTraffic newTraffic = traffic.removeLast();
				// Vehicle not at target
				return getNextTraffic(traffic).map(nextTraffic -> {
					// Move vehicle
					return addTraffics(List.of(newTraffic, nextTraffic.addVehicle(vehicle, traffic.getTime())));
				}).orElseGet(() -> {
					// No way => remove vehicle
					return addTraffics(newTraffic);
				});
			} else if (vehicle.isReturning()) {
				// Vehicle arrived at departure => remove vehicle
				return addTraffics(traffic.removeLast());
			} else {
				// Vehicle arrived at destination => returning
				final Optional<EdgeTraffic> nextTraffic = getRoutePlanner().nextEdge(vehicle.getDestination(),
						vehicle.getDeparture());
				return nextTraffic.map(traffic1 -> {
					if (traffic1.isBusy()) {
						final List<Vehicle> newVehicles = traffic.getVehicles().stream().map(v -> {
							return v.equals(vehicle) ? vehicle.setReturning(true) : v;
						}).collect(Collectors.toList());
						return addTraffics(traffic.setVehicles(newVehicles));
					} else {
						// Move vehicle
						final EdgeTraffic newTraffic = traffic.removeLast();
						final EdgeTraffic newNext = traffic1.addVehicle(vehicle.setReturning(true), traffic.getTime());
						return addTraffics(List.of(newTraffic, newNext));
					}
				}).orElseGet(() -> {
					// No way => remove vehicle
					return addTraffics(traffic.removeLast());
				});
			}
		}).orElse(this);
		return result;
	}

	/** Returns the status builder with all vehicles moved in the edges. */
	TrafficsBuilder moveVehiclesInAllEdges() {
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream().map(et -> et.moveVehicles(time))
				.collect(Collectors.toSet());
		return setTraffics(newTraffics);
	}

	/**
	 * Returns the status builder for an initial status.
	 *
	 * @param initialStatus the initial status
	 */
	public TrafficsBuilder setInitialStatus(final Traffics initialStatus) {
		return new TrafficsBuilder(initialStatus, traffics, time);
	}

	/**
	 * Returns the traffic builder with a given set of edge traffic.
	 *
	 * @param traffics the edge traffics
	 */
	public TrafficsBuilder setTraffics(final Set<EdgeTraffic> traffics) {
		return new TrafficsBuilder(initialStatus, traffics, time);
	}
}
