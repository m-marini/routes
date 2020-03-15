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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Map with the information of the edges and site nodes.
 */
public class GeoMap implements Constants {
	private final static GeoMap EMPTY = new GeoMap(Set.of(), Map.of(), Set.of(), Set.of(), DEFAULT_FREQUENCE);

	/**
	 * Returns random weight for a given set of sites.
	 *
	 * @param sites          the sites
	 * @param weightSupplier the weight supplier
	 */
	public static Map<Tuple2<MapNode, MapNode>, Double> buildWeights(final Set<MapNode> sites,
			final ToDoubleBiFunction<MapNode, MapNode> weightSupplier) {
		final Map<Tuple2<MapNode, MapNode>, Double> result = sites.stream().sorted().flatMap(from -> {
			return sites.stream().sorted().filter(to -> !from.equals(to)).map(to -> {
				return Tuple.of(from, to);
			});
		}).collect(Collectors.toMap(Function.identity(), t -> {
			final double w = weightSupplier.applyAsDouble(t.get1(), t.get2());
			return w;
		}));
		return result;
	}

	/** Returns an empty map */
	public static GeoMap create() {
		return EMPTY;
	}

	/**
	 * Returns a map with edges only.
	 *
	 * @param edges the edges
	 */
	public static GeoMap create(final Set<MapEdge> edges) {
		final Set<MapNode> nodes = retrieveNodeStream(edges).collect(Collectors.toSet());
		return new GeoMap(edges, Map.of(), Set.of(), nodes, DEFAULT_FREQUENCE);
	}

	/**
	 * Returns a map with edges and weights for site.
	 *
	 * @param edges   the edges
	 * @param weights the weights
	 */
	public static GeoMap create(final Set<MapEdge> edges, final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		final Set<MapNode> sites = retrieveSites(weights);
		final Set<MapNode> nodes = Stream.concat(retrieveNodeStream(edges), sites.parallelStream())
				.collect(Collectors.toSet());
		return new GeoMap(edges, weights, sites, nodes, DEFAULT_FREQUENCE);
	}

	/**
	 * Returns a map with edges and a single site.
	 *
	 * @param edges the edges
	 * @param site  the site
	 */
	public static GeoMap create(final Set<MapEdge> edges, final MapNode site) {
		final Set<MapNode> nodes = Stream.concat(retrieveNodeStream(edges), Stream.of(site))
				.collect(Collectors.toSet());
		return new GeoMap(edges, Map.of(), Set.of(site), nodes, DEFAULT_FREQUENCE);
	}

	/**
	 * Returns a random map for a give profile and random generator.
	 *
	 * @param profile the profile
	 * @param random  the random generator
	 */
	public static GeoMap random(final MapProfile profile, final Random random) {
		final Set<Point2D> siteLocations = IntStream.range(0, profile.getSiteCount())
				.mapToObj(i -> new Point2D.Double(random.nextDouble(), random.nextDouble()))
				.collect(Collectors.toSet());

		final Optional<Rectangle2D> seed = Optional.empty();
		final BiFunction<Optional<Rectangle2D>, ? super Point2D, Optional<Rectangle2D>> reducer = (a, pt) -> {
			final Optional<Rectangle2D> result = a.map(acc -> {
				final Rectangle2D newAcc = (Rectangle2D) acc.clone();
				newAcc.add(pt);
				return newAcc;
			}).or(() -> Optional.of(new Rectangle2D.Double(pt.getX(), pt.getY(), 0, 0)));
			return result;
		};

		final BinaryOperator<Optional<Rectangle2D>> merger = (ao, bo) -> {
			return ao.map(a -> {
				return bo.map(b -> {
					return a.createUnion(b);
				}).orElse(a);
			}).or(() -> bo);
		};

		final Rectangle2D bound = siteLocations.parallelStream().<Optional<Rectangle2D>>reduce(seed, reducer, merger)
				.get();

		// normalize
		final double minX = bound.getMinX();
		final double minY = bound.getMinY();
		final double scaleX = profile.getWidth() / bound.getWidth();
		final double scaleY = profile.getHeight() / bound.getHeight();
		final Set<MapNode> sites = siteLocations.parallelStream().map(pt -> {
			return MapNode.create((pt.getX() - minX) * scaleX, (pt.getY() - minY) * scaleY);
		}).collect(Collectors.toSet());

		final Map<Tuple2<MapNode, MapNode>, Double> weights = buildWeights(sites,
				randomWeight(profile.getMinWeight(), random));

		return GeoMap.create(Set.of(), weights).setFrequence(profile.getFrequence());
	}

