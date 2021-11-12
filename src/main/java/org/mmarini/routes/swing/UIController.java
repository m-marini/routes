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
import io.reactivex.rxjava3.core.Flowable;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.swing.SwingUtilities.invokeLater;
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
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    private static final double DEFAULT_SPEED_LIMIT = 130 / 3.6;
    private static final double DEFAULT_FREQUENCY = 0.3;

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
    private final Simulator<Status> simulator;
    private boolean running;
    private StatusView statusView;
    //    private Status status;
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
        simulator = SimulatorImpl.create(this::performTimeTick,
                Status::getTime
        );//.setInterval(1, MILLISECONDS);

        mapElementPane = new MapElementPane();
        this.edgePane = mapElementPane.getEdgePane();
        this.nodePane = mapElementPane.getMapNodePane();
        this.sitePane = mapElementPane.getSiteNodePane();

        mapProfilePane = new MapProfilePane();
        frequencyPane = new FrequencyPane();
        routesPane = new RoutePane();
        fileChooser = new JFileChooser();
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
        simulator.request(s -> {
            Status status = s.setOffset(point);
            statusView = createStatusView(status);
            invokeLater(() -> {
                mapViewPane.reset();
                mapViewPane.selectSelector();
                mainFrame.repaint();
            });
            return status;
        });
    }

    /**
     * @param edge the edge
     */
    public void changeBeginNode(final MapEdge edge) {
        Optional<MapNode> mapNode = chooseNode();
        mapNode.ifPresent(node -> {
            MapEdge newEdge = edge.setBegin(node);
            simulator.request(s -> {
                Status status = s.changeEdge(edge, newEdge);
                statusView = createStatusView(status);
                invokeLater(() -> {
                    refreshTopology();
                    mapViewPane.selectSelector();
                    mapViewPane.reset();
                    statusView.getEdgeViews(newEdge).ifPresent(mapElementPane::setSelectedEdge);
                    mapViewPane.setSelectedEdge(newEdge);
                    mainFrame.repaint();
                });
                return status;
            });
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
        simulator.request(s -> {
            Status status = s.changeEdge(oldEdge, newEdge);
            statusView = createStatusView(status);
            return status;
        });
    }

    /**
     * @param edge the edge
     */
    public void changeEndNode(final MapEdge edge) {
        chooseNode().ifPresent(node -> {
            MapEdge newEdge = edge.setEnd(node);
            simulator.request(s -> {
                Status status = s.changeEdge(edge, newEdge);
                statusView = createStatusView(status);
                invokeLater(() -> {
                    refreshTopology();
                    mapViewPane.selectSelector();
                    mapViewPane.reset();
                    statusView.getEdgeViews(newEdge).ifPresent(mapElementPane::setSelectedEdge);
                    mapViewPane.setSelectedEdge(newEdge);
                    mainFrame.repaint();
                });
                return status;
            });
        });
    }

    /**
     *
     */
    private Optional<MapNode> chooseNode() {
        nodeChooser.setNodeList(statusView.getNodeViews());
        final int opt = JOptionPane.showConfirmDialog(mainFrame, nodeChooser,
                Messages.getString("RouteMediator.nodeChooser.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
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
        double speed = min(statusView.getStatus().getSpeedLimit(), computeSafetySpeed(end.getLocation().distance(begin.getLocation())));
        MapEdge edge1 = new MapEdge(begin, end, speed, edgePriority);
        simulator.request(s -> {
            Status status = s.addEdge(edge1);
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                mapViewPane.reset();
                mapViewPane.setSelectedEdge(edge1);
                statusView.getEdgeViews(edge1).ifPresent(mapElementPane::setSelectedEdge);
                mainFrame.repaint();
            });
            return status;
        });
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

        edgePane.getChangeFlowable().doOnNext(this::changeEdge).subscribe();

        edgePane.getDeleteFlowable().doOnNext(edg -> remove(edg.getEdge())).subscribe();
        edgePane.getBeginNodeFlowable().doOnNext(edg -> changeBeginNode(edg.getEdge())).subscribe();
        edgePane.getEndNodeFlowable().doOnNext(edg -> changeEndNode(edg.getEdge())).subscribe();

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

        simulator.getEvents()
                .subscribe();

        Flowable.interval(1000 / 60, MILLISECONDS)
                .doOnNext(t ->
                        simulator.request(status -> {
                            statusView = createStatusView(status);
                            refresh();
                            mapViewPane.repaint();
                            fpsMeter.tick();
                            return status;
                        }))
                .subscribe();
    }

    /**
     * @param moduleParameters the module parameters
     */
    private void createModule(RouteMap.ModuleParameters moduleParameters) {
        simulator.request(s -> {
            Status status = s.addModule(moduleParameters.getModule(),
                    moduleParameters.getLocation(),
                    moduleParameters.getDirection(),
                    PRECISION);
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                mapViewPane.reset();
                mapViewPane.selectSelector();
                mainFrame.repaint();
            });
            return status;
        });
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
            try {
                JsonNode doc = fromResource("/test.yml");
                StatusImpl status = new RouteAST(doc, JsonPointer.empty()).build();
                statusView = createStatusView(status);
                simulator.setEvent(status);
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
        Topology t = createTopology(List.of(), List.of());
        StatusImpl status = createStatus(t, 0, List.of(), DEFAULT_SPEED_LIMIT, DEFAULT_FREQUENCY);
        simulator.request(s -> {
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                mapViewPane.clearSelection();
                mapElementPane.clearPanel();
                mainFrame.repaint();
            });
            return status;
        });
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
            StatusImpl status = StatusImpl.createRandom(random, profile, DEFAULT_SPEED_LIMIT);
            logger.info("create random {}", status.hashCode());
            simulator.request(s -> {
                logger.info("Set random {}", status.hashCode());
                statusView = createStatusView(status);
                logger.info("random view status {} view{}", status.hashCode(), statusView.hashCode());
                refreshTopology();
                mapViewPane.selectSelector();
                mapViewPane.reset();
                mapViewPane.clearSelection();
                mapElementPane.clearPanel();
                mainFrame.repaint();
                return status;
            });
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
                    StatusImpl status = new RouteAST(doc, JsonPointer.empty()).build();
                    mainFrame.setSaveActionEnabled(true);
                    mainFrame.setTitle(file.getName());
                    simulator.request(s -> {
                        statusView = createStatusView(status);
                        invokeLater(() -> {
                            refreshTopology();
                            mapViewPane.reset();
                        });
                        return status;
                    });

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
            simulator.request(s -> {
                Status status = (optimizeSpeed ? s.optimizeSpeed(speedLimit) : s).optimizeNodes();
                statusView = createStatusView(status);
                invokeLater(() -> {
                    refreshTopology();
                    mapViewPane.reset();
                });
                return status;
            });
        }
    }

    /**
     * Returns the status at a given time from a status
     *
     * @param status the status
     * @param time   the time for the next status
     */
    private Status performTimeTick(Status status, Double time) {
        tpsMeter.tick();
        double dt = time - status.getTime();
        return dt != 0.0
                ? status.next(random, dt)
                : status;
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
            simulator.request(s -> {
                Status status = s.setFrequency(profile.getFrequency())
                        .randomizeWeights(random, profile.getMinWeight());
                statusView = createStatusView(status);
                invokeLater(() -> {
                    refreshTopology();
                    mapViewPane.selectSelector();
                    mapViewPane.reset();
                    mainFrame.repaint();
                });
                return status;
            });
        }
    }

    /**
     *
     */
    private void refresh() {
        routeMap.setStatus(statusView);
        scrollMap.setNumVehicles(statusView.getVehicles().size());
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
    }

    /**
     * @param edge the edge
     */
    public void remove(final MapEdge edge) {
        simulator.request(s -> {
            Status status = s.removeEdge(edge);
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                mapViewPane.clearSelection();
                mapElementPane.clearPanel();
                mainFrame.repaint();
            });
            return status;
        });
    }

    /**
     * @param node the node
     */
    public void remove(final MapNode node) {
        simulator.request(s -> {
            Status status = s.removeNode(node);
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                mapViewPane.clearSelection();
                mapElementPane.clearPanel();
                mainFrame.repaint();
            });
            return status;
        });
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
            simulator.request(s -> {
                Status status = s.setFrequency(frequency);
                statusView = createStatusView(status);
                return status;
            });
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
            simulator.request(s -> {
                Status status = s.setWeights(weights1.getValues());
                statusView = createStatusView(status);
                invokeLater(() -> {
                    refresh();
                    mapViewPane.selectSelector();
                    mapViewPane.reset();
                    mainFrame.repaint();
                });
                return status;
            });
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
        final JScrollPane sp = new JScrollPane(table);
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
                        sv.getNodeView(info.getDestination())
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
        simulator.request(s -> {
            Status status = s.changeNode(site);
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                Optional<CrossNode> node = statusView.findNode(site.getLocation(), PRECISION)
                        .map(n -> (CrossNode) n);
                mapViewPane.reset();
                node.ifPresent(mapViewPane::setSelectedNode);
                node.flatMap(statusView::getNodeView).ifPresent(mapElementPane::setSelectedNode);
                mainFrame.repaint();
            });
            return status;
        });
    }

    /**
     * @param node the node
     */
    public void transformToSite(final MapNode node) {
        simulator.request(s -> {
            Status status = s.changeNode(node);
            statusView = createStatusView(status);
            invokeLater(() -> {
                refreshTopology();
                Optional<SiteNode> site = statusView.findNode(node.getLocation(), PRECISION)
                        .map(n -> (SiteNode) n);
                mapViewPane.reset();
                site.ifPresent(mapViewPane::setSelectedSite);
                site.flatMap(statusView::getNodeView).ifPresent(mapElementPane::setSelectedSite);
                mainFrame.repaint();
            });
            return status;
        });
    }
}