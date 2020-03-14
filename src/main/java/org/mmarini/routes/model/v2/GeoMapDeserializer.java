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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Deserializer of map that converts a json object to a GeoMap.
 */
public class GeoMapDeserializer implements Constants {

	private static final String CURRENT_VERSION = "1";

	/** Returns a new simulation status deserializer. */
	public static GeoMapDeserializer create() {
		return new GeoMapDeserializer(new ObjectMapper(new YAMLFactory()), Map.of(), Set.of(), Map.of(), Set.of(),
				DEFAULT_FREQUENCE);
	}

	private final ObjectMapper mapper;
	private final Map<String, MapNode> names;
	private final Set<MapNode> sites;
	private final Map<Tuple2<MapNode, MapNode>, Double> weights;
	private final Set<MapEdge> edges;
	private final double frequence;

	/**
	 * Create a deserializer.
	 *
	 * @param mapper    the json object mapper
	 * @param names     map between name and map node
	 * @param sites     the sites
	 * @param weights   the map between pairs of departure and destination and the
	 *                  weights
	 * @param edges     the edges
	 * @param frequence the frequency
	 */
	protected GeoMapDeserializer(final ObjectMapper mapper, final Map<String, MapNode> names, final Set<MapNode> sites,
			final Map<Tuple2<MapNode, MapNode>, Double> weights, final Set<MapEdge> edges, final double frequence) {
		super();
		this.mapper = mapper;
		this.names = names;
		this.sites = sites;
		this.weights = weights;
		this.frequence = frequence;
		this.edges = edges;
	}

	/** Returns the built map. */
	private GeoMap build() {
		if (sites.size() == 1) {
			final MapNode site = sites.stream().findAny().get();
			return GeoMap.create(edges, site).setFrequence(frequence);
		} else {
			return GeoMap.create(edges, weights).setFrequence(frequence);
		}
	}

	/**
	 * Returns the map by parsing yaml file.
	 *
	 * @param file the file
	 * @throws IOException             in case of error
	 * @throws JsonProcessingException in case of error
	 */
	public GeoMap parse(final File file) throws JsonProcessingException, IOException {
		final JsonNode tree = mapper.readTree(file);
		final GeoMap result = parse(tree);
		return result;
	}

	/**
	 * Returns the map by parsing json object.
	 *
	 * @param tree the json object
	 */
	private GeoMap parse(final JsonNode tree) {
		final String version = tree.path("version").asText();
		if (!version.equals(CURRENT_VERSION)) {
			throw new IllegalArgumentException(format("Version must be %s", CURRENT_VERSION));
		}

		return parseNodesAndSites(tree).parsePaths(tree.path("paths")).parseEdges(tree.path("edges"))
				.parseFrequence(tree.path("frequence")).build();
	}

	/**
	 * Returns the map by parsing yaml resource url.
	 *
	 * @param resource the resource url
	 * @throws IOException in case of error
	 */
	public GeoMap parse(final URL resource) throws IOException {
		final JsonNode tree = mapper.readTree(resource);
		final GeoMap result = parse(tree);
		return result;
	}

	/**
	 * Returns the edge parsing a json node.
	 *
	 * @param jsonNode the json node
	 */
	private MapEdge parseEdge(final JsonNode jsonNode) {
		final String startId = jsonNode.path("start").asText();
		final MapNode begin = names.get(startId);
		if (begin == null) {
			throw new IllegalArgumentException("Missing start node");
		}
		final MapNode end = names.get(jsonNode.path("end").asText());
		if (end == null) {
			throw new IllegalArgumentException("Missing end node");
		}
		final MapEdge result = MapEdge.create(begin, end)
				.setPriority(YamlUtils.jsonInt(jsonNode.path("priority"), DEFAULT_PRIORITY))
				.setSpeedLimit(YamlUtils.jsonDouble(jsonNode.get("speedLimit"), DEFAULT_SPEED_LIMIT_KMH) * KMH_TO_MPS);
		return result;
	}

