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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Traffics information for a a map.
 * <p>
 * It includes
 * <ul>
 * <li>the map</li>
 * <li>the traffics information of each edge</li>
 * </ul>
 * </p>
 * <p>
 * Several map and traffics manipulation methods are available
 * </p>
 */
public class Traffics implements Constants {
	private static final Traffics EMPTY = new Traffics(GeoMap.create(), Set.of());
	private static final Logger logger = LoggerFactory.getLogger(Traffics.class);

	/** Returns an empty simulation status */
	public static Traffics create() {
		return EMPTY;
	}

	/**
	 * Returns the traffics information for a given map
	 *
	 * @param map the map
	 */
	public static Traffics create(final GeoMap map) {
		final Set<EdgeTraffic> traffics = map.getEdges().parallelStream().map(edge -> {
			return EdgeTraffic.create(edge);
		}).collect(Collectors.toSet());
		return new Traffics(map, traffics);
	}

	/**
	 * Returns a traffics information generating a random map with a given profile
	 *
	 * @param profile the generation map profile
	 * @param random  the random generator
	 */
	public static Traffics random(final MapProfile profile, final Random random) {
		final GeoMap map = GeoMap.random(profile, random);
		return new Traffics(map, Set.of());
	}

	private final GeoMap map;
	private final Set<EdgeTraffic> traffics;

	/**
	 * Create the traffics information for a map and traffics edges
	 *
	 * @param map      the map
	 * @param traffics the traffic edges
	 */
	protected Traffics(final GeoMap map, final Set<EdgeTraffic> traffics) {
		super();
		this.map = map;
		this.traffics = traffics;
	}

	/**
	 * Returns the traffics information adding a new edge to the map
	 *
	 * @param edge the edge
	 */
	public Traffics addEdge(final MapEdge edge) {
		logger.debug("addEdge {}", edge);
		final GeoMap newMap = map.add(edge);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setTime(getTime());
		final Set<EdgeTraffic> newTraffics = Stream.concat(traffics.parallelStream(), Stream.of(traffic))
				.collect(Collectors.toSet());
		return new Traffics(newMap, newTraffics);
	}

	/**
	 * Returns the traffics information adding a set of edges to the map
	 *
	 * @param edges the edges
	 */
	public Traffics addEdges(final Set<MapEdge> edges) {
		final GeoMap newMap = map.addEdges(edges);
		final double time = getTime();
		final Set<EdgeTraffic> newTraffics = newMap.getEdges().parallelStream().map(edge -> {
			final Optional<EdgeTraffic> traffic = traffics.parallelStream().filter(tr -> {
				return tr.getEdge().equals(edge);
			}).findAny();
			final EdgeTraffic tr = traffic.orElseGet(() -> EdgeTraffic.create(edge).setTime(time));
			return tr;
		}).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics);
	}

	/**
	 * Returns the simulation status with an edge properties changed the traffic do
	 * still not change
	 *
	 * @param edge new edge with changed properties
	 */
	public Traffics change(final MapEdge edge) {
		final GeoMap newMap = map.add(edge);
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream().map(traffic -> {
			final MapEdge ed = traffic.getEdge();
			final EdgeTraffic res = ed.equals(edge) ? traffic.setEdge(edge) : traffic;
			return res;
		}).collect(Collectors.toSet());
		final Traffics newStatus = setGeoMap(newMap).setTraffics(newTraffics);
		return newStatus;
	}

	/**
	 * Returns the simulation status with given node changed in type
	 *
	 * @param node           the changing node
	 * @param weightFunction the weight generation function
	 */
	public Traffics changeNode(final MapNode node, final ToDoubleBiFunction<MapNode, MapNode> weightFunction) {
		logger.debug("changeNode {}", node);
		final GeoMap newMap = map.changeNode(node, weightFunction);
		final Set<EdgeTraffic> newTraffics = map.getSite(node).map(n -> {
			return traffics.parallelStream().map(traffic -> {
				final List<Vehicle> newVehicles = traffic.getVehicles().stream().filter(v -> {
					return !v.getTarget().equals(node);
				}).collect(Collectors.toList());
				return traffic.setVehicles(newVehicles);
			}).collect(Collectors.toSet());
		}).orElse(traffics);
		return newMap != map ? setGeoMap(newMap).setTraffics(newTraffics) : this;
	}

	/** Returns the map */
	public GeoMap getMap() {
		return map;
	}

	/** Returns traffics instant */
	public double getTime() {
		return traffics.stream().findAny().map(t -> t.getTime()).orElse(0.0);
	}

	/** Returns the edges' traffic */
	public Set<EdgeTraffic> getTraffics() {
		return traffics;
	}

	/**
	 * Returns the traffic information with edges optimized for a given speed limit
	 *
	 * @param speedLimit the speed limit
	 */
	public Traffics optimizeSpeed(final double speedLimit) {
		final GeoMap newMap = map.optimizeSpeedLimit(speedLimit);
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream().map(traffic -> {
			final MapEdge edge = newMap.getEdge(traffic.getEdge()).get();
			return traffic.setEdge(edge);
		}).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics);
	}

	/**
	 * Returns the traffic information with random vehicle generation weights
	 *
	 * @param minWeight the minimum weight
	 * @param random    the random generator
	 */
	public Traffics randomize(final double minWeight, final Random random) {
		return setGeoMap(map.randomize(minWeight, random));
	}

	/**
	 * Returns the simulation status without edge
	 *
	 * @param edge the removing edge
	 */
	public Traffics removeEdge(final MapEdge edge) {
		logger.debug("removeEdge {}", edge);
		final GeoMap newMap = map.remove(edge);
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream().filter(traffic -> {
			return !edge.equals(traffic.getEdge());
		}).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics);
	}

	/**
	 * Returns simulation status with a removed node
	 *
	 * @param node the node
	 */
	public Traffics removeNode(final MapNode node) {
		logger.debug("removeNode {}", node);
		if (!map.getNodes().contains(node)) {
			return this;
		}
		final GeoMap newMap = map.remove(node);

		final Set<EdgeTraffic> newTraffics = traffics.parallelStream().filter(traffic -> {
			return newMap.getEdges().contains(traffic.getEdge());
		}).map(traffic -> {
			// Remove all vehicles coming from or going to the removed node
			final List<Vehicle> vehicles = traffic.getVehicles().stream().filter(v -> {
				return !(v.getDeparture().equals(node) || v.getDestination().equals(node));
			}).collect(Collectors.toList());
			return traffic.setVehicles(vehicles);
		}).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics);
	}

	/**
	 * Returns the traffic with a given frequency of vehicle generation
	 *
	 * @param frequence the frequency
	 */
	public Traffics setFrequence(final double frequence) {
		final Traffics result = setGeoMap(map.setFrequence(frequence));
		return result;
	}

	/**
	 * Returns the traffic with a given map
	 *
	 * @param map the map
	 */
	public Traffics setGeoMap(final GeoMap map) {
		return new Traffics(map, traffics);
	}

	/**
	 * Returns the traffic with a given set of traffic edges
	 *
	 * @param traffics the traffics edges
	 */
	public Traffics setTraffics(final Set<EdgeTraffic> traffics) {
		return new Traffics(map, traffics);
	}

	/**
	 * Returns the traffic with a given vehicle generation weights
	 *
	 * @param weights the weights
	 */
	public Traffics setWeights(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		return setGeoMap(map.setWeights(weights));
	}
}
