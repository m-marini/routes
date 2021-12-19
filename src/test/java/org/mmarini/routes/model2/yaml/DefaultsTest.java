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
import org.mmarini.yaml.Utils;

import java.io.IOException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.Constants.*;
import static org.mmarini.routes.model2.yaml.TestUtils.text;
import static org.mmarini.yaml.schema.Locator.root;

class DefaultsTest {

    static Stream<Arguments> argsForErrors() {
        return Stream.of(Arguments.of(text(
                        "#1",
                        "frequence: 0.33",
                        "speedLimit: 1",
                        "priority: a"
                ), "/priority must be an integer \\(STRING\\)"
        ), Arguments.of(text(
                        "#2",
                        "frequence: 0.33",
                        "speedLimit: 1",
                        "priority: 1.3"
                ), "/priority must be an integer \\(NUMBER\\)"
        ), Arguments.of(text(
                        "#3",
                        "frequence: 0.33",
                        "speedLimit: aaaa",
                        "priority: 1"
                ), "/speedLimit must be a number \\(STRING\\)"
        ), Arguments.of(text(
                        "#4",
                        "frequence: -0.1",
                        "speedLimit: aaaa",
                        "priority: 1"
                ), "/frequence must be >= 0.0 \\(-0.1\\)"
        ), Arguments.of(text(
                        "#5",
                        "frequence: 0.33",
                        "speedLimit: -0.01",
                        "priority: 1"
                ), "/speedLimit must be >= 0.0 \\(-0.01\\)"
        ));
    }

    @Test
    void validDefault() throws IOException {
        JsonNode file = Utils.fromText("{}");
        SchemaValidators.defaults()
                .apply(root())
                .accept(file);
        SchemaValidators.defaults()
                .apply(root())
                .accept(file);
        assertThat(Parsers.defaultFrequency(file), equalTo(DEFAULT_FREQUENCY));
        assertThat(Parsers.defaultSpeedLimit(file), equalTo(DEFAULT_SPEED_LIMIT_KMH));
        assertThat(Parsers.defaultPriority(file), equalTo(DEFAULT_PRIORITY));
    }

    @Test
    void validate() throws IOException {
        JsonNode file = Utils.fromText(text(
                "---",
                "frequence: 0.5",
                "speedLimit: 90",
                "priority: 1"
        ));
        SchemaValidators.defaults()
                .apply(root())
                .accept(file);
        assertThat(Parsers.defaultFrequency(file), equalTo(0.5));
        assertThat(Parsers.defaultSpeedLimit(file), equalTo(90.0));
        assertThat(Parsers.defaultPriority(file), equalTo(1));
    }

    @ParameterizedTest
    @MethodSource("argsForErrors")
    void validateOnErrors(String text, String expectedError) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(text));
            SchemaValidators.defaults()
                    .apply(root())
                    .accept(root);
        });
        assertThat(ex.getMessage(), matchesPattern(expectedError));
    }
}