	/**
	 * Returns the changed deserializer parsing json tree with edges.
	 *
	 * @param tree the json tree
	 */
	private GeoMapDeserializer parseEdges(final JsonNode tree) {
		final Set<MapEdge> edges = YamlUtils.streamFrom(tree.elements()).map(this::parseEdge)
				.collect(Collectors.toSet());
		return new GeoMapDeserializer(mapper, names, sites, weights, edges, frequence);
	}

	/**
	 * Returns the changed deserializer parsing json frequency node.
	 *
	 * @param jsonNode the frequency node
	 */
	private GeoMapDeserializer parseFrequence(final JsonNode jsonNode) {
		return new GeoMapDeserializer(mapper, names, sites, weights, edges, jsonNode.asDouble());
	}

	/**
	 * Returns the node parsing the json node.
	 *
	 * @param jsonNode the node
	 */
	private MapNode parseMapNode(final JsonNode jsonNode) {
		final double x = jsonNode.path("x").asDouble();
		final double y = jsonNode.path("y").asDouble();
		final MapNode mapNode = MapNode.create(x, y);
		return mapNode;
	}

	/**
	 * Returns the changed deserializer parsing json tree with nodes and sites.
	 * <p>
	 * The result deserializer has the names and sites properties updated with
	 * definition of json tree
	 * </p>
	 *
	 * @param tree the json tree
	 */
	private GeoMapDeserializer parseNodesAndSites(final JsonNode tree) {
		// Parse nodes
		final JsonNode nodesJson = tree.path("nodes");
		final Map<String, MapNode> nodesMap = YamlUtils.streamFrom(nodesJson.fieldNames())
				.collect(Collectors.toMap(Function.identity(), name -> parseMapNode(nodesJson.path(name))));
		// Parse sites
		final JsonNode sitesJson = tree.path("sites");
		final List<String> siteNames = YamlUtils.listFrom(sitesJson.fieldNames());
		final Map<String, MapNode> sitesMap = siteNames.stream()
				.collect(Collectors.toMap(Function.identity(), name -> parseMapNode(sitesJson.path(name))));

		// Merge to produce names
		final Map<String, MapNode> names = new HashMap<>(nodesMap);
		names.putAll(sitesMap);

		final Set<MapNode> sites = Set.copyOf(sitesMap.values());

		return new GeoMapDeserializer(mapper, names, sites, weights, edges, frequence);
	}

	/**
	 * Returns the path data parsing the path json node.
	 *
	 * @param json the json node
	 */
	private Tuple2<Tuple2<MapNode, MapNode>, Double> parsePath(final JsonNode jsonNode) {
		final String depId = jsonNode.path("departure").asText();
		final MapNode departure = names.get(depId);
		if (departure == null) {
			throw new IllegalArgumentException(String.format("Departure site \"%s\" not found", depId));
		}
		final String destId = jsonNode.path("destination").asText();
		final MapNode destination = names.get(destId);
		if (destination == null) {
			throw new IllegalArgumentException(String.format("Destination site \"%s\" not found", destId));
		}
		final double weight = YamlUtils.jsonDouble(jsonNode.path("weight"), DEFAULT_WEIGHT);
		final Tuple2<Tuple2<MapNode, MapNode>, Double> result = Tuple.of(Tuple.of(departure, destination), weight);
		return result;
	}

	/**
	 * Returns the deserializer parsing the paths json node.
	 * <p>
	 * The result deserializer has the sites and weights properties updated with the
	 * defnition on json node
	 * </p>
	 *
	 * @param jsonNode the json node
	 */
	private GeoMapDeserializer parsePaths(final JsonNode jsonNode) {
		final Map<Tuple2<MapNode, MapNode>, Double> weights = YamlUtils.streamFrom(jsonNode.elements())
				.map(this::parsePath).collect(Collectors.toMap(Tuple2::get1, Tuple2::get2));
		final Set<MapNode> wsites = weights.keySet().stream().flatMap(t -> {
			return Set.of(t.get1(), t.get2()).stream();
		}).collect(Collectors.toSet());
		final Set<MapNode> sites = new HashSet<>(this.sites);
		sites.addAll(wsites);
		return new GeoMapDeserializer(mapper, names, sites, weights, edges, frequence);
	}
}
