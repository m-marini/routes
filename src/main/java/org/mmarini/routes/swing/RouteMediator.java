/*
 * RouteMediator.java
 *
 * $Id: RouteMediator.java,v 1.16 2010/10/19 20:32:59 marco Exp $
 *
 * 06/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapElement;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.MapProfile;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.Path;
import org.mmarini.routes.model.RouteHandler;
import org.mmarini.routes.model.RouteInfos;
import org.mmarini.routes.model.SiteNode;
import org.mmarini.routes.model.TrafficInfo;
import org.mmarini.routes.model.Veicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: RouteMediator.java,v 1.16 2010/10/19 20:32:59 marco Exp $
 *
 */
public class RouteMediator {
	private static final Logger logger = LoggerFactory.getLogger(RouteMediator.class);
	private static final int TIME_INTERVAL = 1;
	private static final double NODE_SATURATION = 1f;

	private MainFrame mainFrame;
	private MapViewPane mapViewPane;
	private ExplorerPane explorerPane;
	private MapElementPane mapElementPane;
	private final JFileChooser fileChooser;
	private final RouteHandler handler;
	private final Timer timer;
	private final DefaultListModel<MapNodeEntry> nodeList;
	private final DefaultListModel<MapEdgeEntry> edgeList;
	private final String nodeNamePattern;
	private final String defaultNodeName;
	private final OptimizePane optimizePane;
	private final RoutePane routesPane;
	private final String edgeNamePattern;
	private final NodeChooser nodeChooser;
	private final MapProfilePane mapProfilePane;
	private final FrequencePane frequencePane;
	private long start;
	private double speedSimulation;
	private final String defaultEdgeName;
	private Map<MapNode, Color> nodeColorMap;
	private final Map<String, MapNode> nodeNameMap;
	private boolean paused;

