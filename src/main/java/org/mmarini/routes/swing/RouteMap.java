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

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static org.mmarini.routes.swing.StatusView.DEFAULT_NODE_COLOR;

/**
 * @author marco.marini@mmarini.org
 */
public class RouteMap extends JComponent {

    private static final long BLINKING_ON = 500;
    private static final long BLINKING_TIME = 500;
    private static final String DELETE_ACTION = "DELETE";
    private static final double TRAFFIC_COLOR_SATURATION = 0.9;
    private static final long serialVersionUID = 1L;
    private static final Color EDGE_DRAGGING_COLOR = Color.GRAY;
    private static final int CURSOR_SELECTION_PRECISION = 10;
    private static final int MAP_BORDER = 60;

    private final Rectangle2D mapBound;
    private final Point2D point;
    private final AffineTransform transform;
    private final AffineTransform inverse;
    private final MapElementVisitor cursorPainter;
    private final Point2D begin;
    private final Point2D end;
    private final Painter painter;
    private final Mode selectingMode;
    private final Mode startEdgeMode;
    private final Mode endEdgeMode;
    private final Mode moduleLocationMode;
    private final Mode moduleRotationMode;
    private final Mode centerMode;
    private final Flowable<MouseWheelEvent> mouseWheelFlowable;
    private final Flowable<MouseEvent> mouseFlowable;
    private final PublishProcessor<MapEdge> deleteEdgeProcessor;
    private final PublishProcessor<MapNode> deleteNodeProcessor;
    private final PublishProcessor<Point2D> centerMapProcessor;
    private final PublishProcessor<EdgeCreation> newEdgeProcessor;
    private final PublishProcessor<ModuleParameters> newModuleProcessor;
    private final PublishProcessor<MapElement> unselectProcessor;
    private final PublishProcessor<MapElement> selectElementProcessor;
    private Module module;
    private boolean mouseInside;
    private double scale;
    private double gridSize;
    private MapElement selectedElement;
    private boolean trafficView;
    private Mode currentMode;
    private StatusView status;

    /**
     *
     */
    public RouteMap() {
        painter = new Painter();
        mapBound = new Rectangle2D.Double();
        point = new Point2D.Double();
        begin = new Point2D.Double();
        end = new Point2D.Double();
        transform = new AffineTransform();
        inverse = new AffineTransform();
        cursorPainter = new MapElementVisitor() {

            @Override
            public void visit(final MapEdge edge) {
                painter.paintCursorEdge(edge);
                painter.paintCursorEdgeEnds(edge);
            }

            @Override
            public void visit(final MapNode node) {
                painter.paintNodeCursor(node.getLocation());
            }

            @Override
            public void visit(final SiteNode node) {
            }
        };
        scale = 1;
        selectingMode = new Mode() {
            @Override
            public void handleMouseMoved(MouseEvent ev) {
            }

            @Override
            public void handleMousePressed(MouseEvent ev) {
                computeMapLocation(point, ev.getPoint());
                status.findElement(point, (CURSOR_SELECTION_PRECISION / scale))
                        .ifPresentOrElse(RouteMap.this::setSelectedElement, RouteMap.this::clearSelection);
            }

            @Override
            public void paintMode() {
            }
        };

        startEdgeMode = new Mode() {

            @Override
            public void handleMouseMoved(MouseEvent ev) {

            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                computeMapLocation(begin, ev.getPoint());
                begin.setLocation(status.snapToNode(begin, (CURSOR_SELECTION_PRECISION / scale)));
                end.setLocation(begin);
                currentMode = endEdgeMode;
                repaint();
            }

            @Override
            public void paintMode() {

            }

        };
        endEdgeMode = new Mode() {

            @Override
            public void handleMouseMoved(final MouseEvent ev) {
                computeMapLocation(end, ev.getPoint());
                end.setLocation(status.snapToNode(end, (CURSOR_SELECTION_PRECISION / scale)));
                repaint();
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                handleEndEdge(ev);
            }

            @Override
            public void paintMode() {
                if (mouseInside) {
                    painter.paintEdge(begin, end, EDGE_DRAGGING_COLOR);
                }
            }

        };
        moduleLocationMode = new Mode() {
            @Override
            public void handleMouseMoved(final MouseEvent ev) {
                repaint();
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                computeMapLocation(begin, ev.getPoint());
                currentMode = moduleRotationMode;
                repaint();
            }

            @Override
            public void paintMode() {
                final Point mousePosition = getMousePosition();
                if (mousePosition != null) {
                    computeMapLocation(point, mousePosition);
                    paintModule(point, 0., 0.);
                }
            }

        };
        moduleRotationMode = new Mode() {
            @Override
            public void handleMouseMoved(final MouseEvent ev) {
                repaint();
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                computeMapLocation(point, ev.getPoint());
                newModuleProcessor.onNext(new ModuleParameters(
                        module,
                        begin,
                        new Point2D.Double(
                                point.getX() - begin.getX(),
                                point.getY() - begin.getY())
                ));
                startSelectMode();
                repaint();
            }

            @Override
            public void paintMode() {
                final Point mousePosition = getMousePosition();
                if (mousePosition != null) {
                    computeMapLocation(point, mousePosition);
                    paintModule(begin, point.getX() - begin.getX(), point.getY() - begin.getY());
                } else {
                    paintModule(begin, 0., 0.);
                }
            }

        };
        centerMode = new Mode() {
            @Override
            public void handleMouseMoved(MouseEvent ev) {
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                computeMapLocation(point, ev.getPoint());
                startSelectMode();
                centerMapProcessor.onNext(point);
            }

            @Override
            public void paintMode() {
            }

        };
        this.currentMode = selectingMode;
        mouseFlowable = SwingObservable.mouse(this).toFlowable(BackpressureStrategy.LATEST);
        mouseWheelFlowable = SwingObservable.mouseWheel(this).toFlowable(BackpressureStrategy.LATEST);
        this.deleteEdgeProcessor = PublishProcessor.create();
        this.deleteNodeProcessor = PublishProcessor.create();
        this.centerMapProcessor = PublishProcessor.create();
        this.newEdgeProcessor = PublishProcessor.create();
        this.newModuleProcessor = PublishProcessor.create();
        this.unselectProcessor = PublishProcessor.create();
        this.selectElementProcessor = PublishProcessor.create();
        init();
        createFlows();
    }

