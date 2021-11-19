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

package org.mmarini.yaml;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;

public interface ASTValidator {
    @SafeVarargs
    static Consumer<ASTNode> and(Consumer<ASTNode>... validators) {
        return node -> {
            for (Consumer<ASTNode> validator : validators) {
                validator.accept(node);
            }
        };
    }

    static Consumer<ASTNode> array() {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && !json.isArray()) {
                node.throwError("%s must be an array", json);
            }
        };
    }

    static <T extends ASTNode> Consumer<ASTNode> array(Stream<T> items) {
        return node -> items.forEach(T::validate);
    }

    static Consumer<ASTNode> empty() {
        return node -> {
        };
    }

    static Consumer<ASTNode> integer() {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && !json.isInt()) {
                node.throwError("%s must be an integer", json);
            }
        };
    }

    static Consumer<ASTNode> integer(IntPredicate valid, Function<JsonNode, String> message) {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && json.isNumber() && !valid.test(json.asInt())) {
                node.throwError(message.apply(json));
            }
        };
    }

    static Consumer<ASTNode> notNegativeDouble() {
        return number(value -> value >= 0,
                node -> format("%s must be a not negative number", node)
        );
    }

    static Consumer<ASTNode> notNegativeInt() {
        return integer(value -> value >= 0,
                node -> format("%s must be a not negative integer", node)
        );
    }

    static Consumer<ASTNode> number() {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && !json.isNumber()) {
                node.throwError("%s must be a number", json);
            }
        };
    }

    static Consumer<ASTNode> number(DoublePredicate valid, Function<JsonNode, String> message) {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && json.isNumber() && !valid.test(json.asDouble())) {
                node.throwError(message.apply(json));
            }
        };
    }

    static Consumer<ASTNode> object() {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && !json.isObject()) {
                node.throwError("%s must be an object", json);
            }
        };
    }

    static <T extends ASTNode> Consumer<ASTNode> object(Stream<Map.Entry<String, T>> itemStream) {
        return node -> itemStream.forEach(entry -> entry.getValue().validate());
    }

    static Consumer<ASTNode> regex(String pattern) {
        return node -> {
            JsonNode json = node.getNode();
            boolean x = !Pattern.matches(pattern, json.asText());
            if (!json.isMissingNode() && json.isTextual() &&
                    !Pattern.matches(pattern, json.asText())) {
                node.throwError("%s must match %s", json, pattern);
            }
        };
    }

    static Consumer<ASTNode> required() {
        return node -> {
            JsonNode json = node.getNode();
            if (json.isMissingNode()) {
                node.throwError("is missing");
            }
        };
    }

    static Consumer<ASTNode> text() {
        return node -> {
            JsonNode json = node.getNode();
            if (!json.isMissingNode() && !json.isTextual()) {
                node.throwError("%s must be a text", json);
            }
        };
    }

    /**
     * Returns the validator that validate a node
     *
     * @param node the node
     */
    static Consumer<ASTNode> validate(ASTNode node) {
        return node1 -> node.validate();
    }
}
