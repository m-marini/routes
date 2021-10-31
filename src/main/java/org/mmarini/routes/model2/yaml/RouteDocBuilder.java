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
import org.mmarini.routes.model2.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.*;

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
    static ObjectNode build(Status status) {
        requireNonNull(status);
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
    static ObjectNode createDefault(Status status) {
        requireNonNull(status);
        return mapper.createObjectNode()
                .put("frequence", status.getFrequency())
                .put("speedLimit", status.getSpeedLimit() * 3.6);
    }

    static ArrayNode createEdges(List<MapEdge> edges, Map<MapNode, String> nameByNodes) {
        requireNonNull(edges);
        requireNonNull(nameByNodes);
        ArrayNode result = mapper.createArrayNode();
        edges.forEach(edge -> {
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
        requireNonNull(node);
        return mapper.createObjectNode()
                .put("x", node.getLocation().getX())
                .put("y", node.getLocation().getY());
    }

    static ObjectNode createNodes(List<MapNode> nodes, Map<MapNode, String> nameByNodes) {
        requireNonNull(nodes);
        requireNonNull(nameByNodes);
        ObjectNode result = mapper.createObjectNode();
        nodes.stream()
                .filter(CrossNode.class::isInstance)
                .forEach(node ->
                        result.set(nameByNodes.get(node), createNode(node)));
        return result;
    }

    static ArrayNode createPaths(Status status, Map<MapNode, String> nameByNodes) {
        requireNonNull(status);
        requireNonNull(nameByNodes);
        ArrayNode result = mapper.createArrayNode();
        // Rebuild the weights from cdf
        DoubleMatrix<SiteNode> x = status.getWeightMatrix();
        x.entries()
                .flatMap(entry ->
                        getValue(nameByNodes, entry._1._1)
                                .flatMap(dep ->
                                        getValue(nameByNodes, entry._1._2)
                                                .map(dest ->
                                                        mapper.createObjectNode()
                                                                .put("departure", dep)
                                                                .put("destination", dest)
                                                                .put("weight", entry._2.doubleValue())
                                                ))
                                .stream())
                .forEach(result::add);
        return result;
    }

    static ObjectNode createSites(List<SiteNode> nodes, Map<MapNode, String> nameByNodes) {
        requireNonNull(nodes);
        requireNonNull(nameByNodes);
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
    static void write(File file, Status status) throws IOException {
        requireNonNull(file);
        requireNonNull(status);
        mapper.writeValue(file, build(status));
    }
}
