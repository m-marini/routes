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

import org.mmarini.routes.model.Constants;

/**
 * A vehicle in the simulation model
 */
public class Vehicle implements Constants {

	/**
	 * Creates a vehicle
	 *
	 * @param departure   the departure node site
	 * @param destination the destination node site
	 * @return
	 */
	public static Vehicle create(final SiteNode departure, final SiteNode destination) {
		final UUID id = UUID.randomUUID();
		return new Vehicle(id, departure, destination, 0, 0, false);
	}

	private final UUID id;
	private final SiteNode destination;
	private final SiteNode departure;
	private final double location;
	private final double edgeEntryTime;
	private final boolean returning;

	/**
	 * Creates a vehicle
	 *
	 * @param id            the unique identifier
	 * @param departure     the departure site node
	 * @param destination   the destination site node
	 * @param location      the location in the current edge
	 * @param edgeEntryTime the edge entry time
	 * @param returning     true if vehicle is returning to the departure
	 */
	protected Vehicle(final UUID id, final SiteNode departure, final SiteNode destination, final double location,
			final double edgeEntryTime, final boolean returning) {
		super();
		this.id = id;
		this.destination = destination;
		this.departure = departure;
		this.location = location;
		this.edgeEntryTime = edgeEntryTime;
		this.returning = returning;
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
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the departure site node
	 *
	 * @return the departure site node
	 */
	public SiteNode getDeparture() {
		return departure;
	}

	/**
	 * Returns the destination site node
	 *
	 * @return the destination site node
	 */
	public SiteNode getDestination() {
		return destination;
	}

	/**
	 * Returns the edge entry time
	 *
	 * @return the edge entry time
	 */
	public double getEdgeEntryTime() {
		return edgeEntryTime;
	}

	/**
	 * Returns the unique identifier
	 *
	 * @return the unique identifier
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Returns the location in the current edge
	 *
	 * @return the location in the current edge
	 */
	public double getLocation() {
		return location;
	}

	/**
	 * Returns the target site
	 * <p>
	 * Returns the destination if vehicle is not returning or else the departure
	 *
	 * @return the target site
	 */
	public SiteNode getTarget() {
		return returning ? departure : destination;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Returns true if the vehicle is returning to the departure
	 *
	 * @return true if the vehicle is returning to the departure
	 */
	public boolean isReturning() {
		return returning;
	}

	/**
	 * Returns the new vehicle moved for an maximum interval time and the real
	 * movement interval
	 *
	 * @param edge                the edge on which the vehicle move
	 * @param interval            the maximum interval
	 * @param nextVehicleLocation the location of the vehicle ahead in the edge
	 * @return the new vehicle moved for an maximum interval time and the real
	 *         movement interval
	 */
	public Tuple2<Vehicle, Double> move(final MapEdge edge, final double interval,
			final OptionalDouble nextVehicleLocation) {
		final double length = edge.getDistance();
		final double speed = edge.getSpeedLimit();
		final double maxLocation = location + speed * interval;
		if (!nextVehicleLocation.isPresent()) {
			if (maxLocation > length) {
				final double realInterval = Math.max((length - location) / speed, 0);
				final Vehicle newVeichle = setLocation(length);
				return new Tuple2<>(newVeichle, realInterval);
			} else {
				final Vehicle newVeichle = setLocation(maxLocation);
				return new Tuple2<>(newVeichle, interval);
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
				return new Tuple2<>(newVeichle, interval);
			} else {
				final Vehicle newVeichle = setLocation(maxLocation);
				return new Tuple2<>(newVeichle, interval);
			}
		}
	}

	/**
	 * Returns the vehicle with changed edge entry time
	 *
	 * @param edgeEntryTime the edge entry time
	 * @return the vehicle with changed edge entry time
	 */
	public Vehicle setEdgeEntryTime(final double edgeEntryTime) {
		return new Vehicle(id, departure, destination, location, edgeEntryTime, returning);
	}

	/**
	 * Returns the vehicle with the changed location
	 *
	 * @param location the location
	 * @return the vehicle with the changed location
	 */
	public Vehicle setLocation(final double location) {
		return new Vehicle(id, departure, destination, location, edgeEntryTime, returning);
	}

	/**
	 * Returns the vehicle with the changed returning
	 *
	 * @param returning true if vehicle is returning
	 * @return the vehicle with the changed returning
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
