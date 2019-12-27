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
//import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.mmarini.routes.model.Constants;

/**
 * @author us00852
 *
 */
public class SimulationStatus implements Constants {
	private static final SimulationStatus EMPTY = new SimulationStatus(GeoMap.create(), Collections.emptySet(),
			Collections.emptyMap(), DEFAULT_FREQUENCE, new Random());

	public static SimulationStatus create() {
		return EMPTY;
	}

	private final GeoMap map;

	private final Set<EdgeTraffic> traffics;
	private final Map<Tuple2<SiteNode, SiteNode>, Double> weights;
	private final double frequence;
	private final Random random;

	/**
	 *
	 * @param map
	 * @param traffics
	 * @param weights
	 * @param frequence
	 */
	protected SimulationStatus(final GeoMap map, final Set<EdgeTraffic> traffics,
			final Map<Tuple2<SiteNode, SiteNode>, Double> weights, final double frequence, final Random random) {
		super();
		this.map = map;
		this.traffics = traffics;
		this.weights = weights;
		this.frequence = frequence;
		this.random = random;
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @param weight
	 * @return
	 */
	public SimulationStatus addWeight(final SiteNode from, final SiteNode to, final double weight) {
		final Map<Tuple2<SiteNode, SiteNode>, Double> newWeights = new HashMap<>(weights);
		newWeights.put(new Tuple2<>(from, to), weight);
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
	public Set<EdgeTraffic> getTraffic() {
		return traffics;
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public double getWeight(final SiteNode from, final SiteNode to) {
		final double result = Optional.ofNullable(weights.get(new Tuple2<>(from, to))).map(x -> x.doubleValue())
				.orElseGet(() -> 0.0);
		return result;
	}

	/**
	 * Return the weights
	 */
	Map<Tuple2<SiteNode, SiteNode>, Double> getWeights() {
		return weights;
	}

	/**
	 * Returns a random integer number with Poisson distribution and a given
	 * average.
	 *
	 * @param lambda the average
	 */
	int nextPoison(final double lambda) {
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
	 *
	 * @param frequence
	 * @return
	 */
	public SimulationStatus setFrequence(final double frequence) {
		return new SimulationStatus(map, traffics, weights, frequence, random);
	}

	/**
	 *
	 * @param map
	 * @return
	 */
	public SimulationStatus setGeoMap(final GeoMap map) {
		return new SimulationStatus(map, traffics, weights, frequence, random);
	}

	/**
	 *
	 * @param newWeights
	 * @return
	 */
	public SimulationStatus setRandom(final Random random) {
		return new SimulationStatus(map, traffics, weights, frequence, random);
	}

	/**
	 *
	 * @param traffics
	 * @return
	 */
	public SimulationStatus setTraffics(final Set<EdgeTraffic> traffics) {
		return new SimulationStatus(map, traffics, weights, frequence, random);
	}

	/**
	 *
	 * @param weights
	 * @return
	 */
	public SimulationStatus setWeights(final Map<Tuple2<SiteNode, SiteNode>, Double> weights) {
		return new SimulationStatus(map, traffics, weights, frequence, random);
	}

}
