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
import org.mmarini.yaml.Utils;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mmarini.routes.model2.yaml.TestUtils.text;

class WeightASTTest {

    @Test
    void badDeparture() throws IOException {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "departure: {}",
                    "destination: def",
                    "weight: 2"
            ));
            new WeightAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/departure \\{\\} must be a text"));
    }

    @Test
    void badDestination() throws IOException {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "departure: abc",
                    "destination: []",
                    "weight: 2"
            ));
            new WeightAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/destination \\[\\] must be a text"));
    }

    @Test
    void noDeparture() throws IOException {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "destination: def",
                    "weight: 2"
            ));
            new WeightAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/departure is missing"));
    }

    @Test
    void noDestination() throws IOException {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "departure: abc",
                    "weight: 2"
            ));
            new WeightAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/destination is missing"));
    }

    @Test
    void validate() throws IOException {
        JsonNode file = Utils.fromText(text(
                "---",
                "departure: abc",
                "destination: def",
                "weight: 2"
        ));
        WeightAST node = new WeightAST(file, JsonPointer.empty());
        node.validate();

        assertThat(node.departure().getValue(), equalTo("abc"));
        assertThat(node.destination().getValue(), equalTo("def"));
        assertThat(node.weight().getValue(), equalTo(2.0));
    }

    @Test
    void validateDefault() throws IOException {
        JsonNode file = Utils.fromText(text(
                "---",
                "departure: abc",
                "destination: def"
        ));
        WeightAST node = new WeightAST(file, JsonPointer.empty());
        node.validate();

        assertThat(node.departure().getValue(), equalTo("abc"));
        assertThat(node.destination().getValue(), equalTo("def"));
        assertThat(node.weight().getValue(), equalTo(1.0));
    }

}