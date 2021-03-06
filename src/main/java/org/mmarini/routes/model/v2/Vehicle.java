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

import java.util.OptionalDouble;
import java.util.UUID;

/**
 * Vehicle in the simulation model.
 * <p>
 * It includes:
 * <ul>
 * <li>unique identifier</li>
 * <li>destination node</li>
 * <li>departure node</li>
 * <li>location in the current edge in meters</li>
 * <li>instant of entry in the current edge</li>
 * <li>if it is returning to the departure</li>
 * </ul>
 */
public class Vehicle implements Comparable<Vehicle>, Constants {

	private static final double EPSILON = 1e-3;

	/**
	 * Returns a vehicle.
	 *
	 * @param departure   the departure node site
	 * @param destination the destination node site
	 */
	public static Vehicle create(final MapNode departure, final MapNode destination) {
		final UUID id = UUID.randomUUID();
		return new Vehicle(id, departure, destination, 0, 0, false);
	}

	private final UUID id;
	private final MapNode destination;
	private final MapNode departure;
	private final double location;
	private final double edgeEntryTime;
	private final boolean returning;

	/**
	 * Creates a vehicle.
	 *
	 * @param id            the unique identifier
	 * @param departure     the departure site node
	 * @param destination   the destination site node
	 * @param location      the location in the current edge
	 * @param edgeEntryTime the edge entry time
	 * @param returning     true if vehicle is returning to the departure
	 */
	protected Vehicle(final UUID id, final MapNode departure, final MapNode destination, final double location,
			final double edgeEntryTime, final boolean returning) {
		super();
		assert id != null;
		assert departure != null;
		assert destination != null;
		this.id = id;
		this.destination = destination;
		this.departure = departure;
		this.location = location;
		this.edgeEntryTime = edgeEntryTime;
		this.returning = returning;
	}

	@Override
	public int compareTo(final Vehicle other) {
		return UUIDComparator.compareTo(id, other.id);
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
		final Vehicle other = (Vehicle) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/** Returns the departure site node. */
	public MapNode getDeparture() {
		return departure;
	}

	/** Returns the destination site node. */
	public MapNode getDestination() {
		return destination;
	}

	/** Returns the edge entry time. */
	public double getEdgeEntryTime() {
		return edgeEntryTime;
	}

	/** Returns the unique identifier. */
	public UUID getId() {
		return id;
	}

	/** Returns the location in the current edge. */
	public double getLocation() {
		return location;
	}

	/** Return the name (the uuid). */
	public String getName() {
		return id.toString();
	}

	/** Returns the short name (first 6 characters of uuid). */
	public String getShortName() {
		return getName().substring(0, 6);
	}

	/**
	 * Returns the target site.
	 * <p>
	 * Returns the destination if vehicle is not returning else the departure
	 * </p>
	 */
	public MapNode getTarget() {
		return returning ? departure : destination;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		return result;
	}

	/** Returns true if the vehicle is returning to the departure. */
	public boolean isReturning() {
		return returning;
	}

	/**
	 * Returns the new vehicle moved for an maximum interval time and the real
	 * movement interval.
	 *
	 * @param edge                the edge on which the vehicle move
	 * @param interval            the maximum interval
	 * @param nextVehicleLocation the location of the vehicle ahead in the edge
	 * @return the new vehicle moved for an maximum interval time and the real
	 *         movement interval
	 */
	public Tuple2<Vehicle, Double> move(final MapEdge edge, final double interval,
			final OptionalDouble nextVehicleLocation) {
		final double length = edge.getLength();
		final double speed = edge.getSpeedLimit();
		final double maxLocation = location + speed * interval;
		if (!nextVehicleLocation.isPresent()) {
			if (maxLocation > length) {
				final double realInterval = Math.max((length - location) / speed, 0);
				final Vehicle newVeichle = setLocation(length);
				return Tuple.of(newVeichle, realInterval);
			} else if (maxLocation + EPSILON < length) {
				final Vehicle newVeichle = setLocation(maxLocation);
				return Tuple.of(newVeichle, interval);
			} else {
				final Vehicle newVeichle = setLocation(length);
				return Tuple.of(newVeichle, interval);
			}
		} else {
			final double nextLocation = nextVehicleLocation.getAsDouble();
			final double stopLocation = nextLocation - VEHICLE_LENGTH;
			final double securityDistance = speed * REACTION_TIME;
			if (maxLocation + securityDistance + VEHICLE_LENGTH > nextLocation) {
				final double dLocation = (nextLocation - VEHICLE_LENGTH - location) * interval
						/ (interval + REACTION_TIME);
				final double safetyLocation = location + dLocation;
				final double finalLocation = Math.min(stopLocation, safetyLocation);
				final Vehicle newVeichle = setLocation(finalLocation);
				return Tuple.of(newVeichle, interval);
			} else {
				final Vehicle newVeichle = setLocation(maxLocation);
				return Tuple.of(newVeichle, interval);
			}
		}
	}

	/**
	 * Returns the vehicle with changed edge entry time.
	 *
	 * @param edgeEntryTime the edge entry time
	 */
	public Vehicle setEdgeEntryTime(final double edgeEntryTime) {
		return new Vehicle(id, departure, destination, location, edgeEntryTime, returning);
	}

	/**
	 * Returns the vehicle with the changed location.
	 *
	 * @param location the location
	 */
	public Vehicle setLocation(final double location) {
		return new Vehicle(id, departure, destination, location, edgeEntryTime, returning);
	}

	/**
	 * Returns the vehicle with the changed returning.
	 *
	 * @param returning true if vehicle is returning
	 */
	public Vehicle setReturning(final boolean returning) {
		return new Vehicle(id, departure, destination, location, edgeEntryTime, returning);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Vehicle [").append(id).append("]");
		return builder.toString();
	}
}
