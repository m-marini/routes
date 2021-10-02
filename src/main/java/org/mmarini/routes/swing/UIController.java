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
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.reactivex.rxjava3.core.Observable.interval;

/**
 * The UIController manages the user views and the user action.
 * It creates workflow between the user events (by reactive interface)
 * and the changes on the data views
 *
 * @author marco.marini@mmarini.org
 */
public class UIController {

    public static final float NANOS = 1e-9f;
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    private static final int TIME_INTERVAL = 1;
    private static final double NODE_SATURATION = 1f;
    private static final Color DEFAULT_NODE_COLOR = Color.LIGHT_GRAY;

    /**
     * Returns a PathEntry
     *
     * @param nodes the nodes
     * @param paths the paths
     */
    static SquareMatrixModel<MapNodeEntry> createSquareMatrixModel(List<MapNodeEntry> nodes, List<Path> paths) {
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
    public static List<Path> toPathList(SquareMatrixModel<MapNodeEntry> data) {
        final double[][] values = data.getValues();
        final int n = values.length;
        final List<MapNodeEntry> indices = data.getIndices();
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
    private final DefaultListModel<MapNodeEntry> nodeList;
    private final String nodeNamePattern;
    private final String defaultNodeName;
    private final OptimizePane optimizePane;
    private final RoutePane routesPane;
    private final String edgeNamePattern;
    private final NodeChooser nodeChooser;
    private final MapProfilePane mapProfilePane;
    private final FrequencePane frequencePane;
    private final RouteMap routeMap;
    private final MainFrame mainFrame;
    private final MapViewPane mapViewPane;
    private final ExplorerPane explorerPane;
    private final MapElementPane mapElementPane;
    private final EdgePane edgePane;
    private final MapNodePane mapNodePane;
    private final SiteNodePane siteNodePane;
    private long start;
    private double speedSimulation;
    private boolean running;
    private List<MapNodeEntry> nodeEntries;
    private List<EdgeEntry> edgeEntries;
    private Map<MapNode, MapNodeEntry> entryByNode;

    /**
     *
     */
    public UIController() {
        routeMap = new RouteMap();
        ScrollMap scrollMap = new ScrollMap(this.routeMap);
        mapViewPane = new MapViewPane(scrollMap);
        explorerPane = new ExplorerPane();

        mapElementPane = new MapElementPane();
        this.edgePane = mapElementPane.getEdgePane();
        this.mapNodePane = mapElementPane.getMapNodePane();
        this.siteNodePane = mapElementPane.getSiteNodePane();

        mapProfilePane = new MapProfilePane();
        frequencePane = new FrequencePane();
        routesPane = new RoutePane();
        fileChooser = new JFileChooser();
        handler = new RouteHandler();
        nodeList = new DefaultListModel<>();
        nodeChooser = new NodeChooser();
        mainFrame = new MainFrame(mapViewPane, mapElementPane, explorerPane);

        nodeNamePattern = Messages.getString("RouteMediator.nodeNamePattern"); //$NON-NLS-1$
        defaultNodeName = Messages.getString("RouteMediator.defaultNodeNamePattern"); //$NON-NLS-1$
        edgeNamePattern = Messages.getString("RouteMediator.edgeNamePattern"); //$NON-NLS-1$
        optimizePane = new OptimizePane();
        fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("RouteMediator.filetype.title"), //$NON-NLS-1$
                "yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$
        init();
    }

    /**
     * @param point
     */
    public void centerMap(final Point2D point) {
        handler.centerMap(point);
        mapViewPane.reset();
        mapViewPane.selectSelector();
        mainFrame.repaint();
    }

    /**
     * @param edge
     */
    public void changeBeginNode(final MapEdge edge) {
        Optional<MapNode> mapNode = chooseNode();
        mapNode.ifPresent(node -> {
            handler.changeBeginNode(edge, node);
            refresh();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            getEdgeModel(edge).ifPresent(mapElementPane::setSelectedEdge);
            mapViewPane.setSelectedEdge(edge);
            mainFrame.repaint();
        });
    }

    /**
     * @param edge
     */
    public void changeEndNode(final MapEdge edge) {
        chooseNode().ifPresent(node -> {
            handler.changeEndNode(edge, node);
            refresh();
            mapViewPane.selectSelector();
            mapViewPane.reset();
            mapViewPane.setSelectedEdge(edge);
            getEdgeModel(edge).ifPresent(mapElementPane::setSelectedEdge);
            mainFrame.repaint();
        });
    }

    /**
     *
     */
    private Optional<MapNode> chooseNode() {
        stopSimulation();
        nodeChooser.clearSelection();
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
     * @param bound
     */
    public void computeMapBound(final Rectangle2D bound) {
        handler.computeMapBound(bound);
    }

    /**
     * @return
     */
    public int computeSiteCount() {
        return handler.computeSiteCount();
    }

    /**
     * Returns the map of colors of nodes
     */
    private Map<MapNode, Color> createColorMap() {
        int n = computeSiteCount();
        final Map<MapNode, Color> nodeColorMap = new HashMap<>(n);
        final SwingUtils util = SwingUtils.getInstance();
        int i = 0;
        for (final SiteNode node : getSiteNodes()) {
            final double value = (double) i / (n - 1);
            final Color color = util.computeColor(value, NODE_SATURATION);
            nodeColorMap.put(node, color);
            ++i;
        }
        return nodeColorMap;
    }

    /**
     * @param edge
     */
    private void createEdge(RouteMap.EdgeCreation edge) {
        final MapEdge edge1 = handler.createEdge(edge.getBegin(), edge.getEnd());
        refresh();
        mapViewPane.reset();
        mapViewPane.setSelectedEdge(edge1);
        getEdgeModel(edge1).ifPresent(mapElementPane::setSelectedEdge);
        mainFrame.repaint();
    }

    /**
     *
     */
    private void createFlows() {
        mainFrame.getInfosFlowable().doOnNext(e -> showInfos()).subscribe();
        mainFrame.getVehicleInfoFlowable().doOnNext(e -> showTrafficInfos()).subscribe();
        mainFrame.getStopFlowable().doOnNext(e -> toggleSimulation()).subscribe();
        mainFrame.getSimSpeedFlowable().doOnNext(this::setSpeedSimulation).subscribe();
        mainFrame.getNewMapFlowable().doOnNext(e -> newMap()).subscribe();
        mainFrame.getOpenMapFlowable().doOnNext(e -> open()).subscribe();
        mainFrame.getSaveMapFlowable().doOnNext(e -> save()).subscribe();
        mainFrame.getExitFlowable().doOnNext(e -> System.exit(0)).subscribe();
        mainFrame.getOptimizeFlowable().doOnNext(e -> optimize()).subscribe();
        mainFrame.getRandomizeFlowable().doOnNext(e -> randomize()).subscribe();
        mainFrame.getFrequenceFlowable().doOnNext(e -> setFrequency()).subscribe();
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

        mapNodePane.getChangeFlowable().doOnNext(node -> transformToSite(node.getNode()))
                .subscribe();
        mapNodePane.getDeleteFlowable().doOnNext(node -> remove(node.getNode()))
                .subscribe();

        siteNodePane.getChangeFlowable().doOnNext(node -> transformToNode((SiteNode) node.getNode()))
                .subscribe();
        siteNodePane.getDeleteFlowable().doOnNext(node -> remove(node.getNode()))
                .subscribe();

        explorerPane.getSiteFlowable().doOnNext(site -> {
            handleSiteSelection(site);
            mapViewPane.setSelectedSite(site);
            mapViewPane.scrollTo(site);
        }).subscribe();
        explorerPane.getNodeFlowable().doOnNext(node -> {
            getNodeEntry(node).ifPresent(mapElementPane::setSelectedNode);
            mapViewPane.setSelectedNode(node);
            mapViewPane.scrollTo(node);
        }).subscribe();
        explorerPane.getEdgeFlowable().doOnNext(edge -> {
            handleEdgeSelection(edge.getEdge());
            mapViewPane.setSelectedEdge(edge.getEdge());
            mapViewPane.scrollTo(edge.getEdge());
        }).subscribe();

        routeMap.getEdgeFlowable().doOnNext(this::handleEdgeSelection).subscribe();
        routeMap.getSiteFlowable().doOnNext(this::handleSiteSelection).subscribe();
        routeMap.getNodeFlowable().doOnNext(this::handleNodeSelection).subscribe();
        routeMap.getDeleteEdgeFlowable().doOnNext(this::remove).subscribe();
        routeMap.getDeleteNodeFlowable().doOnNext(this::remove).subscribe();
        routeMap.getCenterMapFlowable().doOnNext(this::centerMap).subscribe();
        routeMap.getNewEdgeFlowable().doOnNext(this::createEdge).subscribe();
        routeMap.getNewModuleProcessor().doOnNext(this::createModule).subscribe();

        interval(TIME_INTERVAL, TimeUnit.MILLISECONDS, SwingSchedulers.edt())
                .doOnNext(t -> {
                    if (running) {
                        performTimeTick();
                    }
                }).subscribe();
    }

    /**
     * Create the data models
     */
    private void createModels() {
        final Map<MapNode, Color> colorMap = createColorMap();
        final List<MapNode> nodes = getNodes();
        nodeEntries = new ArrayList<>(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            final MapNode node = nodes.get(i);
            final String name = MessageFormat.format(nodeNamePattern, i + 1);
            final Color color = colorMap.getOrDefault(node, DEFAULT_NODE_COLOR);
            nodeEntries.add(new MapNodeEntry(name, node, color));
        }
        this.entryByNode = nodeEntries.stream().collect(Collectors.toMap(MapNodeEntry::getNode, Function.identity()));

        final List<MapEdge> edges = getMapEdges();
        edgeEntries = new ArrayList<>(edges.size());
        for (int i = 0; i < nodes.size(); i++) {
            final MapEdge edge = edges.get(i);
            final String begin = Optional.ofNullable(entryByNode.get(edge.getBegin()))
                    .map(MapNodeEntry::getName).orElse("?");
            final String end = Optional.ofNullable(entryByNode.get(edge.getEnd()))
                    .map(MapNodeEntry::getName).orElse("?");
            final String name = MessageFormat.format(edgeNamePattern, i, begin, end);
            edgeEntries.add(new EdgeEntry(edge, name, begin, end, edge.getPriority(), edge.getSpeedLimit()));
        }
    }

    /**
     * @param moduleParameters
     */
    private void createModule(RouteMap.ModuleParameters moduleParameters) {
        handler.addModule(moduleParameters.getModule(),
                moduleParameters.getLocation(),
                moduleParameters.getDirection().getX(),
                moduleParameters.getDirection().getY());
        refresh();
        mapViewPane.reset();
        mapViewPane.selectSelector();
        mainFrame.repaint();
    }

    /**
     * @param point
     * @param precision
     * @return
     */
    public Optional<MapElement> findElement(final Point2D point, final double precision) {
        return Optional.ofNullable(handler.findElement(point, precision));
    }

    private Optional<EdgeEntry> getEdgeModel(MapEdge edge) {
        return edgeEntries.stream().filter(e -> e.getEdge().equals(edge)).findFirst();
    }

    /**
     *
     */
    public List<MapEdge> getMapEdges() {
        Stream.Builder<MapEdge> b = Stream.builder();
        handler.getMapEdges().forEach(b::add);
        return b.build().collect(Collectors.toList());
    }

    /**
     * Return the color of a node
     *
     * @param node the node
     */
    public Color getNodeColor(final MapNode node) {
        return Optional.ofNullable(entryByNode.get(node))
                .map(MapNodeEntry::getColor)
                .orElse(DEFAULT_NODE_COLOR);
    }

    /**
     * @param mapNode the node
     */
    private Optional<MapNodeEntry> getNodeEntry(MapNode mapNode) {
        return Optional.ofNullable(entryByNode.get(mapNode));
    }

    /**
     *
     */
    public List<MapNode> getNodes() {
        Stream.Builder<MapNode> b = Stream.builder();
        handler.getNodes().forEach(b::add);
        return b.build().collect(Collectors.toList());
    }

    /**
     *
     */
    public List<Path> getPaths() {
        Stream.Builder<Path> b = Stream.builder();
        handler.getPaths().forEach(b::add);
        return b.build().collect(Collectors.toList());
    }

    /**
     *
     */
    public List<SiteNode> getSiteNodes() {
        Stream.Builder<SiteNode> b = Stream.builder();
        handler.getSiteNodes().forEach(b::add);
        return b.build().collect(Collectors.toList());
    }

    /**
     *
     */
    public List<Vehicle> getVehicles() {
        Stream.Builder<Vehicle> b = Stream.builder();
        handler.getVeicles().forEach(b::add);
        return b.build().collect(Collectors.toList());
    }

    private void handleEdgeSelection(Optional<MapEdge> edge) {
        edge.ifPresent(this::handleEdgeSelection);
        edge.flatMap(this::getEdgeModel)
                .ifPresentOrElse(explorerPane::setSelectedEdge, explorerPane::clearSelection);
    }

    /**
     * @param edge the edge
     */
    private void handleEdgeSelection(MapEdge edge) {
        assert edge != null;
        handler.setTemplate(edge);
        getEdgeModel(edge).ifPresent(mapElementPane::setSelectedEdge);
    }

    private void handleNodeSelection(Optional<MapNode> node) {
        Optional<MapNodeEntry> entry = node.flatMap(this::getNodeEntry);
        entry.ifPresentOrElse(mapElementPane::setSelectedNode, mapElementPane::clearPanel);
        entry.ifPresentOrElse(explorerPane::setSelectedNode, explorerPane::clearSelection);
    }

    private void handleSiteSelection(Optional<SiteNode> site) {
        site.ifPresent(this::handleSiteSelection);
        Optional<MapNodeEntry> entry = site.flatMap(this::getNodeEntry);
        entry.ifPresentOrElse(explorerPane::setSelectedNode, explorerPane::clearSelection);
    }

    /**
     * @param site the site
     */
    private void handleSiteSelection(SiteNode site) {
        if (site != null) {
            handler.setTemplate(site);
            getNodeEntry(site).ifPresent(mapElementPane::setSelectedSite);
        }
    }

    /**
     *
     */
    private void init() {
        routeMap.setMediator(this);
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
        refresh();
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
        refresh();
        mapViewPane.selectSelector();
        mapViewPane.reset();
        mapViewPane.clearSelection();
        mapElementPane.setSelectedNode(null);
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
            refresh();
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
                refresh();
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
            refresh();
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
            handler.setTimeInterval(interval * NANOS * speedSimulation);
            handler.performSimulation();
            mapViewPane.repaint();
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
    private void refresh() {
        createModels();
        final DefaultListModel<EdgeEntry> nl = explorerPane.getEdgeListModel();
        nl.removeAllElements();
        nl.addAll(edgeEntries);
        final DefaultListModel<MapNodeEntry> el = explorerPane.getNodeListModel();
        el.removeAllElements();
        el.addAll(nodeEntries);
    }

    /**
     * @param edge
     */
    public void remove(final MapEdge edge) {
        handler.remove(edge);
        refresh();
        mapViewPane.clearSelection();
        mapElementPane.clearPanel();
        mainFrame.repaint();
    }

    /**
     * @param node
     */
    public void remove(final MapNode node) {
        handler.remove(node);
        refresh();
        mapViewPane.clearSelection();
        mapElementPane.clearPanel();
        mainFrame.repaint();
    }

    /**
     * @param node
     */
    public String retrieveNodeName(final MapNode node) {
        final int n = nodeList.getSize();
        for (int i = 0; i < n; ++i) {
            final MapNodeEntry entry = nodeList.get(i);
            if (entry.getNode().equals(node)) {
                return entry.getName();
            }
        }
        final Point2D location = node.getLocation();
        return MessageFormat.format(defaultNodeName, location.getX(), location.getY());
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
        frequencePane.setFrequence(frequency);
        final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencePane,
                Messages.getString("RouteMediator.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
        if (opt == JOptionPane.OK_OPTION) {
            stopSimulation();
            handler.setFrequence(frequencePane.getFrequence());
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
        final SquareMatrixModel<MapNodeEntry> pathEntry = createSquareMatrixModel(
                getSiteNodes().stream()
                        .flatMap(s -> getNodeEntry(s).stream())
                        .collect(Collectors.toList()),
                getPaths());
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
     * @param speedSimulation
     */
    public void setSpeedSimulation(final double speedSimulation) {
        this.speedSimulation = speedSimulation;
    }

    /**
     * @param message
     */
    private void showError(final String message) {
        JOptionPane.showMessageDialog(mainFrame, message, Messages.getString("RouteMediator.error.title"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @param pattern
     * @param arguments
     */
    private void showError(final String pattern, final Object[] arguments) {
        showError(MessageFormat.format(pattern, arguments));
    }

    /**
     * @param e
     */
    private void showError(final Throwable e) {
        logger.error(e.getMessage(), e);
        showError("{0}", new Object[]{e.getMessage(), //$NON-NLS-1$
                e.getMessage()});
    }

    /**
     *
     */
    private void showInfos() {
        stopSimulation();
        final RouteInfos infos = new RouteInfos();
        handler.computeRouteInfos(infos);
        final SquareMatrixModel<MapNodeEntry> routeInfo = new SquareMatrixModel<MapNodeEntry>(
                infos.getNodes().stream()
                        .flatMap(s -> getNodeEntry(s).stream())
                        .collect(Collectors.toList()),
                infos.getFrequence());

        final InfosTable table = InfosTable.create(routeInfo);
        final JScrollPane sp = new JScrollPane(table);
        JOptionPane.showMessageDialog(mainFrame, sp, Messages.getString("RouteMediator.infoPane.title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
        startSimulation();
    }

    /**
     *
     */
    private void showTrafficInfos() {
        stopSimulation();
        final List<TrafficInfo> map = new ArrayList<>(0);
        handler.computeTrafficInfos(map);
        final List<TrafficInfoEntry> data = map.stream().flatMap(info ->
                        Optional.ofNullable(entryByNode.get(info.getDestination()))
                                .map(dest ->
                                        new TrafficInfoEntry(dest, info))
                                .stream())
                .sorted(Comparator.comparing(a -> a.getDestination().getName()))
                .collect(Collectors.toList());
        final TrafficInfoModel model = new TrafficInfoModel();
        model.setInfos(data);
        final TrafficInfoTable table = new TrafficInfoTable(model);
        final Component pane = new JScrollPane(table);
        JOptionPane.showMessageDialog(mainFrame, pane, Messages.getString("RouteMediator.trafficInfoPane.title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
        startSimulation();
    }

    /**
     * @param point
     * @param precision
     */
    public void snapToNode(final Point2D point, final double precision) {
        final MapNode elem = handler.findNode(point, precision);
        if (elem != null) {
            point.setLocation(elem.getLocation());
        }
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
        if (running) {
            start = System.nanoTime();
        } else {
            running = true;
        }
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
        refresh();
        mapViewPane.reset();
        mapViewPane.setSelectedNode(node);
        getNodeEntry(node).ifPresent(mapElementPane::setSelectedNode);
        mainFrame.repaint();
    }

    /**
     * @param node the node
     */
    public void transformToSite(final MapNode node) {
        final SiteNode site = handler.transformToSite(node);
        refresh();
        mapViewPane.reset();
        mapViewPane.setSelectedSite(site);
        getNodeEntry(site).ifPresent(mapElementPane::setSelectedSite);
        mainFrame.repaint();
    }
}