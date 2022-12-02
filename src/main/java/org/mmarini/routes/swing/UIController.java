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

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;
import org.mmarini.routes.model2.*;
import org.mmarini.routes.model2.yaml.Parsers;
import org.mmarini.routes.model2.yaml.RouteDocBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.getValue;
import static org.mmarini.routes.model2.Constants.*;
import static org.mmarini.routes.model2.Topology.createTopology;
import static org.mmarini.routes.model2.TrafficEngineImpl.createEngine;
import static org.mmarini.routes.model2.TrafficEngineImpl.createRandom;
import static org.mmarini.routes.swing.RouteMap.TerminalEdgeChange;
import static org.mmarini.routes.swing.UIConstants.*;
import static org.mmarini.yaml.Utils.fromFile;
import static org.mmarini.yaml.Utils.fromResource;

/**
 * The UIController manages the user views and the user action.
 * It creates workflow between the user events (by reactive interface)
 * and the changes on the data views
 *
 * @author marco.marini@mmarini.org
 */
public class UIController {
    public static final double GAMMA = 0.9;
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    private final JFileChooser fileChooser;
    private final OptimizePane optimizePane;
    private final RoutePane routesPane;
    private final MapProfilePane mapProfilePane;
    private final FrequencyPane frequencyPane;
    private final RouteMap routeMap;
    private final MainFrame mainFrame;
    private final MapViewPane mapViewPane;
    private final ExplorerPane explorerPane;
    private final MapElementPane mapElementPane;
    private final EdgePane edgePane;
    private final NodePane nodePane;
    private final SitePane sitePane;
    private final ScrollMap scrollMap;
    private final FrequencyMeter fpsMeter;
    private final FrequencyMeter tpsMeter;
    private final InfoPane infoPane;
    private final Random random;
    private final SimulatorEngine<Status, TrafficEngine> simulator;
    private final ConnectionsPane connectionPane;
    private boolean running;
    private StatusView statusView;
    private int edgePriority;
    private double avgSpeed;

    /**
     *
     */
    public UIController() {
        random = new Random();
        routeMap = new RouteMap();
        this.scrollMap = new ScrollMap(this.routeMap);
        infoPane = new InfoPane();
        mapViewPane = new MapViewPane(scrollMap, infoPane);
        explorerPane = new ExplorerPane();
        connectionPane = new ConnectionsPane();
        TrafficEngine initialSeed = createEngine(DEFAULT_MAX_VEHICLES,
                createTopology(List.of(), List.of()),
                0,
                List.of(),
                DEFAULT_SPEED_LIMIT_MPS,
                DEFAULT_FREQUENCY,
                new double[0][0]);
        simulator = SimulatorEngineImpl.<Status, TrafficEngine>create(
                initialSeed,
                this::performTimeTick,
                TrafficEngine::buildStatus
        ).setEventInterval(Duration.ofNanos(NANOSPS / FPS));


        mapElementPane = new MapElementPane();
        this.edgePane = mapElementPane.getEdgePane();
        this.nodePane = mapElementPane.getMapNodePane();
        this.sitePane = mapElementPane.getSiteNodePane();

        mapProfilePane = new MapProfilePane();
        frequencyPane = new FrequencyPane();
        routesPane = new RoutePane();
        fileChooser = new JFileChooser();

        mainFrame = new MainFrame(mapViewPane, mapElementPane, explorerPane);
        this.fpsMeter = FrequencyMeter.create();
        this.tpsMeter = FrequencyMeter.create();

        //$NON-NLS-1$
        optimizePane = new OptimizePane();
        fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("RouteMediator.filetype.title"), //$NON-NLS-1$
                "yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$
        init();
    }