    /**
     *
     */
    public void clearSelection() {
        final MapElement oldSelectedElement = this.selectedElement;
        this.selectedElement = null;
        if (oldSelectedElement != null) {
            unselectProcessor.onNext(oldSelectedElement);
        }
        repaint();
    }

    /**
     *
     */
    private void computeGridSize() {
        final double size = 10 / scale;
        gridSize = 1;
        while (size > gridSize) {
            gridSize *= 10;
        }
    }

    /**
     *
     */
    private void computeMapBound() {
        if (status != null) {
            mapBound.setFrame(status.computeMapBound());
            computeGridSize();
        }
    }

    /**
     * @param result the result
     * @param point  the point
     */
    public void computeMapLocation(final Point2D result, final Point point) {
        inverse.transform(point, result);
        result.setLocation(Math.round(result.getX()), Math.round(result.getY()));
    }

    /**
     *
     */
    private void computePreferredSize() {
        final int width = (int) Math.round(mapBound.getWidth() * scale) + MAP_BORDER * 2;
        final int height = (int) Math.round(mapBound.getHeight() * scale) + MAP_BORDER * 2;
        setPreferredSize(new Dimension(width, height));
        revalidate();
    }

    /**
     *
     */
    private void computeTransform() {
        transform.setToTranslation(MAP_BORDER, MAP_BORDER);
        transform.scale(scale, scale);
        transform.translate(-mapBound.getMinX(), -mapBound.getMinY());
        inverse.setTransform(transform);
        try {
            inverse.invert();
        } catch (final NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param result
     * @param point
     */
    public void computeViewLocation(final Point result, final Point2D point) {
        transform.transform(point, result);
    }

    /**
     *
     */
    private void createFlows() {
        getActionMap().put(DELETE_ACTION, new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (selectedElement instanceof MapEdge) {
                    deleteEdgeProcessor.onNext((MapEdge) selectedElement);
                } else {
                    deleteNodeProcessor.onNext((MapNode) selectedElement);
                }
            }
        });

        mouseFlowable
                .doOnNext(ev -> {
                    switch (ev.getID()) {
                        case MouseEvent.MOUSE_ENTERED:
                            handleMouseEntered();
                            break;
                        case MouseEvent.MOUSE_EXITED:
                            handleMouseExited();
                            break;
                        case MouseEvent.MOUSE_PRESSED:
                            currentMode.handleMousePressed(ev);
                            break;
                        case MouseEvent.MOUSE_MOVED:
                            currentMode.handleMouseMoved(ev);
                            break;
                    }
                }).subscribe();
    }

