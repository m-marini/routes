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

package org.mmarini.routes.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author mmarini
 */
public class SimulatorLoader implements Constants {

    /**
     * @param file
     * @param builder2
     */
    public static void load(final File file, final Simulator simulator) {
        new SimulatorLoader(simulator).load(file);
    }

    /**
     * @param url
     * @param builder
     */
    public static void load(final URL url, final Simulator builder) {
        new SimulatorLoader(builder).load(url);
    }

    private final ObjectMapper mapper;
    private final Simulator simulator;
    private double defaultSpeedLimit;
    private int defaultPriority;
    private Map<String, SiteNode> sites;
    private Map<String, MapNode> nodes;

    /**
     * @param simulator
     */
    protected SimulatorLoader(final Simulator simulator) {
        super();
        this.simulator = simulator;
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * @param jsonNode
     * @param string
     * @return
     */
    MapNode findNode(final JsonNode jsonNode, final String key) {
        final JsonNode json = jsonNode.get(key);
        if (json == null) {
            throw new IllegalArgumentException(format("Missing %s property", key));
        }
        if (!json.isTextual()) {
            throw new IllegalArgumentException(format("%s property must be a text", key));
        }
        final String name = json.asText();
        final SiteNode site = sites.get(name);
        if (site != null) {
            return site;
        } else {
            final MapNode node = nodes.get(name);
            if (node != null) {
                return node;
            } else {
                throw new IllegalArgumentException(format("%s node \"%s\" not found", key, name));
            }
        }
    }

    /**
     * @param json
     * @return
     */
    SiteNode findSite(final JsonNode jsonNode, final String key) {
        final JsonNode json = jsonNode.get(key);
        if (json == null || !json.isTextual()) {
            throw new IllegalArgumentException(format("Missing %s property", key));
        }
        final String name = json.asText();
        final SiteNode site = sites.get(name);
        if (site == null) {
            throw new IllegalArgumentException(format("%s site \"%s\" not found", key, name));
        }
        return site;
    }

    /**
     * @param url
     */
    public SimulatorLoader load(final File file) {
        try {
            final JsonNode tree = mapper.readTree(file);
            return load(tree);
        } catch (final IOException e) {
            throw new Error(e.getMessage(), e);
        } catch (final IllegalArgumentException e) {
            throw new Error(format("Error parsing %s: %s", file.getAbsolutePath(), e.getMessage()), e);
        }
    }

    /**
     * @param tree
     * @return
     */
    SimulatorLoader load(final JsonNode tree) {
        simulator.clear();
        loadDefault(tree.get("default"));

        sites = loadSites(tree.get("sites"));
        sites.values().forEach(simulator::add);

        final List<Path> paths = loadPaths(tree.get("paths"));
        paths.forEach(simulator::add);

        nodes = loadNodes(tree.get("nodes"));
        nodes.values().forEach(simulator::add);

        final List<MapEdge> edges = loadEdges(tree.get("edges"));
        edges.forEach(simulator::add);

        simulator.init();
        return this;
    }

    /**
     * @param url
     */
    public SimulatorLoader load(final URL url) {
        try {
            final InputStream is = url.openStream();
            final JsonNode tree = mapper.readTree(is);
            return load(tree);
        } catch (final IOException e) {
            throw new Error(e.getMessage(), e);
        } catch (final IllegalArgumentException e) {
            throw new Error(format("Error parsing %s: %s", url.toExternalForm(), e.getMessage()), e);
        }
    }

    /**
     * @param def
     * @return
     */
    SimulatorLoader loadDefault(final JsonNode def) {
        if (def == null || !def.isObject()) {
            this.defaultSpeedLimit = DEFAULT_SPEED_LIMIT_KMH;
            this.defaultPriority = DEFAULT_PRIORITY;
            simulator.setFrequence(DEFAULT_FREQUENCE);
        } else {
            this.defaultSpeedLimit = YamlUtils.jsonDouble(def.get("speedLimit"), DEFAULT_SPEED_LIMIT_KMH);
            this.defaultPriority = YamlUtils.jsonInt(def.get("defaultPriority"), DEFAULT_PRIORITY);
            simulator.setFrequence(YamlUtils.jsonDouble(def.get("defaultFrequence"), DEFAULT_FREQUENCE));
        }
        return this;
    }

    /**
     * @param jsonNode
     * @return
     */
    List<MapEdge> loadEdges(final JsonNode jsonNode) {
        if (jsonNode == null) {
            throw new IllegalArgumentException("Missing edges property");
        }
        if (!jsonNode.isArray()) {
            throw new IllegalArgumentException("edges property must be an array");
        }
        final List<MapEdge> edges = YamlUtils.toStream(jsonNode.elements()).map(this::toEdge)
                .collect(Collectors.toList());
        return edges;
    }

    /**
     * @param jsonNode
     * @return
     */
    Map<String, MapNode> loadNodes(final JsonNode jsonNode) {
        if (jsonNode == null) {
            throw new IllegalArgumentException("Missing nodes property");
        }
        if (!jsonNode.isObject()) {
            throw new IllegalArgumentException("nodes property must be an object");
        }
        final Map<String, MapNode> nodes = YamlUtils.toStream(jsonNode.fieldNames()).collect(
                Collectors.<String, String, MapNode>toMap(Function.identity(), name -> toMapNode(jsonNode.get(name))));
        return nodes;
    }

    /**
     * @param jsonNode
     * @return
     */
    List<Path> loadPaths(final JsonNode jsonNode) {
        if (jsonNode == null) {
            throw new IllegalArgumentException("Missing paths property");
        }
        if (!jsonNode.isArray()) {
            throw new IllegalArgumentException("paths property must be an array");
        }
        final List<Path> paths = YamlUtils.toStream(jsonNode.elements()).map(this::toPath).collect(Collectors.toList());
        return paths;
    }

    /**
     * @param sitesJson
     * @return
     */
    Map<String, SiteNode> loadSites(final JsonNode sitesJson) {
        if (sitesJson == null) {
            throw new IllegalArgumentException("Missing sites property");
        }
        if (!sitesJson.isObject()) {
            throw new IllegalArgumentException("sites property must be an object");
        }
        final List<String> names = YamlUtils.toList(sitesJson.fieldNames());
        if (names.size() < 2) {
            throw new IllegalArgumentException("There must be at least two sites");
        }
        final Map<String, SiteNode> sites = names.stream().collect(Collectors
                .<String, String, SiteNode>toMap(Function.identity(), name -> toSiteNode(sitesJson.get(name))));
        return sites;
    }

    /**
     * @param jsonNode
     * @return
     */
    MapEdge toEdge(final JsonNode jsonNode) {
        final MapEdge result = new MapEdge();
        result.setBegin(findNode(jsonNode, "start"));
        result.setEnd(findNode(jsonNode, "end"));
        result.setPriority(YamlUtils.jsonInt(jsonNode.get("priority"), defaultPriority));
        result.setSpeedLimit(YamlUtils.jsonDouble(jsonNode.get("speedLimit"), defaultSpeedLimit) * KMH_TO_MPS);
        return result;
    }

    /**
     * @param jsonNode
     * @return
     */
    MapNode toMapNode(final JsonNode jsonNode) {
        final MapNode mapNode = new MapNode();
        final double x = jsonNode.get("x").asDouble();
        final double y = jsonNode.get("y").asDouble();
        mapNode.setLocation(new Point2D.Double(x, y));
        return mapNode;
    }

    /**
     * @param jsonNode
     * @return
     */
    Path toPath(final JsonNode jsonNode) {
        final Path result = new Path();
        result.setDeparture(findSite(jsonNode, "departure"));
        result.setDestination(findSite(jsonNode, "destination"));
        result.setWeight(YamlUtils.jsonDouble(jsonNode.get("weight"), DEFAULT_WEIGHT));
        return result;
    }

    /**
     * @param jsonNode
     * @return
     */
    SiteNode toSiteNode(final JsonNode jsonNode) {
        final SiteNode mapNode = new SiteNode();
        final double x = jsonNode.get("x").asDouble();
        final double y = jsonNode.get("y").asDouble();
        mapNode.setLocation(new Point2D.Double(x, y));
        return mapNode;
    }
}
