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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import hu.akarnokd.rxjava3.swing.SwingSchedulers;
import org.mmarini.Tuple2;
import org.mmarini.routes.model2.*;
import org.mmarini.routes.model2.yaml.ModuleAST;
import org.mmarini.routes.model2.yaml.RouteAST;
import org.mmarini.routes.model2.yaml.RouteDocBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.reactivex.rxjava3.core.Observable.interval;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.getValue;
import static org.mmarini.routes.model2.Constants.PRECISION;
import static org.mmarini.routes.model2.Constants.computeSafetySpeed;
import static org.mmarini.routes.model2.StatusImpl.createStatus;
import static org.mmarini.routes.model2.Topology.createTopology;
import static org.mmarini.routes.swing.StatusView.createStatusView;
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

    public static final float SECS_PER_NANO = 1e-9f;
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    private static final int TIME_INTERVAL = 1;

    private final JFileChooser fileChooser;
    private final OptimizePane optimizePane;
    private final RoutePane routesPane;
    private final NodeChooser nodeChooser;
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
    private final Random random;
    private long start;
    private double speedSimulation;
    private boolean running;
    private StatusView statusView;
    private Status status;
    private int edgePriority;

    /**
     *
     */
    public UIController() {
        random = new Random();
        routeMap = new RouteMap();
        this.scrollMap = new ScrollMap(this.routeMap);
        mapViewPane = new MapViewPane(scrollMap);
        explorerPane = new ExplorerPane();

        mapElementPane = new MapElementPane();
        this.edgePane = mapElementPane.getEdgePane();
        this.nodePane = mapElementPane.getMapNodePane();
        this.sitePane = mapElementPane.getSiteNodePane();

        mapProfilePane = new MapProfilePane();
        frequencyPane = new FrequencyPane();
        routesPane = new RoutePane();
        fileChooser = new JFileChooser();
//        handler = new RouteHandler();
        nodeChooser = new NodeChooser();
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
        UnaryOperator<Status> tx = s -> {
            status = s.setOffset(point);
            // handler.centerMap(point);
            mapViewPane.reset();
            mapViewPane.selectSelector();
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
    }

    /**
     * @param edge the edge
     */
    public void changeBeginNode(final MapEdge edge) {
        Optional<MapNode> mapNode = chooseNode();
        mapNode.ifPresent(node -> {
            MapEdge newEdge = edge.setBegin(node);
            UnaryOperator<Status> tx = s -> {
                status = s.changeEdge(edge, newEdge);
                refreshTopology();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                statusView.getEdgeViews(newEdge).ifPresent(mapElementPane::setSelectedEdge);
                mapViewPane.setSelectedEdge(newEdge);
                mainFrame.repaint();
                return status;
            };
            tx.apply(status);
        });
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
        UnaryOperator<Status> tx = s -> {
            status = s.changeEdge(oldEdge, newEdge);
            return status;
        };
        tx.apply(status);
    }

    /**
     * @param edge the edge
     */
    public void changeEndNode(final MapEdge edge) {
        chooseNode().ifPresent(node -> {
            MapEdge newEdge = edge.setEnd(node);
            UnaryOperator<Status> tx = s -> {
                status = s.changeEdge(edge, newEdge);
                refreshTopology();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                statusView.getEdgeViews(newEdge).ifPresent(mapElementPane::setSelectedEdge);
                mapViewPane.setSelectedEdge(newEdge);
                mainFrame.repaint();
                return status;
            };
            tx.apply(status);
        });
    }

    /**
     *
     */
    private Optional<MapNode> chooseNode() {
        stopSimulation();
        nodeChooser.setNodeList(statusView.getNodeViews());
        final int opt = JOptionPane.showConfirmDialog(mainFrame, nodeChooser,
                Messages.getString("RouteMediator.nodeChooser.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        startSimulation();
        if (opt == JOptionPane.OK_OPTION) {
            return nodeChooser.getSelectedNode();
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param edge the edge
     */
    private void createEdge(RouteMap.EdgeCreation edge) {
        MapNode begin = statusView.findNode(edge.getBegin(), PRECISION)
                .orElseGet(() -> new CrossNode(edge.getBegin()));
        MapNode end = statusView.findNode(edge.getEnd(), PRECISION)
                .orElseGet(() -> new CrossNode(edge.getEnd()));
        double speed = min(status.getSpeedLimit(), computeSafetySpeed(end.getLocation().distance(begin.getLocation())));
        MapEdge edge1 = new MapEdge(begin, end, speed, edgePriority);
        UnaryOperator<Status> tx = s -> {
            status = s.addEdge(edge1);
            refreshTopology();
            mapViewPane.reset();
            mapViewPane.setSelectedEdge(edge1);
            statusView.getEdgeViews(edge1).ifPresent(mapElementPane::setSelectedEdge);
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
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
        mainFrame.getRoutesFlowable().doOnNext(e -> setRouteSetting()).subscribe();
        mainFrame.getNewRandomFlowable().doOnNext(e -> newRandomMap()).subscribe();
        mainFrame.getSaveAsFlowable().doOnNext(e -> saveAs()).subscribe();
        mainFrame.getWindowFlowable().doOnNext(e -> start()).subscribe();

        edgePane.getChangeFlowable().doOnNext(edg -> {
                    changeEdge(edg);
                    logger.info("change edge {}", edg);
//                    throw new Error("Not implemented");// TODO implement
                }
//                handler.changeEdgeProperties(edg.getEdge(), edg.getPriority(), edg.getSpeedLimit())
        ).subscribe();

        edgePane.getDeleteFlowable().doOnNext(edg -> remove(edg.getEdge())
        ).subscribe();
        edgePane.getBeginNodeFlowable().doOnNext(edg -> changeBeginNode(edg.getEdge())
        ).subscribe();
        edgePane.getEndNodeFlowable().doOnNext(edg -> changeEndNode(edg.getEdge())
        ).subscribe();

        nodePane.getChangeFlowable().doOnNext(node -> transformToSite(node.getNode()))
                .subscribe();
        nodePane.getDeleteFlowable().doOnNext(node -> remove(node.getNode()))
                .subscribe();

        sitePane.getChangeFlowable().doOnNext(node -> transformToNode((SiteNode) node.getNode()))
                .subscribe();
        sitePane.getDeleteFlowable().doOnNext(node -> remove(node.getNode()))
                .subscribe();

        explorerPane.getSiteFlowable().doOnNext(site -> {
            handleSiteSelection(site);
            mapViewPane.setSelectedSite(site);
            mapViewPane.scrollTo(site);
        }).subscribe();
        explorerPane.getNodeFlowable().doOnNext(node -> {
            statusView.getNodeView(node).ifPresent(mapElementPane::setSelectedNode);
            mapViewPane.setSelectedNode(node);
            mapViewPane.scrollTo(node);
        }).subscribe();
        explorerPane.getEdgeFlowable().doOnNext(edge -> {
            handleEdgeSelection(edge.getEdge());
            mapViewPane.setSelectedEdge(edge.getEdge());
            mapViewPane.scrollTo(edge.getEdge());
        }).subscribe();

        routeMap.getSelectElementFlowable().doOnNext(this::handleElementSelection).subscribe();
        routeMap.getUnselectFlowable().doOnNext(this::handleMapUnselecting).subscribe();
        routeMap.getDeleteEdgeFlowable().doOnNext(this::remove).subscribe();
        routeMap.getDeleteNodeFlowable().doOnNext(this::remove).subscribe();
        routeMap.getCenterMapFlowable().doOnNext(this::centerMap).subscribe();
        routeMap.getNewEdgeFlowable().doOnNext(this::createEdge).subscribe();
        routeMap.getNewModuleFlowable().doOnNext(this::createModule).subscribe();

        fpsMeter.getFlowable().doOnNext(scrollMap::setFps).subscribe();
        tpsMeter.getFlowable().doOnNext(scrollMap::setTps).subscribe();

        interval(TIME_INTERVAL, TimeUnit.MILLISECONDS, SwingSchedulers.edt())
                .doOnNext(t -> {
                    if (running) {
                        performTimeTick();
                    }
                }).subscribe();
    }

    /**
     * @param moduleParameters the module parameters
     */
    private void createModule(RouteMap.ModuleParameters moduleParameters) {
        UnaryOperator<Status> tx = s -> {
            status = s.addModule(moduleParameters.getModule(),
                    moduleParameters.getLocation(),
                    moduleParameters.getDirection(),
                    PRECISION);
            refreshTopology();
            mapViewPane.reset();
            mapViewPane.selectSelector();
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
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
        mapViewPane.reset();
        createFlows();
        refreshTopology();
    }

    /**
     *
     */
    private void loadDefault() {
        final URL url = getClass().getResource("/test.yml"); //$NON-NLS-1$
        if (url != null) {
            UnaryOperator<Status> tx = s -> {
                try {
                    JsonNode doc = fromResource("/test.yml");
                    status = new RouteAST(doc, JsonPointer.empty()).build();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return status;
            };
            tx.apply(status);
        }
    }

    private List<MapModule> loadModules() {
        final File path = new File("modules");
        if (path.isDirectory()) {
            Map<String, MapModule> moduleByName = Arrays.stream(path.listFiles())
                    .filter(file -> file.isFile() && file.canRead() && file.getName().endsWith(".yml"))
                    .flatMap(file -> {
                        try {
                            JsonNode doc = fromFile(file);
                            MapModule module = new ModuleAST(doc, JsonPointer.empty()).build();
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
        UnaryOperator<Status> tx = s -> {
            Topology t = createTopology(List.of(), List.of());
            status = createStatus(t, 0, List.of(), s.getSpeedLimit(), s.getFrequency());
            refreshTopology();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mapViewPane.clearSelection();
            mapElementPane.clearPanel();
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
    }

    /**
     *
     */
    private void newRandomMap() {
        mapProfilePane.setDifficultyOnly(false);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
                Messages.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            stopSimulation();
            final MapProfile profile = mapProfilePane.getProfile();
            UnaryOperator<Status> tx = s -> {
                status = StatusImpl.createRandom(random, profile, s.getSpeedLimit());
                refreshTopology();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                mapViewPane.clearSelection();
                mapElementPane.clearPanel();
                mainFrame.repaint();
                startSimulation();
                return status;
            };
            tx.apply(status);
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
                stopSimulation();
                UnaryOperator<Status> tx = s -> {
                    try {
                        JsonNode doc = fromFile(file);
                        status = new RouteAST(doc, JsonPointer.empty()).build();
                        mainFrame.setSaveActionEnabled(true);
                        mainFrame.setTitle(file.getName());
                    /*
                    handler.load(file);
                    mainFrame.setSaveActionEnabled(true);
                    mainFrame.setTitle(file.getName());

                } catch (final SAXParseException e) {
                    logger.error(e.getMessage(), e);
                    showError(Messages.getString("RouteMediator.parseError.message"), new Object[]{e.getMessage(), //$NON-NLS-1$
                            e.getLineNumber(), e.getColumnNumber()});
                     */
                    } catch (final Exception e) {
                        logger.error(e.getMessage(), e);
                        showError(e.getMessage());
                        status = s;
                    } catch (final Throwable e) {
                        logger.error(e.getMessage(), e);
                        showError(e);
                        status = s;
                    }
                    refreshTopology();
                    mapViewPane.reset();
                    startSimulation();
                    return status;
                };
                tx.apply(status);
            }
        }
    }

    /**
     *
     */
    private void optimize() {
        optimizePane.setSpeedLimit(status.getSpeedLimit());
        final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
                Messages.getString("RouteMediator.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final double speedLimit = optimizePane.getSpeedLimit();
            final boolean optimizeSpeed = optimizePane.isOptimizeSpeed();
            stopSimulation();
            UnaryOperator<Status> tx = s -> {
                status = (optimizeSpeed ? s.optimizeSpeed(speedLimit) : s).optimizeNodes();
                refreshTopology();
                mapViewPane.reset();
                startSimulation();
                return status;
            };
            tx.apply(status);
        }
    }

    /**
     *
     */
    private void performTimeTick() {
        final long now = System.nanoTime();
        long interval = now - start;
        start = now;
        if (interval > 0) {
            double dt = interval * SECS_PER_NANO * speedSimulation;
            status = status.next(random, dt);
            refresh();
            mapViewPane.repaint();
            fpsMeter.tick();
            tpsMeter.tick();
        }
    }

    /**
     * Randomize the traffic generator
     */
    private void randomize() {
        mapProfilePane.setDifficultyOnly(true);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
                Messages.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            stopSimulation();
            final MapProfile profile = mapProfilePane.getProfile();
            UnaryOperator<Status> tx = s -> {
                status = s.setFrequency(profile.getFrequency())
                        .randomizeWeights(random, profile.getMinWeight());
                refreshTopology();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                mainFrame.repaint();
                startSimulation();
                return status;
            };
            tx.apply(status);
        }
    }

    /**
     *
     */
    private void refresh() {
        this.statusView = createStatusView(status);
        routeMap.setStatus(statusView);
        scrollMap.setNumVehicles(statusView.getVehicles().size());
    }

    /**
     *
     */
    private void refreshTopology() {
        this.statusView = createStatusView(status);
        final DefaultListModel<EdgeView> nl = explorerPane.getEdgeListModel();
        nl.removeAllElements();
        nl.addAll(statusView.getEdgesViews());
        final DefaultListModel<NodeView> el = explorerPane.getNodeListModel();
        el.removeAllElements();
        el.addAll(statusView.getNodeViews());
        routeMap.setStatus(statusView);
    }

    /**
     * @param edge the edge
     */
    public void remove(final MapEdge edge) {
        UnaryOperator<Status> tx = s -> {
            status = s.removeEdge(edge);
            refreshTopology();
            mapViewPane.clearSelection();
            mapElementPane.clearPanel();
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
    }

    /**
     * @param node the node
     */
    public void remove(final MapNode node) {
        UnaryOperator<Status> tx = s -> {
            status = s.removeNode(node);
            refreshTopology();
            mapViewPane.clearSelection();
            mapElementPane.clearPanel();
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
    }

    /**
     *
     */
    private void save() {
        final File file = fileChooser.getSelectedFile();
        if (file.exists() && !file.canWrite()) {
            showError(Messages.getString("RouteMediator.writeError.message"), new Object[]{file}); //$NON-NLS-1$
        } else {
            stopSimulation();
            UnaryOperator<Status> tx = s -> {
                try {
                    RouteDocBuilder.write(file, s);
                    mainFrame.setSaveActionEnabled(true);
                    mainFrame.setTitle(file.getPath());
                    startSimulation();
                 /*
                handler.save(file);

             */
                } catch (final Throwable e) {
                    logger.error(e.getMessage(), e);
                    showError(e);
                }
                return s;
            };
            tx.apply(status);
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
        final double frequency = status.getFrequency();
        frequencyPane.setFrequency(frequency);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencyPane,
                Messages.getString("RouteMediator.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            stopSimulation();
            UnaryOperator<Status> tx = s -> {
                status = s.setFrequency(frequencyPane.getFrequence());
                refresh();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                mainFrame.repaint();
                startSimulation();
                return status;
            };
            tx.apply(status);
        }
    }

    /**
     *
     */
    public void setRouteSetting() {
        stopSimulation();
        DoubleMatrix<NodeView> weights = statusView.getWeightMatrix();
        routesPane.setPathEntry(weights);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, routesPane,
                Messages.getString("RouteMediator.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final DoubleMatrix<NodeView> weights1 = routesPane.getPathEntry();
            UnaryOperator<Status> tx = s -> {
                status = s.setWeights(weights1.getValues());
                refresh();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                mainFrame.repaint();
                return status;
            };
            tx.apply(status);
        }
        startSimulation();
    }

    /**
     * @param speedSimulation the speed simulation
     */
    public void setSpeedSimulation(final double speedSimulation) {
        this.speedSimulation = speedSimulation;
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
        stopSimulation();
        DoubleMatrix<NodeView> frequencies = statusView.getFrequencies();
        final InfosTable table = InfosTable.create(frequencies);
        final JScrollPane sp = new JScrollPane(table);
        JOptionPane.showMessageDialog(mainFrame, sp, Messages.getString("RouteMediator.infoPane.title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
        startSimulation();
    }

    /**
     *
     */
    private void showTrafficInfo() {
        stopSimulation();
        final List<TrafficInfo> map = statusView.getTrafficInfo();
        final List<TrafficInfoView> data = map.stream().flatMap(info ->
                        statusView.getNodeView(info.getDestination())
                                .map(dest ->
                                        new TrafficInfoView(dest, info))
                                .stream())
                .sorted(Comparator.comparing(a -> a.getDestination().getName()))
                .collect(Collectors.toList());
        final TrafficInfoModel model = new TrafficInfoModel();
        model.setInfo(data);
        final TrafficInfoTable table = new TrafficInfoTable(model);
        final Component pane = new JScrollPane(table);
        JOptionPane.showMessageDialog(mainFrame, pane, Messages.getString("RouteMediator.trafficInfoPane.title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
        startSimulation();
    }

    /**
     *
     */
    private void start() {
        mapViewPane.scaleToFit();
        setSpeedSimulation(1f);
        startSimulation();
    }

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
        }
        start = System.nanoTime();
    }

    /**
     *
     */
    private void stopSimulation() {
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
        UnaryOperator<Status> tx = s -> {
            status = s.changeNode(site);
            refreshTopology();
            Optional<CrossNode> node = statusView.findNode(site.getLocation(), PRECISION)
                    .map(n -> (CrossNode) n);
            mapViewPane.reset();
            node.ifPresent(mapViewPane::setSelectedNode);
            node.flatMap(statusView::getNodeView).ifPresent(mapElementPane::setSelectedNode);
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
    }

    /**
     * @param node the node
     */
    public void transformToSite(final MapNode node) {
        UnaryOperator<Status> tx = s -> {
            status = s.changeNode(node);
            refreshTopology();
            Optional<SiteNode> site = statusView.findNode(node.getLocation(), PRECISION)
                    .map(n -> (SiteNode) n);
            mapViewPane.reset();
            site.ifPresent(mapViewPane::setSelectedSite);
            site.flatMap(statusView::getNodeView).ifPresent(mapElementPane::setSelectedSite);
            mainFrame.repaint();
            return status;
        };
        tx.apply(status);
    }
}