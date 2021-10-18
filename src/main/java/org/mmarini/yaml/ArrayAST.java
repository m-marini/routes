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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayAST<T extends ASTNode> extends ASTNode {
    /**
     * Returns an ArrayAST
     *
     * @param root    the root document
     * @param pointer the node pointer
     * @param builder the child builder
     * @param <T>     the type of children
     */
    public static <T extends ASTNode> ArrayAST<T> createRequired(
            JsonNode root,
            JsonPointer pointer,
            BiFunction<JsonNode, JsonPointer, T> builder) {
        return new ArrayAST<>(root, pointer, builder, ASTValidator.required());
    }

    private final BiFunction<JsonNode, JsonPointer, T> builder;
    private final Consumer<ASTNode> additionalValidator;


    /**
     * @param root                the json node
     * @param at                  location of json node
     * @param builder             the T Builder
     * @param additionalValidator additionalValidator
     */
    public ArrayAST(JsonNode root,
                    JsonPointer at,
                    BiFunction<JsonNode, JsonPointer, T> builder,
                    Consumer<ASTNode> additionalValidator
    ) {
        super(root, at);
        this.builder = builder;
        this.additionalValidator = additionalValidator;
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.array(),
                ASTValidator.array(itemStream()),
                additionalValidator
        );
    }

    public Stream<T> itemStream() {
        return IntStream.range(0, size())
                .mapToObj(i -> builder.apply(getRoot(), path(String.valueOf(i))));
    }

    public List<T> items() {
        return itemStream().collect(Collectors.toList());
    }

    public int size() {
        JsonNode node = getNode();
        return node.isArray() ? node.size() : 0;
    }
}
