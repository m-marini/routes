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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;
import org.mmarini.routes.model2.*;
import org.mmarini.yaml.*;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Map.entry;
import static org.mmarini.Utils.entriesToMap;
import static org.mmarini.Utils.getValue;
import static org.mmarini.routes.model2.Constants.DEFAULT_MAX_VEHICLES;
import static org.mmarini.routes.model2.Constants.KMPHSPM;
import static org.mmarini.routes.model2.StatusImpl.createStatus;
import static org.mmarini.yaml.Utils.fromFile;
import static org.mmarini.yaml.Utils.fromResource;

public class RouteAST extends ASTNode {

    public static final String VERSION_PATTERN = "(\\d+)\\.(\\d+)";
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION;

    /**
     * Returns the status reading from file
     *
     * @param file the filename
     * @throws IOException in case of error
     */
    static StatusImpl readFromFile(String file) throws IOException {
        return new RouteAST(fromFile(file), JsonPointer.empty()).build();
    }

    /**
     * Returns the status reading from a resource
     *
     * @param name the resource name
     * @throws IOException in case of error
     */
    static StatusImpl readFromResource(String name) throws IOException {
        return new RouteAST(fromResource(name), JsonPointer.empty()).build();
    }


    /**
     * @param root the json node
     * @param at   location of json node
     */
    public RouteAST(JsonNode root, JsonPointer at) {
        super(root, at);
    }

    /**
     * Returns the status defined by yaml
     */
    public StatusImpl build() {
        validate();
        Map<String, SiteNode> siteByName = getSites();
        Map<String, MapNode> anyNodeByName = new HashMap<>(getNodes());
        anyNodeByName.putAll(siteByName);
        List<MapEdge> edgeList = edges().itemStream()
                .map(edgeAST -> {
                    MapNode start = anyNodeByName.get(edgeAST.start().getValue());
                    MapNode end = anyNodeByName.get(edgeAST.end().getValue());
                    double speedLimit = edgeAST.speedLimit().getValue() / KMPHSPM;
                    int priority = edgeAST.priority().getValue();
                    return new MapEdge(start, end, speedLimit, priority);
                })
                .collect(Collectors.toList());
        List<SiteNode> siteList = new ArrayList<>(siteByName.values());
        List<MapNode> nodeList = new ArrayList<>(anyNodeByName.values());
        Topology topology = Topology.createTopology(nodeList, edgeList);
        double frequency = defaults().defaultFrequence().getValue();
        Stream<Tuple2<Tuple2<SiteNode, SiteNode>, Double>> w = getPaths(s -> getValue(siteByName, s));
        double[][] weights = DoubleMatrix.from(w, siteList).getValues();
        double speedLimit = defaults().speedLimit().getValue() / KMPHSPM;
        int maxVehicles = maxVehicles().getValue();
        return createStatus(maxVehicles, speedLimit, frequency, 0,
                topology, List.of(), weights);
    }

    /**
     *
     */
    public DefaultsAST defaults() {
        return new DefaultsAST(getRoot(), path("default"));
    }

    /**
     *
     */
    public ArrayAST<EdgeAST> edges() {
        return ArrayAST.createRequired(getRoot(),
                path("edges"),
                (root, ptr) -> new EdgeAST(root, ptr,
                        defaults().defaultPriority().getValue(),
                        defaults().speedLimit().getValue()));
    }

    /**
     *
     */
    public Map<String, MapNode> getAllNodes() {
        HashMap<String, MapNode> allNodes = new HashMap<>(getNodes());
        allNodes.putAll(getSites());
        return allNodes;
    }

    Map<String, CrossNode> getNodes() {
        return nodes().itemStream()
                .map(entry -> entry(entry.getKey(), entry.getValue().getMapNode()))
                .collect(entriesToMap());
    }

    Stream<Tuple2<Tuple2<SiteNode, SiteNode>, Double>> getPaths(Function<String, Optional<SiteNode>> mapper) {
        return paths().itemStream()
                .flatMap(path ->
                        mapper.apply(path.departure().getValue())
                                .flatMap(dep ->
                                        mapper.apply(path.destination().getValue())
                                                .map(dest ->
                                                        Tuple2.of(
                                                                Tuple2.of(dep, dest),
                                                                path.weight().getValue())))
                                .stream());
    }

