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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.mmarini.routes.model.Constants;

/**
 * A traffic information for a given edge
 */
public class EdgeTraffic implements Comparable<EdgeTraffic>, Constants {

	/**
	 * Returns the new empty traffic information for an edge
	 *
	 * @param edge the edge
	 */
	public static EdgeTraffic create(final MapEdge edge) {
		return new EdgeTraffic(edge, Collections.emptyList(), 0, OptionalDouble.empty());
	}

	private final MapEdge edge;
	private final List<Vehicle> vehicles;
	private final double time;
	private final OptionalDouble lastTravelTime;

	/**
	 * Creates the empty traffic information for an edge
	 *
	 * @param edge     the edge
	 * @param vehicles the ordered by distance list of vehicles in the edge
	 * @param time     the instant of information
	 */
	protected EdgeTraffic(final MapEdge edge, final List<Vehicle> vehicles, final double time,
			final OptionalDouble lastTravelTime) {
		super();
		assert edge != null;
		this.edge = edge;
		this.vehicles = vehicles;
		this.time = time;
		this.lastTravelTime = lastTravelTime;
	}

	/**
	 * Returns the edge traffic with a new vehicle
	 *
	 * @param vehicle the vehicle
	 * @param time    the instant of insertion
	 */
	public EdgeTraffic addVehicle(final Vehicle vehicle, final double time) {
		assert !isBusy();
		final OptionalDouble nextDistance = vehicles.isEmpty() ? OptionalDouble.empty()
				: OptionalDouble.of(vehicles.get(0).getLocation());
		final Vehicle v = vehicle.setLocation(0).move(edge, this.time - time, nextDistance).getElem1()
				.setEdgeEntryTime(time);
		final List<Vehicle> newVehicles = Stream.concat(Stream.of(v), vehicles.stream()).collect(Collectors.toList());
		final EdgeTraffic result = setVehicles(newVehicles);
		return result;
	}

	/**
	 * Returns the cross of edges
	 *
	 * @param other
	 * @see org.mmarini.routes.model.v2.MapEdge#cross(MapEdge)
	 */
	public int compareDirection(final EdgeTraffic other) {
		return edge.cross(other.edge);
	}

	/*
	 */

	/**
	 * Return the comparison between the exiting time.
	 * <ul>
	 * <li>zero both edges have no vehicle or have the same exit times</li>
	 * <li>negative if other has no vehicle or edge has exit time lower then
	 * other</li>
	 * <li>positive if has no vehicle or it has exit time greater the other
	 * edge</li>
	 * </ul>
	 *
	 * @param other the other traffic edge
	 */
	public int compareExitTime(final EdgeTraffic other) {
		final DoubleStream ets = getExitTime().stream();
		final DoubleStream oets = other.getExitTime().stream();

		final int result = ets.mapToInt(et -> oets.mapToInt(oet -> {
			// Both with vehicle
			return Double.compare(et, oet);
		}).findAny().orElse(
				// with vehicles and other without vehicles
				-1)).findAny().orElseGet(() -> oets.mapToInt(oet ->
		// without vehicles and other with vehicles
		1).findAny().orElse(
				// both without vehicles
				0));
		return result;
	}

	/**
	 * Return positive if edge priority higher then other edge priority, negative if
	 * edge priority greater then other edge priority or 0 if same priority
	 *
	 * @param other the other traffic edge
	 */
	public int comparePriority(final EdgeTraffic other) {
		return Integer.compare(edge.getPriority(), other.edge.getPriority());
	}

