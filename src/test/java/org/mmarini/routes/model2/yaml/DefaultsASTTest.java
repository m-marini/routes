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
import static org.mmarini.routes.model2.Constants.DEFAULT_SPEED_LIMIT_KMH;
import static org.mmarini.routes.model2.Constants.DEFAULT_SPEED_LIMIT_MPS;
import static org.mmarini.routes.model2.yaml.TestUtils.text;

class DefaultsASTTest {

    @Test
    void validDefault() throws IOException {
        JsonNode file = Utils.fromText(text(
                "---",
                "defaultFrequence: 0.33",
                "speedLimit: 90",
                "defaultPriority: 1"
        ));
        DefaultsAST node = new DefaultsAST(file, JsonPointer.empty());
        node.validate();
        assertThat(node.defaultFrequence().getValue(), equalTo(0.33));
        assertThat(node.speedLimit().getValue(), equalTo(90.0));
        assertThat(node.defaultPriority().getValue(), equalTo(1));
    }

    @Test
    void validDefaultNoSpeed() throws IOException {
        JsonNode file = Utils.fromText(text(
                "---",
                "defaultFrequence: 0.33",
                "defaultPriority: 1"
        ));
        DefaultsAST node = new DefaultsAST(file, JsonPointer.empty());
        node.validate();
        assertThat(node.defaultFrequence().getValue(), equalTo(0.33));
        assertThat(node.speedLimit().getValue(), equalTo(DEFAULT_SPEED_LIMIT_KMH));
        assertThat(node.defaultPriority().getValue(), equalTo(1));
    }

    @Test
    void validNoDefault() throws IOException {
        JsonNode file = Utils.fromText("---");
        DefaultsAST node = new DefaultsAST(file, JsonPointer.valueOf("/defaults"));
        node.validate();
        assertThat(node.defaultFrequence().getValue(), equalTo(1.0));
        assertThat(node.speedLimit().getValue(), equalTo(DEFAULT_SPEED_LIMIT_KMH));
        assertThat(node.defaultPriority().getValue(), equalTo(0));
    }

    @Test
    void wrongPriority() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "defaultFrequence: 0.33",
                    "speedLimit: -1",
                    "defaultPriority: a"
            ));
            new DefaultsAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/defaultPriority \"a\" must be an integer"));
    }

    @Test
    void wrongPriority1() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "defaultFrequence: 0.33",
                    "speedLimit: -1",
                    "defaultPriority: 1.3"
            ));
            new DefaultsAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/defaultPriority 1.3 must be an integer"));
    }

    @Test
    void wrongSpeed() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "defaultFrequence: 0.33",
                    "speedLimit: aaaa",
                    "defaultPriority: 1"
            ));
            new DefaultsAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/speedLimit \"aaaa\" must be a number"));
    }

    @Test
    void wrongSpeed1() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = Utils.fromText(text(
                    "---",
                    "defaultFrequence: 0.33",
                    "speedLimit: -1",
                    "defaultPriority: 1"
            ));
            new DefaultsAST(root, JsonPointer.empty()).validate();
        });
        assertThat(ex.getMessage(), matchesPattern("/speedLimit -1 must be a not negative number"));
    }

}