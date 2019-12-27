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

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mmarini.routes.model.Constants;
import org.mmarini.routes.model.YamlUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author mmarini
 *
 */
public class SimulationStatusDeserializer implements Constants {

	private final ObjectMapper mapper;

	public SimulationStatusDeserializer() {
		super();
		this.mapper = new ObjectMapper(new YAMLFactory());
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
		final SimulationStatus result = SimulationStatus.create().setGeoMap(map).setFrequence(frequence)
				.setWeights(weights);
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
		return parse(tree);
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
		final Set<SiteNode> sites = parseSites(tree.path("sites"));
		final Set<MapNode> nodes = parseNodes(tree.path("nodes"));
		final Set<MapNode> all = new HashSet<>(nodes);
		all.addAll(sites);
		final Map<String, MapNode> nodeMap = all.stream()
				.collect(Collectors.toMap(n -> n.getId().toString(), Function.identity()));
		final Set<MapEdge> edges = parseEdges(tree.path("edges"), nodeMap);
		final GeoMap g = GeoMap.create().setSites(sites).setNodes(nodes).setEdges(edges);
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
	 *
	 * @param path
	 * @return
	 */
	private Set<MapNode> parseNodes(final JsonNode nodesJson) {
		final List<String> names = YamlUtils.toList(nodesJson.fieldNames());
		final Set<MapNode> sites = names.stream().map(name -> parseMapNode(nodesJson.path(name)))
				.collect(Collectors.toSet());
		return sites;
	}

	/**
	 *
	 * @param json
	 * @param sites
	 * @return
	 */
	private Tuple2<Tuple2<SiteNode, SiteNode>, Double> parsePath(final JsonNode jsonNode, final Set<SiteNode> sites) {
		final String depId = jsonNode.path("departure").asText();
		final SiteNode departure = sites.stream().filter(s -> s.getId().toString().equals(depId)).findFirst().get();
		final String destId = jsonNode.path("destination").asText();
		final SiteNode destination = sites.stream().filter(s -> s.getId().toString().equals(destId)).findFirst().get();
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
				.map(json -> parsePath(json, sites));
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
	private Set<SiteNode> parseSites(final JsonNode sitesJson) {
		final List<String> names = YamlUtils.toList(sitesJson.fieldNames());
		if (names.size() < 2) {
			throw new IllegalArgumentException("There must be at least two sites");
		}
		final Set<SiteNode> sites = names.stream().map(name -> parseSiteNode(sitesJson.path(name)))
				.collect(Collectors.toSet());
		return sites;
	}
}