	/**
	 * Returns the natural order of edge
	 *
	 * @param other other edge
	 * @see org.mmarini.routes.model.v2.MapEdge#compareTo(MapEdge)
	 */
	@Override
	public int compareTo(final EdgeTraffic other) {
		return edge.compareTo(other.edge);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EdgeTraffic other = (EdgeTraffic) obj;
		if (!edge.equals(other.edge)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the edge
	 */
	public MapEdge getEdge() {
		return edge;
	}

	/**
	 * Returns the expected time of exit of last vehicle in the edge
	 */
	public OptionalDouble getExitTime() {
		final Optional<Double> result1 = getLast().map(last -> {
			return (edge.getLength() - last.getLocation()) / edge.getSpeedLimit() + time;
		});
		final OptionalDouble result = vehicles.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(result1.get());
		return result;
	}

	/**
	 * Returns the last vehicle in the edge
	 */
	public Optional<Vehicle> getLast() {
		return vehicles.isEmpty() ? Optional.empty() : Optional.of(vehicles.get(vehicles.size() - 1));
	}

	/**
	 * Returns the last travel time if exists
	 */
	OptionalDouble getLastTravelTime() {
		return lastTravelTime;
	}

	/**
	 * Returns the instant of traffic information
	 */
	public double getTime() {
		return time;
	}

	/**
	 *
	 * @return
	 */
	public double getTrafficCongestion() {
		final int n = getVehicles().size();
		final double length = edge.getLength();
		final double max = length / VEHICLE_LENGTH + 1;
		final double mid = length / (VEHICLE_LENGTH + edge.getSpeedLimit() * REACTION_TIME) + 1;
		final double optim = Math.min(n / mid, 1);
		final double over = Math.max((n - mid) / (max - mid), 0);
		final double congestion = (over > 0 ? Math.pow(over, 0.25) + 1 : optim) * 0.5;
		return congestion;
	}

	/**
	 * Returns the estimated travel time in the edge.
	 * <p>
	 * The estimation is the travel time of last exited vehicle or the minimum
	 * travel time of edge if no vehicles are present in the edge
	 * </p>
	 */
	public double getTravelTime() {
		return lastTravelTime.orElseGet(() -> edge.getTransitTime());
	}

	/**
	 * Returns the ordered by distance list of vehicles in the edge
	 */
	public List<Vehicle> getVehicles() {
		return vehicles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + edge.hashCode();
		return result;
	}

	/**
	 * Returns true if all the traffics are coming from left
	 *
	 * @param traffics traffics
	 */
	public boolean isAllFromLeft(final Set<EdgeTraffic> traffics) {
		final boolean result = traffics.parallelStream().allMatch(traffic -> {
			return this.equals(traffic) || this.compareDirection(traffic) > 0;
		});
		return result;
	}

	/**
	 *
	 * @return
	 */
	public boolean isBusy() {
		return !getVehicles().isEmpty() && getVehicles().get(0).getLocation() <= VEHICLE_LENGTH;
	}

	/**
	 * Returns true if the traffic edge is crossing with other
	 *
	 * @param the other traffic edge
	 */
	public boolean isCrossing(final EdgeTraffic other) {
		return edge.isCrossing(other.edge);
	}

	/**
	 * Returns the traffic information by moving all the vehicles to the given time
	 *
	 * @param time the time
	 */
	public EdgeTraffic moveToTime(final double time) {
		if (time <= this.time) {
			return this;
		}
		if (vehicles.isEmpty()) {
			return setTime(time);
		}
		// The moving order is from the last to the first vehicle in the edge
		final List<Vehicle> reversed = new ArrayList<>(vehicles);
		Collections.reverse(reversed);

		final List<Vehicle> newVehicles = new ArrayList<>();
		// Max delta time
		OptionalDouble nextLocation = OptionalDouble.empty();
		final double dt = time - this.time;
		for (final Vehicle v : reversed) {
			final Tuple2<Vehicle, Double> tuple = v.move(edge, dt, nextLocation);
			final Vehicle movedVehicle = tuple.getElem1();
			nextLocation = OptionalDouble.of(movedVehicle.getLocation());
			newVehicles.add(0, movedVehicle);
		}

		final EdgeTraffic result = setVehicles(newVehicles).setTime(time);
		return result;
	}

	/**
	 * Returns the traffic information by moving all the vehicles to a given maximum
	 * time
	 *
	 * @param time the maximum time
	 */
	public EdgeTraffic moveVehicles(final double time) {
		if (time <= this.time) {
			return this;
		}
		if (vehicles.isEmpty()) {
			return setTime(time);
		}
		// The moving order is from the last to the first vehicle in the edge
		final List<Vehicle> reversed = new ArrayList<>(vehicles);
		Collections.reverse(reversed);

		final List<Vehicle> newVehicles = new ArrayList<>();
		// Max delta time
		final double dtMax = time - this.time;

		OptionalDouble nextLocation = OptionalDouble.empty();
		OptionalDouble dt = OptionalDouble.empty();

		for (final Vehicle v : reversed) {
			final double dt1 = dt.orElse(dtMax);
			final Tuple2<Vehicle, Double> tuple = v.move(edge, dt1, nextLocation);
			final Vehicle movedVehicle = tuple.getElem1();
			nextLocation = OptionalDouble.of(movedVehicle.getLocation());
			if (dt.isEmpty()) {
				dt = OptionalDouble.of(tuple.getElem2());
			}
			newVehicles.add(0, movedVehicle);
		}

		final EdgeTraffic result = setVehicles(newVehicles).setTime(this.time + dt.getAsDouble());
		return result;
	}

	/**
	 * Returns the edge traffic with last vehicle moved and the last travel time set
	 */
	public EdgeTraffic removeLast() {
		final EdgeTraffic result = getLast().filter(vehicle -> {
			return vehicle.getLocation() == edge.getLength();
		}).map(vehicle -> {
			final List<Vehicle> newFromVehicle = vehicles.stream().takeWhile(v -> !vehicle.equals(v))
					.collect(Collectors.toList());
			final EdgeTraffic result1 = newFromVehicle.isEmpty() ? this
					: setLastTravelTime(OptionalDouble.of(time - vehicle.getEdgeEntryTime()));
			final EdgeTraffic result2 = result1.setVehicles(newFromVehicle);
			return result2;
		}).orElse(this);
		return result;
	}

	/**
	 * Returns a traffic information with the new edge
	 *
	 * @param edge the edge
	 */
	public EdgeTraffic setEdge(final MapEdge edge) {
		final EdgeTraffic result = getLast().map(last -> {
			if (last.getLocation() <= edge.getLength()) {
				// no shrink
				return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
			} else {
				// Shrink and cut
				final List<Vehicle> reverse = new ArrayList<>(vehicles);
				Collections.reverse(reverse);

				final List<Vehicle> newVehicles = new ArrayList<>();
				double maxLocation = edge.getLength();
				for (final Vehicle v : reverse) {
					if (maxLocation >= 0) {
						if (v.getLocation() < maxLocation) {
							newVehicles.add(v);
							maxLocation = v.getLocation() - VEHICLE_LENGTH;
						} else {
							newVehicles.add(v.setLocation(maxLocation));
							maxLocation -= VEHICLE_LENGTH;
						}
					}
				}
				Collections.reverse(newVehicles);
				return new EdgeTraffic(edge, newVehicles, time, lastTravelTime);
			}
		}).orElseGet(() -> {
			return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
		});
		return result;
	}

	/**
	 * Returns the traffic information with the given last travel time
	 *
	 * @param lastTravelTime last travel time
	 */
	public EdgeTraffic setLastTravelTime(final OptionalDouble lastTravelTime) {
		return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
	}

	/**
	 * Returns the traffic information with the given time
	 *
	 * @param time the instant
	 */
	public EdgeTraffic setTime(final double time) {
		return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
	}

	/**
	 * Returns the traffic information with the given vehicle list
	 *
	 * @param vehicles the list of vehicles
	 */
	public EdgeTraffic setVehicles(final List<Vehicle> vehicles) {
		return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("EdgeTraffic [").append(edge.getId()).append("]");
		return builder.toString();
	}
}
