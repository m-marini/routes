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

import hu.akarnokd.rxjava3.swing.SwingSchedulers;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static io.reactivex.rxjava3.core.Observable.interval;
import static org.mmarini.Utils.*;
import static org.mmarini.routes.swing.StatusView.DEFAULT_NODE_COLOR;

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
    private static final double NODE_SATURATION = 1;

    /**
     * @param handler the handler
     */
    public static StatusView create(RouteHandler handler) {
        List<MapNode> nodes = toList(handler.getNodes());
        List<SiteNode> sites = toList(handler.getSiteNodes());
        List<MapEdge> edges = toList(handler.getMapEdges());
        List<Vehicle> vehicles = toList(handler.getVeicles());
        List<Path> paths = toList(handler.getPaths());
        List<TrafficInfo> trafficInfo = new ArrayList<>(0);
        handler.computeTrafficInfos(trafficInfo);
        final RouteInfos info = new RouteInfos();
        handler.computeRouteInfos(info);

        // Computes site color map
        int noSites = sites.size();
        SwingUtils util = SwingUtils.getInstance();
        Map<SiteNode, Color> colorBySite = zipWithIndex(sites).map(entry -> {
            int i = entry.getKey();
            SiteNode node = entry.getValue();
            final double value = (double) i / (noSites - 1);
            Color color = util.computeColor(value, NODE_SATURATION);
            return Map.entry(node, color);
        }).collect(toMap());

        // Converts to node view
        String nodeNamePattern = Messages.getString("RouteMediator.nodeNamePattern"); //$NON-NLS-1$
        List<NodeView> nodeViews = zipWithIndex(nodes)
                .map(entry -> {
                    int i = entry.getKey();
                    MapNode node = entry.getValue();
                    return new NodeView(
                            MessageFormat.format(nodeNamePattern, i + 1),
                            node,
                            Optional.ofNullable(colorBySite.get(node)).orElse(DEFAULT_NODE_COLOR));
                })
                .collect(Collectors.toList());
        Map<MapNode, NodeView> viewByNode = nodeViews.stream().collect(Collectors.toMap(NodeView::getNode, Function.identity()));

        // Converts to edgeView
        String edgeNamePattern = Messages.getString("RouteMediator.edgeNamePattern"); //$NON-NLS-1$
        List<EdgeView> edgesViews = zipWithIndex(edges)
                .map(entry -> {
                    int i = entry.getKey();
                    MapEdge edge = entry.getValue();
                    final String begin = Optional.ofNullable(viewByNode.get(edge.getBegin()))
                            .map(NodeView::getName).orElse("?");
                    final String end = Optional.ofNullable(viewByNode.get(edge.getEnd()))
                            .map(NodeView::getName).orElse("?");
                    final String name = MessageFormat.format(edgeNamePattern, i, begin, end);
                    return new EdgeView(edge, name, begin, end, edge.getPriority(), edge.getSpeedLimit());
                })
                .collect(Collectors.toList());
        Map<MapEdge, EdgeView> viewByEdge = edgesViews.stream()
                .collect(Collectors.toMap(EdgeView::getEdge, Function.identity()));
        return new StatusView(nodes,
                sites,
                edges,
                vehicles,
                trafficInfo,
                paths,
                info,
                nodeViews,
                edgesViews,
                viewByNode,
                viewByEdge);
    }

    /**
     * Returns a PathEntry
     *
     * @param nodes the nodes
     * @param paths the paths
     */
    static SquareMatrixModel<NodeView> createSquareMatrixModel(List<NodeView> nodes, List<Path> paths) {
        final ToIntFunction<MapNode> indexOfNode = node -> {
            final int n = nodes.size();
            int idx = -1;
            for (int i = 0; i < n; i++) {
                if (nodes.get(i).getNode().equals(node)) {
                    idx = i;
                    break;
                }
            }
            return idx;
        };
        final Function<Path, Optional<int[]>> indicesOfPath = path -> {
            final int i = indexOfNode.applyAsInt(path.getDeparture());
            final int j = indexOfNode.applyAsInt(path.getDestination());
            return (i >= 0 && j >= 0) ? Optional.of(new int[]{i, j}) : Optional.empty();
        };
        int n = nodes.size();
        final double[][] weights = new double[n][n];
        for (Path path : paths) {
            indicesOfPath.apply(path).ifPresent(indices ->
                    weights[indices[0]][indices[1]] = path.getWeight());
        }
        return new SquareMatrixModel<>(nodes, weights);
    }

    /**
     *
     */
    public static List<Path> toPathList(SquareMatrixModel<NodeView> data) {
        final double[][] values = data.getValues();
        final int n = values.length;
        final List<NodeView> indices = data.getIndices();
        final List<Path> paths = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            SiteNode dep = (SiteNode) indices.get(i).getNode();
            for (int j = 0; j < n; j++) {
                SiteNode dest = (SiteNode) indices.get(j).getNode();
                if (i != j && values[i][j] > 0) {
                    paths.add(new Path(dep, dest, values[i][j]));
                }
            }
        }
        return paths;
    }

    private final JFileChooser fileChooser;
    private final RouteHandler handler;
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
    private long start;
    private double speedSimulation;
    private boolean running;
    private StatusView status;

    /**
     *
     */
    public UIController() {
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
        handler = new RouteHandler();
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
        handler.centerMap(point);
        mapViewPane.reset();
        mapViewPane.selectSelector();
        mainFrame.repaint();
    }

    /**
     * @param edge the edge
     */
    public void changeBeginNode(final MapEdge edge) {
        Optional<MapNode> mapNode = chooseNode();
        mapNode.ifPresent(node -> {
            handler.changeBeginNode(edge, node);
            refreshTopology();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            status.getEdgeViews(edge).ifPresent(mapElementPane::setSelectedEdge);
            mapViewPane.setSelectedEdge(edge);
            mainFrame.repaint();
        });
    }

    /**
     * @param edge the edge
     */
    public void changeEndNode(final MapEdge edge) {
        chooseNode().ifPresent(node -> {
            handler.changeEndNode(edge, node);
            refreshTopology();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mapViewPane.setSelectedEdge(edge);
            status.getEdgeViews(edge).ifPresent(mapElementPane::setSelectedEdge);
            mainFrame.repaint();
        });
    }

    /**
     *
     */
    private Optional<MapNode> chooseNode() {
        stopSimulation();
        nodeChooser.setNodeList(status.getNodeViews());
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
        final MapEdge edge1 = handler.createEdge(edge.getBegin(), edge.getEnd());
        refreshTopology();
        mapViewPane.reset();
        mapViewPane.setSelectedEdge(edge1);
        status.getEdgeViews(edge1).ifPresent(mapElementPane::setSelectedEdge);
        mainFrame.repaint();
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

        edgePane.getChangeFlowable().doOnNext(edg ->
                handler.changeEdgeProperties(edg.getEdge(), edg.getPriority(), edg.getSpeedLimit())
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
            status.getNodeView(node).ifPresent(mapElementPane::setSelectedNode);
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
        routeMap.getNewModuleProcessor().doOnNext(this::createModule).subscribe();

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
        handler.addModule(moduleParameters.getModule(),
                moduleParameters.getLocation(),
                moduleParameters.getDirection().getX(),
                moduleParameters.getDirection().getY());
        refreshTopology();
        mapViewPane.reset();
        mapViewPane.selectSelector();
        mainFrame.repaint();
    }

    /**
     * @param edge the edge
     */
    private void handleEdgeSelection(MapEdge edge) {
        assert edge != null;
        handler.setTemplate(edge);
        status.getEdgeViews(edge).ifPresent(mapElementPane::setSelectedEdge);
    }

    /**
     * @param edge the edge
     */
    private void handleEdgeSelection1(MapEdge edge) {
        this.handleEdgeSelection(edge);
        status.getEdgeViews(edge).ifPresentOrElse(
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
            public Void visit(MapNode node) {
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
        Optional<NodeView> entry = status.getNodeView(node);
        entry.ifPresentOrElse(mapElementPane::setSelectedNode, mapElementPane::clearPanel);
        entry.ifPresentOrElse(explorerPane::setSelectedNode, explorerPane::clearSelection);
    }

    /**
     * @param site the site
     */
    private void handleSiteSelection(SiteNode site) {
        assert site != null;
        handler.setTemplate(site);
        status.getNodeView(site).ifPresent(mapElementPane::setSelectedSite);
    }

    private void handleSiteSelection1(SiteNode site) {
        this.handleSiteSelection(site);
        status.getNodeView(site).ifPresentOrElse(
                explorerPane::setSelectedNode,
                explorerPane::clearSelection);
    }

    /**
     *
     */
    private void init() {
        loadDefault();
        final List<Module> modules = new ArrayList<>(0);
        try {
            handler.retrieveModule(modules);
        } catch (final Exception e) {
            showError(e);
        }
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
                handler.load(url);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     *
     */
    private void newMap() {
        handler.clearMap();
        refreshTopology();
        mapViewPane.selectSelector();
        mapViewPane.reset();
        mapViewPane.clearSelection();
        mapElementPane.clearPanel();
        mainFrame.repaint();
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
            handler.createRandomMap(profile);
            refreshTopology();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mapViewPane.clearSelection();
            mapElementPane.clearPanel();
            mainFrame.repaint();
            startSimulation();
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
                try {
                    handler.load(file);
                    mainFrame.setSaveActionEnabled(true);
                    mainFrame.setTitle(file.getName());
                } catch (final SAXParseException e) {
                    logger.error(e.getMessage(), e);
                    showError(Messages.getString("RouteMediator.parseError.message"), new Object[]{e.getMessage(), //$NON-NLS-1$
                            e.getLineNumber(), e.getColumnNumber()});
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                    showError(e.getMessage());
                } catch (final Throwable e) {
                    logger.error(e.getMessage(), e);
                    showError(e);
                }
                refreshTopology();
                mapViewPane.reset();
                startSimulation();
            }
        }
    }

    /**
     *
     */
    private void optimize() {
        final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
                Messages.getString("RouteMediator.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final double speedLimit = optimizePane.getSpeedLimit();
            final boolean optimizeSpeed = optimizePane.isOptimizeSpeed();
            final boolean optimizeNodes = optimizePane.isOptimizeNodes();
            stopSimulation();
            handler.optimize(optimizeNodes, optimizeSpeed, speedLimit);
            refreshTopology();
            mapViewPane.reset();
            startSimulation();
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
            handler.setTimeInterval(interval * SECS_PER_NANO * speedSimulation);
            handler.performSimulation();
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
            handler.randomize(profile);
            refreshTopology();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mainFrame.repaint();
            startSimulation();
        }
    }

    /**
     *
     */
    private void refresh() {
        this.status = create(handler);
        routeMap.setStatus(status);
        scrollMap.setNumVehicles(status.getVehicles().size());
    }

    /**
     *
     */
    private void refreshTopology() {
        this.status = create(handler);
        final DefaultListModel<EdgeView> nl = explorerPane.getEdgeListModel();
        nl.removeAllElements();
        nl.addAll(status.getEdgesViews());
        final DefaultListModel<NodeView> el = explorerPane.getNodeListModel();
        el.removeAllElements();
        el.addAll(status.getNodeViews());
        routeMap.setStatus(status);
    }

    /**
     * @param edge the edge
     */
    public void remove(final MapEdge edge) {
        handler.remove(edge);
        refreshTopology();
        mapViewPane.clearSelection();
        mapElementPane.clearPanel();
        mainFrame.repaint();
    }

    /**
     * @param node the node
     */
    public void remove(final MapNode node) {
        handler.remove(node);
        refreshTopology();
        mapViewPane.clearSelection();
        mapElementPane.clearPanel();
        mainFrame.repaint();
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
            try {
                handler.save(file);
                mainFrame.setSaveActionEnabled(true);
                mainFrame.setTitle(file.getPath());
            } catch (final Throwable e) {
                logger.error(e.getMessage(), e);
                showError(e);
            }
            stopSimulation();
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
        final double frequency = handler.getFrequence();
        frequencyPane.setFrequency(frequency);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencyPane,
                Messages.getString("RouteMediator.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            stopSimulation();
            handler.setFrequence(frequencyPane.getFrequence());
            refresh();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mainFrame.repaint();
            startSimulation();
        }
    }

    /**
     *
     */
    public void setRouteSetting() {
        stopSimulation();
        final SquareMatrixModel<NodeView> pathEntry = createSquareMatrixModel(
                status.getSites().stream()
                        .flatMap(s -> status.getNodeView(s).stream())
                        .collect(Collectors.toList()),
                status.getPaths());
        routesPane.setPathEntry(pathEntry);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, routesPane,
                Messages.getString("RouteMediator.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            final List<Path> list = toPathList(routesPane.getPathEntry());
            handler.loadPaths(list);
            refresh();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mainFrame.repaint();
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
        final RouteInfos info = status.getRouteInfos();
        final SquareMatrixModel<NodeView> routeInfo = new SquareMatrixModel<>(
                info.getNodes().stream()
                        .flatMap(s -> status.getNodeView(s).stream())
                        .collect(Collectors.toList()),
                info.getFrequence());

        final InfosTable table = InfosTable.create(routeInfo);
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
        final List<TrafficInfo> map = status.getTrafficInfo();
        final List<TrafficInfoView> data = map.stream().flatMap(info ->
                        status.getNodeView(info.getDestination())
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
        final MapNode node = handler.transformToNode(site);
        refreshTopology();
        mapViewPane.reset();
        mapViewPane.setSelectedNode(node);
        status.getNodeView(node).ifPresent(mapElementPane::setSelectedNode);
        mainFrame.repaint();
    }

    /**
     * @param node the node
     */
    public void transformToSite(final MapNode node) {
        final SiteNode site = handler.transformToSite(node);
        refreshTopology();
        mapViewPane.reset();
        mapViewPane.setSelectedSite(site);
        status.getNodeView(site).ifPresent(mapElementPane::setSelectedSite);
        mainFrame.repaint();
    }
}