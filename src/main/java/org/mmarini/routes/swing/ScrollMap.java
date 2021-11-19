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
import org.mmarini.routes.model2.MapElement;
import org.mmarini.routes.model2.MapModule;
import org.mmarini.routes.model2.MapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author marco.marini@mmarini.org
 */
public class ScrollMap extends JScrollPane {
    private static final double SCALE_FACTOR = Math.sqrt(2);
    private static final long serialVersionUID = 1L;
    private static final Point LEGEND_LOCATION = new Point(5, 5);
    private static final Insets LEGEND_INSETS = new Insets(3, 3, 3, 3);

    private final RouteMap routeMap;
    private final Rectangle rect;
    private final String[] pointLegendPattern;
    private final String[] gridLegendPattern;
    private final String[] edgeLegendPattern;
    private Point point;
    private Point2D mapPoint;
    private double fps;
    private long numVehicles;
    private double tps;
    private double speed;

    /**
     * @param routeMap the route map
     */
    public ScrollMap(RouteMap routeMap) {
        this.routeMap = routeMap;
        point = new Point();
        mapPoint = new Point2D.Double();
        rect = new Rectangle();
        gridLegendPattern = loadPatterns("ScrollMap.gridLegendPattern");
        pointLegendPattern = loadPatterns("ScrollMap.pointLegendPattern");
        edgeLegendPattern = loadPatterns("ScrollMap.edgeLegendPattern");
        init();
        createFlows();
        setDoubleBuffered(true);
    }

    /**
     * @param location the location
     */
    private void centerTo(final Point2D location) {
        point = routeMap.computeViewLocation(location);
        final JViewport viewport = getViewport();
        final Rectangle rec = viewport.getVisibleRect();
        point.x -= rec.width / 2;
        point.y -= rec.height / 2;
        validateView(point);
        viewport.setViewPosition(point);
    }

    public void clearSelection() {
        routeMap.clearSelection();
    }

    /**
     * @param text the text
     */
    private void computeRect(final String... text) {
        final FontMetrics fm = getFontMetrics(getFont());
        rect.x = LEGEND_LOCATION.x;
        rect.y = LEGEND_LOCATION.y;
        final int n = text.length;
        int w = 0;
        for (final String tx : text) {
            w = Math.max(w, fm.stringWidth(tx));
        }
        rect.width = w + LEGEND_INSETS.left + LEGEND_INSETS.right;
        rect.height = fm.getHeight() * n + LEGEND_INSETS.bottom + LEGEND_INSETS.top;
    }

    /**
     *
     */
    private void createFlows() {
        routeMap.getMouseWheelFlowable().doOnNext(this::handleMouseWheelMoved).subscribe();
        routeMap.getMouseFlowable()
                .filter(ev -> ev.getID() == MouseEvent.MOUSE_MOVED)
                .doOnNext(ev -> repaint())
                .subscribe();
    }

    /**
     * Return the current view scale
     *
     * @return the current view scale (real px/virtual px)
     */
    public double getScale() {
        return routeMap.getScale();
    }

    /**
     * Sets the view scale
     *
     * @param scale the scale (real px/virtual px)
     */
    public void setScale(final double scale) {
        routeMap.setScale(scale);
    }

    /**
     * @param e the event
     */
    protected void handleMouseWheelMoved(final MouseWheelEvent e) {
        final double scale = Math.pow(SCALE_FACTOR, e.getWheelRotation());
        scale(e.getPoint(), getScale() * scale);
        repaint();
    }

