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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.routes.model2.StatusImpl;
import org.mmarini.yaml.Utils;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.Constants.*;
import static org.mmarini.routes.model2.yaml.TestUtils.text;
import static org.mmarini.yaml.schema.Locator.root;

class RouteTest {

    static Stream<Arguments> argsForError() {
        return Stream.of(Arguments.of(text(
                        "#¹",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/sites is missing"
        ), Arguments.of(text(
                        "#2",
                        "sites: {}",
                        "paths: []",
                        "edges: []"
                ), "/nodes is missing"
        ), Arguments.of(text(
                        "#3",
                        "maxVehicles: aaa",
                        "sites: {}",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/maxVehicles must be an integer \\(STRING\\)"
        ), Arguments.of(text(
                        "#4",
                        "maxVehicles: -1",
                        "sites: {}",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/maxVehicles must be >= 0 \\(-1\\)"
        ), Arguments.of(text(
                        "#5",
                        "version: \"aaa\"",
                        "sites: {}",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/version must match pattern \"\\(\\\\d\\+\\)\\\\.\\(\\\\d\\+\\)\" \\(aaa\\)"
        ), Arguments.of(text(
                        "#6",
                        "version: \"2.0\"",
                        "sites: {}",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/version must be compatible with 1.0 \\(2.0\\)"
        ), Arguments.of(text(
                        "#7",
                        "version: \"1.1\"",
                        "sites: {}",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/version must be compatible with 1.0 \\(1.1\\)"
        ), Arguments.of(text(
                        "#8",
                        "sites: {}",
                        "nodes: {}",
                        "paths: []"
                ), "/edges is missing"
        ), Arguments.of(text(
                        "#9",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "    c:",
                        "        x: 0",
                        "        y: 0",
                        "nodes: {}",
                        "paths: []",
                        "edges: []"
                ), "/sites/c must not have the same location of /sites/a"
        ), Arguments.of(text(
                        "#10",
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
                ), "/nodes/c must not have the same location of /nodes/a"
        ), Arguments.of(text(
                        "#11",
                        "nodes:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "sites:",
                        "    c:",
                        "        x: 0",
                        "        y: 0",
                        "paths: []",
                        "edges: []"
                ), "/nodes/a must not have the same location of /sites/c"
        ), Arguments.of(text(
                        "#12",
                        "nodes:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "paths: []",
                        "edges: []"
                ), "/nodes/a must not have the same key of /sites/a"
        ), Arguments.of(text(
                        "#13",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "nodes:",
                        "    c:",
                        "        x: 5",
                        "        y: 0",
                        "paths: []",
                        "edges:",
                        "  - start: a1",
                        "    end: a"
                ), "/edges/0/start must match a value in \\[a, b, c\\] \\(a1\\)"
        ), Arguments.of(text(
                        "#14",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "nodes:",
                        "    c:",
                        "        x: 5",
                        "        y: 0",
                        "paths: []",
                        "edges:",
                        "  - start: a",
                        "    end: a1"
                ), "/edges/0/end must match a value in \\[a, b, c\\] \\(a1\\)"
        ), Arguments.of(text(
                        "#15",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "nodes:",
                        "    c:",
                        "        x: 5",
                        "        y: 0",
                        "paths: []",
                        "edges:",
                        "  - start: a",
                        "    end: a"
                ), "/edges/0/end must not be equal to /edges/0/start \\(a\\)"
        ), Arguments.of(text(
                        "#16",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "nodes: {}",
                        "paths:",
                        "  - departure: a1",
                        "    destination: a",
                        "edges: []"
                ), "/paths/0/departure must match a value in \\[a, b\\] \\(a1\\)"
        ), Arguments.of(text(
                        "#17",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "nodes: {}",
                        "paths:",
                        "  - departure: a",
                        "    destination: a1",
                        "edges: []"
                ), "/paths/0/destination must match a value in \\[a, b\\] \\(a1\\)"
        ), Arguments.of(text(
                        "#18",
                        "sites:",
                        "    a:",
                        "        x: 0",
                        "        y: 0",
                        "    b:",
                        "        x: 10",
                        "        y: 0",
                        "nodes: {}",
                        "paths:",
                        "  - departure: a",
                        "    destination: a",
                        "edges: []"
                ), "/paths/0/destination must not be equal to /paths/0/departure \\(a\\)"
        ));
    }

    static Consumer<JsonNode> create() {
        return SchemaValidators.route()
                .apply(root())
                .andThen(CrossValidators.route().apply(root()));
    }

    @ParameterizedTest
    @MethodSource("argsForError")
    void validateErrors(String text, String expectedPattern) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text);
            create().accept(root);
        });
        assertThat(ex.getMessage(), matchesPattern(expectedPattern));
    }

    @Test
    void validateNoDefaults() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "version: \"1.0\"",
                "maxVehicles: 1000",
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
                "sites:",
                "  node0:",
                "    x: 0",
                "    y: 0",
                "  node2:",
                "    x: 10",
                "    y: 0",
                "nodes:",
                "  node1:",
                "    x: 5",
                "    y: 0",
                "paths:",
                "- departure: node0",
                "  destination: node2",
                "  weight: 2",
                "- departure: node2",
                "  destination: node0"
        ));

