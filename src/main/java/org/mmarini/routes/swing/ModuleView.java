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

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Marco
 */
public class ModuleView {
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_WIDTH = 16;
    private Module module;
    private Icon icon;

    /**
     *
     */
    public ModuleView() {
    }

    /**
     * Returns the Icon for a moduòe
     *
     * @param module the module
     */
    private Icon createIcon(final Module module) {
        final BufferedImage image = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D gr = image.createGraphics();
        Rectangle2D bound = new Rectangle2D.Double(0, 0, ICON_WIDTH, ICON_HEIGHT);
        bound = module.getBound();
        final double scale = Math.min(ICON_WIDTH / bound.getWidth(), ICON_HEIGHT / bound.getHeight());
        gr.translate(ICON_WIDTH * 0.5, ICON_HEIGHT * 0.5);
        gr.scale(scale, scale);
        gr.translate(-bound.getCenterX(), -bound.getCenterY());
        gr.setColor(Color.WHITE);
        gr.fill(bound);

        final Painter painter = new Painter(gr, false, false);

        for (final MapEdge edge : module.getEdges()) {
            painter.paintEdge(edge);
        }

        final Icon icon = new ImageIcon(image);
        return icon;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @return the module
     */
    public Module getModule() {
        return module;
    }

    /**
     * @param module the module to set
     */
    public void setModule(final Module module) {
        this.module = module;
        this.icon = createIcon(module);
    }
}
