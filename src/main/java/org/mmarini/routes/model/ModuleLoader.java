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

package org.mmarini.routes.model;

import static java.lang.String.format;
import static org.mmarini.routes.model.YamlUtils.jsonDouble;
import static org.mmarini.routes.model.YamlUtils.jsonInt;
import static org.mmarini.routes.model.YamlUtils.toStream;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author mmarini
 *
 */
public class ModuleLoader {

	private static final double MPS_TO_KMH = 3.6;
	private static final int DEFAULT_PRIORITY = 0;
	private static final double DEFAULT_SPEED_LIMIT = 25.0;

	/**
	 *
	 * @param file
	 * @return
	 */
	public static Module load(final File file) {
		final Module result = new ModuleLoader().loadModule(file);
		return result;
	}

	/**
	 *
	 * @param tree
	 * @return
	 */
	static Map<String, MapNode> loadNodes(final JsonNode tree) {
		if (tree == null || !tree.isObject()) {
			throw new IllegalArgumentException("Missing nodes attribute");
		}
		final Map<String, MapNode> nodes = toStream(tree.fieldNames()).collect(
				Collectors.<String, String, MapNode>toMap(Function.identity(), name -> toMapNode(tree.get(name))));
		return nodes;
	}

	/**
	 *
	 * @param jsonNode
	 * @return
	 */
	private static MapNode toMapNode(final JsonNode jsonNode) {
		final MapNode mapNode = new MapNode();
		final double x = jsonNode.get("x").asDouble();
		final double y = jsonNode.get("y").asDouble();
		mapNode.setLocation(new Point2D.Double(x, y));
		return mapNode;
	}

	private final ObjectMapper mapper;
	private final ModuleBuilder builder;
	private int defaultPriority;
	private double defaultSpeedLimit;
	private Map<String, MapNode> nodes;

	/**
	 *
	 */
	protected ModuleLoader() {
		super();
		this.builder = new ModuleBuilder();
		this.mapper = new ObjectMapper(new YAMLFactory());
	}

	/**
	 *
	 * @param tree
	 * @return
	 */
	List<MapEdge> loadEdges(final JsonNode tree) {
		if (!tree.isArray()) {
			throw new IllegalArgumentException("Missing edges attribute");
		}
		final Stream<JsonNode> x = toStream(tree.elements());
		final List<MapEdge> edges = x.map(edgeTree -> toMapEdge(edgeTree)).collect(Collectors.toList());
		return edges;
	}

	/**
	 *
	 * @param file
	 * @return
	 */
	public Module loadModule(final File file) {
		try {
			final JsonNode tree = mapper.readTree(file);
			final Module result = loadModule(tree);
			return result;
		} catch (final IOException e) {
			throw new Error(e.getMessage(), e);
		} catch (final IllegalArgumentException e) {
			throw new Error(format("Error parsing %s: %s", file.getAbsolutePath(), e.getMessage()), e);
		}
	}

	/**
	 *
	 * @param tree
	 * @return
	 */
	Module loadModule(final JsonNode tree) {
		final Module result = new Module();
		builder.setModule(result);
		builder.clear();
		this.defaultPriority = jsonInt(tree.get("priority"), DEFAULT_PRIORITY);
		this.defaultSpeedLimit = jsonDouble(tree.get("speedLimit"), DEFAULT_SPEED_LIMIT);
		nodes = loadNodes(tree.get("nodes"));
		nodes.values().forEach(builder::add);

		final List<MapEdge> edges = loadEdges(tree.get("edges"));
		edges.forEach(builder::add);

		return result;
	}

	/**
	 * @param edgeTree
	 * @return
	 */
	MapEdge toMapEdge(final JsonNode edgeTree) {
		final MapEdge result = new MapEdge();
		result.setPriority(jsonInt(edgeTree.get("priority"), defaultPriority));
		result.setSpeedLimit(jsonDouble(edgeTree.get("speedLimit"), defaultSpeedLimit) / MPS_TO_KMH);

		final JsonNode startJson = edgeTree.get("start");
		if (startJson == null) {
			throw new IllegalArgumentException("Missing start property");
		}
		final String startName = startJson.asText();
		final MapNode start = nodes.get(startName);
		if (start == null) {
			throw new IllegalArgumentException(format("Missing start node \"%s\"", startName));
		}

		final JsonNode endJson = edgeTree.get("end");
		if (endJson == null) {
			throw new IllegalArgumentException("Missing end property");
		}
		final String endName = endJson.asText();
		final MapNode end = nodes.get(endName);
		if (end == null) {
			throw new IllegalArgumentException(format("Missing end node \"%s\"", endName));
		}

		result.setBegin(start);
		result.setEnd(end);

		return result;
	}
}
