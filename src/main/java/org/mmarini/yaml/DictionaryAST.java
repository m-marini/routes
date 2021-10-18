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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mmarini.Utils.toMap;

public class DictionaryAST<T extends ASTNode> extends ASTNode {
    public static <T extends ASTNode> DictionaryAST<T> createRequired(
            JsonNode root,
            JsonPointer sites,
            BiFunction<JsonNode, JsonPointer, T> builder) {
        return new DictionaryAST<>(root, sites, builder, ASTValidator.required());
    }

    private final BiFunction<JsonNode, JsonPointer, T> builder;
    private final Consumer<ASTNode> additionalValidator;

    /**
     * @param root                the json node
     * @param at                  location of json node
     * @param additionalValidator the additional validator
     */
    public DictionaryAST(JsonNode root,
                         JsonPointer at,
                         BiFunction<JsonNode, JsonPointer, T> builder,
                         Consumer<ASTNode> additionalValidator) {
        super(root, at);
        this.builder = builder;
        this.additionalValidator = additionalValidator;
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.object(itemStream()),
                additionalValidator);
    }

    /**
     * Returns the stream of name and item
     */
    public Stream<Map.Entry<String, T>> itemStream() {
        return Utils.iter2Stream(getNode().fieldNames()).map(name ->
                Map.entry(name, builder.apply(getRoot(), path(name)))
        );
    }

    /**
     * Returns the map of name and item
     */
    public Map<String, T> items() {
        return itemStream().collect(toMap());
    }
}
