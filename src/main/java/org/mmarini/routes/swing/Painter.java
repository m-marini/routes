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

import org.mmarini.routes.model2.MapEdge;
import org.mmarini.routes.model2.MapModule;

import java.awt.*;
import java.awt.geom.*;

import static org.mmarini.routes.model2.Constants.VEHICLE_LENGTH;

/**
 *
 */
public class Painter {

    public static final BasicStroke THIN_STROKE = new BasicStroke(0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color END_NODE_COLOR = Color.RED;
    private static final Color BEGIN_NODE_COLOR = Color.GREEN;
    private static final Color SELECTED_SITE_COLOR = Color.WHITE;
    private static final Color EDGE_COLOR = Color.LIGHT_GRAY;
    private static final Color MAJOR_GRID_COLOR = new Color(0xd0d0d0);
    private static final Color MINOR_GRID_COLOR = new Color(0xe0e0e0);
    private static final Color MAJOR_GRID_REVERSED_COLOR = new Color(0x202020);
    private static final Color MINOR_GRID_REVERSED_COLOR = new Color(0x101010);
    private static final Color SELECTED_NODE_COLOR = Color.RED;
    private static final Color SELECTED_EDGE_COLOR = Color.YELLOW;
    private static final double VEHICLE_WIDTH = 3;
    public static final Rectangle2D.Double VEHICLE_SHAPE = new Rectangle2D.Double(-VEHICLE_LENGTH * 0.5, -VEHICLE_WIDTH * 0.5, VEHICLE_LENGTH, VEHICLE_WIDTH);
    private static final double EDGE_WIDTH = 5;
    public static final BasicStroke ROAD_STROKE = new BasicStroke((float) EDGE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static final Ellipse2D.Double EDGE_END_POINT = new Ellipse2D.Double(-EDGE_WIDTH * 0.5, -EDGE_WIDTH * 0.5, EDGE_WIDTH, EDGE_WIDTH);
    private static final double NODE_SIZE = 10;
    public static final Ellipse2D.Double SITE_SHAPE = new Ellipse2D.Double(-NODE_SIZE * 0.5, -NODE_SIZE * 0.5, NODE_SIZE, NODE_SIZE);
    private final Line2D line;
    private Graphics2D graphics;
    private boolean borderPainted;
    private boolean reversed;

    /**
     *
     */
    public Painter() {
        this(null, false, false);
    }

    /**
     * @param graphics      the graphics
     * @param borderPainted true if painting the border
     * @param reversed      true if painting in reverse
     */
    public Painter(Graphics2D graphics, boolean borderPainted, boolean reversed) {
        this.graphics = graphics;
        this.borderPainted = borderPainted;
        this.reversed = reversed;
        line = new Line2D.Double();
    }

    /**
     * @param edge the edge
     */
    public void paintCursorEdge(final MapEdge edge) {
        final Point2D beginLocation = edge.getBeginLocation();
        final Point2D endLocation = edge.getEndLocation();
        paintEdge(beginLocation, endLocation, SELECTED_EDGE_COLOR);
    }

    /**
     * @param edge the edge
     */
    public void paintCursorEdgeEnds(final MapEdge edge) {
        final Point2D beginLocation = edge.getBeginLocation();
        final Point2D endLocation = edge.getEndLocation();
        paintShape(EDGE_END_POINT, beginLocation, BEGIN_NODE_COLOR);
        paintShape(EDGE_END_POINT, endLocation, END_NODE_COLOR);
    }

    /**
     * @param edge the edge
     */
    public void paintEdge(final MapEdge edge) {
        paintEdge(edge, EDGE_COLOR);
    }

    /**
     * @param edge  the edge
     * @param color the color
     */
    public void paintEdge(final MapEdge edge, final Color color) {
        paintEdge(edge.getBeginLocation(), edge.getEndLocation(), color);
    }

    /**
     * @param from  the beginning point
     * @param to    the end point
     * @param color the color
     */
    public void paintEdge(final Point2D from, final Point2D to, final Color color) {
        line.setLine(from, to);
        graphics.setColor(color);
        graphics.setStroke(ROAD_STROKE);
        graphics.draw(line);
    }

    /**
     * @param bound the bound
     * @param size  the size
     */
    public void paintGrid(final Rectangle2D bound, final double size) {
        final double x0 = bound.getMinX();
        final double x1 = bound.getMaxX();
        final double y1 = bound.getMaxY();
        final double y0 = bound.getMinY();
        final Color minorColor = reversed ? MINOR_GRID_REVERSED_COLOR : MINOR_GRID_COLOR;
        final Color majorColor = reversed ? MAJOR_GRID_REVERSED_COLOR : MAJOR_GRID_COLOR;
        graphics.setColor(minorColor);
        graphics.setStroke(THIN_STROKE);
        for (double x = Math.floor(x0 / size) * size; x <= x1; x += size) {
            final double xg = Math.floor(x / size / 10.) * 10. * size;
            if (x == xg) {
                graphics.setColor(majorColor);
            } else {
                graphics.setColor(minorColor);
            }
            line.setLine(x, y0, x, y1);
            graphics.draw(line);
        }
        for (double y = Math.floor(y0 / size) * size; y <= y1; y += size) {
            final double xg = Math.floor(y / size / 10.) * 10. * size;
            if (y == xg) {
                graphics.setColor(majorColor);
            } else {
                graphics.setColor(minorColor);
            }
            line.setLine(x0, y, x1, y);
            graphics.draw(line);
        }
    }

    /**
     * @param mapModule the mapModule
     * @param location  the location
     * @param vecx      the x direction vector
     * @param vecy      the y direction vector
     * @param color     the color of module
     */
    public void paintModule(final MapModule mapModule, final Point2D location, final double vecx, final double vecy, Color color) {
        final AffineTransform old = graphics.getTransform();
        final AffineTransform tr = graphics.getTransform();
        tr.translate(location.getX(), location.getY());
        tr.rotate(vecx, vecy);
        graphics.setTransform(tr);
        for (final MapEdge edge : mapModule.getEdges()) {
            paintEdge(edge, color);
        }
        graphics.setTransform(old);
    }

    /**
     * @param center the center
     */
    public void paintNodeCursor(final Point2D center) {
        paintShape(EDGE_END_POINT, center, SELECTED_NODE_COLOR);
    }

    /**
     * @param shape    the shape
     * @param location the location
     * @param color    the color
     */
    private void paintShape(final Shape shape, final Point2D location, final Color color) {
        graphics.setColor(color);
        final AffineTransform tr = graphics.getTransform();
        graphics.translate(location.getX(), location.getY());
        graphics.fill(shape);
        if (borderPainted) {
            graphics.setStroke(THIN_STROKE);
            graphics.setColor(reversed ? Color.WHITE : Color.BLACK);
            graphics.draw(shape);
        }
        graphics.setTransform(tr);
    }

    /**
     * @param location the location
     * @param color    the color
     */
    public void paintSite(final Point2D location, final Color color) {
        paintShape(SITE_SHAPE, location, color);
    }

    /**
     * @param center the center
     */
    public void paintSiteCursor(final Point2D center) {
        paintSite(center, SELECTED_SITE_COLOR);
    }

    /**
     * @param location the location
     * @param vec      the direction vector
     * @param color    the color
     */
    public void paintVehicle(final Point2D location, final Point2D vec, final Color color) {
        graphics.setColor(color);
        final AffineTransform old = graphics.getTransform();
        final AffineTransform tr = graphics.getTransform();
        tr.setToTranslation(location.getX(), location.getY());
        tr.rotate(vec.getX(), vec.getY());
        graphics.transform(tr);
        graphics.fill(VEHICLE_SHAPE);
        if (borderPainted) {
            graphics.setStroke(THIN_STROKE);
            graphics.setColor(Color.BLACK);
            graphics.draw(VEHICLE_SHAPE);
        }
        graphics.setTransform(old);
    }

    /**
     * @param borderPainted the borderPainted to set
     */
    public void setBorderPainted(final boolean borderPainted) {
        this.borderPainted = borderPainted;
    }

    /**
     * @param graphics the graphics to set
     */
    public void setGraphics(final Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * @param reversed the reversed to set
     */
    public void setReversed(final boolean reversed) {
        this.reversed = reversed;
    }
}
