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
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.mmarini.routes.model.Constants;

/**
 * A traffic information for a given edge
 */
public class EdgeTraffic implements Constants {

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
		assert (edge != null);
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
		assert (!isBusy());
		final OptionalDouble nextDistance = vehicles.isEmpty() ? OptionalDouble.empty()
				: OptionalDouble.of(vehicles.get(0).getLocation());
		final Vehicle v = vehicle.setLocation(0).move(edge, this.time - time, nextDistance).getElem1()
				.setEdgeEntryTime(time);
		final List<Vehicle> newVehicles = new ArrayList<>(vehicles);
		newVehicles.add(0, v);
		final EdgeTraffic result = setVehicles(newVehicles);
		return result;
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
		if (edge == null) {
			if (other.edge != null) {
				return false;
			}
		} else if (!edge.equals(other.edge)) {
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
	 * Returns the last vehicle in the edge
	 */
	public Vehicle getLast() {
		assert (!vehicles.isEmpty());
		return vehicles.get(vehicles.size() - 1);
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
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
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
		final List<Vehicle> reversed = new ArrayList<>(vehicles);
		Collections.reverse(reversed);
		final Tuple2<Vehicle, Double> initial = reversed.get(0).move(edge, time - this.time, OptionalDouble.empty());
		final Vehicle v0 = initial.getElem1();
		final double dt = initial.getElem2();
		if (reversed.size() == 1) {
			return setVehicles(List.of(v0)).setTime(this.time + dt);
		}
		double prevLoc = v0.getLocation();
		final List<Vehicle> newVehicles = new ArrayList<>();
		newVehicles.add(v0);
		for (int i = 1; i < reversed.size(); i++) {
			final Vehicle v = reversed.get(i);
			final Vehicle nv = v.move(edge, dt, OptionalDouble.of(prevLoc)).getElem1();
			prevLoc = nv.getLocation();
			newVehicles.add(0, nv);
		}

		final EdgeTraffic result = setVehicles(newVehicles).setTime(this.time + dt);
		return result;
	}

	/**
	 * Returns the edge traffic with last vehicle moved and the last travel time set
	 */
	public EdgeTraffic removeLast() {
		final Vehicle vehicle = getLast();
		assert (vehicle.getLocation() == edge.getDistance());
		final List<Vehicle> newFromVehicle = vehicles.stream().takeWhile(v -> !vehicle.equals(v))
				.collect(Collectors.toList());
		final EdgeTraffic result1 = newFromVehicle.isEmpty() ? this
				: setLastTravelTime(OptionalDouble.of(time - vehicle.getEdgeEntryTime()));
		final EdgeTraffic result = result1.setVehicles(newFromVehicle);
		return result;
	}

	/**
	 * Returns the edge traffic with the given last vehicle
	 *
	 * @param vehicle the vehicle
	 */
	public EdgeTraffic setLast(final Vehicle vehicle) {
		final List<Vehicle> newVehicles = new ArrayList<>(vehicles);
		newVehicles.set(newVehicles.size() - 1, vehicle);
		return setVehicles(newVehicles);
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
