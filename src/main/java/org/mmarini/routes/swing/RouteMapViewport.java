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
import org.mmarini.routes.model2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.mmarini.routes.model2.Constants.VEHICLE_LENGTH;
import static org.mmarini.routes.swing.StatusView.DEFAULT_NODE_COLOR;
import static org.mmarini.routes.swing.UIConstants.*;


/**
 * @author marco.marini@mmarini.org
 */
public class RouteMapViewport extends JComponent {
    public static final double MIN_SCALE = 20e-3;
    public static final double MAX_SCALE = 12;
    public static final long SCROLL_INTERVAL = 1000L / 60; // ms
    public static final double SCROLL_RATIO = SCROLL_INTERVAL / 800D;
    private static final Logger logger = LoggerFactory.getLogger(RouteMapViewport.class);
    private static final long BLINKING_ON = 450;
    private static final long BLINKING_TIME = 500;
    private static final String DELETE_ACTION = "DELETE";
    private static final double TRAFFIC_COLOR_SATURATION = 0.9;
    private static final long serialVersionUID = 1L;
    private static final Color EDGE_DRAGGING_COLOR = new Color(
            Color.GRAY.getRed(),
            Color.GRAY.getGreen(),
            Color.GRAY.getBlue(),
            128);
    private static final int MAP_BORDER = 60;
    private final Rectangle2D mapBound;
    private final AffineTransform transform;
    private final AffineTransform inverse;
    private final MapElementVisitor<Void> cursorPainter;
    private final Painter painter;
    private final Mode selectingMode;
    private final Mode startEdgeMode;
    private final Mode endEdgeMode;
    private final Mode changeEndEdgeMode;
    private final Mode changeBeginEdgeMode;
    private final Mode moduleLocationMode;
    private final Mode moduleRotationMode;
    private final Mode centerMode;
    private final Flowable<MouseWheelEvent> mouseWheelFlowable;
    private final Flowable<MouseEvent> mouseFlowable;
    private final PublishProcessor<MapEdge> deleteEdgeProcessor;
    private final PublishProcessor<MapNode> deleteNodeProcessor;
    private final PublishProcessor<Point2D> centerMapProcessor;
    private final PublishProcessor<EdgeCreation> newEdgeProcessor;
    private final PublishProcessor<TerminalEdgeChange> beginEdgeChangeProcessor;
    private final PublishProcessor<TerminalEdgeChange> endEdgeChangeProcessor;
    private final PublishProcessor<ModuleParameters> newModuleProcessor;
    private final PublishProcessor<MapElement> unselectProcessor;
    private final PublishProcessor<MapElement> selectElementProcessor;
    private Point2D begin;
    private Point2D end;
    private MapModule mapModule;
    private boolean mouseInside;
    private double scale;
    private double gridSize;
    private MapElement selectedElement;
    private boolean trafficView;
    private Mode currentMode;
    private StatusView status;
    private boolean ctrPressed;
    private MapEdge changingEdge;
    private Point2D viewportLocation;