    /**
     *
     */
    private void init() {
        setViewportView(this.routeMap);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, new JButton(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                scaleToFit();
            }
        }));

        setDoubleBuffered(true);
        setOpaque(false);
    }

    /**
     * @param edge the edge
     */
    private boolean isShown(final MapEdge edge) {
        final Rectangle rect = getViewport().getViewRect();
        /*
         * Top right visible point
         */
        mapPoint = routeMap.computeMapLocation(rect.getLocation());
//        mapPoint.setLocation(routeMap.computeMapLocation(rect.getLocation()));
        final double x0 = mapPoint.getX();
        final double y0 = mapPoint.getY();

        /*
         * Bottom left visible point
         */
        point.x = (int) rect.getMaxX();
        point.y = (int) rect.getMaxY();
        mapPoint = routeMap.computeMapLocation(point);
//        mapPoint.setLocation(routeMap.computeMapLocation(point));
        final double x1 = mapPoint.getX();
        final double y1 = mapPoint.getY();

        /*
         * begin point
         */
        Point2D pt = edge.getBeginLocation();
        double x2 = pt.getX();
        double y2 = pt.getY();

        /*
         * end point
         */
        pt = edge.getEndLocation();
        double x3 = pt.getX();
        double y3 = pt.getY();

        final double dx = x3 - x2;
        final double dy = y3 - y2;
        if (Math.abs(dx) >= Math.abs(dy)) {
            final double k = dy / dx;
            if (x3 < x2) {
                double t = x3;
                x3 = x2;
                x2 = t;
                t = y3;
                y3 = y2;
                y2 = t;
            }
            if (x3 < x0 || x2 > x1) {
                return false;
            }
            if (x2 < x0) {
                y2 = (x0 - x2) * k + y2;
                x2 = x0;
            }
            if (x3 > x1) {
                y3 = (x1 - x2) * k + y2;
            }
            return Math.max(y2, y3) >= y0 && Math.min(y2, y3) <= y1;
        } else {
            final double k = dx / dy;
            if (y3 < y2) {
                double t = x3;
                x3 = x2;
                x2 = t;
                t = y3;
                y3 = y2;
                y2 = t;
            }
            if (y3 < y0 || y2 > y1) {
                return false;
            }
            if (y2 < y0) {
                x2 = (y0 - y2) * k + x2;
                y2 = y0;
            }
            if (y3 > y1) {
                x3 = (y1 - y2) * k + x2;
            }
            return Math.max(x2, x3) >= x0 && Math.min(x2, x3) <= x1;
        }
    }

    /**
     * @param node the node
     */
    private boolean isShown(final MapNode node) {
        final Rectangle rect = getViewport().getViewRect();
        point = routeMap.computeViewLocation(node.getLocation());
        return rect.contains(point);
    }

    /**
     * @param key the key
     */
    private String[] loadPatterns(final String key) {
        final List<String> list = new ArrayList<>(0);
        int i = 0;
        for (; ; ) {
            final String text = Messages.getString(key + "." + i);
            if (text.startsWith("!")) {
                break;
            }
            list.add(text);
            ++i;
        }
        return list.toArray(new String[0]);
    }

    /**
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    @Override
    protected void paintChildren(final Graphics g) {
        super.paintChildren(g);
        paintInfo(g);
    }

    private void paintFps(Graphics g) {
        final FontMetrics fm = getFontMetrics(getFont());
        String[] msg = new String[]{
                String.format("TPS: %.2f", tps),
                String.format("FPS: %.2f", fps),
                String.format("Speed: %-2.2f", speed),
                String.format("Vehicles: %d", this.numVehicles)
        };
        computeRect(msg);
        rect.x = LEGEND_LOCATION.x;
        rect.y = getHeight() - getInsets().bottom - rect.height - fm.getHeight() - LEGEND_INSETS.bottom - LEGEND_INSETS.bottom;
        g.setColor(Color.WHITE);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.setColor(Color.BLACK);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
        final int x = rect.x + LEGEND_INSETS.left;
        final int fh = fm.getHeight();
        int y = rect.y + LEGEND_INSETS.top + fh - fm.getDescent();
        for (final String text : msg) {
            g.drawString(text, x, y);
            y += fh;
        }
    }

    /**
     * @param g graphics
     */
    private void paintInfo(final Graphics g) {
        /*
         * Compute the parameters
         */
        final Point pt = routeMap.getMousePosition();
        if (pt != null) {
            mapPoint = routeMap.computeMapLocation(pt);
//            mapPoint.setLocation(routeMap.computeMapLocation(pt));
        }
        /*
         * Compute the pattern
         */
        final String[] pattern;
        if (pt == null) {
            pattern = gridLegendPattern;
        } else if (routeMap.isSelectingEnd()) {
            pattern = edgeLegendPattern;
        } else {
            pattern = pointLegendPattern;
        }

        final String[] text = Arrays.stream(pattern).map(ptn ->
                MessageFormat.format(ptn,
                        routeMap.getGridSize(),
                        mapPoint.getX(),
                        mapPoint.getY(),
                        routeMap.getEdgeLength())
        ).toArray(String[]::new);

        paintMessageBox(g, text);
        paintFps(g);
    }

    /**
     * @param g        graphics
     * @param messages messages
     */
    private void paintMessageBox(final Graphics g, final String... messages) {
        computeRect(messages);
        final FontMetrics fm = getFontMetrics(getFont());
        g.setColor(Color.WHITE);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.setColor(Color.BLACK);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
        final int x = rect.x + LEGEND_INSETS.left;
        final int fh = fm.getHeight();
        int y = rect.y + LEGEND_INSETS.top + fh - fm.getDescent();
        for (final String text : messages) {
            g.drawString(text, x, y);
            y += fh;
        }
    }

    /**
     * Reset the status of the view.<br>
     * This method reset the map view. It must be call when a map is changed.
     */
    public void reset() {
        routeMap.reset();
    }

    /**
     * @param ref   reference
     * @param scale scale
     */
    private void scale(final Point ref, final double scale) {
        final JViewport viewport = getViewport();
        final Point pt = viewport.getViewPosition();
        final int dx = ref.x - pt.x;
        final int dy = ref.y - pt.y;
        mapPoint = routeMap.computeMapLocation(ref);
        routeMap.setScale(scale);
        point = routeMap.computeViewLocation(mapPoint);
        point.x -= dx;
        point.y -= dy;
        viewport.setViewPosition(point);
    }

    /**
     * Scale the view to fit the current component size
     */
    public void scaleToFit() {
        routeMap.scaleToFit(getViewport().getSize());
        repaint();
    }

    /**
     * Scroll the map view and center to an edge
     *
     * @param edge the edge element to center the view to
     */
    public void scrollTo(final MapEdge edge) {
        if (!isShown(edge)) {
            final Point2D b = edge.getBeginLocation();
            final Point2D e = edge.getEndLocation();
            final double x = (b.getX() + e.getX()) * 0.5;
            final double y = (b.getY() + e.getY()) * 0.5;
            mapPoint = new Point2D.Double(x, y);
            centerTo(mapPoint);
        }
    }

    /**
     * Scroll the map to the node
     *
     * @param node the node
     */
    public void scrollTo(final MapNode node) {
        if (!isShown(node)) {
            centerTo(node.getLocation());
        }
    }

    /**
     * Sets the frames per second
     *
     * @param fps the frames per second
     */
    public void setFps(double fps) {
        this.fps = fps;
        repaint();

    }

    public void setNumVehicles(long numVehicles) {
        this.numVehicles = numVehicles;
        repaint();
    }

    /**
     * Sets the selected element
     *
     * @param element the element
     */
    public void setSelectedElement(final MapElement element) {
        routeMap.setSelectedElement(element);
    }

    /**
     * @param speed the speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
        repaint();
    }

    /**
     * Sets the transitions per second
     *
     * @param tps the transitions per second
     */
    public void setTps(double tps) {
        this.tps = tps;
        repaint();
    }

    /**
     * @param trafficView true if traffic view
     */
    public void setTrafficView(final boolean trafficView) {
        routeMap.setTrafficView(trafficView);
    }

    /**
     *
     */
    public void startCenterMode() {
        routeMap.startCenterMode();
    }

    /**
     *
     */
    public void startEdgeMode() {
        routeMap.startEdgeMode();
    }

    /**
     * @param mapModule the mapModule
     */
    public void startModuleMode(final MapModule mapModule) {
        routeMap.startModuleMode(mapModule);
    }

    /**
     *
     */
    public void startSelectMode() {
        routeMap.startSelectMode();
    }

    /**
     * @param point the point
     */
    private void validateView(final Point point) {
        final JViewport viewport = getViewport();
        final Dimension size = viewport.getViewSize();
        final Rectangle rec = viewport.getVisibleRect();
        if (point.x + rec.width >= size.width) {
            point.x = size.width - rec.width;
        }
        if (point.x < 0) {
            point.x = 0;
        }
        if (point.y + rec.height >= size.height) {
            point.y = size.height - rec.height;
        }
        if (point.y < 0) {
            point.y = 0;
        }
    }

    /**
     *
     */
    public void zoomIn() {
        final Rectangle rect = getViewport().getViewRect();
        point.x = (int) Math.round(rect.getCenterX());
        point.y = (int) Math.round(rect.getCenterY());
        scale(point, getScale() * SCALE_FACTOR);
    }

    /**
     *
     */
    public void zoomOut() {
        final Rectangle rect = getViewport().getViewRect();
        point.x = (int) Math.round(rect.getCenterX());
        point.y = (int) Math.round(rect.getCenterY());
        scale(point, getScale() / SCALE_FACTOR);
    }
}
