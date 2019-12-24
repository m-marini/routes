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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import org.mmarini.routes.model.Constants;

/**
 *
 * @author mmarini
 *
 */
public class EdgeTraffic implements Constants {

	/**
	 *
	 * @param edge
	 * @return
	 */
	public static EdgeTraffic create(final MapEdge edge) {
		return new EdgeTraffic(edge, Collections.emptyList(), 0, OptionalDouble.empty());
	}

	private final MapEdge edge;
	private final List<Vehicle> vehicles;
	private final double time;
	private final OptionalDouble lastTravelTime;

	/**
	 * @param edge
	 * @param vehicles
	 * @param time
	 */
	protected EdgeTraffic(final MapEdge edge, final List<Vehicle> vehicles, final double time,
			final OptionalDouble lastTravelTime) {
		super();
		this.edge = edge;
		this.vehicles = vehicles;
		this.time = time;
		this.lastTravelTime = lastTravelTime;
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
	 * @return the edge
	 */
	public MapEdge getEdge() {
		return edge;
	}

	/**
	 *
	 * @return
	 */
	OptionalDouble getLastTravelTime() {
		return lastTravelTime;
	}

	/**
	 * 
	 * @return
	 */
	public double getTime() {
		return time;
	}

	/**
	 *
	 * @return
	 */
	public double getTravelTime() {
		return lastTravelTime.orElseGet(() -> edge.getTransitTime());
	}

	/**
	 *
	 * @return
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
	 *
	 * @param interval
	 * @return
	 */
	public EdgeTraffic moveVehicles(final double interval) {
		if (vehicles.isEmpty()) {
			return setTime(interval + time);
		}
		final List<Vehicle> reversed = new ArrayList<>(vehicles);
		Collections.reverse(reversed);
		final Tuple2<Vehicle, Double> initial = reversed.get(0).move(edge, interval, OptionalDouble.empty());
		final Vehicle v0 = initial.getElem1();
		final double dt = initial.getElem2();
		if (reversed.size() == 1) {
			return setVehicles(Arrays.asList(v0)).setTime(time + dt);
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

		final EdgeTraffic result = setVehicles(newVehicles).setTime(time + dt);
		return result;
	}

	/**
	 * 
	 * @param time
	 * @return
	 */
	public EdgeTraffic setLastTravelTime(final OptionalDouble lastTravelTime) {
		return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
	}

	/**
	 * 
	 * @param time
	 * @return
	 */
	public EdgeTraffic setTime(final double time) {
		return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
	}

	/**
	 *
	 * @param vehicles
	 * @return
	 */
	public EdgeTraffic setVehicles(final List<Vehicle> vehicles) {
		return new EdgeTraffic(edge, vehicles, time, lastTravelTime);
	}
}
