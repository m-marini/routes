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
import org.mmarini.routes.model2.CrossNode;
import org.mmarini.routes.model2.MapEdge;
import org.mmarini.routes.model2.MapNode;
import org.mmarini.routes.model2.SiteNode;
import org.mmarini.yaml.Utils;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.Constants.KMPHSPM;
import static org.mmarini.routes.model2.yaml.TestUtils.text;
import static org.mmarini.yaml.schema.Locator.root;

class EdgeTest {

    static Stream<Arguments> argsForError() {
        return Stream.of(Arguments.of(text(
                        "#1",
                        "end: def"
                ), "/start is missing"
        ), Arguments.of(text(
                        "#2",
                        "start: abc"
                ), "/end is missing"
        ), Arguments.of(text(
                        "#3",
                        "start: {}",
                        "end: def"
                ), "/start must be a string \\(OBJECT\\)"
        ), Arguments.of(text(
                        "#4",
                        "start: abd",
                        "end: {}"
                ), "/end must be a string \\(OBJECT\\)"
        ), Arguments.of(text(
                        "#5",
                        "start: aaa",
                        "end: efg"
                ), "/start must match a value in \\[abc, efg] \\(aaa\\)"
        ), Arguments.of(text(
                        "#6",
                        "start: abc",
                        "end: aaa"
                ), "/end must match a value in \\[abc, efg] \\(aaa\\)"
        ), Arguments.of(text(
                        "#7",
                        "start: abc",
                        "end: aaa",
                        "priority: a",
                        "speedLimit: 130"
                ), "/priority must be an integer \\(STRING\\)"
        ), Arguments.of(text(
                        "#8",
                        "start: abc",
                        "end: aaa",
                        "priority: 2",
                        "speedLimit: aaa"
                ), "/speedLimit must be a number \\(STRING\\)"
        ), Arguments.of(text(
                        "#9",
                        "start: abc",
                        "end: aaa",
                        "priority: 2",
                        "speedLimit: -0.01"
                ), "/speedLimit must be >= 0.0 \\(-0.01\\)"
        ));
    }

    @Test
    void validate() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "start: abc",
                "end: def",
                "priority: 2",
                "speedLimit: 130"
        ));
        Map<String, ? extends MapNode> nodes = Map.of(
                "abc", SiteNode.createSite(0, 0),
                "def", CrossNode.createNode(0, 1)
        );
        SchemaValidators.edge().apply(root())
                .andThen(CrossValidators.edge(nodes.keySet()).apply(root()))
                .accept(root);
        MapEdge edge = Parsers.edge(root, 0, 90, nodes);
        assertThat(edge, hasProperty("begin",
                hasProperty("location", equalTo(
                        new Point2D.Double(0, 0)))));
        assertThat(edge, hasProperty("end",
                hasProperty("location", equalTo(
                        new Point2D.Double(0, 1)))));
        assertThat(edge, hasProperty("priority", equalTo(2)));
        assertThat(edge, hasProperty("speedLimit", equalTo(130.0 / KMPHSPM)));
    }

    @Test
    void validateDefault() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "start: abc",
                "end: def"
        ));
        Map<String, ? extends MapNode> nodes = Map.of(
                "abc", SiteNode.createSite(0, 0),
                "def", CrossNode.createNode(0, 1)
        );
        SchemaValidators.edge()
                .apply(root())
                .andThen(CrossValidators.edge(nodes.keySet()).apply(root()))
                .accept(root);
        MapEdge edge = Parsers.edge(root, 0, 90, nodes);
        assertThat(edge, hasProperty("begin",
                hasProperty("location", equalTo(
                        new Point2D.Double(0, 0)))));
        assertThat(edge, hasProperty("end",
                hasProperty("location", equalTo(
                        new Point2D.Double(0, 1)))));
        assertThat(edge, hasProperty("priority", equalTo(0)));
        assertThat(edge, hasProperty("speedLimit", equalTo(90.0 / KMPHSPM)));
    }

    @ParameterizedTest
    @MethodSource("argsForError")
    void validateOnErrors(String text, String expectedPattern) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text);
            SchemaValidators.edge()
                    .apply(root())
                    .andThen(CrossValidators.edge(List.of("efg", "abc")).apply(root()))
                    .accept(root);
        });
        assertThat(ex.getMessage(), matchesPattern(expectedPattern));
    }
}