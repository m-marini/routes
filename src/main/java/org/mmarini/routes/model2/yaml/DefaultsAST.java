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
import org.mmarini.yaml.ASTNode;
import org.mmarini.yaml.ASTValidator;
import org.mmarini.yaml.DoubleAST;
import org.mmarini.yaml.IntAST;

import java.util.function.Consumer;

import static org.mmarini.routes.model2.Constants.*;

/**
 * AST node with default values of routes
 */
public class DefaultsAST extends ASTNode {

    /**
     * @param root the json node
     * @param at   location of json node
     */
    public DefaultsAST(JsonNode root, JsonPointer at) {
        super(root, at);
    }

    public DoubleAST defaultFrequence() {
        return DoubleAST.createOptionalNotNegativeDouble(
                getRoot(),
                path("frequence"),
                DEFAULT_FREQUENCY);
    }

    public IntAST defaultPriority() {
        return IntAST.createOptionalInteger(
                getRoot(),
                path("priority"),
                DEFAULT_PRIORITY);
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.object(),
                ASTValidator.validate(defaultFrequence()),
                ASTValidator.validate(defaultFrequence()),
                ASTValidator.validate(defaultPriority()),
                ASTValidator.validate(speedLimit())
        );
    }

    public DoubleAST speedLimit() {
        return DoubleAST.createOptionalNotNegativeDouble(
                getRoot(),
                path("speedLimit"),
                DEFAULT_SPEED_LIMIT_KMH);
    }
}
