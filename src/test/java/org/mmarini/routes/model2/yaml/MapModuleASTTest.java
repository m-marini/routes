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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.routes.model2.MapModule;
import org.mmarini.yaml.Utils;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.Constants.DEFAULT_SPEED_LIMIT_KMH;
import static org.mmarini.routes.model2.Constants.DEFAULT_SPEED_LIMIT_MPS;
import static org.mmarini.routes.model2.yaml.TestUtils.text;

class MapModuleASTTest {

    static Stream<Arguments> argsForError() {
        return Stream.of(Arguments.of(text(
                        "---",
                        "edges: []"
                ), "/nodes is missing"
        ), Arguments.of(text(
                        "---",
                        "nodes: {}"
                ), "/edges is missing"
        ), Arguments.of(text(
                        "---",
                        "nodes:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "    c:",
                        "        x: 0",
                        "        y: 0",
                        "sites: {}",
                        "paths: []",
                        "edges: []"
                ), "/nodes/a has the same location of /nodes/c"
        ), Arguments.of(text(
                        "---",
                        "nodes:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "    c:",
                        "        x: 5",
                        "        y: 0",
                        "edges:",
                        "  - start: a1",
                        "    end: a"
                ), "/edges/0/start node a1 undefined"
        ), Arguments.of(text(
                        "---",
                        "nodes:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "    c:",
                        "        x: 5",
                        "        y: 0",
                        "edges:",
                        "  - start: a",
                        "    end: a1"
                ), "/edges/0/end node a1 undefined"
        ), Arguments.of(text(
                        "---",
                        "nodes:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "    c:",
                        "        x: 5",
                        "        y: 0",
                        "edges:",
                        "  - start: a",
                        "    end: a"
                ), "/edges/0/end must be different from /edges/0/start"
        ));
    }

    @Test
    void validate() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "edges:",
                "- start: node0",
                "  end: node1",
                "  priority: 1",
                "  speedLimit: 90",
                "- start: node1",
                "  end: node2",
                "- start: node1",
                "  end: node0",
                "- start: node2",
                "  end: node1",
                "nodes:",
                "  node0:",
                "    x: 0",
                "    y: 0",
                "  node2:",
                "    x: 10",
                "    y: 0",
                "  node1:",
                "    x: 5",
                "    y: 0"
        ));
        ModuleAST node = new ModuleAST(root, JsonPointer.empty());
        node.validate();

        List<EdgeAST> edges = node.edges().items();
        assertThat(edges, hasSize(4));
        assertThat(edges.get(0).start().getValue(), equalTo("node0"));
        assertThat(edges.get(0).end().getValue(), equalTo("node1"));
        assertThat(edges.get(0).speedLimit().getValue(), equalTo(90.0));
        assertThat(edges.get(0).priority().getValue(), equalTo(1));

        assertThat(edges.get(1).start().getValue(), equalTo("node1"));
        assertThat(edges.get(1).end().getValue(), equalTo("node2"));
        assertThat(edges.get(1).speedLimit().getValue(), equalTo(DEFAULT_SPEED_LIMIT_KMH));
        assertThat(edges.get(1).priority().getValue(), equalTo(0));

        assertThat(edges.get(2).start().getValue(), equalTo("node1"));
        assertThat(edges.get(2).end().getValue(), equalTo("node0"));
        assertThat(edges.get(2).speedLimit().getValue(), equalTo(DEFAULT_SPEED_LIMIT_KMH));
        assertThat(edges.get(2).priority().getValue(), equalTo(0));

        assertThat(edges.get(3).start().getValue(), equalTo("node2"));
        assertThat(edges.get(3).end().getValue(), equalTo("node1"));
        assertThat(edges.get(2).speedLimit().getValue(), equalTo(DEFAULT_SPEED_LIMIT_KMH));
        assertThat(edges.get(2).priority().getValue(), equalTo(0));

        Map<String, NodeAST> nodes = node.nodes().items();
        assertThat(nodes.size(), equalTo(3));
        assertThat(nodes.get("node0"), hasProperty("mapNode",
                hasProperty("location",
                        equalTo(new Point2D.Double(0, 0)))));
        assertThat(nodes.get("node2"), hasProperty("mapNode",
                hasProperty("location",
                        equalTo(new Point2D.Double(10, 0)))));
        assertThat(nodes.get("node1"), hasProperty("mapNode",
                hasProperty("location",
                        equalTo(new Point2D.Double(5, 0)))));

        MapModule mapModule = node.build();
        assertNotNull(mapModule);
        assertThat(mapModule.getEdges(), containsInAnyOrder(
                allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(0.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(1)),
                        hasProperty("speedLimit", equalTo(90.0 / 3.6))
                ), allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(10.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(0)),
                        hasProperty("speedLimit", equalTo(DEFAULT_SPEED_LIMIT_MPS))
                ), allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(10.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(0)),
                        hasProperty("speedLimit", equalTo(DEFAULT_SPEED_LIMIT_MPS))
                ), allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(0.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(0)),
                        hasProperty("speedLimit", equalTo(DEFAULT_SPEED_LIMIT_MPS))
                )
        ));
    }

    @ParameterizedTest
    @MethodSource("argsForError")
    void validateErrors(String text, String expectedPattern) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text);
            new ModuleAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern(expectedPattern));
    }
}