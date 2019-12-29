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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mmarini.routes.model.Constants;
import org.mmarini.routes.model.YamlUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author mmarini
 *
 */
public class SimulationStatusDeserializer implements Constants {

	/**
	 * Returns a new simulation status deserializer
	 */
	public static SimulationStatusDeserializer create() {
		return new SimulationStatusDeserializer(new ObjectMapper(new YAMLFactory()), Collections.emptyMap(),
				Collections.emptyMap());
	}

	private final ObjectMapper mapper;
	private final Map<String, SiteNode> sites;
	private final Map<String, MapNode> nodes;

	/**
	 * @param mapper
	 * @param sites
	 * @param nodes
	 */
	public SimulationStatusDeserializer(final ObjectMapper mapper, final Map<String, SiteNode> sites,
			final Map<String, MapNode> nodes) {
		super();
		this.mapper = mapper;
		this.sites = sites;
		this.nodes = nodes;
	}

	/**
	 *
	 * @param tree
	 * @return
	 */
	private SimulationStatusDeserializer loadNodes(final JsonNode tree) {
		final Map<String, SiteNode> sites = parseSites(tree.path("sites"));
		final Map<String, MapNode> nodes = parseNodes(tree.path("nodes"));
		return new SimulationStatusDeserializer(mapper, sites, nodes);
	}

	/**
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public SimulationStatus parse(final File file) throws JsonProcessingException, IOException {
		final JsonNode tree = mapper.readTree(file);
		return loadNodes(tree).parse(tree);
	}

	/**
	 *
	 * @param tree
	 * @return
	 */
	private SimulationStatus parse(final JsonNode tree) {
		final double frequence = tree.path("default").path("frequence").asDouble();
		final GeoMap map = parseMap(tree);
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = parsePaths(tree.path("paths"), map.getSites());
		final Set<EdgeTraffic> traffics = map.getEdges().parallelStream().map(EdgeTraffic::create)
				.collect(Collectors.toSet());
		final SimulationStatus result = SimulationStatus.create().setGeoMap(map).setFrequence(frequence)
				.setWeights(weights).setTraffics(traffics);
		return result;
	}

	/**
	 *
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public SimulationStatus parse(final URL resource) throws IOException {
		final JsonNode tree = mapper.readTree(resource);
		return loadNodes(tree).parse(tree);
	}

	/**
	 *
	 * @param jsonNode
	 * @return
	 */
	private MapEdge parseEdge(final JsonNode jsonNode, final Map<String, MapNode> nodeMap) {
		final MapNode begin = nodeMap.get(jsonNode.path("start").asText());
		if (begin == null) {
			throw new IllegalArgumentException("Missing start node");
		}
		final MapNode end = nodeMap.get(jsonNode.path("end").asText());
		if (end == null) {
			throw new IllegalArgumentException("Missing end node");
		}
		final MapEdge result = MapEdge.create(begin, end)
				.setPriority(YamlUtils.jsonInt(jsonNode.path("priority"), DEFAULT_PRIORITY))
				.setSpeedLimit(YamlUtils.jsonDouble(jsonNode.get("speedLimit"), DEFAULT_SPEED_LIMIT_KMH) * KMH_TO_MPS);
		return result;
	}

	/**
	 *
	 * @param tree
	 * @param sites
	 * @param nodeMap
	 * @return
	 */
	private Set<MapEdge> parseEdges(final JsonNode tree, final Map<String, MapNode> nodeMap) {
		final Set<MapEdge> edges = YamlUtils.toStream(tree.elements()).map(node -> parseEdge(node, nodeMap))
				.collect(Collectors.toSet());
		return edges;
	}

	/**
	 *
	 * @param tree
	 * @return
	 */
	private GeoMap parseMap(final JsonNode tree) {
		final Map<String, MapNode> all = new HashMap<>(nodes);
		all.putAll(sites);
		final Set<MapEdge> edges = parseEdges(tree.path("edges"), all);
		final GeoMap g = GeoMap.create().setSites(Set.copyOf(sites.values())).setNodes(Set.copyOf(nodes.values()))
				.setEdges(edges);
		return g;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private MapNode parseMapNode(final JsonNode jsonNode) {
		final double x = jsonNode.path("x").asDouble();
		final double y = jsonNode.path("y").asDouble();
		final MapNode mapNode = MapNode.create(x, y);
		return mapNode;
	}

	/**
	 * Returns the map between names and nodes
	 *
	 * @param path the json node with nodes
	 */
	private Map<String, MapNode> parseNodes(final JsonNode nodesJson) {
		final Map<String, MapNode> nodes = YamlUtils.toStream(nodesJson.fieldNames())
				.collect(Collectors.toMap(Function.identity(), name -> parseMapNode(nodesJson.path(name))));
		return nodes;
	}

	/**
	 *
	 * @param json
	 * @param sites
	 * @return
	 */
	private Tuple2<Tuple2<SiteNode, SiteNode>, Double> parsePath(final JsonNode jsonNode) {
		final String depId = jsonNode.path("departure").asText();
		final SiteNode departure = sites.get(depId);
		if (departure == null) {
			throw new IllegalArgumentException(String.format("Departure site \"%s\" not found", depId));
		}

		final String destId = jsonNode.path("destination").asText();
		final SiteNode destination = sites.get(destId);
		if (destination == null) {
			throw new IllegalArgumentException(String.format("Destination site \"%s\" not found", destId));
		}
		final double weight = YamlUtils.jsonDouble(jsonNode.path("weight"), DEFAULT_WEIGHT);
		final Tuple2<Tuple2<SiteNode, SiteNode>, Double> result = new Tuple2<>(new Tuple2<>(departure, destination),
				weight);
		return result;
	}

	/**
	 *
	 * @param path
	 * @param sites
	 * @return
	 */
	private Map<Tuple2<SiteNode, SiteNode>, Double> parsePaths(final JsonNode jsonNode, final Set<SiteNode> sites) {
		final Stream<Tuple2<Tuple2<SiteNode, SiteNode>, Double>> paths = YamlUtils.toStream(jsonNode.elements())
				.map(json -> parsePath(json));
		final Map<Tuple2<SiteNode, SiteNode>, Double> result = paths
				.collect(Collectors.toMap(path -> path.getElem1(), path -> path.getElem2()));
		return result;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private SiteNode parseSiteNode(final JsonNode jsonNode) {
		final double x = jsonNode.path("x").asDouble();
		final double y = jsonNode.path("y").asDouble();
		final SiteNode mapNode = SiteNode.create(x, y);
		return mapNode;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private Map<String, SiteNode> parseSites(final JsonNode sitesJson) {
		final List<String> names = YamlUtils.toList(sitesJson.fieldNames());
		if (names.size() < 2) {
			throw new IllegalArgumentException("There must be at least two sites");
		}
		final Map<String, SiteNode> sites = names.stream()
				.collect(Collectors.toMap(Function.identity(), name -> parseSiteNode(sitesJson.path(name))));
		return sites;
	}
}
