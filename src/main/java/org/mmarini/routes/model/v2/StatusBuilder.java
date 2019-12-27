//

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
import java.util.Set;
import java.util.stream.Collectors;

import org.mmarini.routes.model.Constants;

/**
 * A builder of simulation status
 * <p>
 * Builds the simulation status starting from an initial status applying the
 * simulation process for a given time interval
 * </p>
 */
public class StatusBuilder implements Constants {
	/**
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	static int compareTrafficsForTime(final EdgeTraffic a, final EdgeTraffic b) {
		return Double.compare(a.getTime(), b.getTime());
	}

	/**
	 *
	 * @return
	 */
	public static StatusBuilder create() {
		return new StatusBuilder(SimulationStatus.create(), Collections.emptySet(), 0);
	}

	/**
	 * Returns a builder from an initial status for a given instant
	 *
	 * @param status the status
	 * @param time   the instant
	 */
	public static StatusBuilder create(final SimulationStatus status, final double time) {
		return new StatusBuilder(status, status.getTraffic(), time);
	}

	/**
	 * Select the edge with priority.
	 *
	 * @param edgesInfo the list of edge
	 * @return
	 */
	static EdgeTraffic selectByPriority(final Collection<EdgeTraffic> edgesInfo) {
		// Compute the time limit
		final double timeLimit = edgesInfo.stream().min(StatusBuilder::compareTrafficsForTime).get().getTime()
				+ REACTION_TIME;
		// Filter the conflicting edges
		final List<EdgeTraffic> filtered = edgesInfo.stream().filter(ei -> ei.getTime() <= timeLimit)
				.collect(Collectors.toList());
		// Compute the max edge priority
		final int priority = filtered.stream().mapToInt(f -> f.getEdge().getPriority()).max().getAsInt();
		// Filter the max priority edges
		final List<EdgeTraffic> maxPriority = filtered.stream().filter(f -> f.getEdge().getPriority() == priority)
				.collect(Collectors.toList());
		// Select for edge coming from right direction
		final Optional<EdgeTraffic> selected = maxPriority.stream().filter(edge -> maxPriority.stream()
				.filter(f -> f != edge).allMatch(other -> edge.getEdge().cross(other.getEdge()) > 0)).findFirst();
		final EdgeTraffic result = selected.orElseGet(() -> maxPriority.get(0));
		return result;
	}

	private final Set<EdgeTraffic> traffics;
	private final double time;
	private final SimulationStatus initialStatus;

	/**
	 * @param status
	 * @param initialStatus
	 * @param time
	 */
	protected StatusBuilder(final SimulationStatus status, final Set<EdgeTraffic> traffics, final double time) {
		super();
		this.initialStatus = status;
		this.traffics = traffics;
		this.time = time;
	}

