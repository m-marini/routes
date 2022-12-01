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

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;
import org.mmarini.routes.model2.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static org.mmarini.Utils.stream;
import static org.mmarini.routes.model2.Constants.*;
import static org.mmarini.routes.model2.StatusImpl.createStatus;
import static org.mmarini.yaml.schema.Locator.root;

public class Parsers {

    /**
     * Returns the default frequency
     *
     * @param node the defaults json node
     */
    static double defaultFrequency(JsonNode node) {
        requireNonNull(node);
        return node.path("frequence").asDouble(DEFAULT_FREQUENCY);
    }

    /**
     * Returns the default priority
     *
     * @param node the defaults json node
     */
    static int defaultPriority(JsonNode node) {
        requireNonNull(node);
        return node.path("priority").asInt(DEFAULT_PRIORITY);
    }

    /**
     * Returns the default speed limit in Km/h
     *
     * @param node the defaults json node
     */
    static double defaultSpeedLimit(JsonNode node) {
        requireNonNull(node);
        return node.path("speedLimit").asDouble(DEFAULT_SPEED_LIMIT_KMH);
    }

    /**
     * Returns the edge
     *
     * @param node              the edge json node
     * @param defaultPriority   the default priority
     * @param defaultSpeedLimit the default speed limit in K/h
     * @param nodeByName        the node map
     */
    public static MapEdge edge(JsonNode node, int defaultPriority, double defaultSpeedLimit, Map<String, ? extends MapNode> nodeByName) {
        requireNonNull(node);
        requireNonNull(nodeByName);
        return new MapEdge(
                nodeByName.get(node.path("start").asText("")),
                nodeByName.get(node.path("end").asText("")),
                node.path("speedLimit").asDouble(defaultSpeedLimit) / KMPHSPM,
                node.path("priority").asInt(defaultPriority)
        );
    }

    /**
     * Returns the list of edge
     *
     * @param node              the edges json node
     * @param defaultPriority   the default priority
     * @param defaultSpeedLimit the default speed limit in K/h
     * @param nodeByName        the node map
     */
    static List<MapEdge> edges(JsonNode node, int defaultPriority, double defaultSpeedLimit, Map<String, ? extends MapNode> nodeByName) {
        requireNonNull(node);
        requireNonNull(nodeByName);
        return stream(node.elements())
                .map(n -> edge(n, defaultPriority, defaultSpeedLimit, nodeByName))
                .collect(Collectors.toList());
    }

    /**
     * Returns the module by parsing the json tree
     *
     * @param node the module json node
     */
    static MapModule module(JsonNode node) {
        JsonNode defaultsNode = node.path("defaults");
        int defaultPriority = defaultPriority(defaultsNode);
        double defaultSpeedLimit = defaultSpeedLimit(defaultsNode);
        Map<String, ? extends MapNode> anyNodeByName = nodes(node.path("nodes"));
        List<MapEdge> edges = edges(node.path("edges"), defaultPriority, defaultSpeedLimit, anyNodeByName);
        return new MapModule(edges);
    }

    /**
     * @param node the node json node
     */
    public static CrossNode node(JsonNode node) {
        requireNonNull(node);
        return CrossNode.createNode(
                node.path("x").asDouble(0),
                node.path("y").asDouble(0)
        );
    }

    /**
     * Returns the map of nodes by name
     *
     * @param node the nodes json node
     */
    static Map<String, ? extends CrossNode> nodes(JsonNode node) {
        requireNonNull(node);
        return stream(node.fieldNames())
                .collect(Collectors.toMap(
                        identity(),
                        name -> node(node.path(name))
                ));
    }

    /**
     * Returns the module by parsing the json tree
     *
     * @param node the module json node
     */
    public static MapModule parseModule(JsonNode node) {
        requireNonNull(node);
        SchemaValidators.module().apply(root())
                .andThen(CrossValidators.module().apply(root()))
                .accept(node);
        return module(node);
    }

    /**
     * Returns the status by parsing the json tree
     *
     * @param node the status json node
     */
    public static StatusImpl parseStatus(JsonNode node) {
        requireNonNull(node);
        SchemaValidators.route().apply(root())
                .andThen(CrossValidators.route().apply(root()))
                .accept(node);
        return status(node);
    }

    /**
     * Returns the stream of weights
     *
     * @param node       the paths node
     * @param siteByName the sites by name
     */
    private static Stream<Tuple2<Tuple2<? extends SiteNode, ? extends SiteNode>, Double>> paths(JsonNode node, Map<String, ? extends SiteNode> siteByName) {
        requireNonNull(node);
        requireNonNull(siteByName);
        return stream(node.elements())
                .map(Parsers::weight)
                .map(t -> t.setV1(
                        Tuple2.of(siteByName.get(t._1._1), siteByName.get(t._1._2))
                ));
    }

    /**
     * Returns the site node
     *
     * @param node the site json node
     */
    public static SiteNode site(JsonNode node) {
        requireNonNull(node);
        return SiteNode.createSite(
                node.path("x").asDouble(0),
                node.path("y").asDouble(0)
        );
    }

    /**
     * Returns the sites map
     *
     * @param node the sites json node
     */
    public static Map<String, ? extends SiteNode> sites(JsonNode node) {
        requireNonNull(node);
        return stream(node.fieldNames())
                .collect(Collectors.toMap(
                        identity(),
                        name -> site(node.path(name))
                ));
    }

    /**
     * @param node the status node
     */
    public static StatusImpl status(JsonNode node) {
        requireNonNull(node);
        int maxVehicles = node.path("maxVehicles").asInt(DEFAULT_MAX_VEHICLES);

        JsonNode defaultsNode = node.path("default");
        int defaultPriority = defaultPriority(defaultsNode);
        double defaultSpeedLimit = defaultSpeedLimit(defaultsNode);
        double frequency = defaultFrequency(defaultsNode);

        Map<String, ? extends SiteNode> siteByName = sites(node.path("sites"));
        Map<String, MapNode> anyNodeByName = new HashMap<>(nodes(node.path("nodes")));
        anyNodeByName.putAll(siteByName);

        List<MapEdge> edgeList = edges(node.path("edges"), defaultPriority, defaultSpeedLimit, anyNodeByName);
        List<? extends SiteNode> siteList = new ArrayList<>(siteByName.values());
        List<MapNode> nodeList = new ArrayList<>(anyNodeByName.values());
        Topology topology = Topology.createTopology(nodeList, edgeList);

        Stream<Tuple2<Tuple2<? extends SiteNode, ? extends SiteNode>, Double>> w = paths(node.path("paths"), siteByName);

        double[][] weights = DoubleMatrix.from(w, siteList).getValues();
        return createStatus(maxVehicles, defaultSpeedLimit / KMPHSPM, frequency, 0,
                topology, List.of(), weights);
    }

    /**
     * @param node the weight node
     */
    public static Tuple2<Tuple2<String, String>, Double> weight(JsonNode node) {
        requireNonNull(node);
        return Tuple2.of(
                Tuple2.of(
                        node.path("departure").asText(),
                        node.path("destination").asText()),
                node.path("weight").asDouble(DEFAULT_WEIGHT)
        );
    }
}
