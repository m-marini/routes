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

import org.mmarini.routes.model.Constants;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
public class SimulationStatusSerializer implements Constants {

	private final SimulationStatus status;
	private final ObjectMapper mapper;

	/**
	 * @param status
	 *
	 */
	public SimulationStatusSerializer(final SimulationStatus status) {
		super();
		mapper = new ObjectMapper(new YAMLFactory());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		this.status = status;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode buildDefaultNode() {
		final ObjectNode result = mapper.createObjectNode();
		result.put("frequence", status.getFrequence());
		return result;
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	private JsonNode buildEdge(final MapEdge edge) {
		final ObjectNode result = mapper.createObjectNode().put("start", edge.getBegin().getId().toString())
				.put("end", edge.getEnd().getId().toString()).put("priority", edge.getPriority())
				.put("speedLimit", edge.getSpeedLimit() * MPS_TO_KMH);
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode buildEdgesNode() {
		final ArrayNode result = mapper.createArrayNode();
		status.getMap().getEdges().forEach(edge -> result.add(buildEdge(edge)));
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
	 * @return
	 */
	private JsonNode buildNodesNode() {
		final ObjectNode result = mapper.createObjectNode();
		status.getMap().getNodes().forEach(node -> result.set(node.getId().toString(), buildNode(node)));
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode buildPathsNode() {
		final ArrayNode result = mapper.createArrayNode();
		status.getMap().getSites().forEach(
				from -> status.getMap().getSites().forEach(to -> status.getWeight(from, to).ifPresent(weight -> {
					final ObjectNode path = mapper.createObjectNode().put("departure", from.getId().toString())
							.put("destination", to.getId().toString()).put("weight", weight);
					result.add(path);
				})));
		return result;
	}

	/**
	 *
	 * @return
	 */
	private JsonNode buildSitesNode() {
		final ObjectNode result = mapper.createObjectNode();
		status.getMap().getSites().forEach(node -> result.set(node.getId().toString(), buildNode(node)));
		return result;
	}

	/**
	 *
	 * @param status
	 * @return
	 */
	JsonNode toJson() {
		final ObjectNode result = mapper.createObjectNode();
		result.set("default", buildDefaultNode());
		result.set("sites", buildSitesNode());
		result.set("paths", buildPathsNode());
		result.set("nodes", buildNodesNode());
		result.set("edges", buildEdgesNode());
		return result;
	}

	/**
	 *
	 * @param file
	 * @param status
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	void writeFile(final File file) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(file, toJson());
	}

}
