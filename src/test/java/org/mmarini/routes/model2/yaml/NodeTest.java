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
import org.mmarini.yaml.Utils;

import java.io.IOException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.yaml.TestUtils.text;
import static org.mmarini.yaml.schema.Locator.root;

class NodeTest {

    static Stream<Arguments> argsForErrors() {
        return Stream.of(Arguments.of(text(
                        "#1",
                        "y: 2"
                ), "/x is missing"
        ), Arguments.of(text(
                        "#1",
                        "x: 1"
                ), "/y is missing"
        ));
    }

    @Test
    void getSite() throws IOException {
        JsonNode root = Utils.fromText(text(
                "---",
                "x: 1",
                "y: 2"
        ));
        SchemaValidators.node()
                .apply(root())
                .accept(root);

        CrossNode result = Parsers.node(root);
        assertNotNull(result);
        assertThat(result.getLocation(), allOf(
                hasProperty("x", equalTo(1.0)),
                hasProperty("y", equalTo(2.0))
        ));
    }

    @ParameterizedTest
    @MethodSource("argsForErrors")
    void validateOnErrors(String text, String expectedError) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(text));
            SchemaValidators.node()
                    .apply(root())
                    .accept(root);
        });
        assertThat(ex.getMessage(), matchesPattern(expectedError));
    }
}