    /**
     * @param point the point
     */
    public void centerMap(final Point2D point) {
        simulator.request(engine -> engine.setOffset(point))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    routeMap.reset();
                    mapViewPane.selectSelector();
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     * Change the edge
     *
     * @param edge the new edge properties
     */
    private void changeEdge(EdgeView edge) {
        MapEdge oldEdge = edge.getEdge();
        MapEdge newEdge = oldEdge.setSpeedLimit(edge.getSpeedLimit())
                .setPriority(edge.getPriority());
        simulator.request(engine -> engine.changeEdge(oldEdge, newEdge))
                .doOnSuccess(engine -> statusView = createStatusView(engine.buildStatus()))
                .subscribe();
    }

    /**
     * @param edge the edge
     */
    private void createEdge(RouteMap.EdgeCreation edge) {
        MapNode begin = statusView.findNode(edge.getBegin(), PRECISION)
                .orElseGet(() -> new CrossNode(edge.getBegin()));
        MapNode end = statusView.findNode(edge.getEnd(), PRECISION)
                .orElseGet(() -> new CrossNode(edge.getEnd()));
        double speed = min(statusView.getStatus().getSpeedLimit(), computeSafetySpeed(end.getLocation().distance(begin.getLocation())));
        MapEdge edge1 = new MapEdge(begin, end, speed, edgePriority);
        simulator.request(engine -> engine.addEdge(edge1))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    routeMap.reset();
                    routeMap.setSelectedElement(edge1);
                    statusView.getEdgeViews(edge1).ifPresent(mapElementPane::setSelectedEdge);
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     *
     */
    private void createFlows() {
        mainFrame.getInfosFlowable().doOnNext(e -> showInfo()).subscribe();
        mainFrame.getVehicleInfoFlowable().doOnNext(e -> showTrafficInfo()).subscribe();
        mainFrame.getStopFlowable().doOnNext(e -> toggleSimulation()).subscribe();
        mainFrame.getSimSpeedFlowable().doOnNext(this::setSpeedSimulation).subscribe();
        mainFrame.getNewMapFlowable().doOnNext(e -> newMap()).subscribe();
        mainFrame.getOpenMapFlowable().doOnNext(e -> open()).subscribe();
        mainFrame.getSaveMapFlowable().doOnNext(e -> save()).subscribe();
        mainFrame.getExitFlowable().doOnNext(e -> System.exit(0)).subscribe();
        mainFrame.getOptimizeFlowable().doOnNext(e -> optimize()).subscribe();
        mainFrame.getRandomizeFlowable().doOnNext(e -> randomize()).subscribe();
        mainFrame.getFrequencyFlowable().doOnNext(e -> setFrequency()).subscribe();
        mainFrame.getLatticeFlowable().doOnNext(e -> generateConnections()).subscribe();
        mainFrame.getRoutesFlowable().doOnNext(e -> setRouteSetting()).subscribe();
        mainFrame.getNewRandomFlowable().doOnNext(e -> newRandomMap()).subscribe();
        mainFrame.getSaveAsFlowable().doOnNext(e -> saveAs()).subscribe();
        mainFrame.getWindowFlowable()
                .filter(e -> e.getID() == WindowEvent.WINDOW_OPENED)
                .doOnNext(e -> {
                    logger.info("Window opened");
                    start();
                })
                .subscribe();

        edgePane.getChangeFlowable().doOnNext(this::changeEdge).subscribe();

        edgePane.getDeleteFlowable()
                .map(EdgeView::getEdge)
                .doOnNext(this::remove)
                .subscribe();
        edgePane.getBeginNodeFlowable()
                .map(EdgeView::getEdge)
                .doOnNext(routeMap::startEdgeBeginNodeMode)
                .subscribe();
        edgePane.getEndNodeFlowable()
                .map(EdgeView::getEdge)
                .doOnNext(routeMap::startEdgeEndNodeMode)
                .subscribe();

        nodePane.getChangeFlowable()
                .map(NodeView::getNode)
                .doOnNext(this::transformToSite)
                .subscribe();
        nodePane.getDeleteFlowable()
                .map(NodeView::getNode)
                .doOnNext(this::remove)
                .subscribe();

        sitePane.getChangeFlowable()
                .map(node -> (SiteNode) node.getNode())
                .doOnNext(this::transformToNode)
                .subscribe();
        sitePane.getDeleteFlowable()
                .map(NodeView::getNode)
                .doOnNext(this::remove)
                .subscribe();

        explorerPane.getSiteFlowable().doOnNext(site -> {
            handleSiteSelection(site);
            routeMap.setSelectedElement(site);
            scrollMap.scrollTo(site);
        }).subscribe();
        explorerPane.getNodeFlowable().doOnNext(node -> {
            statusView.getNodeView(node).ifPresent(mapElementPane::setSelectedNode);
            routeMap.setSelectedElement(node);
            scrollMap.scrollTo(node);
        }).subscribe();
        explorerPane.getEdgeFlowable().doOnNext(edge -> {
            handleEdgeSelection(edge.getEdge());
            routeMap.setSelectedElement(edge.getEdge());
            scrollMap.scrollTo(edge.getEdge());
        }).subscribe();

        mapViewPane.getZoomInFlowable().doOnNext(ev -> {
            scrollMap.zoomIn();
            infoPane.setGridSize(routeMap.getGridSize());
        }).subscribe();
        mapViewPane.getZoomOutFlowable().doOnNext(ev -> {
            scrollMap.zoomOut();
            infoPane.setGridSize(routeMap.getGridSize());
        }).subscribe();
        mapViewPane.getFitInWindowFlowable().doOnNext(ev -> {
            scrollMap.scaleToFit();
            infoPane.setGridSize(routeMap.getGridSize());
        }).subscribe();
        mapViewPane.getZoomDefaultFlowable().doOnNext(ev -> {
            routeMap.setScale(1);
            infoPane.setGridSize(routeMap.getGridSize());
        }).subscribe();
        mapViewPane.getNormalViewFlowable().doOnNext(ev ->
                        routeMap.setTrafficView(false))
                .subscribe();
        mapViewPane.getTrafficViewFlowable()
                .doOnNext(ev ->
                        routeMap.setTrafficView(true))
                .subscribe();
        mapViewPane.getSelectFlowable().doOnNext(ev ->
                        routeMap.startSelectMode())
                .subscribe();
        mapViewPane.getEdgeFlowable().doOnNext(ev ->
                        routeMap.startEdgeMode())
                .subscribe();
        mapViewPane.getModuleFlowable()
                .doOnNext(routeMap::startModuleMode)
                .subscribe();
        mapViewPane.getCenterFlowable().doOnNext(ev ->
                        routeMap.startCenterMode())
                .subscribe();

        routeMap.getSelectElementFlowable().doOnNext(this::handleElementSelection).subscribe();
        routeMap.getUnselectFlowable().doOnNext(this::handleMapUnselecting).subscribe();
        routeMap.getDeleteEdgeFlowable().doOnNext(this::remove).subscribe();
        routeMap.getDeleteNodeFlowable().doOnNext(this::remove).subscribe();
        routeMap.getCenterMapFlowable().doOnNext(this::centerMap).subscribe();
        routeMap.getNewEdgeFlowable().doOnNext(this::createEdge).subscribe();
        routeMap.getNewModuleFlowable().doOnNext(this::createModule).subscribe();
        routeMap.getEndEdgeChangeFlowable().doOnNext(this::handleChangeEdgeEnd).subscribe();
        routeMap.getBeginEdgeChangeFlowable().doOnNext(this::handleChangeEdgeBegin).subscribe();

        routeMap.getMouseWheelFlowable().doOnNext(this::handleMouseWheelMoved).subscribe();
        routeMap.getMouseFlowable()
                .filter(ev -> ev.getID() == MouseEvent.MOUSE_MOVED)
                .doOnNext(ev -> {
                    final Point pt = routeMap.getMousePosition();
                    if (pt != null) {
                        Point2D mapPoint = routeMap.computeMapLocation(pt);
                        infoPane.setMapPoint(mapPoint);
                    }
                    infoPane.setEdgeLegend(routeMap.isSelectingEdge());
                    infoPane.setEdgeLength(routeMap.getEdgeLength());
                })
                .subscribe();

        fpsMeter.getFlowable().doOnNext(infoPane::setFps).subscribe();
        tpsMeter.getFlowable().doOnNext(infoPane::setTps).subscribe();
        simulator.setOnSpeed(s -> {
            avgSpeed = avgSpeed * GAMMA + s * (1 - GAMMA);
            infoPane.setSpeed(avgSpeed);
        });

        simulator.setOnEvent(status -> {
            if (!status.equals(statusView.getStatus())) {
                statusView = createStatusView(status);
                fpsMeter.tick();
                refresh();
            }
        });
    }

    /**
     * @param moduleParameters the module parameters
     */
    private void createModule(RouteMap.ModuleParameters moduleParameters) {
        simulator.request(engine -> engine.addModule(moduleParameters.getModule(),
                moduleParameters.getLocation(),
                moduleParameters.getDirection(),
                MAX_PRECISION_DISTANCE)).doOnSuccess(engine -> {
            statusView = createStatusView(engine.buildStatus());
            refreshTopology();
            routeMap.reset();
            mapViewPane.selectSelector();
            mainFrame.repaint();
        }).subscribe();
    }

    StatusView createStatusView(Status status) {
        return statusView != null ? statusView.update(status) : StatusView.createStatusView(status);
    }

    private void generateConnections() {
        final int opt = JOptionPane.showConfirmDialog(mainFrame, connectionPane,
                Messages.getString("UIController.latticePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            simulator.request(engine -> engine.generateConnections(connectionPane.getSelectedBuilder()))
                    .doOnSuccess(engine -> {
                        statusView = createStatusView(engine.buildStatus());
                        refreshTopology();
                        routeMap.reset();
                    }).subscribe();
        }
    }

    /**
     * Handles the changing of edge begin
     *
     * @param change the change event
     */
    private void handleChangeEdgeBegin(TerminalEdgeChange change) {
        MapEdge edge = change.getEdge();
        Point2D terminal = change.getTerminal();
        MapNode begin = statusView.findNode(terminal, computePrecisionDistance(routeMap.getScale()))
                .orElseGet(() -> new CrossNode(terminal));
        MapEdge newEdge = edge.setBegin(begin);
        simulator.request(engine -> engine.changeEdge(edge, newEdge))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    mapViewPane.selectSelector();
                    routeMap.reset();
                    statusView.getEdgeViews(newEdge).ifPresent(mapElementPane::setSelectedEdge);
                    routeMap.setSelectedElement(newEdge);
                    mainFrame.repaint();
                }).subscribe();
    }


    /**
     * Handles the changing of edge end
     *
     * @param change the change event
     */
    private void handleChangeEdgeEnd(TerminalEdgeChange change) {
        MapEdge edge = change.getEdge();
        Point2D terminal = change.getTerminal();
        MapNode end = statusView.findNode(terminal, computePrecisionDistance(routeMap.getScale()))
                .orElseGet(() -> new CrossNode(terminal));
        MapEdge newEdge = edge.setEnd(end);
        simulator.request(engine -> engine.changeEdge(edge, newEdge))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    mapViewPane.selectSelector();
                    routeMap.reset();
                    statusView.getEdgeViews(newEdge).ifPresent(mapElementPane::setSelectedEdge);
                    routeMap.setSelectedElement(newEdge);
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     * @param edge the edge
     */
    private void handleEdgeSelection(MapEdge edge) {
        requireNonNull(edge);
        edgePriority = edge.getPriority();
        statusView.getEdgeViews(edge).ifPresent(mapElementPane::setSelectedEdge);
    }

    /**
     * @param edge the edge
     */
    private void handleEdgeSelection1(MapEdge edge) {
        this.handleEdgeSelection(edge);
        statusView.getEdgeViews(edge).ifPresentOrElse(
                explorerPane::setSelectedEdge,
                explorerPane::clearSelection);
    }

    /**
     * @param mapElement the selected element
     */
    private void handleElementSelection(MapElement mapElement) {
        mapElement.apply(new MapElementVisitor<Void>() {
            @Override
            public Void visit(MapEdge edge) {
                handleEdgeSelection1(edge);
                return null;
            }

            @Override
            public Void visit(CrossNode node) {
                handleNodeSelection(node);
                return null;
            }

            @Override
            public Void visit(SiteNode node) {
                handleSiteSelection1(node);
                return null;
            }
        });
    }

    /**
     * @param element the unselect element
     */
    private void handleMapUnselecting(MapElement element) {
        mapElementPane.clearPanel();
        explorerPane.clearSelection();
    }

    private void handleMouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        final double scale = Math.pow(SCALE_FACTOR, mouseWheelEvent.getWheelRotation());
        scrollMap.scale(mouseWheelEvent.getPoint(), routeMap.getScale() * scale);
        infoPane.setGridSize(routeMap.getGridSize());
    }

    /**
     * @param node the node
     */
    private void handleNodeSelection(MapNode node) {
        Optional<NodeView> entry = statusView.getNodeView(node);
        entry.ifPresentOrElse(mapElementPane::setSelectedNode, mapElementPane::clearPanel);
        entry.ifPresentOrElse(explorerPane::setSelectedNode, explorerPane::clearSelection);
    }

    /**
     * @param site the site
     */
    private void handleSiteSelection(SiteNode site) {
        requireNonNull(site);
        statusView.getNodeView(site).ifPresent(mapElementPane::setSelectedSite);
    }

    private void handleSiteSelection1(SiteNode site) {
        this.handleSiteSelection(site);
        statusView.getNodeView(site).ifPresentOrElse(
                explorerPane::setSelectedNode,
                explorerPane::clearSelection);
    }

    /**
     *
     */
    private void init() {
        loadDefault();
        List<MapModule> modules = loadModules();
        mapViewPane.setModule(modules);
        routeMap.reset();
        createFlows();
        refreshTopology();
    }

    /**
     *
     */
    private void loadDefault() {
        final URL url = getClass().getResource("/test.yml"); //$NON-NLS-1$
        if (url != null) {
            try {
                JsonNode doc = fromResource("/test.yml");
                StatusImpl status = Parsers.parseStatus(doc);
                statusView = createStatusView(status);
                TrafficEngineImpl seed = createEngine(DEFAULT_MAX_VEHICLES,
                        status.getTopology(),
                        status.getTime(),
                        List.of(),
                        status.getSpeedLimit(),
                        status.getFrequency(),
                        status.getWeightMatrix().getValues());
                simulator.pushSeed(seed);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns the modules loaded from modules path
     */
    private List<MapModule> loadModules() {
        final File path = new File("modules");
        if (path.isDirectory()) {
            Map<String, MapModule> moduleByName = Optional.ofNullable(path.listFiles())
                    .stream()
                    .flatMap(Arrays::stream)
                    .filter(file -> file.isFile() && file.canRead() && file.getName().endsWith(".yml"))
                    .flatMap(file -> {
                        try {
                            JsonNode doc = fromFile(file);
                            MapModule module = Parsers.parseModule(doc);
                            return Stream.of(Tuple2.of(file.getName(), module));
                        } catch (IOException ex) {
                            return Stream.empty();
                        }
                    })
                    .collect(Tuple2.toMap());
            return moduleByName.keySet().stream()
                    .sorted().
                    flatMap(name -> getValue(moduleByName, name).stream())
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    /**
     *
     */
    private void newMap() {
        Topology t = createTopology(List.of(), List.of());
        TrafficEngineImpl engine = createEngine(DEFAULT_MAX_VEHICLES, t, 0, List.of(),
                DEFAULT_SPEED_LIMIT_KMH / KMPHSPM,
                DEFAULT_FREQUENCY);
        routeMap.startSelectMode();
        mapElementPane.clearPanel();
        simulator.pushSeed(engine)
                .doOnSuccess(engine1 -> {
                    statusView = createStatusView(engine1.buildStatus());
                    refreshTopology();
                    mapViewPane.selectSelector();
                    routeMap.reset();
                    routeMap.clearSelection();
                    mapElementPane.clearPanel();
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     *
     */
    private void newRandomMap() {
        mapProfilePane.setDifficultyOnly(false);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
                Messages.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final MapProfile profile = mapProfilePane.getProfile();
            TrafficEngineImpl randomStatus = createRandom(DEFAULT_MAX_VEHICLES, random, profile,
                    DEFAULT_SPEED_LIMIT_MPS);
            routeMap.startSelectMode();
            mapElementPane.clearPanel();
            simulator.pushSeed(randomStatus)
                    .doOnSuccess(engine -> {
                        statusView = createStatusView(engine.buildStatus());
                        refreshTopology();
                        mapViewPane.selectSelector();
                        routeMap.reset();
                        routeMap.clearSelection();
                        mapElementPane.clearPanel();
                        mainFrame.repaint();
                    }).subscribe();
        }
    }

    /**
     *
     */
    private void open() {
        final int choice = fileChooser.showOpenDialog(mainFrame);
        if (choice == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            if (!file.canRead()) {
                showError(Messages.getString("RouteMediator.readError.message"), new Object[]{file}); //$NON-NLS-1$
            } else {
                try {
                    JsonNode doc = fromFile(file);
                    StatusImpl status = Parsers.parseStatus(doc);
                    mainFrame.setSaveActionEnabled(true);
                    mainFrame.setTitle(file.getName());
                    TrafficEngineImpl seed = createEngine(status.getMaxVehicle(),
                            status.getTopology(),
                            status.getTime(),
                            List.of(),
                            status.getSpeedLimit(),
                            status.getFrequency(),
                            status.getWeightMatrix().getValues());
                    routeMap.startSelectMode();
                    mapElementPane.clearPanel();
                    simulator.pushSeed(seed)
                            .doOnSuccess(engine -> {
                                statusView = createStatusView(engine.buildStatus());
                                refreshTopology();
                                routeMap.reset();
                            }).subscribe();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                    showError(e.getMessage());
                } catch (final Throwable e) {
                    logger.error(e.getMessage(), e);
                    showError(e);
                }
            }
        }
    }

    /**
     *
     */
    private void optimize() {
        optimizePane.setSpeedLimit(statusView.getStatus().getSpeedLimit());
        final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
                Messages.getString("RouteMediator.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final double speedLimit = optimizePane.getSpeedLimit();
            final boolean optimizeSpeed = optimizePane.isOptimizeSpeed();
            simulator.request(engine -> (optimizeSpeed ? engine.optimizeSpeed(speedLimit) : engine).optimizeNodes())
                    .doOnSuccess(engine -> {
                        statusView = createStatusView(engine.buildStatus());
                        refreshTopology();
                        routeMap.reset();
                    }).subscribe();
        }

    }

    /**
     * Returns the engine at a time interval from an engine
     *
     * @param engine the engine
     * @param dt     the time interval
     */
    private Tuple2<TrafficEngine, Double> performTimeTick(TrafficEngine engine, double dt) {
        tpsMeter.tick();
        return dt > 0.0
                ? engine.next(random, dt)
                : Tuple2.of(engine, dt);
    }

    /**
     * Randomize the traffic generator
     */
    private void randomize() {
        mapProfilePane.setDifficultyOnly(true);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
                Messages.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final MapProfile profile = mapProfilePane.getProfile();
            simulator.request(engine -> engine.setFrequency(profile.getFrequency())
                            .randomizeWeights(random, profile.getMinWeight()))
                    .doOnSuccess(engine -> {
                        statusView = createStatusView(engine.buildStatus());
                        refreshTopology();
                        mapViewPane.selectSelector();
                        routeMap.reset();
                        mainFrame.repaint();
                    }).subscribe();
        }
    }

    /**
     *
     */
    private void refresh() {
        routeMap.setStatus(statusView);
        infoPane.setNumVehicles(statusView.getVehicles().size());
        infoPane.setTime(round(statusView.getStatus().getTime()));
        scrollMap.repaint();
    }

    /**
     *
     */
    private void refreshTopology() {
        final DefaultListModel<EdgeView> nl = explorerPane.getEdgeListModel();
        nl.removeAllElements();
        nl.addAll(statusView.getEdgesViews());
        final DefaultListModel<NodeView> el = explorerPane.getNodeListModel();
        el.removeAllElements();
        el.addAll(statusView.getNodeViews());
        routeMap.setStatus(statusView);
        scrollMap.repaint();
    }

    /**
     * @param edge the edge
     */
    public void remove(final MapEdge edge) {
        simulator.request(engine -> engine.removeEdge(edge))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    routeMap.clearSelection();
                    mapElementPane.clearPanel();
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     * @param node the node
     */
    public void remove(final MapNode node) {
        simulator.request(engine -> engine.removeNode(node))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    routeMap.clearSelection();
                    mapElementPane.clearPanel();
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     *
     */
    private void save() {
        final File file = fileChooser.getSelectedFile();
        if (file.exists() && !file.canWrite()) {
            showError(Messages.getString("RouteMediator.writeError.message"), new Object[]{file}); //$NON-NLS-1$
        } else {
            try {
                RouteDocBuilder.write(file, statusView.getStatus());
                mainFrame.setSaveActionEnabled(true);
                mainFrame.setTitle(file.getPath());
            } catch (final Throwable e) {
                logger.error(e.getMessage(), e);
                showError(e);
            }
        }
    }

    /**
     *
     */
    private void saveAs() {
        final int choice = fileChooser.showSaveDialog(mainFrame);
        if (choice == JFileChooser.APPROVE_OPTION) {
            save();
        }
    }

    /**
     *
     */
    private void setFrequency() {
        frequencyPane.setFrequency(statusView.getStatus().getFrequency());
        final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencyPane,
                Messages.getString("RouteMediator.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            double frequency = frequencyPane.getFrequence();
            simulator.request(engine -> engine.setFrequency(frequency))
                    .doOnSuccess(engine -> statusView = createStatusView(engine.buildStatus()))
                    .subscribe();
        }
    }

    /**
     *
     */
    public void setRouteSetting() {
        DoubleMatrix<NodeView> weights = statusView.getWeightMatrix();
        routesPane.setPathEntry(weights);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, routesPane,
                Messages.getString("RouteMediator.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final DoubleMatrix<NodeView> weights1 = routesPane.getPathEntry();
            simulator.request(engine -> engine.setWeights(weights1.getValues()))
                    .doOnSuccess(engine -> {
                        statusView = createStatusView(engine.buildStatus());
                        refresh();
                        mapViewPane.selectSelector();
                        routeMap.reset();
                        mainFrame.repaint();
                    }).subscribe();
        }
    }

    /**
     * @param speedSimulation the speed simulation
     */
    public void setSpeedSimulation(final double speedSimulation) {
        simulator.setSpeed(speedSimulation);
    }

    /**
     * @param message the message
     */
    private void showError(final String message) {
        JOptionPane.showMessageDialog(mainFrame, message, Messages.getString("RouteMediator.error.title"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @param pattern   the message pattern
     * @param arguments the arguments
     */
    private void showError(final String pattern, final Object[] arguments) {
        showError(MessageFormat.format(pattern, arguments));
    }

    /**
     * @param e the error
     */
    private void showError(final Throwable e) {
        logger.error(e.getMessage(), e);
        showError("{0}", new Object[]{e.getMessage(), //$NON-NLS-1$
                e.getMessage()});
    }

    /**
     *
     */
    private void showInfo() {
        DoubleMatrix<NodeView> frequencies = statusView.getFrequencies();
        final InfosTable table = InfosTable.create(frequencies);
        final JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JOptionPane.showMessageDialog(mainFrame, sp, Messages.getString("RouteMediator.infoPane.title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *
     */
    private void showTrafficInfo() {
        StatusView sv = statusView;
        final List<TrafficInfo> map = sv.getTrafficInfo();
        final List<TrafficInfoView> data = map.stream().flatMap(info ->
                        sv.getNodeView(info.getSite())
                                .map(dest ->
                                        new TrafficInfoView(dest, info))
                                .stream())
                .collect(Collectors.toList());
        final TrafficInfoModel model = new TrafficInfoModel();
        model.setInfo(data);
        final TrafficInfoTable table = new TrafficInfoTable(model);
        final Component pane = new JScrollPane(table);
        JOptionPane.showMessageDialog(mainFrame, pane, Messages.getString("RouteMediator.trafficInfoPane.title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *
     */
    private void start() {
        scrollMap.scaleToFit();
        infoPane.setGridSize(routeMap.getGridSize());
        setSpeedSimulation(1f);
        startSimulation();
    }

    /**
     *
     */
    public void startApp() {
        mainFrame.setVisible(true);
    }

    /**
     *
     */
    private void startSimulation() {
        if (!running) {
            running = true;
            fpsMeter.reset();
            tpsMeter.reset();
            simulator.start();
        }
//        start = System.nanoTime();
    }

    /**
     *
     */
    private void stopSimulation() {
        simulator.stop();
        running = false;
    }

    /**
     *
     */
    private void toggleSimulation() {
        if (running) {
            stopSimulation();
        } else {
            startSimulation();
        }
    }

    /**
     * @param site the site
     */
    public void transformToNode(final SiteNode site) {
        simulator.request(engine -> engine.changeNode(site))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    Optional<CrossNode> node = statusView.findNode(site.getLocation(), PRECISION)
                            .map(n -> (CrossNode) n);
                    routeMap.reset();
                    node.ifPresent(routeMap::setSelectedElement);
                    node.flatMap(statusView::getNodeView).ifPresent(mapElementPane::setSelectedNode);
                    mainFrame.repaint();
                }).subscribe();
    }

    /**
     * @param node the node
     */
    public void transformToSite(final MapNode node) {
        simulator.request(engine -> engine.changeNode(node))
                .doOnSuccess(engine -> {
                    statusView = createStatusView(engine.buildStatus());
                    refreshTopology();
                    Optional<SiteNode> site = statusView.findNode(node.getLocation(), PRECISION)
                            .map(n -> (SiteNode) n);
                    routeMap.reset();
                    site.ifPresent(routeMap::setSelectedElement);
                    site.flatMap(statusView::getNodeView).ifPresent(mapElementPane::setSelectedSite);
                    mainFrame.repaint();
                }).subscribe();
    }
}