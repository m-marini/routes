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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.mmarini.routes.model.Constants;

/**
 * @author us00852
 *
 */
public class SimulationStatus implements Constants {
	private static final SimulationStatus EMPTY = new SimulationStatus(GeoMap.create(), Collections.emptySet(),
			Collections.emptyMap(), DEFAULT_FREQUENCE, new TrafficInfo());

	public static SimulationStatus create() {
		return EMPTY;
	}

	private final GeoMap map;
	private final Set<EdgeTraffic> vehicles;
	private final TrafficInfo traffic;
	private final Map<SiteNode, Map<SiteNode, Double>> weights;
	private final double frequence;

	/**
	 *
	 * @param map
	 * @param vehicles
	 * @param weights
	 * @param frequence
	 * @param trafficInf
	 */
	protected SimulationStatus(final GeoMap map, final Set<EdgeTraffic> vehicles,
			final Map<SiteNode, Map<SiteNode, Double>> weights, final double frequence, final TrafficInfo trafficInfo) {
		super();
		this.map = map;
		this.vehicles = vehicles;
		this.weights = weights;
		this.frequence = frequence;
		this.traffic = trafficInfo;
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @param weight
	 * @return
	 */
	public SimulationStatus addWeight(final SiteNode from, final SiteNode to, final double weight) {
		final Map<SiteNode, Double> newFrom = new HashMap<>(weights.getOrDefault(from, Collections.emptyMap()));
		newFrom.put(to, weight);
		final Map<SiteNode, Map<SiteNode, Double>> newWeights = new HashMap<>(weights);
		newWeights.put(from, newFrom);
		return setWeights(newWeights);
	}

	/**
	 *
	 * @return
	 */
	public double getFrequence() {
		return frequence;
	}

	/**
	 *
	 * @return
	 */
	public GeoMap getMap() {
		return map;
	}

	/**
	 *
	 * @return
	 */
	public Set<EdgeTraffic> getVehicles() {
		return vehicles;
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public Optional<Double> getWeight(final SiteNode from, final SiteNode to) {
		final Optional<Double> result = Optional.ofNullable(weights.get(from))
				.flatMap(x -> Optional.ofNullable(x.get(to)));
		return result;
	}

	/**
	 *
	 * @param frequence
	 * @return
	 */
	public SimulationStatus setFrequence(final double frequence) {
		return new SimulationStatus(map, vehicles, weights, frequence, traffic);
	}

	/**
	 *
	 * @param map
	 * @return
	 */
	public SimulationStatus setGeoMap(final GeoMap map) {
		return new SimulationStatus(map, vehicles, weights, frequence, traffic);
	}

	/**
	 *
	 * @param vehicles
	 * @return
	 */
	public SimulationStatus setVehicles(final Set<EdgeTraffic> vehicles) {
		return new SimulationStatus(map, vehicles, weights, frequence, traffic);
	}

	/**
	 *
	 * @param weights
	 * @return
	 */
	public SimulationStatus setWeights(final Map<SiteNode, Map<SiteNode, Double>> weights) {
		return new SimulationStatus(map, vehicles, weights, frequence, traffic);
	}

}
