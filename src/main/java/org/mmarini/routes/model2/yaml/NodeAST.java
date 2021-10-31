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
import org.mmarini.routes.model2.CrossNode;
import org.mmarini.yaml.ASTNode;
import org.mmarini.yaml.ASTValidator;
import org.mmarini.yaml.DoubleAST;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

public class NodeAST extends ASTNode {
    /**
     * @param root the json node
     * @param at   location of json node
     */
    public NodeAST(JsonNode root, JsonPointer at) {
        super(root, at);
    }

    /**
     *
     */
    public CrossNode getMapNode() {
        return new CrossNode(new Point2D.Double(x().getValue(), y().getValue()));
    }

    @Override
    public Consumer<ASTNode> getValidator() {
        return ASTValidator.and(
                ASTValidator.required(),
                ASTValidator.object(),
                ASTValidator.validate(x()),
                ASTValidator.validate(y())
        );
    }

    public DoubleAST x() {
        return DoubleAST.createRequiredDouble(getRoot(), path("x"));
    }

    public DoubleAST y() {
        return DoubleAST.createRequiredDouble(getRoot(), path("y"));
    }
}
