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

/**
 *
 */
public class EdgeStats {

	/**
	 *
	 * @param edge
	 * @return
	 */
	public static EdgeStats create(final MapEdge edge) {
		return new EdgeStats(edge, Optional.empty(), 0);
	}

	private final MapEdge edge;
	private final int vehicleCount;
	private final Optional<Double> lastTravelTime;

	/**
	 *
	 * @param edge
	 * @param lastTravelTime
	 * @param vehicleCount
	 */
	protected EdgeStats(final MapEdge edge, final Optional<Double> lastTravelTime, final int vehicleCount) {
		super();
		this.edge = edge;
		this.lastTravelTime = lastTravelTime;
		this.vehicleCount = vehicleCount;
	}

	/**
	 *
	 * @return
	 */
	MapEdge getEdge() {
		return edge;
	}

	/**
	 *
	 * @return
	 */
	Optional<Double> getLastTravelTime() {
		return lastTravelTime;
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
	int getVehicleCount() {
		return vehicleCount;
	}

	/**
	 *
	 * @param lastTravelTime
	 * @return
	 */
	public EdgeStats setLastTravelTime(final Optional<Double> lastTravelTime) {
		return new EdgeStats(edge, lastTravelTime, vehicleCount);
	}

	/**
	 *
	 * @param vehicleCount
	 * @return
	 */
	public EdgeStats setVehicleCount(final int vehicleCount) {
		return new EdgeStats(edge, lastTravelTime, vehicleCount);
	}
}
