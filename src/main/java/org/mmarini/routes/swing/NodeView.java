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
package org.mmarini.routes.swing;

import org.mmarini.routes.model.MapNode;

import java.awt.*;

/**
 * @author marco.marini@mmarini.org
 */
public class NodeView {
    private final MapNode node;
    private final String name;
    private final Color color;

    /**
     * @param name  the node name
     * @param node  the node
     * @param color the node color
     */
    public NodeView(final String name, final MapNode node, Color color) {
        this.node = node;
        this.name = name;
        this.color = color;
    }

    /**
     *
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the node
     */
    public MapNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return name;
    }

}