    /**
     *
     */
    public PublishProcessor<Point2D> getCenterMapFlowable() {
        return centerMapProcessor;
    }

    public Flowable<MapEdge> getDeleteEdgeFlowable() {
        return deleteEdgeProcessor;
    }

    public Flowable<MapNode> getDeleteNodeFlowable() {
        return deleteNodeProcessor;
    }

    /**
     *
     */
    public double getEdgeLength() {
        return begin.distance(end);
    }

    /**
     * Returns the gridSize
     */
    public double getGridSize() {
        return gridSize;
    }

    /**
     *
     */
    public Flowable<MouseEvent> getMouseFlowable() {
        return mouseFlowable;
    }

    /**
     *
     */
    public Flowable<MouseWheelEvent> getMouseWheelFlowable() {
        return mouseWheelFlowable;
    }

    /**
     *
     */
    public PublishProcessor<EdgeCreation> getNewEdgeFlowable() {
        return newEdgeProcessor;
    }

    public PublishProcessor<ModuleParameters> getNewModuleProcessor() {
        return newModuleProcessor;
    }

    /**
     * Return the current view scale
     *
     * @return the current view scale (real px/virtual px)
     */
    public double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(final double scale) {
        this.scale = scale;
        reset();
    }

    /**
     *
     */
    public Flowable<MapElement> getSelectElementFlowable() {
        return selectElementProcessor;
    }

    /**
     * Returns the selectedElement
     */
    public MapElement getSelectedElement() {
        return selectedElement;
    }

    /**
     * @param selectedElement the selected element to set
     */
    public void setSelectedElement(final MapElement selectedElement) {
        assert selectedElement != null;
        final MapElement oldSelectedElement = this.selectedElement;
        this.selectedElement = selectedElement;
        if (!selectedElement.equals(oldSelectedElement)) {
            selectElementProcessor.onNext(selectedElement);
            repaint();
        }
    }

    /**
     *
     */
    public Flowable<MapElement> getUnselectFlowable() {
        return unselectProcessor;
    }

    /**
     * @param ev the mouse event
     */
    private void handleEndEdge(final MouseEvent ev) {
        computeMapLocation(end, ev.getPoint());
        end.setLocation(status.snapToNode(end, (CURSOR_SELECTION_PRECISION / scale)));
        newEdgeProcessor.onNext(new EdgeCreation(begin, end));
        begin.setLocation(end);
        repaint();
    }

    /**
     *
     */
    private void handleMouseEntered() {
        this.mouseInside = true;
        repaint();
    }

    /**
     *
     */
    private void handleMouseExited() {
        this.mouseInside = false;
        repaint();
    }

