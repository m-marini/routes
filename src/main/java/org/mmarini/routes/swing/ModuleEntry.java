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
public class ModuleEntry {
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_WIDTH = 16;
    private Module module;
    private Icon icon;

    /**
     *
     */
    public ModuleEntry() {
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

        final Painter painter = new Painter();
        painter.setGraphics(gr);
        painter.setBorderPainted(false);

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
