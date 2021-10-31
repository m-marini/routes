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
import org.mmarini.yaml.TextAST;

import java.util.function.Consumer;

public class WeightAST extends ASTNode {
    private static final double DEFAULT_WEIGHT = 1;

    /**
     * @param root the json node
     * @param at   location of json node
     */
    public WeightAST(JsonNode root, JsonPointer at) {
        super(root, at);
    }

    /**
     *
     */
    public TextAST departure() {
        return TextAST.createRequired(getRoot(), path("departure"));
    }

    /**
     *
     */
    public TextAST destination() {
        return TextAST.createRequired(getRoot(), path("destination"));
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.required(),
                ASTValidator.object(),
                ASTValidator.validate(departure()),
                ASTValidator.validate(destination()),
                ASTValidator.validate(weight()));
    }

    /**
     *
     */
    public DoubleAST weight() {
        return DoubleAST.createOptionalNotNegativeDouble(getRoot(), path("weight"), DEFAULT_WEIGHT);
    }
}