    /**
     *
     */
    private void init() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed DELETE"), DELETE_ACTION);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed BACK_SPACE"), DELETE_ACTION);
        setBackground(Color.WHITE);
    }

    /**
     * Returns the selectingEnd
     */
    public boolean isSelectingEnd() {
        return currentMode.equals(endEdgeMode);
    }

    /**
     *
     */
    private boolean isShowingCursor() {
        final long t = System.currentTimeMillis() % BLINKING_TIME;
        return t <= BLINKING_ON;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Dimension size = getSize();
        Color bg;
        if (trafficView) {
            bg = Color.BLACK;
        } else {
            bg = getBackground();
        }
        g.setColor(bg);
        g.fillRect(0, 0, size.width, size.height);
        if (status != null) {
            final Graphics2D gr = (Graphics2D) g.create();
            gr.transform(transform);
            painter.setGraphics(gr);
            painter.setBorderPainted(scale >= 1f);
            painter.setReversed(trafficView);
            painter.paintGrid(mapBound, gridSize);
            paintEdges();
            paintSites();
            currentMode.paintMode();
            paintCursor();
            if (!trafficView) {
                paintVehicles();
            }
        }
    }

    /**
     *
     */
    private void paintCursor() {
        final MapElement element = getSelectedElement();
        if (element != null && isShowingCursor()) {
            element.apply(cursorPainter);
        }
    }

    /**
     *
     */
    private void paintEdges() {
        if (trafficView) {
            for (final MapEdge edge : status.getEdges()) {
                if (!(edge.equals(selectedElement) && isShowingCursor())) {
                    double trafficLevel = edge.getTrafficLevel();
                    //trafficLevel = Math.sqrt(trafficLevel);
                    final Color color = SwingUtils.getInstance().computeColor(trafficLevel, TRAFFIC_COLOR_SATURATION);
                    painter.paintEdge(edge, color);
                }
            }
        } else {
            for (final MapEdge edge : status.getEdges()) {
                if (!(edge.equals(selectedElement) && isShowingCursor())) {
                    painter.paintEdge(edge);
                }
            }
        }
    }

    /**
     * @param location
     * @param x
     * @param y
     */
    private void paintModule(final Point2D location, final double x, final double y) {
        if (module != null) {
            painter.paintModule(module, location, x, y);
        }
    }

    /**
     *
     */
    private void paintSites() {
        for (final SiteNode node : status.getSites()) {
            if (node.equals(selectedElement) && isShowingCursor()) {
                painter.paintSiteCursor(node.getLocation());
            } else {
                painter.paintSite(node.getLocation(),
                        status.getNodeView(node)
                                .map(NodeView::getColor)
                                .orElse(DEFAULT_NODE_COLOR));
            }

        }
    }

    /**
     *
     */
    private void paintVehicles() {
        for (final Vehicle vehicle : status.getVehicles()) {
            if (vehicle.isRunning()) {
                vehicle.retrieveLocation(point);
                final Color color = status.getNodeView(vehicle.getDestination())
                        .map(NodeView::getColor)
                        .orElse(DEFAULT_NODE_COLOR);
                painter.paintVehicle(point, vehicle.getVector(), color);
            }
        }
    }

    /**
     *
     */
    public void reset() {
        computeMapBound();
        computeTransform();
        computePreferredSize();
    }

    /**
     * @param size
     */
    public void scaleToFit(final Dimension size) {
        computeMapBound();
        double scale = Math.min((size.width - MAP_BORDER * 2) / mapBound.getWidth(),
                (size.height - MAP_BORDER * 2) / mapBound.getHeight());
        scale = Math.max(scale, 1e-6);
        setScale(scale);
    }

    public void setStatus(StatusView status) {
        this.status = status;
        repaint();
    }

    /**
     * @param trafficView
     */
    public void setTrafficView(final boolean trafficView) {
        this.trafficView = trafficView;
        repaint();
    }

    /**
     *
     */
    public void startCenterMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.currentMode = centerMode;
        repaint();
    }

    /**
     *
     */
    public void startEdgeMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.currentMode = startEdgeMode;
    }

    /**
     * @param module
     */
    public void startModuleMode(final Module module) {
        this.module = module;
        this.currentMode = moduleLocationMode;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /**
     *
     */
    public void startSelectMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        this.currentMode = selectingMode;
        repaint();
    }

    interface Mode {

        /**
         * @param ev
         */
        void handleMouseMoved(final MouseEvent ev);

        /**
         * @param ev
         */
        void handleMousePressed(final MouseEvent ev);

        /**
         *
         */
        void paintMode();
    }

    public static class ModuleParameters {
        private final Module module;
        private final Point2D location;
        private final Point2D direction;

        public ModuleParameters(Module module, Point2D location, Point2D direction) {
            this.module = module;
            this.location = location;
            this.direction = direction;
        }

        public Point2D getDirection() {
            return direction;
        }

        public Point2D getLocation() {
            return location;
        }

        public Module getModule() {
            return module;
        }
    }

    public static class EdgeCreation {
        private final Point2D begin;
        private final Point2D end;

        public EdgeCreation(Point2D begin, Point2D end) {
            this.begin = begin;
            this.end = end;
        }

        public Point2D getBegin() {
            return begin;
        }

        public Point2D getEnd() {
            return end;
        }
    }
}