	/**
	 * Returns the status builder with new traffics added
	 *
	 * @param traffics the added traffics
	 * @return the status builder with new traffics added
	 */
	StatusBuilder addTraffics(final Collection<EdgeTraffic> traffics) {
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
	 *
	 * @param egde
	 */
	private StatusBuilder addTraffics(final EdgeTraffic egde) {
		final Set<EdgeTraffic> newTraffics = new HashSet<>(this.traffics);
		newTraffics.remove(egde);
		newTraffics.add(egde);
		return setTraffics(newTraffics);
	}

	/**
	 * Returns the simulation status at the given instant
	 */
	public SimulationStatus build() {
		final StatusBuilder st = simulate();
		return st.initialStatus.setTraffics(st.getTraffics());
	}

	/**
	 *
	 * @return
	 */
	TrafficStats buildTrafficStats() {
		final TrafficStats result = TrafficStats.create().setEdgeStats(traffics);
		return result;
	}

	/**
	 *
	 * @return
	 */
	public StatusBuilder createVehicles() {
		final double frequence = initialStatus.getFrequence();
		final double t0 = initialStatus.getTraffic().stream().findAny().map(x -> x.getTime()).get();
		final double dt = time - t0;
		final int noSites = getInitialStatus().getMap().getSites().size();
		final double lambda0 = frequence * dt / (noSites - 1) / 2;
		final TrafficStats ts = buildTrafficStats();
		StatusBuilder result = this;
		for (final Entry<Tuple2<SiteNode, SiteNode>, Double> entry : initialStatus.getWeights().entrySet()) {
			final SiteNode from = entry.getKey().getElem1();
			final SiteNode to = entry.getKey().getElem2();
			if (!from.equals(to)) {
				final Optional<EdgeTraffic> edge = ts.nextEdge(from, to);
				final double weight = entry.getValue();
				final int n = initialStatus.nextPoison(lambda0 * weight);
				final StatusBuilder builder = result;
				result = edge.map(ed -> builder.createVehicles(n, from, to, ed, t0)).orElseGet(() -> this);
			}
		}
		return result;
	}

	/**
	 * Returns the status builder with n new vehicles
	 *
	 * @param n    number of vehicles
	 * @param from departure
	 * @param to   destination
	 * @param ed   the edge
	 * @param t0   the insertion time
	 */
	StatusBuilder createVehicles(final int n, final SiteNode from, final SiteNode to, final EdgeTraffic ed,
			final double t0) {
		if (n <= 0) {
			return this;
		} else {
			EdgeTraffic newEdge = ed;
			for (int i = 0; i < n && !newEdge.isBusy(); i++) {
				final Vehicle v = Vehicle.create(from, to);
				newEdge = newEdge.addVehicle(v, t0);
			}
			return addTraffics(newEdge);
		}
	}

	/**
	 * Returns the initial status
	 */
	SimulationStatus getInitialStatus() {
		return initialStatus;
	}

	/**
	 *
	 * @return
	 */
	Set<EdgeTraffic> getTraffics() {
		return traffics;
	}

	/**
	 * Returns the status builder after moving all vehicles
	 */
	StatusBuilder moveAllVehicles() {
		StatusBuilder st = this;
		for (;;) {
			final StatusBuilder st1 = st;
			final Set<EdgeTraffic> newTraffics = st.traffics.parallelStream().map(et -> et.moveVehicles(st1.time))
					.collect(Collectors.toSet());
			st = setTraffics(newTraffics);
			final EdgeTraffic earlier = newTraffics.parallelStream().min(StatusBuilder::compareTrafficsForTime).get();
			// TODO moving from head
			if (earlier.getTime() == st.time) {
				break;
			}
			// Filter the crossing edge
			final MapNode end = earlier.getEdge().getEnd();
			final Set<EdgeTraffic> crossingEdges = newTraffics.parallelStream()
					.filter(et -> !et.getVehicles().isEmpty() && et.getEdge().getEnd().equals(end))
					.collect(Collectors.toSet());
			st = st.moveVehicleAtCross(crossingEdges);
		}
		return st;
	}

	/**
	 * Returns the status builder with the last vehicle of a given edge moved to a
	 * new edge.
	 *
	 * @param from source edge
	 * @param to   destination edge
	 * @return the new status builder
	 */
	StatusBuilder moveVehicle(final EdgeTraffic from, final EdgeTraffic to) {
		final Vehicle last = from.getLast();
		final EdgeTraffic newFrom = from.removeLast();
		final EdgeTraffic newTo = to.addVehicle(last, from.getTime());
		final StatusBuilder result = addTraffics(List.of(newFrom, newTo));
		return result;
	}

	/**
	 * Returns the status builder with the last vehicle of priority moved to cross
	 *
	 * @param crossingEdges the sorted by time incoming edges at the crossing node
	 */
	StatusBuilder moveVehicleAtCross(final Collection<EdgeTraffic> crossingEdges) {
		// Select the priority inbound edge in the cross
		final EdgeTraffic priorityEdge = selectByPriority(crossingEdges);
		// Select all other inbound edges in the cross
		final List<EdgeTraffic> idleEdges = crossingEdges.stream()
				.filter(e -> !e.equals(priorityEdge) && Double.compare(e.getTime(), priorityEdge.getTime()) < 0)
				.collect(Collectors.toList());
		// Get the exiting vehicle
		final Vehicle vehicle = priorityEdge.getLast();
		if (vehicle.getTarget().equals(priorityEdge.getEdge().getEnd())) {
			// Vehicle reached the destination
			if (vehicle.isReturning()) {
				// Remove the vehicle
				final EdgeTraffic newegde = priorityEdge.removeLast();
				final StatusBuilder result = addTraffics(newegde).stopEdges(idleEdges, priorityEdge.getTime());
				return result;
			} else {
				// Invert vehicle
				final EdgeTraffic retOutboundEdge = priorityEdge.setLast(vehicle.setReturning(true));
				final StatusBuilder result = addTraffics(retOutboundEdge);
				return result;
			}
		} else {
			final TrafficStats ts = buildTrafficStats();
			final Optional<EdgeTraffic> next = ts.nextEdge(priorityEdge.getEdge().getEnd(), vehicle.getTarget());
			final StatusBuilder result1 = next.stream().map(edge -> {
				if (edge.isBusy()) {
					final double nextTime = edge.getTime();
					final StatusBuilder result = stopEdges(idleEdges, nextTime).stopEdges(List.of(priorityEdge),
							nextTime);
					return result;
				} else {
					final StatusBuilder result = moveVehicle(priorityEdge, edge).stopEdges(idleEdges,
							priorityEdge.getTime());
					return result;
				}
			}).findAny().orElseGet(() -> {
				// Remove the vehicle
				final EdgeTraffic newegde = priorityEdge.removeLast();
				final StatusBuilder result = addTraffics(newegde).stopEdges(idleEdges, priorityEdge.getTime());
				return result;
			});
			return result1;
		}
	}

	/**
	 * Returns the status builder for an initial status
	 *
	 * @param initialStatus the initial status
	 */
	public StatusBuilder setInitialStatus(final SimulationStatus initialStatus) {
		return new StatusBuilder(initialStatus, traffics, time);
	}

	/**
	 *
	 * @param traffics
	 * @return
	 */
	public StatusBuilder setTraffics(final Set<EdgeTraffic> traffics) {
		return new StatusBuilder(initialStatus, traffics, time);
	}

	/**
	 * Returns the status builder after simulation to the given instant
	 */
	StatusBuilder simulate() {
		return moveAllVehicles().createVehicles();
	}

	/**
	 * Returns the status builder with a edge collection stopped until a given
	 * instant
	 *
	 * @param edges the edge collection
	 * @param time  the instant
	 */
	StatusBuilder stopEdges(final Collection<EdgeTraffic> edges, final double time) {
		if (edges.isEmpty()) {
			return this;
		} else {
			final List<EdgeTraffic> newIdleEdges = edges.stream().map(e -> e.setTime(time))
					.collect(Collectors.toList());
			final StatusBuilder result = addTraffics(newIdleEdges);
			return result;
		}
	}
}
