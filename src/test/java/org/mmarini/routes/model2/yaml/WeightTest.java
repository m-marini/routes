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
import org.mmarini.Tuple2;
import org.mmarini.yaml.Utils;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.yaml.TestUtils.text;
import static org.mmarini.yaml.schema.Locator.root;

class WeightTest {
    static Stream<Arguments> argsForError() {
        return Stream.of(Arguments.of(text(
                        "#1",
                        "destination: def",
                        "weight: 2"
                ), "/departure is missing"
        ), Arguments.of(text(
                        "#2",
                        "departure: abc",
                        "weight: 2"
                ), "/destination is missing"
        ), Arguments.of(text(
                        "#3",
                        "departure: {}",
                        "destination: def",
                        "weight: 2"
                ), "/departure must be a string \\(OBJECT\\)"
        ), Arguments.of(text(
                        "#4",
                        "departure: abc",
                        "destination: {}",
                        "weight: 2"
                ), "/destination must be a string \\(OBJECT\\)"
        ), Arguments.of(text(
                        "#5",
                        "departure: abc",
                        "destination: edf",
                        "weight: a"
                ), "/weight must be a number \\(STRING\\)"
        ), Arguments.of(text(
                        "#6",
                        "departure: abc",
                        "destination: edf",
                        "weight: -1"
                ), "/weight must be >= 0.0 \\(-1.0\\)"
        ), Arguments.of(text(
                        "#6",
                        "departure: xxx",
                        "destination: edf",
                        "weight: 0"
                ), "/departure must match a value in \\[abc, def\\] \\(xxx\\)"
        ), Arguments.of(text(
                        "#6",
                        "departure: abc",
                        "destination: xxx",
                        "weight: 0"
                ), "/destination must match a value in \\[abc, def\\] \\(xxx\\)"
        ));
    }

    static Consumer<JsonNode> createValidator() {
        return SchemaValidators.weight()
                .apply(root())
                .andThen(CrossValidators.weight(List.of("abc", "def")).apply(root()));
    }

    @Test
    void validate() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "departure: abc",
                "destination: def",
                "weight: 2"
        ));
        createValidator().accept(root);
        Tuple2<Tuple2<String, String>, Double> weight = Parsers.weight(root);

        assertNotNull(weight);
        assertThat(weight._1._1, equalTo("abc"));
        assertThat(weight._1._2, equalTo("def"));
        assertThat(weight._2, equalTo(2.0));
    }

    @Test
    void validateDefault() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "departure: abc",
                "destination: def"
        ));
        createValidator().accept(root);
        Tuple2<Tuple2<String, String>, Double> weight = Parsers.weight(root);

        assertNotNull(weight);
        assertThat(weight._1._1, equalTo("abc"));
        assertThat(weight._1._2, equalTo("def"));
        assertThat(weight._2, equalTo(1.0));
    }

    @ParameterizedTest
    @MethodSource("argsForError")
    void validateOnErrors(String text, String expectedPattern) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text);
            createValidator().accept(root);
        });
        assertThat(ex.getMessage(), matchesPattern(expectedPattern));
    }

}