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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author mmarini
 *
 */
public class SimulatorWriter implements Constants {
	/**
	 *
	 * @param file
	 * @param simulator
	 */
	public static void write(final File file, final Simulator simulator) {
		new SimulatorWriter(simulator).write(file);
	}

	private final ObjectMapper mapper;
	private final Simulator sim;
	private final Map<MapNode, String> nodeMap;

	/**
	 *
	 * @param sim
	 */
	public SimulatorWriter(final Simulator sim) {
		super();
		this.sim = sim;
		mapper = new ObjectMapper(new YAMLFactory());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		final List<MapNode> nodeList = new ArrayList<>(sim.getSiteNodes());
		nodeList.addAll(sim.getMapNodes());
		nodeMap = new HashMap<>();
		IntStream.range(0, nodeList.size()).forEach(i -> nodeMap.put(nodeList.get(i), "Node_" + i));
	}

	/**
	 *
	 * @return
	 */
	private JsonNode build() {
		final ObjectNode result = mapper.createObjectNode();
		result.set("default", createDefaultNode());
		result.set("sites", createSitesNode());
		result.set("paths", createPathsNode());
		result.set("nodes", createNodesNode());
		result.set("edges", createEdgesNode());
		return result;
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	private JsonNode buildEdge(final MapEdge edge) {
		final ObjectNode result = mapper.createObjectNode().put("start", nodeMap.get(edge.getBegin()))
				.put("end", nodeMap.get(edge.getEnd())).put("priority", edge.getPriority())
				.put("speedLimit", edge.getSpeedLimit() * MPS_TO_KMH);
		return result;
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	private JsonNode buildNode(final MapNode node) {
		final ObjectNode result = mapper.createObjectNode().put("x", node.getLocation().getX()).put("y",
				node.getLocation().getY());
		return result;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private JsonNode buildPath(final Path path) {
		final ObjectNode result = mapper.createObjectNode().put("departure", nodeMap.get(path.getDeparture()))
				.put("destination", nodeMap.get(path.getDestination())).put("weight", path.getWeight());
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode createDefaultNode() {
		final ObjectNode result = mapper.createObjectNode();
		result.put("frequence", sim.getFrequence());
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode createEdgesNode() {
		final ArrayNode result = mapper.createArrayNode();
		sim.getMapEdges().forEach(edge -> result.add(buildEdge(edge)));
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode createNodesNode() {
		final ObjectNode result = mapper.createObjectNode();
		sim.getMapNodes().forEach(node -> result.set(nodeMap.get(node), buildNode(node)));
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode createPathsNode() {
		final ArrayNode result = mapper.createArrayNode();
		sim.getPaths().forEach(path -> result.add(buildPath(path)));
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode createSitesNode() {
		final ObjectNode result = mapper.createObjectNode();
		sim.getSiteNodes().forEach(node -> result.set(nodeMap.get(node), buildNode(node)));
		return result;
	}

	/**
	 *
	 * @param file
	 */
	private void write(final File file) {
		try {
			mapper.writeValue(file, build());
		} catch (final IOException e) {
			throw new Error(e.getMessage(), e);
		}
	}
}
