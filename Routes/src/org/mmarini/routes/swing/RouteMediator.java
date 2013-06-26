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
import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapElement;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.MapProfile;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.RouteHandler;
import org.mmarini.routes.model.RouteInfos;
import org.mmarini.routes.model.SiteNode;
import org.mmarini.routes.model.TrafficInfo;
import org.mmarini.routes.model.Veicle;
import org.mmarini.routes.xml.Path;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: RouteMediator.java,v 1.16 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class RouteMediator {

	private static final int TIME_INTERVAL = 1;

	private MainFrame mainFrame;
	private MapViewPane mapViewPane;
	private ExplorerPane explorerPane;
	private MapElementPane mapElementPane;
	private JFileChooser fileChooser;
	private RouteHandler handler;
	private Timer timer;
	private DefaultListModel nodeList;
	private DefaultListModel edgeList;
	private String nodeNamePattern;
	private String defaultNodeName;
	private OptimizePane optimizePane;
	private RoutePane routesPane;
	private String edgeNamePattern;
	private NodeChooser nodeChooser;
	private MapProfilePane mapProfilePane;
	private FrequencePane frequencePane;
	private long start;
	private double speedSimulation;
	private String defaultEdgeName;
	private Map<MapNode, Color> nodeColorMap;
	private Map<String, MapNode> nodeNameMap;
	private boolean paused;
	private static final double NODE_SATURATION = 1f;

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
		nodeList = new DefaultListModel();
		edgeList = new DefaultListModel();
		nodeChooser = new NodeChooser();
		nodeNamePattern = Messages.getString("RouteMediator.nodeNamePattern"); //$NON-NLS-1$
		defaultNodeName = Messages
				.getString("RouteMediator.defaultNodeNamePattern"); //$NON-NLS-1$
		defaultEdgeName = Messages
				.getString("RouteMediator.defaultEdgeNamePattern"); //$NON-NLS-1$
		edgeNamePattern = Messages.getString("RouteMediator.edgeNamePattern"); //$NON-NLS-1$
		timer = new Timer(TIME_INTERVAL, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				performTimeTick();
			}
		});
		optimizePane = new OptimizePane();

		fileChooser.setFileFilter(new FileNameExtensionFilter(Messages
				.getString("RouteMediator.filetype.title"), //$NON-NLS-1$
				"xml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$
		routesPane.setMediator(this);
	}

	/**
	 * @param module
	 * @param location
	 * @param vecx
	 * @param vecy
	 */
	public void addModule(Module module, Point2D location, double vecx,
			double vecy) {
		handler.addModule(module, location, vecx, vecy);
		refresh();
		mapViewPane.reset();
		mapViewPane.selectSelector();
		mainFrame.repaint();
	}

	/**
	 * @param point
	 */
	public void centerMap(Point2D point) {
		handler.centerMap(point);
		mapViewPane.reset();
		mapViewPane.selectSelector();
		mainFrame.repaint();
	}

	/**
	 * @param edge
	 */
	public void changeBeginNode(MapEdge edge) {
		MapNode node = chooseNode();
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
	public void changeEndNode(MapEdge edge) {
		MapNode node = chooseNode();
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
	public void changePriority(MapEdge edge, int priority) {
		handler.changePriority(edge, priority);
	}

	/**
	 * @param edge
	 * @param speedLimit
	 */
	public void changeSpeedLimit(MapEdge edge, double speedLimit) {
		handler.changeSpeedLimit(edge, speedLimit);
	}

	/**
	 * @return
	 */
	private MapNode chooseNode() {
		stopSimulation();
		nodeChooser.clearSelection();
		int opt = JOptionPane
				.showConfirmDialog(
						mainFrame,
						nodeChooser,
						Messages.getString("RouteMediator.nodeChooser.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		startSimulation();
		if (opt == JOptionPane.OK_OPTION) {
			return nodeChooser.getSelectedNode();
		} else
			return null;
	}

	/**
	 * @param bound
	 */
	public void computeMapBound(Rectangle2D bound) {
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
	public void createEdge(Point2D begin, Point2D end) {
		MapEdge edge = handler.createEdge(begin, end);
		refresh();
		mapViewPane.reset();
		mapViewPane.setSelectedElement(edge);
		mapElementPane.setSelectedElement(edge);
		mainFrame.repaint();
	}

	/**
         * 
         */
	public void dump() {
		try {
			handler.dump();
			JOptionPane.showMessageDialog(mainFrame, Messages
					.getString("RouteMediator.dumpMessage.text")); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
			showError(e);
		}
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
	public MapElement findElement(Point2D point, double precision) {
		return handler.findElement(point, precision);
	}

	/**
	 * @param selectedElement
	 * @return
	 */
	public int getEdgeListIndex(MapElement selectedElement) {
		int n = edgeList.getSize();
		for (int i = 0; i < n; ++i) {
			MapEdgeEntry entry = (MapEdgeEntry) edgeList.get(i);
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
	 * @param node
	 *            the node
	 * @return the color
	 */
	public Color getNodeColor(MapNode node) {
		return nodeColorMap.get(node);
	}

	/**
	 * @return the nodeList
	 */
	public DefaultListModel getNodeList() {
		return nodeList;
	}

	/**
	 * @param selectedElement
	 * @return
	 */
	public int getNodeListIndex(MapElement selectedElement) {
		int n = nodeList.getSize();
		for (int i = 0; i < n; ++i) {
			MapNodeEntry entry = (MapNodeEntry) nodeList.get(i);
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
	private void handleEdgeSelection(MapEdge edge) {
		if (edge != null)
			handler.setTemplate(edge);
		mapElementPane.setSelectedElement(edge);
	}

	/**
	 * @param site
	 */
	private void handleSiteSelection(SiteNode site) {
		if (site != null)
			handler.setTemplate(site);
		mapElementPane.setSelectedElement(site);
	}

	public void init() {
		MapElementListener mapViewListener = new MapElementListener() {

			public void edgeSelected(MapElementEvent mapElementEvent) {
				MapEdge edge = mapElementEvent.getEdge();
				handleEdgeSelection(edge);
				explorerPane.setSelectedElement(edge);
			}

			public void nodeSelected(MapElementEvent mapElementEvent) {
				MapNode node = mapElementEvent.getNode();
				mapElementPane.setSelectedElement(node);
				explorerPane.setSelectedElement(node);
			}

			public void siteSelected(MapElementEvent mapElementEvent) {
				SiteNode site = mapElementEvent.getSite();
				handleSiteSelection(site);
				explorerPane.setSelectedElement(site);
			}
		};
		MapElementListener explorerListener = new MapElementListener() {

			public void edgeSelected(MapElementEvent mapElementEvent) {
				MapEdge edge = mapElementEvent.getEdge();
				handleEdgeSelection(edge);
				mapViewPane.setSelectedElement(edge);
				if (edge != null)
					mapViewPane.scrollTo(edge);
			}

			public void nodeSelected(MapElementEvent mapElementEvent) {
				MapNode node = mapElementEvent.getNode();
				mapElementPane.setSelectedElement(node);
				mapViewPane.setSelectedElement(node);
				if (node != null)
					mapViewPane.scrollTo(node);
			}

			public void siteSelected(MapElementEvent mapElementEvent) {
				SiteNode site = mapElementEvent.getSite();
				handleSiteSelection(site);
				mapViewPane.setSelectedElement(site);
				if (site != null)
					mapViewPane.scrollTo(site);
			}
		};
		mapViewPane.addMapElementListener(mapViewListener);
		explorerPane.addMapElementListener(explorerListener);
		mapViewPane.setMediator(this);
		explorerPane.setMediator(this);
		mapElementPane.setMediator(this);
		loadDefault();
		List<Module> modules = new ArrayList<Module>(0);
		try {
			handler.retrieveModule(modules);
		} catch (Exception e) {
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
		SwingUtils util = SwingUtils.getInstance();
		for (SiteNode node : getSiteNodes()) {
			double value = (double) i / n;
			Color color = util.computeColor(value, NODE_SATURATION);
			nodeColorMap.put(node, color);
			++i;
		}
	}

	/**
         * 
         */
	private void loadDefault() {
		URL url = getClass().getResource("/test.xml"); //$NON-NLS-1$
		if (url != null) {
			try {
				handler.load(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		int opt = JOptionPane
				.showConfirmDialog(
						mainFrame,
						mapProfilePane,
						Messages
								.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			MapProfile profile = new MapProfile();
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
		int choice = fileChooser.showOpenDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.canRead()) {
				showError(
						Messages.getString("RouteMediator.readError.message"), new Object[] { file }); //$NON-NLS-1$
			} else {
				try {
					handler.load(file);
					mainFrame.setSaveActionEnabled(true);
					mainFrame.setTitle(file.getName());
				} catch (ParserConfigurationException e) {
					showError(e.getMessage());
					e.printStackTrace();
				} catch (SAXParseException e) {
					e.printStackTrace();
					showError(
							Messages
									.getString("RouteMediator.parseError.message"), new Object[] { e.getMessage(), //$NON-NLS-1$
									e.getLineNumber(), e.getColumnNumber() });
				} catch (SAXException e) {
					e.printStackTrace();
					showError(e);
				} catch (IOException e) {
					e.printStackTrace();
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
		int opt = JOptionPane
				.showConfirmDialog(
						mainFrame,
						optimizePane,
						Messages.getString("RouteMediator.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			double speedLimit = optimizePane.getSpeedLimit();
			boolean optimizeSpeed = optimizePane.isOptimizeSpeed();
			boolean optimizeNodes = optimizePane.isOptimizeNodes();
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
		long now = System.currentTimeMillis();
		long interval = now - start;
		start = now;
		if (interval < TIME_INTERVAL)
			interval = TIME_INTERVAL;
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
		int opt = JOptionPane
				.showConfirmDialog(
						mainFrame,
						mapProfilePane,
						Messages
								.getString("RouteMediator.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			MapProfile profile = new MapProfile();
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
		for (MapNode node : handler.getNodes()) {
			String name = MessageFormat.format(nodeNamePattern,
					new Object[] { i });
			nodeNameMap.put(name, node);
			nodeList.addElement(new MapNodeEntry(name, node));
			++i;
		}
		edgeList.removeAllElements();
		i = 1;
		for (MapEdge edge : handler.getMapEdges()) {
			String begin = retrieveNodeName(edge.getBegin());
			String end = retrieveNodeName(edge.getEnd());
			edgeList.addElement(new MapEdgeEntry(MessageFormat.format(
					edgeNamePattern, new Object[] { i, begin, end }), edge));
			++i;
		}
		explorerPane.setNodeList(nodeList);
		explorerPane.setEdgeList(edgeList);
		nodeChooser.setNodeList(nodeList);
	}

	/**
	 * @param edge
	 */
	public void remove(MapEdge edge) {
		handler.remove(edge);
		refresh();
		mapViewPane.setSelectedElement((MapEdge) null);
		mapElementPane.setSelectedElement((MapEdge) null);
		mainFrame.repaint();
	}

	/**
	 * @param node
	 */
	public void remove(MapNode node) {
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
	public String retrieveEdgeName(MapEdge edge) {
		int n = edgeList.getSize();
		for (int i = 0; i < n; ++i) {
			MapEdgeEntry entry = (MapEdgeEntry) edgeList.get(i);
			if (entry.getEdge().equals(edge))
				return entry.getName();
		}
		String begin = retrieveNodeName(edge.getBegin());
		String end = retrieveNodeName(edge.getEnd());
		return MessageFormat.format(defaultEdgeName,
				new Object[] { begin, end });
	}

	/**
	 * @param node
	 */
	public String retrieveNodeName(MapNode node) {
		int n = nodeList.getSize();
		for (int i = 0; i < n; ++i) {
			MapNodeEntry entry = (MapNodeEntry) nodeList.get(i);
			if (entry.getNode().equals(node))
				return entry.getName();
		}
		Point2D location = node.getLocation();
		return MessageFormat.format(defaultNodeName, new Object[] {
				location.getX(), location.getY() });
	}

	/**
	 * 
	 * @return
	 */
	public String createNodeName(MapNode node) {
		int i = handler.getNodeIndex(node);
		return MessageFormat.format(nodeNamePattern, new Object[] { i + 1 });
	}

	/**
         * 
         */
	public void save() {
		File file = fileChooser.getSelectedFile();
		if (file.exists() && !file.canWrite()) {
			showError(
					Messages.getString("RouteMediator.writeError.message"), new Object[] { file }); //$NON-NLS-1$
		} else {
			try {
				handler.save(file);
				mainFrame.setSaveActionEnabled(true);
				mainFrame.setTitle(file.getPath());
			} catch (Exception e) {
				e.printStackTrace();
				showError(e);
			}
		}
	}

	/**
         * 
         */
	public void saveAs() {
		stopSimulation();
		int choice = fileChooser.showSaveDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			save();
		}
		startSimulation();
	}

	/**
	 * @param explorerPane
	 *            the explorerPane to set
	 */
	public void setExplorerPane(ExplorerPane explorerPane) {
		this.explorerPane = explorerPane;
	}

	/**
	 * 
	 */
	public void setFrequence() {
		stopSimulation();
		double frequence = handler.getFrequence();
		frequencePane.setFrequence(frequence);
		int opt = JOptionPane
				.showConfirmDialog(
						mainFrame,
						frequencePane,
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
	 * @param mainFrame
	 *            the mainFrame to set
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	/**
	 * @param mapElementPane
	 *            the mapElementPane to set
	 */
	public void setMapElementPane(MapElementPane mapElementPane) {
		this.mapElementPane = mapElementPane;
	}

	/**
	 * @param mapViewPane
	 *            the mapViewPane to set
	 */
	public void setMapViewPane(MapViewPane mapViewPane) {
		this.mapViewPane = mapViewPane;
	}

	/**
	 * @param speedSimulation
	 */
	public void setSpeedSimulation(double speedSimulation) {
		this.speedSimulation = speedSimulation;
	}

	/**
	 * @param e
	 */
	private void showError(Exception e) {
		showError("", new Object[] { e.getMessage(), //$NON-NLS-1$
				e.getMessage() });
	}

	/**
	 * @param message
	 */
	private void showError(String message) {
		JOptionPane.showMessageDialog(mainFrame, message, Messages
				.getString("RouteMediator.error.title"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @param pattern
	 * @param arguments
	 */
	private void showError(String pattern, Object[] arguments) {
		showError(MessageFormat.format(pattern, arguments));
	}

	/**
         * 
         * 
         */
	public void showInfos() {
		stopSimulation();
		RouteInfos infos = new RouteInfos();
		handler.computeRouteInfos(infos);
		RouteInfoModel model = new RouteInfoModel();
		model.setInfos(infos);
		InfosTable table = new InfosTable(model);
		table.setMediator(this);
		JScrollPane sp = new JScrollPane(table);
		Component pane = sp;
		JOptionPane
				.showMessageDialog(
						mainFrame,
						pane,
						Messages.getString("RouteMediator.infoPane.title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$	
		startSimulation();
	}

	/**
     * 
     */
	public void showTrafficInfos() {
		stopSimulation();
		List<TrafficInfo> map = new ArrayList<TrafficInfo>(0);
		handler.computeTrafficInfos(map);
		TrafficInfoModel model = new TrafficInfoModel();
		model.setMediator(this);
		model.setInfos(map);
		TrafficInfoTable table = new TrafficInfoTable(model);
		table.setMediator(this);
		Component pane = new JScrollPane(table);
		JOptionPane
				.showMessageDialog(
						mainFrame,
						pane,
						Messages
								.getString("RouteMediator.trafficInfoPane.title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$	
		startSimulation();
	}

	/**
	 * @param point
	 * @param precision
	 */
	public void snapToNode(Point2D point, double precision) {
		MapNode elem = handler.findNode(point, precision);
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
	public void transformToNode(SiteNode site) {
		MapNode node = handler.transformToNode(site);
		refresh();
		mapViewPane.reset();
		mapViewPane.setSelectedElement(node);
		mapElementPane.setSelectedElement(node);
		mainFrame.repaint();
	}

	/**
	 * @param node
	 */
	public void transformToSite(MapNode node) {
		SiteNode site = handler.transformToSite(node);
		refresh();
		mapViewPane.reset();
		mapViewPane.setSelectedElement(site);
		mapElementPane.setSelectedElement(site);
		mainFrame.repaint();
	}

	/**
	 * 
	 */
	public void setRouteSetting() {
		stopSimulation();
		routesPane.loadPath();
		int opt = JOptionPane
				.showConfirmDialog(
						mainFrame,
						routesPane,
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
	 * 
	 * @return
	 */
	public Iterable<Path> getPaths() {
		return handler.getPaths();
	}

	/**
	 * 
	 * @param list
	 */
	public void loadPaths(List<Path> list) {
		handler.loadPaths(list);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public MapNode findSiteNode(String name) {
		return nodeNameMap.get(name);
	}
}