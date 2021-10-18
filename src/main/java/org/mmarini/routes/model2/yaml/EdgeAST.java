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
import org.mmarini.yaml.*;

import java.util.function.Consumer;

public class EdgeAST extends ASTNode {
    private final int defaultPriority;
    private final double defaultSpeedLimit;

    /**
     * @param root              the json node
     * @param at                location of json node
     * @param defaultPriority   the default priority
     * @param defaultSpeedLimit the default speed limit
     */
    public EdgeAST(JsonNode root, JsonPointer at, int defaultPriority, double defaultSpeedLimit) {
        super(root, at);
        this.defaultPriority = defaultPriority;
        this.defaultSpeedLimit = defaultSpeedLimit;
    }

    public TextAST end() {
        return TextAST.createRequired(getRoot(), path("end"));
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.required(),
                ASTValidator.object(),
                ASTValidator.validate(start()),
                ASTValidator.validate(end()),
                ASTValidator.validate(priority()),
                ASTValidator.validate(speedLimit())
        );
    }

    public IntAST priority() {
        return IntAST.createOptionalInteger(getRoot(), path("priority"), defaultPriority);
    }

    public DoubleAST speedLimit() {
        return DoubleAST.createOptionalNotNegativeDouble(getRoot(), path("speedLimit"), defaultSpeedLimit);
    }

    public TextAST start() {
        return TextAST.createRequired(getRoot(), path("start"));
    }

}
