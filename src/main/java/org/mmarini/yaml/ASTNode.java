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

public abstract class ASTNode {

    private final JsonNode root;
    private final JsonPointer at;

    /**
     * @param root the json node
     * @param at   location of json node
     */
    public ASTNode(JsonNode root, JsonPointer at) {
        this.root = root;
        this.at = at;
    }

    /**
     * Returns the pointer to current node
     */
    public JsonPointer getAt() {
        return at;
    }

    /**
     * Returns the current node
     */
    public JsonNode getNode() {
        return root.at(at);
    }

    /**
     * Returns the root of document
     */
    public JsonNode getRoot() {
        return root;
    }

    /**
     * Returns the validator
     */
    public abstract Consumer<ASTNode> getValidator();

    /**
     * Returns the pointer of a child node
     *
     * @param ptr the position of child node
     */
    public JsonPointer path(JsonPointer ptr) {
        return at.append(ptr);
    }

    /**
     * Returns the pointer of a child node
     *
     * @param path the path of child node
     */
    public JsonPointer path(String path) {
        return at.append(JsonPointer.valueOf("/" + path));
    }

    /**
     * @param tpl  the string template
     * @param args the arguments
     */
    public void throwError(String tpl, Object... args) {
        throw new IllegalArgumentException(String.format("%s %s",
                at,
                String.format(tpl, args)));
    }

    /**
     * Validates the current node
     */
    public void validate() {
        getValidator().accept(this);
    }
}