	/**
	 * Returns the weight function generator.
	 *
	 * @param minWeight minimum weight
	 * @param random    the ranom generator
	 */
	public static ToDoubleBiFunction<MapNode, MapNode> randomWeight(final double minWeight, final Random random) {
		final double scale = 1 - minWeight;
		return (a, b) -> minWeight + scale * random.nextDouble();
	}

	/**
	 * Returns the stream of begine and and nodes of a set of edges.
	 *
	 * @param edges the edges
	 */
	private static Stream<MapNode> retrieveNodeStream(final Set<MapEdge> edges) {
		return edges.parallelStream().flatMap(edge -> {
			return Stream.of(edge.getBegin(), edge.getEnd());
		}).distinct();
	}

	/**
	 * Returns the sites for a given map of weights.
	 *
	 * @param weights the weight map
	 */
	private static Set<MapNode> retrieveSites(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		final Set<MapNode> result = weights.keySet().parallelStream().flatMap(t -> {
			return Set.of(t.get1(), t.get2()).stream();
		}).collect(Collectors.toSet());
		return result;
	}

	private final Set<MapEdge> edges;

	private final Map<Tuple2<MapNode, MapNode>, Double> weights;
	private final Set<MapNode> sites;
	private final Set<MapNode> nodes;
	private final double frequence;

	/**
	 * Creates a geo map.
	 *
	 * @param edges     the edges
	 * @param weights   the weights of vehicle creation for a give departure and
	 *                  destination
	 * @param sites     the site nodes
	 * @param nodes     the nodes
	 * @param frequence the frequency of vehicle creation
	 */
	protected GeoMap(final Set<MapEdge> edges, final Map<Tuple2<MapNode, MapNode>, Double> weights,
			final Set<MapNode> sites, final Set<MapNode> nodes, final double frequence) {
		super();
		assert (edges != null);
		assert (weights != null);
		assert (sites != null);
		assert (nodes != null);
		this.edges = edges;
		this.weights = weights;
		this.sites = sites;
		this.nodes = nodes;
		this.frequence = frequence;
	}

	/**
	 * Returns the map with a new edge.
	 *
	 * @param edge the edge
	 */
	public GeoMap add(final MapEdge edge) {
		// Find for any existing edge
		if (edges.parallelStream().anyMatch(e -> {
			final boolean res = e.equals(edge) && e.getPriority() == edge.getPriority()
					&& e.getSpeedLimit() == edge.getSpeedLimit();
			return res;
		})) {
			// No changes
			return this;
		} else if (edges.contains(edge)) {
			// Replace edge

			final Set<MapEdge> newEdges = edges.parallelStream().map(e -> {
				return e.equals(edge) ? edge : e;
			}).collect(Collectors.toSet());
			return setEdges(newEdges);
		} else {
			// Add edge
			final Set<MapEdge> edges = Stream.concat(this.edges.parallelStream(), Stream.of(edge))
					.collect(Collectors.toSet());
			return setEdges(edges);
		}
	}

	/**
	 * Returns the map with added edges.
	 * <p>
	 * New edges are created using the the node instances of this map
	 * </p>
	 *
	 * @param edges the edges
	 */
	public GeoMap addEdges(final Set<MapEdge> edges) {
		final Stream<MapEdge> newEdgesStrem = edges.parallelStream().map(edge -> {
			final MapNode begin = getNode(edge.getBegin()).orElse(edge.getBegin());
			final MapNode end = getNode(edge.getEnd()).orElse(edge.getEnd());
			return edge.setEnds(begin, end);
		});
		final Set<MapEdge> newEdges = Stream.concat(this.edges.parallelStream(), newEdgesStrem)
				.collect(Collectors.toSet());
		return setEdges(newEdges);
	}

