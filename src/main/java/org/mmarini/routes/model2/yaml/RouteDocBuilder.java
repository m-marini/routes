/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.routes.model2.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.mmarini.routes.model2.MapEdge;
import org.mmarini.routes.model2.MapNode;
import org.mmarini.routes.model2.SiteNode;
import org.mmarini.routes.model2.StatusImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mmarini.Utils.toMap;
import static org.mmarini.Utils.zipWithIndex;

public class RouteDocBuilder {
    static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Returns the json node with the route definitions
     *
     * @param status the status
     */
    static ObjectNode build(StatusImpl status) {
        Map<MapNode, String> nameByNodes = zipWithIndex(status.getNodes())
                .map(entry -> Map.entry(entry.getValue(),
                        "Node_" + entry.getKey()
                ))
                .collect(toMap());
        ObjectNode root = mapper.createObjectNode();
        root.set("default", createDefault(status));
        root.set("sites", createSites(status.getSites(), nameByNodes));
        root.set("paths", createPaths(status, nameByNodes));
        root.set("nodes", createNodes(status.getNodes(), nameByNodes));
        root.set("edges", createEdges(status.getEdges(), nameByNodes));
        return root;
    }

    /**
     * Returns json node with default values
     *
     * @param status the status
     */
    static ObjectNode createDefault(StatusImpl status) {
        return mapper.createObjectNode()
                .put("frequence", status.getFrequency());
    }

    static ArrayNode createEdges(List<MapEdge> edges, Map<MapNode, String> nameByNodes) {
        ArrayNode result = mapper.createArrayNode();
        edges.stream().forEach(edge -> {
            String begin = nameByNodes.get(edge.getBegin());
            String end = nameByNodes.get(edge.getEnd());
            ObjectNode jsonEdge = mapper.createObjectNode()
                    .put("start", begin)
                    .put("end", end)
                    .put("priority", edge.getPriority())
                    .put("speedLimit", edge.getSpeedLimit() * 3.6);
            result.add(jsonEdge);
        });
        return result;
    }

    static ObjectNode createNode(MapNode node) {
        return mapper.createObjectNode()
                .put("x", node.getLocation().getX())
                .put("y", node.getLocation().getY());
    }

    static ObjectNode createNodes(List<MapNode> nodes, Map<MapNode, String> nameByNodes) {
        ObjectNode result = mapper.createObjectNode();
        nodes.stream()
                .filter(node -> !(node instanceof SiteNode))
                .forEach(node ->
                        result.set(nameByNodes.get(node), createNode(node)));
        return result;
    }

    static ArrayNode createPaths(StatusImpl status, Map<MapNode, String> nameByNodes) {
        ArrayNode result = mapper.createArrayNode();
        double[][] cdf = status.getPathCdf();
        int n = cdf.length;
        // Rebuild the weights from cdf
        double[][] weights = new double[n][n];
        for (int i = 0; i < n; i++) {
            weights[i][0] = cdf[i][0];
            for (int j = 1; j < n; j++) {
                weights[i][j] = cdf[i][j] - cdf[i][j - 1];
            }
        }
        for (int i = 0; i < n; i++) {
            String departure = nameByNodes.get(status.getSites().get(i));
            for (int j = 0; j < n; j++) {
                if (i != j && weights[i][j] != 0) {
                    String destination = nameByNodes.get(status.getSites().get(j));
                    ObjectNode path = mapper.createObjectNode()
                            .put("departure", departure)
                            .put("destination", destination)
                            .put("weight", weights[i][j]);
                    result.add(path);
                }
            }
        }

        return result;
    }

    static ObjectNode createSites(List<SiteNode> nodes, Map<MapNode, String> nameByNodes) {
        ObjectNode result = mapper.createObjectNode();
        for (SiteNode node : nodes) {
            result.set(nameByNodes.get(node), createNode(node));
        }
        return result;
    }

    /**
     * Writes the status to file
     *
     * @param file   the file
     * @param status the status
     * @throws IOException in case of error
     */
    static void write(File file, StatusImpl status) throws IOException {
        mapper.writeValue(file, build(status));
    }
}
