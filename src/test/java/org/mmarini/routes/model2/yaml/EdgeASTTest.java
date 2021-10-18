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
import org.mmarini.yaml.Utils;

import java.io.IOException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.yaml.TestUtils.text;

class EdgeASTTest {

    static Stream<Arguments> argsForError() {
        return Stream.of(Arguments.of(text(
                        "---",
                        "end: def"
                ), "/start is missing"
        ), Arguments.of(text(
                        "---",
                        "start: abc"
                ), "/end is missing"
        ), Arguments.of(text(
                        "---",
                        "start: {}",
                        "end: def"
                ), "/start \\{\\} must be a text"
        ), Arguments.of(text(
                        "---",
                        "start: abd",
                        "end: {}"
                ), "/end \\{\\} must be a text"
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
        EdgeAST node = new EdgeAST(root, JsonPointer.empty(), 0, 90);
        node.validate();

        assertThat(node.start().getValue(), equalTo("abc"));
        assertThat(node.end().getValue(), equalTo("def"));
        assertThat(node.priority().getValue(), equalTo(2));
        assertThat(node.speedLimit().getValue(), equalTo(130.0));
    }

    @Test
    void validateDefault() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "start: abc",
                "end: def"
        ));
        EdgeAST node = new EdgeAST(root, JsonPointer.empty(), 0, 90);
        node.validate();

        assertThat(node.start().getValue(), equalTo("abc"));
        assertThat(node.end().getValue(), equalTo("def"));
        assertThat(node.priority().getValue(), equalTo(0));
        assertThat(node.speedLimit().getValue(), equalTo(90.0));
    }

    @ParameterizedTest
    @MethodSource("argsForError")
    void validateErrors(String text, String expectedPattern) throws IOException {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text);
            new EdgeAST(root, JsonPointer.empty(), 0, 90).validate();
        });
        assertThat(ex.getMessage(), matchesPattern(expectedPattern));
    }
}