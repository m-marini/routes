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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mmarini.routes.model.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author us00852
 *
 */
public class SimulationStatus implements Constants {
	private static final SimulationStatus EMPTY = new SimulationStatus(GeoMap.create(), Collections.emptySet(),
			Collections.emptyMap(), DEFAULT_FREQUENCE, new Random());
	private static final Logger logger = LoggerFactory.getLogger(SimulationStatus.class);

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
	 * @param edge
	 * @return
	 */
	public SimulationStatus addEdge(final MapEdge edge) {
		logger.debug("addEdge {}", edge);
		final GeoMap newMap = map.addEdge(edge);
		final EdgeTraffic traffic = EdgeTraffic.create(edge).setTime(getTime());
		final Set<EdgeTraffic> newTraffics = new HashSet<>(traffics);
		newTraffics.add(traffic);
		final Map<Tuple2<SiteNode, SiteNode>, Double> newWeights = new HashMap<>(weights);
		final MapNode begin = edge.getBegin();
		if (begin instanceof SiteNode && !map.getSites().contains(begin)) {
			for (final SiteNode old : map.getSites()) {
				newWeights.put(new Tuple2<>((SiteNode) begin, old), 1.0);
				newWeights.put(new Tuple2<>(old, (SiteNode) begin), 1.0);
			}
		}
		final MapNode end = edge.getEnd();
		if (end instanceof SiteNode && !map.getSites().contains(end)) {
			for (final SiteNode old : map.getSites()) {
				newWeights.put(new Tuple2<>((SiteNode) end, old), 1.0);
				newWeights.put(new Tuple2<>(old, (SiteNode) end), 1.0);
			}
		}
		return new SimulationStatus(newMap, newTraffics, newWeights, frequence, random);
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
	 * Returns the simulation status with changed node
	 *
	 * @param edge the removing edge
	 */
	public SimulationStatus changeNode(final MapNode node) {
		logger.debug("changeNode {}", node);
		// Check for site node
		if (node instanceof SiteNode) {
			if (!map.getSites().contains(node)) {
				return this;
			}
		} else if (!map.getNodes().contains(node)) {
			return this;
		}

		// Creates the new node, the new sets of sites and newNodes and the new map
		final MapNode newNode = (node instanceof SiteNode) ? MapNode.create(node.getLocation())
				: SiteNode.create(node.getLocation());
		final Set<SiteNode> newSites = new HashSet<>(map.getSites());
		final Set<MapNode> newNodes = new HashSet<>(map.getNodes());
		if (node instanceof SiteNode) {
			newSites.remove(node);
			newNodes.add(newNode);
		} else {
			newSites.add((SiteNode) newNode);
			newNodes.remove(node);
		}
		final Map<Tuple2<SiteNode, SiteNode>, Double> newWeights = newSites.parallelStream().flatMap(
				from -> newSites.parallelStream().filter(to -> !to.equals(from)).map(to -> new Tuple2<>(from, to)))
				.collect(Collectors.toMap(Function.identity(), t -> weights.getOrDefault(t, 1.0)));

		// Filter the edges with the referenced nodes
		final Map<MapEdge, MapEdge> changeEdgeMap = map.getEdges().parallelStream()
				.filter(edge -> edge.getBegin().equals(node) || edge.getEnd().equals(node)).map(edge -> {
					final MapEdge newEdge = edge.changeNode(node, newNode);
					return new Tuple2<>(edge, newEdge);
				}).collect(Collectors.toMap(t -> t.getElem1(), t -> t.getElem2()));

		final Set<MapEdge> newEdges = new HashSet<>(map.getEdges());
		newEdges.removeAll(changeEdgeMap.keySet());
		newEdges.addAll(changeEdgeMap.values());
		final GeoMap newMap = map.setSites(newSites).setNodes(newNodes).setEdges(newEdges);
		// Creates new traffics
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream().map(traffic -> {
			final Optional<MapEdge> changingEdge = Optional.ofNullable(changeEdgeMap.get(traffic.getEdge()));
			final EdgeTraffic newTraffic = changingEdge.map(edge -> traffic.setEdge(edge)).orElseGet(() -> traffic);
			if (node instanceof SiteNode) {
				// Remove all vehicles traveling from or to the previous node
				final List<Vehicle> newVehicles = traffic.getVehicles().stream()
						.filter(v -> !(v.getDeparture().equals(node) || v.getDestination().equals(node)))
						.collect(Collectors.toList());
				return newTraffic.setVehicles(newVehicles);
			} else {
				return newTraffic;
			}
		}).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics).setWeights(newWeights);
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
	 * Returns the instant
	 */
	public double getTime() {
		return traffics.stream().findAny().map(t -> t.getTime()).orElse(0.0);
	}

	/**
	 *
	 * @return
	 */
	public Set<EdgeTraffic> getTraffics() {
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
	 * Returns the simulation status without edge
	 *
	 * @param edge the removing edge
	 */
	public SimulationStatus removeEdge(final MapEdge edge) {
		logger.debug("removeEdge {}", edge);
		final GeoMap newMap = map.remove(edge);
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream()
				.filter(traffic -> !edge.equals(traffic.getEdge())).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics);
	}

	/**
	 * Returns simulation status with a removed node
	 *
	 * @param node the node
	 */
	public SimulationStatus removeNode(final MapNode node) {
		logger.debug("removeNode {}", node);
		if (!map.getSites().contains(node) && !map.getNodes().contains(node)) {
			return this;
		}

		final GeoMap newMap = map.removeNodeFromMap(node);
		final Map<Tuple2<SiteNode, SiteNode>, Double> newWeights = weights.keySet().parallelStream()
				.filter(t -> !t.getElem1().equals(node) && !t.getElem2().equals(node))
				.collect(Collectors.toMap(Function.identity(), weights::get));
		final Set<EdgeTraffic> newTraffics = traffics.parallelStream()
				.filter(traffic -> newMap.getEdges().contains(traffic.getEdge())).map(traffic -> {
					final List<Vehicle> vehicles = traffic.getVehicles().stream()
							.filter(v -> !v.getDeparture().equals(node) && !v.getDestination().equals(node))
							.collect(Collectors.toList());
					return traffic.setVehicles(vehicles);
				}).collect(Collectors.toSet());
		return setGeoMap(newMap).setTraffics(newTraffics).setWeights(newWeights);
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
