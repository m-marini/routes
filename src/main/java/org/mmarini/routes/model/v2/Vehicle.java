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

import java.util.Optional;
import java.util.UUID;

import org.mmarini.routes.model.Constants;

/**
 *
 * @author us00852
 *
 */
public class Vehicle implements Constants {

	/**
	 *
	 * @param departure
	 * @param destination
	 * @return
	 */
	public static Vehicle create(final MapNode departure, final MapNode destination) {
		final UUID id = UUID.randomUUID();
		return new Vehicle(id, departure, destination, Optional.empty(), 0, 0);
	}

	private final UUID id;
	private final MapNode destination;
	private final MapNode departure;
	private final Optional<MapEdge> edge;
	private final double edgeLocation;
	private final double edgeEntryTime;

	/**
	 *
	 * @param id
	 * @param destination
	 * @param departure
	 * @param edge
	 * @param edgeLocation
	 */
	protected Vehicle(final UUID id, final MapNode departure, final MapNode destination, final Optional<MapEdge> edge,
			final double edgeLocation, final double edgeEntryTime) {
		super();
		this.id = id;
		this.edge = edge;
		this.destination = destination;
		this.departure = departure;
		this.edgeLocation = edgeLocation;
		this.edgeEntryTime = edgeEntryTime;
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
	 *
	 * @return
	 */
	public MapNode getDeparture() {
		return departure;
	}

	/**
	 *
	 * @return
	 */
	public MapNode getDestination() {
		return destination;
	}

	/**
	 *
	 * @return
	 */
	public Optional<MapEdge> getEdge() {
		return edge;
	}

	/**
	 *
	 * @return
	 */
	public double getEdgeEntryTime() {
		return edgeEntryTime;
	}

	/**
	 *
	 * @return
	 */
	public double getEdgeLocation() {
		return edgeLocation;
	}

	/**
	 *
	 * @return
	 */
	public UUID getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 *
	 * @param interval
	 * @param nextVehicle
	 * @return
	 */
	public Tuple2<Vehicle, Double> move(final double interval, final Optional<Double> nextVehicleLocation) {
		final Tuple2<Vehicle, Double> result = edge.map(edg -> {
			final double sx = edg.getDistance();
			final double v = edg.getSpeedLimit();
			final double s1 = edgeLocation + v * interval;
			if (nextVehicleLocation.isEmpty()) {
				if (s1 > sx) {
					final double realInterval = (sx - edgeLocation) / v;
					final Vehicle newVeichle = setEdgeLocation(sx);
					return new Tuple2<>(newVeichle, realInterval);
				} else {
					final Vehicle newVeichle = setEdgeLocation(s1);
					return new Tuple2<>(newVeichle, interval);
				}
			} else {
				final double next = nextVehicleLocation.get();
				final double security = v * REACTION_TIME;
				if (s1 + security + VEHICLE_LENGTH > next) {
					final double ds = (next - VEHICLE_LENGTH) * interval / (interval + REACTION_TIME);
					final Vehicle newVeichle = setEdgeLocation(edgeLocation + ds);
					return new Tuple2<>(newVeichle, interval);
				} else {
					final Vehicle newVeichle = setEdgeLocation(s1);
					return new Tuple2<>(newVeichle, interval);
				}
			}
		}).orElseGet(() -> new Tuple2<>(this, interval));
		return result;
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	public Vehicle setEdge(final Optional<MapEdge> edge) {
		return new Vehicle(id, departure, destination, edge, edgeLocation, edgeEntryTime);
	}

	/**
	 *
	 * @param edgeEntryTime
	 * @return
	 */
	public Vehicle setEdgeEntryTime(final double edgeEntryTime) {
		return new Vehicle(id, departure, destination, edge, edgeLocation, edgeEntryTime);
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	public Vehicle setEdgeLocation(final double edgeLocation) {
		return new Vehicle(id, departure, destination, edge, edgeLocation, edgeEntryTime);
	}
}