	/**
	 * Returns the map with node type changed.
	 *
	 * @param node          the changing node
	 * @param weightBuilder the function that computes the weights between the
	 *                      departure and the destination when the changing node
	 *                      becomes a site node
	 */
	public GeoMap changeNode(final MapNode node, final ToDoubleBiFunction<MapNode, MapNode> weightBuilder) {
		if (!nodes.contains(node)) {
			return this;
		} else if (getSite(node).isPresent()) {
			// site -> node => remove the weights
			if (sites.size() == 1) {
				// no weights
				return setWeights(Map.of());
			} else if (sites.size() == 2) {
				// Single site
				final MapNode site = sites.stream().filter(e -> !e.equals(node)).findAny().get();
				return setSite(site);
			} else {
				// remove weights
				final Map<Tuple2<MapNode, MapNode>, Double> newWeights = weights.entrySet().parallelStream()
						.filter(entry -> {
							final Tuple2<MapNode, MapNode> k = entry.getKey();
							return !(k.get1().equals(node) || k.get2().equals(node));
						}).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
				return setWeights(newWeights);
			}
		} else if (sites.isEmpty()) {
			// node -> single site
			return setSite(node);
		} else {
			// node -> site => add weights
			final Map<Tuple2<MapNode, MapNode>, Double> addWeights = sites.parallelStream().flatMap(s -> {
				return Set.of(Tuple.of(s, node), Tuple.of(node, s)).stream();
			}).collect(Collectors.toMap(Function.identity(), k -> {
				return weightBuilder.applyAsDouble(k.get1(), k.get2());
			}));
			final Map<Tuple2<MapNode, MapNode>, Double> newWeights = new HashMap<>(weights);
			newWeights.putAll(addWeights);
			return setWeights(newWeights);
		}
	}

	/**
	 * Returns the nearest node to the point within maximum distance.
	 *
	 * @param point    the point
	 * @param distance the distance
	 */
	public Optional<MapNode> findNearst(final Point2D point, final double distance) {
		final double d2 = distance * distance;
		final Optional<MapNode> result = nodes.parallelStream().filter(n -> {
			return point.distanceSq(n.getLocation()) <= d2;
		}).min((a, b) -> {
			final double da2 = a.getLocation().distanceSq(point);
			final double db2 = b.getLocation().distanceSq(point);
			return Double.compare(da2, db2);
		});
		return result;
	}

	/** Returns the map bound. */
	public Rectangle2D getBound() {
		final Set<MapNode> all = Stream.concat(sites.parallelStream(), nodes.parallelStream())
				.collect(Collectors.toSet());
		final OptionalDouble x0 = all.parallelStream().mapToDouble(n -> n.getX()).min();
		final OptionalDouble x1 = all.parallelStream().mapToDouble(n -> n.getX()).max();
		final OptionalDouble y0 = all.parallelStream().mapToDouble(n -> n.getY()).min();
		final OptionalDouble y1 = all.parallelStream().mapToDouble(n -> n.getY()).max();

		final Rectangle2D result = (x0.isPresent() && x1.isPresent() && y0.isPresent() && y1.isPresent())
				? new Rectangle2D.Double(x0.getAsDouble(), y0.getAsDouble(), x1.getAsDouble() - x0.getAsDouble(),
						y1.getAsDouble() - y0.getAsDouble())
				: new Rectangle2D.Double();
		return result;
	}

	/**
	 * Returns the edge matching the pattern.
	 *
	 * @param pattern the pattern
	 */
	public Optional<MapEdge> getEdge(final MapEdge pattern) {
		return edges.parallelStream().filter(e -> e.equals(pattern)).findAny();
	}

	/** Returns the edges */
	public Set<MapEdge> getEdges() {
		return edges;
	}

	/** Returns the frequency */
	public double getFrequence() {
		return frequence;
	}

	/**
	 * Returns the node matching the pattern.
	 *
	 * @param pattern the pattern
	 */
	public Optional<MapNode> getNode(final MapNode pattern) {
		return nodes.parallelStream().filter(n -> n.equals(pattern)).findAny();
	}

	/** Returns the nodes */
	public Set<MapNode> getNodes() {
		return nodes;
	}

	/**
	 * Returns the site matching the pattern.
	 *
	 * @param pattern the pattern
	 */
	public Optional<MapNode> getSite(final MapNode pattern) {
		return sites.parallelStream().filter(s -> s.equals(pattern)).findAny();
	}

	/** Returns the sites */
	public Set<MapNode> getSites() {
		return sites;
	}

	/**
	 * Returns the weights between the departure site and the destination site.
	 *
	 * @param from the departure
	 * @param to   the destination
	 */
	public double getWeight(final MapNode from, final MapNode to) {
		return getWeight(Tuple.of(from, to));
	}

	/**
	 * Returns the weights between the pair of departure site and destination site
	 *
	 * @param pair the pair of departure site and destination site
	 */
	public double getWeight(final Tuple2<MapNode, MapNode> pair) {
		return Optional.ofNullable(weights.get(pair)).orElse(0.0);
	}

	/** Returns the weight map. */
	public Map<Tuple2<MapNode, MapNode>, Double> getWeights() {
		return weights;
	}