        create().accept(root);

        StatusImpl status = Parsers.status(root);

        assertNotNull(status);

        assertThat(status.getMaxVehicle(), equalTo(1000));
        assertThat(status.getFrequency(), equalTo(DEFAULT_FREQUENCY));
        assertThat(status.getSpeedLimit(), equalTo(DEFAULT_SPEED_LIMIT_MPS));

        assertThat(status.getVehicles(), hasSize(0));
        assertThat(status.getSites(), containsInAnyOrder(
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(0.0)),
                        hasProperty("y", equalTo(0.0))
                )),
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(10.0)),
                        hasProperty("y", equalTo(0.0))
                ))
        ));
        assertThat(status.getNodes(), containsInAnyOrder(
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(0.0)),
                        hasProperty("y", equalTo(0.0))
                )),
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(10.0)),
                        hasProperty("y", equalTo(0.0))
                )),
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(5.0)),
                        hasProperty("y", equalTo(0.0))
                ))
        ));

        assertThat(status.getEdges(), containsInAnyOrder(
                allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(0.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(1)),
                        hasProperty("speedLimit", equalTo(90 / KMPHSPM))
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

        assertThat(status.getWeightMatrix().getValues(), equalTo(new double[][]{
                {0, 1},
                {2, 0}
        }));
    }

    @Test
    void validateWithDefaults() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "version: \"1.0\"",
                "maxVehicles: 1000",
                "default:",
                "  frequence: 0.5",
                "  speedLimit: 60",
                "  priority: 2",
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
                "sites:",
                "  node0:",
                "    x: 0",
                "    y: 0",
                "  node2:",
                "    x: 10",
                "    y: 0",
                "nodes:",
                "  node1:",
                "    x: 5",
                "    y: 0",
                "paths:",
                "- departure: node0",
                "  destination: node2",
                "  weight: 2",
                "- departure: node2",
                "  destination: node0"
        ));

        create().accept(root);

        StatusImpl status = Parsers.status(root);

        assertNotNull(status);

        assertThat(status.getMaxVehicle(), equalTo(1000));
        assertThat(status.getFrequency(), equalTo(0.5));
        assertThat(status.getSpeedLimit(), equalTo(60d / KMPHSPM));

        assertThat(status.getVehicles(), hasSize(0));
        assertThat(status.getSites(), containsInAnyOrder(
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(0.0)),
                        hasProperty("y", equalTo(0.0))
                )),
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(10.0)),
                        hasProperty("y", equalTo(0.0))
                ))
        ));
        assertThat(status.getNodes(), containsInAnyOrder(
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(0.0)),
                        hasProperty("y", equalTo(0.0))
                )),
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(10.0)),
                        hasProperty("y", equalTo(0.0))
                )),
                hasProperty("location", allOf(
                        hasProperty("x", equalTo(5.0)),
                        hasProperty("y", equalTo(0.0))
                ))
        ));

        assertThat(status.getEdges(), containsInAnyOrder(
                allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(0.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(1)),
                        hasProperty("speedLimit", equalTo(90 / KMPHSPM))
                ), allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(10.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(2)),
                        hasProperty("speedLimit", equalTo(60d / KMPHSPM))
                ), allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(10.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(2)),
                        hasProperty("speedLimit", equalTo(60d / KMPHSPM))
                ), allOf(
                        hasProperty("begin", hasProperty("location", allOf(
                                hasProperty("x", equalTo(5.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("end", hasProperty("location", allOf(
                                hasProperty("x", equalTo(0.0)),
                                hasProperty("y", equalTo(0.0))))),
                        hasProperty("priority", equalTo(2)),
                        hasProperty("speedLimit", equalTo(60d / KMPHSPM))
                )
        ));

        assertThat(status.getWeightMatrix().getValues(), equalTo(new double[][]{
                {0, 1},
                {2, 0}
        }));
    }
}