    Map<String, SiteNode> getSites() {
        return sites().itemStream()
                .map(entry -> entry(entry.getKey(), entry.getValue().getSite()))
                .collect(entriesToMap());
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.object(),
                ASTValidator.validate(defaults()),
                ASTValidator.validate(edges()),
                ASTValidator.validate(sites()),
                ASTValidator.validate(paths()),
                ASTValidator.validate(nodes())
        );
    }

    public IntAST maxVehicles() {
        return IntAST.createOtionalNotNegative(
                getRoot(),
                path("maxVehicles"),
                DEFAULT_MAX_VEHICLES);
    }

    /**
     *
     */
    public DictionaryAST<NodeAST> nodes() {
        return DictionaryAST.createRequired(getRoot(),
                path("nodes"),
                NodeAST::new);
    }

    /**
     *
     */
    public ArrayAST<WeightAST> paths() {
        return ArrayAST.createRequired(getRoot(),
                path("paths"),
                WeightAST::new);
    }

    /**
     *
     */
    public DictionaryAST<SiteAST> sites() {
        return DictionaryAST.createRequired(getRoot(),
                path("sites"),
                SiteAST::new);
    }

    @Override
    public void validate() {
        super.validate();
        version().validate();

        String ver = version().getValue();
        Matcher matcher = Pattern.compile(VERSION_PATTERN).matcher(ver);
        matcher.matches();
        int major = parseInt(matcher.group(1));
        int minor = parseInt(matcher.group(2));
        if (major != MAJOR_VERSION || minor > MINOR_VERSION) {
            version().throwError("%s not compatible with %s", ver, VERSION);
        }
        maxVehicles().validate();
        // find node key duplicated
        Map<String, SiteAST> sites = sites().items();
        nodes().itemStream()
                .flatMap(entry ->
                        Optional.ofNullable(sites.get(entry.getKey()))
                                .map(siteAst -> entry(siteAst, entry.getValue()))
                                .stream())
                .findAny()
                .ifPresent(entry ->
                        entry.getKey().throwError("has the same key of %s", entry.getValue().getAt())
                );

        // Finds the duplicated site locations
        Map<Point2D, List<SiteAST>> sitesByLocation = sites().items()
                .values()
                .stream()
                .collect(Collectors.groupingBy(
                        x -> x.getSite().getLocation()
                ));
        sitesByLocation.values().stream()
                .filter(list -> list.size() > 1)
                .findAny()
                .ifPresent(list ->
                        list.get(0).throwError(
                                "has the same location of %s",
                                list.get(1).getAt()));

        // Finds the duplicated node locations
        Map<Point2D, List<NodeAST>> nodesByLocation = nodes().items().values()
                .stream()
                .collect(Collectors.groupingBy(
                        x -> x.getMapNode().getLocation()
                ));
        nodesByLocation.values().stream()
                .filter(list -> list.size() > 1)
                .findAny()
                .ifPresent(list ->
                        list.get(0).throwError(
                                "has the same location of %s",
                                list.get(1).getAt()));

        // Finds the cross duplicated locations
        nodes().itemStream().map(Map.Entry::getValue)
                .flatMap(node ->
                        Optional.ofNullable(
                                        sitesByLocation.get(
                                                node.getMapNode().getLocation()
                                        ))
                                .map(list -> entry(node, list.get(0)))
                                .stream())
                .findAny()
                .ifPresent(entry ->
                        entry.getKey().throwError(
                                "has the same location of %s",
                                entry.getValue().getAt()));

        // Validates the edges
        Set<String> nodeNames = getAllNodes().keySet();
        edges().itemStream()
                .flatMap(edge -> {
                    Stream.Builder<TextAST> builder = Stream.builder();
                    TextAST start = edge.start();
                    if (!nodeNames.contains(start.getValue())) {
                        builder.add(start);
                    }
                    TextAST end = edge.end();
                    if (!nodeNames.contains(end.getValue())) {
                        builder.add(end);
                    }
                    return builder.build();
                })
                .findAny()
                .ifPresent(node ->
                        node.throwError("node %s undefined", node.getValue()));
        edges().itemStream()
                .filter(edge ->
                        edge.start().getValue().equals(edge.end().getValue()))
                .findAny()
                .ifPresent(node ->
                        node.end().throwError("must be different from %s", node.start().getAt()));

        // Validate paths
        paths().itemStream().filter(path ->
                        !sites.containsKey(path.departure().getValue()))
                .findAny()
                .ifPresent(path ->
                        path.departure().throwError(
                                "site %s undefined",
                                path.departure().getValue())
                );
        paths().itemStream().filter(path ->
                        !sites.containsKey(path.destination().getValue()))
                .findAny()
                .ifPresent(path ->
                        path.destination().throwError(
                                "site %s undefined",
                                path.destination().getValue())
                );
        paths().itemStream().filter(path ->
                        path.departure().getValue().equals(path.destination().getValue()))
                .findAny()
                .ifPresent(path ->
                        path.destination().throwError(
                                "must be different from %s",
                                path.departure().getAt())
                );
    }

    public TextAST version() {
        return new TextAST(getRoot(), path("version"), ASTValidator.regex(VERSION_PATTERN), VERSION);
    }
}
