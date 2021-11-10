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

import java.util.function.Consumer;

public class IntAST extends ASTNode {
    public static IntAST createOptionalInteger(JsonNode root, JsonPointer at, int defaultValue) {
        return new IntAST(root, at, ASTValidator.empty(), defaultValue);
    }

    private final Consumer<ASTNode> additionalValidator;
    private final int defaultValue;

    /**
     * @param root                the json node
     * @param at                  location of json node
     * @param additionalValidator the additional validator
     * @param defaultValue        the default value
     */
    public IntAST(JsonNode root,
                  JsonPointer at,
                  Consumer<ASTNode> additionalValidator,
                  int defaultValue) {
        super(root, at);
        this.additionalValidator = additionalValidator;
        this.defaultValue = defaultValue;
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.integer(),
                additionalValidator);
    }

    /**
     * Returns the value
     */
    public int getValue() {
        return getNode().isMissingNode() ? defaultValue : getNode().intValue();
    }
}