    /**
     *
     */
    public RouteMapViewport() {
        logger.debug("RouteMapViewport created.");
        painter = new Painter();
        mapBound = new Rectangle2D.Double();
        begin = new Point2D.Double();
        end = new Point2D.Double();
        transform = new AffineTransform();
        inverse = new AffineTransform();
        viewportLocation = new Point2D.Double();
        cursorPainter = new MapElementVisitorAdapter<>() {

            @Override
            public Void visit(final MapEdge edge) {
                painter.paintCursorEdge(edge);
                painter.paintCursorEdgeEnds(edge);
                return null;
            }

            @Override
            public Void visit(final CrossNode node) {
                painter.paintNodeCursor(node.getLocation());
                return null;
            }
        };
        scale = 1;
        selectingMode = new Mode() {
            @Override
            public void handleMouseMoved(MouseEvent ev) {
            }

            @Override
            public void handleMousePressed(MouseEvent ev) {
                Point2D point = computeMapLocation(ev.getPoint());
                status.findElement(point, computePrecisionDistance(scale))
                        .ifPresentOrElse(RouteMapViewport.this::setSelectedElement, RouteMapViewport.this::clearSelection);
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
                begin = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        computePrecisionDistance(scale));
                end = begin;
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
                end = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        (CURSOR_SELECTION_PRECISION / scale));
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
        changeEndEdgeMode = new Mode() {

            @Override
            public void handleMouseMoved(final MouseEvent ev) {
                end = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        (CURSOR_SELECTION_PRECISION / scale));
                repaint();
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                handleChangeEndEdge(ev);
            }

            @Override
            public void paintMode() {
                if (mouseInside) {
                    painter.paintEdge(begin, end, EDGE_DRAGGING_COLOR);
                }
            }
        };
        changeBeginEdgeMode = new Mode() {

            @Override
            public void handleMouseMoved(final MouseEvent ev) {
                begin = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        (CURSOR_SELECTION_PRECISION / scale));
                repaint();
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                handleChangeBeginEdge(ev);
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
                RouteMapViewport.this.ctrPressed = (ev.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK;
                repaint();
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                begin = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        computePrecisionDistance(scale));
                RouteMapViewport.this.ctrPressed = (ev.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK;
                if (ctrPressed) {
                    begin = status.snapToNode(begin, computePrecisionDistance(scale));
                }
                currentMode = moduleRotationMode;
                repaint();
            }

            @Override
            public void paintMode() {
                final Point mousePosition = getMousePosition();
                if (mousePosition != null) {
                    Point2D point = status.snapToNode(
                            computeMapLocation(mousePosition),
                            computePrecisionDistance(scale));
                    if (ctrPressed) {
                        point = status.snapToNode(point, computePrecisionDistance(scale));
                    }
                    paintModule(point, 0d, 0d);
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
                Point2D point = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        computePrecisionDistance(scale));
                newModuleProcessor.onNext(new ModuleParameters(
                        mapModule,
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
                    Point2D point = status.snapToNode(
                            computeMapLocation(mousePosition),
                            computePrecisionDistance(scale));
                    paintModule(begin, point.getX() - begin.getX(), point.getY() - begin.getY());
                } else {
                    paintModule(begin, 0, 0);
                }
            }

        };
        centerMode = new Mode() {
            @Override
            public void handleMouseMoved(MouseEvent ev) {
            }

            @Override
            public void handleMousePressed(final MouseEvent ev) {
                Point2D point = status.snapToNode(
                        computeMapLocation(ev.getPoint()),
                        computePrecisionDistance(scale));
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
        this.endEdgeChangeProcessor = PublishProcessor.create();
        this.beginEdgeChangeProcessor = PublishProcessor.create();
        this.newModuleProcessor = PublishProcessor.create();
        this.unselectProcessor = PublishProcessor.create();
        this.selectElementProcessor = PublishProcessor.create();
        init();
        createFlows();
    }

    /**
     * Centers the map view to a point
     *
     * @param point the center point (m)
     */
    private void centerTo(Point2D point) {
        centerTo(point.getX(), point.getY());
    }

    /**
     * Centers the map view to a point
     *
     * @param x the x center (m)
     * @param y the y center (m)
     */
    private void centerTo(double x, double y) {
        Dimension size = getInnerSize();
        double width = size.width / scale;
        double height = size.height / scale;
        setViewLocation(x - width / 2,
                y - height / 2
        );
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
     * Updates the map bound and the grid size
     */
    private void computeMapBound() {
        if (status != null) {
            mapBound.setFrame(status.computeMapBound());
            computeGridSize();
        }
    }

    /**
     * Returns the location in the map of a screen point
     *
     * @param point the screen point (pixs)
     */
    public Point2D computeMapLocation(final Point point) {
        Point2D.Double result = new Point2D.Double();
        inverse.transform(point, result);
        result.setLocation(Math.round(result.getX()), Math.round(result.getY()));
        return result;
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
        transform.translate(-viewportLocation.getX(), -viewportLocation.getY());
        inverse.setTransform(transform);
        try {
            inverse.invert();
        } catch (final NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the screen location of a map point
     *
     * @param point the map point
     */
    public Point computeViewLocation(final Point2D point) {
        final Point result = new Point();
        transform.transform(point, result);
        return result;
    }

    /**
     *
     */
    private void createFlows() {
        Flowable<Boolean> scrolling = mouseFlowable
                .filter(e -> e.getButton() == MouseEvent.BUTTON3
                        && (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED))
                .map(e -> e.getID() == MouseEvent.MOUSE_PRESSED);
        Flowable.combineLatest(Flowable.interval(SCROLL_INTERVAL, TimeUnit.MILLISECONDS),
                        scrolling,
                        (a, b) -> b)
                .filter(scroll -> scroll)
                .map(unused -> Optional.ofNullable(getScrollDirection()))
                .filter(Optional::isPresent)
                .map(p -> Algebra.prod(p.orElseThrow(), SCROLL_RATIO))
                .doOnNext(this::scrollBy)
                .subscribe();

        getActionMap().put(DELETE_ACTION, new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (selectedElement != null) {
                    if (selectedElement instanceof MapEdge) {
                        deleteEdgeProcessor.onNext((MapEdge) selectedElement);
                    } else {
                        deleteNodeProcessor.onNext((MapNode) selectedElement);
                    }
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
                            if (ev.getButton() == MouseEvent.BUTTON1) {
                                currentMode.handleMousePressed(ev);
                            }
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
    public Flowable<TerminalEdgeChange> getBeginEdgeChangeFlowable() {
        return beginEdgeChangeProcessor;
    }

    /**
     *
     */
    public Flowable<Point2D> getCenterMapFlowable() {
        return centerMapProcessor;
    }

    /**
     *
     */
    public Flowable<MapEdge> getDeleteEdgeFlowable() {
        return deleteEdgeProcessor;
    }

    /**
     *
     */
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
     *
     */
    public Flowable<TerminalEdgeChange> getEndEdgeChangeFlowable() {
        return endEdgeChangeProcessor;
    }

    /**
     * Returns the gridSize
     */
    public double getGridSize() {
        return gridSize;
    }

    /**
     * Returns the inner rectangle of displayed map
     */
    Dimension getInnerSize() {
        Dimension size = getSize();
        size.width -= MAP_BORDER * 2;
        size.height -= MAP_BORDER * 2;
        return size;
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
    public Flowable<EdgeCreation> getNewEdgeFlowable() {
        return newEdgeProcessor;
    }

    public Flowable<ModuleParameters> getNewModuleFlowable() {
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
     * Returns the move direction if the mouse is in the moving area
     * otherwise return null
     */
    private Point2D getScrollDirection() {
        Point mouse = getMousePosition();
        if (mouse == null) {
            return null;
        }
        Dimension size = getSize();
        return Algebra.norma(
                Algebra.sub(mouse,
                        new Point2D.Double(size.width * 0.5, size.height * 0.5))
        );
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

    private Rectangle getViewRect() {
        Dimension size = getInnerSize();
        return new Rectangle(MAP_BORDER, MAP_BORDER, size.width, size.height);
    }

    /**
     * @param ev the mouse event
     */
    private void handleChangeBeginEdge(final MouseEvent ev) {
        begin = status.snapToNode(
                computeMapLocation(ev.getPoint()),
                computePrecisionDistance(scale));
        if (end.distance(begin) > VEHICLE_LENGTH) {
            beginEdgeChangeProcessor.onNext(new TerminalEdgeChange(changingEdge, begin));
            startSelectMode();
        }
        repaint();
    }

    /**
     * @param ev the mouse event
     */
    private void handleChangeEndEdge(final MouseEvent ev) {
        end = status.snapToNode(
                computeMapLocation(ev.getPoint()),
                computePrecisionDistance(scale));
        if (end.distance(begin) > VEHICLE_LENGTH) {
            endEdgeChangeProcessor.onNext(new TerminalEdgeChange(changingEdge, end));
            startSelectMode();
        }
        repaint();
    }

    /**
     * @param ev the mouse event
     */
    private void handleEndEdge(final MouseEvent ev) {
        end = status.snapToNode(
                computeMapLocation(ev.getPoint()),
                computePrecisionDistance(scale));
        if (end.distance(begin) > VEHICLE_LENGTH) {
            newEdgeProcessor.onNext(new EdgeCreation(begin, end));
            begin = end;
        }
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
     * Returns true if mode is selecting edge
     */
    public boolean isSelectingEdge() {
        return currentMode.equals(endEdgeMode)
                || currentMode.equals(changeBeginEdgeMode)
                || currentMode.equals(changeEndEdgeMode);
    }

    /**
     * Returns true id the cursor is shown
     */
    private boolean isShowingCursor() {
        final long t = System.currentTimeMillis() % BLINKING_TIME;
        return t <= BLINKING_ON;
    }

    /**
     * Returns true if the edge is shown
     *
     * @param edge the edge
     */
    private boolean isShown(final MapEdge edge) {
        final Rectangle rect = getViewRect();
        /*
         * Top right visible point
         */
        Point2D mapPoint = computeMapLocation(rect.getLocation());
        final double x0 = mapPoint.getX();
        final double y0 = mapPoint.getY();

        /*
         * Bottom left visible point
         */
        Point point = new Point((int) rect.getMaxX(), (int) rect.getMaxY());
        mapPoint = computeMapLocation(point);
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
     * Returns true if the node is shown
     *
     * @param node the node
     */
    private boolean isShown(final MapNode node) {
        final Rectangle rect = getViewRect();
        Point point = computeViewLocation(node.getLocation());
        return rect.contains(point);
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
                    double trafficLevel = status.getEdgesTrafficLevel(edge);
                    Color color = SwingUtils.getInstance().computeColor(trafficLevel, TRAFFIC_COLOR_SATURATION);
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
     * @param location the location
     * @param x        the x coordinate
     * @param y        the y coordinate
     */
    private void paintModule(final Point2D location, final double x, final double y) {
        if (mapModule != null) {
            painter.paintModule(mapModule, location, x, y, EDGE_DRAGGING_COLOR);
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
            vehicle.getLocation().ifPresent(point ->
                    vehicle.getDirection().ifPresent(direction -> {
                        final Color color = status.getNodeView(vehicle.getCurrentDestination())
                                .map(NodeView::getColor)
                                .orElse(DEFAULT_NODE_COLOR);
                        painter.paintVehicle(point, direction, color);
                    }));
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
     * Scale the map with pivot point
     *
     * @param pivot the pivot point (pix)
     * @param scale the scale pix/m
     */
    public void scale(Point pivot, double scale) {
        scale = min(max(scale, MIN_SCALE), MAX_SCALE);
        Point2D screenVect = Algebra.sub(pivot, new Point(MAP_BORDER, MAP_BORDER));
        Point2D newLocation = Algebra.sum(Algebra.prod(screenVect, 1 / this.scale - 1 / scale), viewportLocation);
        setScale(scale);
        setViewLocation(newLocation);
    }

    /**
     *
     */
    public void scaleToFit() {
        Dimension size = getInnerSize();
        computeMapBound();
        double scale = min(size.width / mapBound.getWidth(),
                size.height / mapBound.getHeight());
        scale = max(scale, MIN_SCALE);
        setScale(scale);
        centerTo(mapBound.getCenterX(), mapBound.getCenterY());
    }

    /**
     * Scrolls the view by a ratio vector
     *
     * @param ratio the ratio vector
     */
    public void scrollBy(Point2D ratio) {
        Dimension size = getInnerSize();
        // Computes the scroll size (m)
        double scrollSize = min(size.height, size.width) / scale;
        // Computes the scroll amount (m)
        Point2D scrollAmount = Algebra.prod(ratio, scrollSize);
        // Computes the new view location
        Point2D newLocation = Algebra.sum(
                viewportLocation,
                scrollAmount);
        // Clip scroll on map edge
        double xMax = max(mapBound.getMaxX() - size.width / scale, 0);
        double yMax = max(mapBound.getMaxY() - size.height / scale, 0);
        double x = viewportLocation.getX();
        double y = viewportLocation.getY();
        double x1 = newLocation.getX();
        double y1 = newLocation.getY();
        x = scrollAmount.getX() < 0
                ? max(x1, mapBound.getMinX())
                : min(x1, xMax);
        y = scrollAmount.getY() < 0
                ? max(y1, mapBound.getMinY())
                : min(y1, yMax);
        setViewLocation(x, y);
    }

    /**
     * Center the map to node
     *
     * @param node the node
     */
    public void scrollTo(MapNode node) {
        if (!isShown(node)) {
            centerTo(node.getLocation());
        }
    }

    /**
     * Centers the map to center of edge
     *
     * @param edge the edge
     */
    public void scrollTo(MapEdge edge) {
        if (!isShown(edge)) {
            centerTo(
                    Algebra.prod(
                            Algebra.sum(edge.getBeginLocation(), edge.getEndLocation()),
                            0.5));
        }
    }

    /**
     * @param status the status
     */
    public void setStatus(StatusView status) {
        this.status = status;
        repaint();
    }

    /**
     * @param trafficView the traffic view
     */
    public void setTrafficView(final boolean trafficView) {
        this.trafficView = trafficView;
        repaint();
    }

    /**
     * Sets the viewport location
     *
     * @param location the offset location (m)
     */
    public void setViewLocation(Point2D location) {
        viewportLocation = location;
        reset();
    }

    /**
     * Sets the viewport location
     *
     * @param x the x offset coordinate (m)
     * @param y the y offset coordinate (m)
     */
    public void setViewLocation(double x, double y) {
        setViewLocation(new Point2D.Double(x, y));
    }

    /**
     *
     */
    public void startCenterMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.currentMode = centerMode;
        clearSelection();
        repaint();
    }

    /**
     * Starts the selection of begin node of an edge
     *
     * @param edge the edge
     */
    public void startEdgeBeginNodeMode(MapEdge edge) {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.currentMode = changeBeginEdgeMode;
        this.end = edge.getEndLocation();
        this.changingEdge = edge;
        repaint();
    }

    /**
     * Starts the selection of end node of an edge
     *
     * @param edge the edge
     */
    public void startEdgeEndNodeMode(MapEdge edge) {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.currentMode = changeEndEdgeMode;
        this.begin = edge.getBeginLocation();
        this.changingEdge = edge;
        repaint();
    }

    /**
     * Starts the selection of begin node of a new edge
     */
    public void startEdgeMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.currentMode = startEdgeMode;
        clearSelection();
    }

    /**
     * @param module the mapModule
     */
    public void startModuleMode(final MapModule module) {
        this.mapModule = module;
        this.currentMode = moduleLocationMode;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        clearSelection();
    }

    /**
     *
     */
    public void startSelectMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        this.currentMode = selectingMode;
        clearSelection();
        repaint();
    }

    public void zoomIn() {
        Dimension size = getSize();
        Point pivot = new Point(size.width / 2, size.height / 2);
        double newScale = min(this.scale * SCALE_FACTOR, MAX_SCALE);
        scale(pivot, newScale);
    }

    public void zoomOut() {
        Dimension size = getSize();
        Point pivot = new Point(size.width / 2, size.height / 2);
        double newScale = max(this.scale / SCALE_FACTOR, MIN_SCALE);
        scale(pivot, newScale);
    }

    interface Mode {

        void handleMouseMoved(final MouseEvent ev);

        void handleMousePressed(final MouseEvent ev);

        void paintMode();
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

    public static class TerminalEdgeChange {
        private final MapEdge edge;
        private final Point2D terminal;

        public TerminalEdgeChange(MapEdge edge, Point2D terminal) {
            this.edge = edge;
            this.terminal = terminal;
        }

        public MapEdge getEdge() {
            return edge;
        }

        public Point2D getTerminal() {
            return terminal;
        }
    }

    public static class ModuleParameters {
        private final MapModule module;
        private final Point2D location;
        private final Point2D direction;

        public ModuleParameters(MapModule module, Point2D location, Point2D direction) {
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

        public MapModule getModule() {
            return module;
        }
    }
}
