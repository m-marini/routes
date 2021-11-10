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
import org.mmarini.routes.model2.CrossNode;
import org.mmarini.routes.model2.MapEdge;
import org.mmarini.routes.model2.MapModule;
import org.mmarini.routes.model2.MapNode;
import org.mmarini.yaml.*;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.mmarini.Utils.entriesToMap;
import static org.mmarini.yaml.Utils.fromFile;
import static org.mmarini.yaml.Utils.fromResource;

public class ModuleAST extends ASTNode {

    /**
     * Returns the status reading from file
     *
     * @param file the filename
     * @throws IOException in case of error
     */
    static MapModule readFromFile(String file) throws IOException {
        return new ModuleAST(fromFile(file), JsonPointer.empty()).build();
    }

    /**
     * Returns the status reading from a resource
     *
     * @param name the resource name
     * @throws IOException in case of error
     */
    static MapModule readFromResource(String name) throws IOException {
        return new ModuleAST(fromResource(name), JsonPointer.empty()).build();
    }


    /**
     * @param root the json node
     * @param at   location of json node
     */
    public ModuleAST(JsonNode root, JsonPointer at) {
        super(root, at);
    }

    /**
     * Returns the status defined by yaml
     */
    public MapModule build() {
        validate();
        Map<String, MapNode> anyNodeByName = new HashMap<>(getNodes());
        List<MapEdge> edges = edges().itemStream()
                .map(edgeAST -> {
                    MapNode start = anyNodeByName.get(edgeAST.start().getValue());
                    MapNode end = anyNodeByName.get(edgeAST.end().getValue());
                    double speedLimit = edgeAST.speedLimit().getValue() / 3.6;
                    int priority = edgeAST.priority().getValue();
                    return new MapEdge(start, end, speedLimit, priority);
                })
                .collect(Collectors.toList());
        return new MapModule(edges);
    }

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

    Map<String, CrossNode> getNodes() {
        return nodes().itemStream()
                .map(entry -> entry(entry.getKey(), entry.getValue().getMapNode()))
                .collect(entriesToMap());
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.object(),
                ASTValidator.validate(defaults()),
                ASTValidator.validate(edges()),
                ASTValidator.validate(nodes())
        );
    }

    /**
     *
     */
    public DictionaryAST<NodeAST> nodes() {
        return DictionaryAST.createRequired(getRoot(),
                path("nodes"),
                NodeAST::new);
    }

    @Override
    public void validate() {
        super.validate();

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


        // Validates the edges
        Set<String> nodeNames = getNodes().keySet();
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

    }
}