	/**
	*
	*/
	public RouteMediator() {
		nodeNameMap = new HashMap<String, MapNode>();
		mapProfilePane = new MapProfilePane();
		frequencePane = new FrequencePane();
		routesPane = new RoutePane();
		fileChooser = new JFileChooser();
		handler = new RouteHandler();
		nodeList = new DefaultListModel<>();
		edgeList = new DefaultListModel<>();
		nodeChooser = new NodeChooser();
		nodeNamePattern = Messages.getString("RouteMediator.nodeNamePattern"); //$NON-NLS-1$
		defaultNodeName = Messages.getString("RouteMediator.defaultNodeNamePattern"); //$NON-NLS-1$
		defaultEdgeName = Messages.getString("RouteMediator.defaultEdgeNamePattern"); //$NON-NLS-1$
		edgeNamePattern = Messages.getString("RouteMediator.edgeNamePattern"); //$NON-NLS-1$
		timer = new Timer(TIME_INTERVAL, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				performTimeTick();
			}
		});
		optimizePane = new OptimizePane();

		fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("RouteMediator.filetype.title"), //$NON-NLS-1$
				"yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$
		routesPane.setMediator(this);
	}

	/**
	 * @param module
	 * @param location
	 * @param vecx
	 * @param vecy
	 */
	public void addModule(final Module module, final Point2D location, final double vecx, final double vecy) {
		handler.addModule(module, location, vecx, vecy);
		refresh();
		mapViewPane.reset();
		mapViewPane.selectSelector();
		mainFrame.repaint();
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
		final MapNode node = chooseNode();
		if (node != null) {
			handler.changeBeginNode(edge, node);
			refresh();
			mapViewPane.selectSelector();
			mapViewPane.reset();
			mapViewPane.setSelectedElement(edge);
			mapElementPane.setSelectedElement(edge);
			mainFrame.repaint();
		}
	}

	/**
	 * @param edge
	 */
	public void changeEndNode(final MapEdge edge) {
		final MapNode node = chooseNode();
		if (node != null) {
			handler.changeEndNode(edge, node);
			refresh();
			mapViewPane.selectSelector();
			mapViewPane.reset();
			mapViewPane.setSelectedElement(edge);
			mapElementPane.setSelectedElement(edge);
			mainFrame.repaint();
		}
	}

	/**
	 * @param edge
	 * @param priority
	 */
	public void changePriority(final MapEdge edge, final int priority) {
		handler.changePriority(edge, priority);
	}

	/**
	 * @param edge
	 * @param speedLimit
	 */
	public void changeSpeedLimit(final MapEdge edge, final double speedLimit) {
		handler.changeSpeedLimit(edge, speedLimit);
	}

	/**
	 * @return
	 */
	private MapNode chooseNode() {
		stopSimulation();
		nodeChooser.clearSelection();
		final int opt = JOptionPane.showConfirmDialog(mainFrame, nodeChooser,
				Messages.getString("RouteMediator.nodeChooser.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		startSimulation();
		if (opt == JOptionPane.OK_OPTION) {
			return nodeChooser.getSelectedNode();
		} else {
			return null;
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
	 * @param begin
	 * @param end
	 */
	public void createEdge(final Point2D begin, final Point2D end) {
		final MapEdge edge = handler.createEdge(begin, end);
		refresh();
		mapViewPane.reset();
		mapViewPane.setSelectedElement(edge);
		mapElementPane.setSelectedElement(edge);
		mainFrame.repaint();
	}

	/**
	 *
	 * @return
	 */
	public String createNodeName(final MapNode node) {
		final int i = handler.getNodeIndex(node);
		return MessageFormat.format(nodeNamePattern, new Object[] { i + 1 });
	}

	/**
	     *
	     */
	public void exit() {
		System.exit(0);
	}

	/**
	 * @param point
	 * @param precision
	 * @return
	 */
	public MapElement findElement(final Point2D point, final double precision) {
		return handler.findElement(point, precision);
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public MapNode findSiteNode(final String name) {
		return nodeNameMap.get(name);
	}

	/**
	 * @param selectedElement
	 * @return
	 */
	public int getEdgeListIndex(final MapElement selectedElement) {
		final int n = edgeList.getSize();
		for (int i = 0; i < n; ++i) {
			final MapEdgeEntry entry = edgeList.get(i);
			if (entry.getEdge().equals(selectedElement)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return
	 */
	public Iterable<MapEdge> getMapEdges() {
		return handler.getMapEdges();
	}

	/**
	 * Return the color of a node
	 *
	 * @param node the node
	 * @return the color
	 */
	public Color getNodeColor(final MapNode node) {
		return nodeColorMap.get(node);
	}

	/**
	 * @return the nodeList
	 */
	public DefaultListModel<MapNodeEntry> getNodeList() {
		return nodeList;
	}

	/**
	 * @param selectedElement
	 * @return
	 */
	public int getNodeListIndex(final MapElement selectedElement) {
		final int n = nodeList.getSize();
		for (int i = 0; i < n; ++i) {
			final MapNodeEntry entry = nodeList.get(i);
			if (entry.getNode().equals(selectedElement)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return
	 */
	public Iterable<MapNode> getNodes() {
		return handler.getNodes();
	}

	/**
	 *
	 * @return
	 */
	public Iterable<Path> getPaths() {
		return handler.getPaths();
	}

	/**
	 * @return
	 * @return
	 */
	public Iterable<SiteNode> getSiteNodes() {
		return handler.getSiteNodes();
	}

	/**
	 * @return
	 * @return
	 */
	public Iterable<Veicle> getVeicles() {
		return handler.getVeicles();
	}

	/**
	 * @param edge
	 */
	private void handleEdgeSelection(final MapEdge edge) {
		if (edge != null) {
			handler.setTemplate(edge);
		}
		mapElementPane.setSelectedElement(edge);
	}

	/**
	 * @param site
	 */
	private void handleSiteSelection(final SiteNode site) {
		if (site != null) {
			handler.setTemplate(site);
		}
		mapElementPane.setSelectedElement(site);
	}

	public void init() {
		final MapElementListener mapViewListener = new MapElementListener() {

			@Override
			public void edgeSelected(final MapElementEvent mapElementEvent) {
				final MapEdge edge = mapElementEvent.getEdge();
				handleEdgeSelection(edge);
				explorerPane.setSelectedElement(edge);
			}

			@Override
			public void nodeSelected(final MapElementEvent mapElementEvent) {
				final MapNode node = mapElementEvent.getNode();
				mapElementPane.setSelectedElement(node);
				explorerPane.setSelectedElement(node);
			}

			@Override
			public void siteSelected(final MapElementEvent mapElementEvent) {
				final SiteNode site = mapElementEvent.getSite();
				handleSiteSelection(site);
				explorerPane.setSelectedElement(site);
			}
		};
		final MapElementListener explorerListener = new MapElementListener() {

			@Override
			public void edgeSelected(final MapElementEvent mapElementEvent) {
				final MapEdge edge = mapElementEvent.getEdge();
				handleEdgeSelection(edge);
				mapViewPane.setSelectedElement(edge);
				if (edge != null) {
					mapViewPane.scrollTo(edge);
				}
			}

			@Override
			public void nodeSelected(final MapElementEvent mapElementEvent) {
				final MapNode node = mapElementEvent.getNode();
				mapElementPane.setSelectedElement(node);
				mapViewPane.setSelectedElement(node);
				if (node != null) {
					mapViewPane.scrollTo(node);
				}
			}

			@Override
			public void siteSelected(final MapElementEvent mapElementEvent) {
				final SiteNode site = mapElementEvent.getSite();
				handleSiteSelection(site);
				mapViewPane.setSelectedElement(site);
				if (site != null) {
					mapViewPane.scrollTo(site);
				}
			}
		};
		mapViewPane.addMapElementListener(mapViewListener);
		explorerPane.addMapElementListener(explorerListener);
		mapViewPane.setMediator(this);
		explorerPane.setMediator(this);
		mapElementPane.setMediator(this);
		loadDefault();
		final List<Module> modules = new ArrayList<Module>(0);
		try {
			handler.retrieveModule(modules);
		} catch (final Exception e) {
			showError(e);
		}
		mapViewPane.setModule(modules);
		mapViewPane.reset();
		refresh();
	}

	/**
	 * Initializes the map of colors of nodes
	 */
	private void initNodeColorMap() {
		int n = computeSiteCount();
		nodeColorMap = new HashMap<MapNode, Color>(n);
		int i = 0;
		n--;
		final SwingUtils util = SwingUtils.getInstance();
		for (final SiteNode node : getSiteNodes()) {
			final double value = (double) i / n;
			final Color color = util.computeColor(value, NODE_SATURATION);
			nodeColorMap.put(node, color);
			++i;
		}
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
	 * @param list
	 */
	public void loadPaths(final List<Path> list) {
		handler.loadPaths(list);
	}

	/**
	     *
	     */
	public void newMap() {
		handler.clearMap();
		refresh();
		mapViewPane.selectSelector();
		mapViewPane.reset();
		mapViewPane.setSelectedElement((MapNode) null);
		mapElementPane.setSelectedElement((MapNode) null);
		mainFrame.repaint();
	}

	/**
	     *
	     *
	     */
	public void newRandomMap() {
		stopSimulation();
		mapProfilePane.setDifficultyOnly(false);
		final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
				Messages.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			final MapProfile profile = new MapProfile();
			mapProfilePane.retrieveProfile(profile);
			handler.createRandomMap(profile);
			refresh();
			mapViewPane.selectSelector();
			mapViewPane.reset();
			mapViewPane.setSelectedElement((MapNode) null);
			mapElementPane.setSelectedElement((MapNode) null);
			mainFrame.repaint();
		}
		startSimulation();
	}

	/**
	 * 
	 */
	public void open() {
		stopSimulation();
		final int choice = fileChooser.showOpenDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			if (!file.canRead()) {
				showError(Messages.getString("RouteMediator.readError.message"), new Object[] { file }); //$NON-NLS-1$
			} else {
				try {
					handler.load(file);
					mainFrame.setSaveActionEnabled(true);
					mainFrame.setTitle(file.getName());
				} catch (final SAXParseException e) {
					logger.error(e.getMessage(), e);
					showError(Messages.getString("RouteMediator.parseError.message"), new Object[] { e.getMessage(), //$NON-NLS-1$
							e.getLineNumber(), e.getColumnNumber() });
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
					showError(e.getMessage());
				} catch (final Error e) {
					logger.error(e.getMessage(), e);
					showError(e);
				}
				refresh();
				mapViewPane.reset();
			}
		}
		startSimulation();
	}

	/**
	     *
	     */
	public void optimize() {
		stopSimulation();
		final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
				Messages.getString("RouteMediator.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			final double speedLimit = optimizePane.getSpeedLimit();
			final boolean optimizeSpeed = optimizePane.isOptimizeSpeed();
			final boolean optimizeNodes = optimizePane.isOptimizeNodes();
			handler.optimize(optimizeNodes, optimizeSpeed, speedLimit);
			refresh();
			mapViewPane.reset();
		}
		startSimulation();
	}

	/**
	     *
	     */
	private void performTimeTick() {
		final long now = System.currentTimeMillis();
		long interval = now - start;
		start = now;
		if (interval < TIME_INTERVAL) {
			interval = TIME_INTERVAL;
		}
		handler.setTimeInterval(interval * 1e-3f * speedSimulation);
		handler.performSimulation();
		mapViewPane.repaint();
	}

	/**
	 * Randomize the traffic generator
	 */
	public void randomize() {
		stopSimulation();
		mapProfilePane.setDifficultyOnly(true);
		final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
				Messages.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			final MapProfile profile = new MapProfile();
			mapProfilePane.retrieveProfile(profile);
			handler.randomize(profile);
			refresh();
			mapViewPane.selectSelector();
			mapViewPane.reset();
			mainFrame.repaint();
		}
		startSimulation();
	}

	/**
	     *
	     *
	     */
	private void refresh() {
		initNodeColorMap();
		nodeNameMap.clear();
		nodeList.removeAllElements();
		int i = 1;
		for (final MapNode node : handler.getNodes()) {
			final String name = MessageFormat.format(nodeNamePattern, new Object[] { i });
			nodeNameMap.put(name, node);
			nodeList.addElement(new MapNodeEntry(name, node));
			++i;
		}
		edgeList.removeAllElements();
		i = 1;
		for (final MapEdge edge : handler.getMapEdges()) {
			final String begin = retrieveNodeName(edge.getBegin());
			final String end = retrieveNodeName(edge.getEnd());
			edgeList.addElement(
					new MapEdgeEntry(MessageFormat.format(edgeNamePattern, new Object[] { i, begin, end }), edge));
			++i;
		}
		explorerPane.setNodeList(nodeList);
		explorerPane.setEdgeList(edgeList);
		nodeChooser.setNodeList(nodeList);
	}

	/**
	 * @param edge
	 */
	public void remove(final MapEdge edge) {
		handler.remove(edge);
		refresh();
		mapViewPane.setSelectedElement((MapEdge) null);
		mapElementPane.setSelectedElement((MapEdge) null);
		mainFrame.repaint();
	}

	/**
	 * @param node
	 */
	public void remove(final MapNode node) {
		handler.remove(node);
		refresh();
		mapViewPane.setSelectedElement((MapNode) null);
		mapElementPane.setSelectedElement((MapNode) null);
		mainFrame.repaint();
	}

	/**
	 * @param edge
	 * @return
	 */
	public String retrieveEdgeName(final MapEdge edge) {
		final int n = edgeList.getSize();
		for (int i = 0; i < n; ++i) {
			final MapEdgeEntry entry = edgeList.get(i);
			if (entry.getEdge().equals(edge)) {
				return entry.getName();
			}
		}
		final String begin = retrieveNodeName(edge.getBegin());
		final String end = retrieveNodeName(edge.getEnd());
		return MessageFormat.format(defaultEdgeName, new Object[] { begin, end });
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
		return MessageFormat.format(defaultNodeName, new Object[] { location.getX(), location.getY() });
	}

	/**
	     *
	     */
	public void save() {
		final File file = fileChooser.getSelectedFile();
		if (file.exists() && !file.canWrite()) {
			showError(Messages.getString("RouteMediator.writeError.message"), new Object[] { file }); //$NON-NLS-1$
		} else {
			try {
				handler.save(file);
				mainFrame.setSaveActionEnabled(true);
				mainFrame.setTitle(file.getPath());
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
				showError(e);
			}
		}
	}

	/**
	     *
	     */
	public void saveAs() {
		stopSimulation();
		final int choice = fileChooser.showSaveDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			save();
		}
		startSimulation();
	}

	/**
	 * @param explorerPane the explorerPane to set
	 */
	public void setExplorerPane(final ExplorerPane explorerPane) {
		this.explorerPane = explorerPane;
	}

	/**
	 *
	 */
	public void setFrequence() {
		stopSimulation();
		final double frequence = handler.getFrequence();
		frequencePane.setFrequence(frequence);
		final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencePane,
				Messages.getString("RouteMediator.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			handler.setFrequence(frequencePane.getFrequence());
			refresh();
			mapViewPane.selectSelector();
			mapViewPane.reset();
			mainFrame.repaint();
		}
		startSimulation();
	}

	/**
	 * @param mainFrame the mainFrame to set
	 */
	public void setMainFrame(final MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	/**
	 * @param mapElementPane the mapElementPane to set
	 */
	public void setMapElementPane(final MapElementPane mapElementPane) {
		this.mapElementPane = mapElementPane;
	}

	/**
	 * @param mapViewPane the mapViewPane to set
	 */
	public void setMapViewPane(final MapViewPane mapViewPane) {
		this.mapViewPane = mapViewPane;
	}

	/**
	 *
	 */
	public void setRouteSetting() {
		stopSimulation();
		routesPane.loadPath();
		final int opt = JOptionPane.showConfirmDialog(mainFrame, routesPane,
				Messages.getString("RouteMediator.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			routesPane.copyPathsTo();
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
		showError("{0}", new Object[] { e.getMessage(), //$NON-NLS-1$
				e.getMessage() });
	}

	/**
	     *
	     *
	     */
	public void showInfos() {
		stopSimulation();
		final RouteInfos infos = new RouteInfos();
		handler.computeRouteInfos(infos);
		final RouteInfoModel model = new RouteInfoModel();
		model.setInfos(infos);
		final InfosTable table = new InfosTable(model);
		table.setMediator(this);
		final JScrollPane sp = new JScrollPane(table);
		final Component pane = sp;
		JOptionPane.showMessageDialog(mainFrame, pane, Messages.getString("RouteMediator.infoPane.title"), //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
		startSimulation();
	}

	/**
	 *
	 */
	public void showTrafficInfos() {
		stopSimulation();
		final List<TrafficInfo> map = new ArrayList<TrafficInfo>(0);
		handler.computeTrafficInfos(map);
		final TrafficInfoModel model = new TrafficInfoModel();
		model.setMediator(this);
		model.setInfos(map);
		final TrafficInfoTable table = new TrafficInfoTable(model);
		table.setMediator(this);
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
	public void start() {
		mapViewPane.scaleToFit();
		setSpeedSimulation(1f);
		startSimulation();
	}

	/**
	     *
	     *
	     */
	private void startSimulation() {
		if (!paused && !timer.isRunning()) {
			start = System.currentTimeMillis();
			timer.start();
		}
	}

	/**
	     *
	     *
	     */
	private void stopSimulation() {
		if (timer.isRunning()) {
			timer.stop();
		}
	}

	/**
	     *
	     */
	public void toogleSimulation() {
		paused = !paused;
		if (paused) {
			stopSimulation();
		} else {
			startSimulation();
		}
	}

	/**
	 * @param site
	 */
	public void transformToNode(final SiteNode site) {
		final MapNode node = handler.transformToNode(site);
		refresh();
		mapViewPane.reset();
		mapViewPane.setSelectedElement(node);
		mapElementPane.setSelectedElement(node);
		mainFrame.repaint();
	}

	/**
	 * @param node
	 */
	public void transformToSite(final MapNode node) {
		final SiteNode site = handler.transformToSite(node);
		refresh();
		mapViewPane.reset();
		mapViewPane.setSelectedElement(site);
		mapElementPane.setSelectedElement(site);
		mainFrame.repaint();
	}
}