	/**
	 * Returns the map with optimal speed limit for each edge.
	 * <p>
	 * The length of edge and the maximum speed limits determine the edge speed
	 * limit
	 * </p>
	 *
	 * @param speedLimit the maximum speed limit
	 */
	public GeoMap optimizeSpeedLimit(final double speedLimit) {
		final Set<MapEdge> newEdges = edges.parallelStream().map(edge -> {
			return edge.optimizedSpeedLimit(speedLimit);
		}).collect(Collectors.toSet());
		return setEdges(newEdges);
	}

	/**
	 * Returns the map randomized weights.
	 *
	 * @param minWeight the minimum weight value
	 * @param random    the random generator
	 */
	public GeoMap randomize(final double minWeight, final Random random) {
		return setWeights(buildWeights(sites, randomWeight(minWeight, random)));
	}

	/**
	 * Returns the map without the given edge.
	 *
	 * @param edge the edge
	 */
	public GeoMap remove(final MapEdge edge) {
		if (edges.contains(edge)) {
			final Set<MapEdge> newEdges = edges.parallelStream().filter(e -> {
				return !e.equals(edge);
			}).collect(Collectors.toSet());
			return setEdges(newEdges);
		} else {
			return this;
		}
	}

	/**
	 * Returns the map with removed node and edges.
	 *
	 * @param node the node to remove
	 */
	public GeoMap remove(final MapNode node) {
		if (nodes.contains(node)) {
			final Set<MapEdge> newEdges = edges.parallelStream().filter(e -> {
				return !(e.getBegin().equals(node) || e.getEnd().equals(node));
			}).collect(Collectors.toSet());

			if (!sites.contains(node)) {
				// only edge node
				return setEdges(newEdges);
			} else if (sites.size() > 2) {
				// recompute weights, sites
				final Map<Tuple2<MapNode, MapNode>, Double> newWeights = weights.entrySet().parallelStream()
						.filter(entry -> {
							final Tuple2<MapNode, MapNode> k = entry.getKey();
							return !(k.get1().equals(node) || k.get2().equals(node));
						}).collect(Collectors.toMap(e -> {
							return e.getKey();
						}, e -> {
							return e.getValue();
						}));
				return create(newEdges, newWeights);
			} else if (sites.size() == 2) {
				// no weights, single site, recompute edges
				final MapNode site = sites.stream().filter(s -> {
					return !s.equals(node);
				}).findAny().get();
				return create(newEdges, site);
			} else {
				// no weights, no sites recompute edges
				return create(newEdges);
			}
		} else {
			return this;
		}
	}

	/**
	 * Returns the map with the given edges.
	 *
	 * @param edges the edges
	 */
	public GeoMap setEdges(final Set<MapEdge> edges) {
		final Set<MapNode> nodes = Stream.concat(sites.parallelStream(), retrieveNodeStream(edges))
				.collect(Collectors.toSet());
		return new GeoMap(edges, weights, sites, nodes, frequence);
	}

	/**
	 * Returns the map with the give frequence.
	 *
	 * @param frequence the frequence
	 */
	public GeoMap setFrequence(final double frequence) {
		return new GeoMap(edges, weights, sites, nodes, frequence);
	}

	/**
	 * Returns the map with the a single given site.
	 *
	 * @param site the site
	 */
	public GeoMap setSite(final MapNode site) {
		final Set<MapNode> nodes = Stream.concat(retrieveNodeStream(edges), Stream.of(site))
				.collect(Collectors.toSet());
		return new GeoMap(edges, Map.of(), Set.of(site), nodes, frequence);
	}

	/**
	 * Returns the map with weight associated a departure and destination sites.
	 *
	 * @param from   departure site
	 * @param to     destination site
	 * @param weight the weight
	 */
	public GeoMap setWeight(final MapNode from, final MapNode to, final double weight) {
		return setWeight(Tuple.of(from, to), weight);
	}

	/**
	 * Returns the map with weight associated to a key.
	 *
	 * @param key    the key
	 * @param weight the weight
	 */
	public GeoMap setWeight(final Tuple2<MapNode, MapNode> key, final double weight) {
		final Map<Tuple2<MapNode, MapNode>, Double> newWeights = new HashMap<>(weights);
		newWeights.put(key, weight);
		return setWeights(newWeights);
	}

	/**
	 * Returns the map with the given weights.
	 *
	 * @param weights the weights
	 */
	public GeoMap setWeights(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		final Set<MapNode> sites = retrieveSites(weights);
		final Set<MapNode> nodes = Stream.concat(retrieveNodeStream(edges), sites.parallelStream())
				.collect(Collectors.toSet());
		return new GeoMap(edges, weights, sites, nodes, frequence);
	}
}
