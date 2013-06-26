/*
 * RouteHandler.java
 *
 * $Id: Simulator.java,v 1.18 2010/10/19 20:32:59 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mmarini.routes.xml.Dumpable;
import org.mmarini.routes.xml.Dumper;
import org.mmarini.routes.xml.Path;
import org.w3c.dom.Element;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Simulator.java,v 1.18 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class Simulator implements Constants, Dumpable {

	private static final double DEFAULT_FREQUENCE = 1.;
	private static final double DEFAULT_MIN_WEIGHT = 1.;
	private static final double NO_CONNECTION = Double.POSITIVE_INFINITY;
	private static final double MAX_TIME = 1e10f;
	private static final int NO_NODE = -1;
	private static final double DEFAULT_MAP_SIZE = 5000f;
	private static final double DEFAULT_PRECISION = 0.5f;
	private static final double TIME_INTERVAL = 40e-3f;
	private static final Rectangle2D DEFAULT_MAP_BOUND = new Rectangle2D.Double(
			0, 0, DEFAULT_MAP_SIZE, DEFAULT_MAP_SIZE);

	private List<MapNode> nodes;
	private List<SiteNode> sites;
	private List<MapEdge> edges;
	private List<Veicle> veicleList;
	private SimContextImpl context;
	private ExtendedRandom random;
	private List<Veicle> temporaryList;
	private double[][] timeMatrix;
	private int[][] previousMatrix;
	private MapEdge[][] edgeMap;
	private MapEdge edgeTemplate;
	private SiteNode siteTemplate;
	private Map<SiteNode, Map<SiteNode, Double>> transitTime;
	private List<Path> path;
	private double frequence;

	/**
         * 
         */
	public Simulator() {
		nodes = new ArrayList<MapNode>(0);
		edges = new ArrayList<MapEdge>(0);
		sites = new ArrayList<SiteNode>(0);
		path = new ArrayList<Path>(0);
		transitTime = new HashMap<SiteNode, Map<SiteNode, Double>>();
		veicleList = new ArrayList<Veicle>(0);
		temporaryList = new ArrayList<Veicle>(0);
		context = new SimContextImpl();
		context.setSimulator(this);
		random = new ExtendedRandom();
		siteTemplate = new SiteNode();
		edgeTemplate = new MapEdge();
		frequence = DEFAULT_FREQUENCE;

		edgeTemplate.setPriority(DEFAULT_EDGE_PRIORITY);
		edgeTemplate.setSpeedLimit(DEFAULT_SPEED_LIMIT);
		context.setTime(TIME_INTERVAL);
	}

	/**
	 * @param edge
	 */
	public void add(MapEdge edge) {
		edges.add(edge);
	}

	/**
	 * @param node
	 */
	public void add(MapNode node) {
		nodes.add(node);
	}

	/**
	 * @param module
	 * @param location
	 * @param vecx
	 * @param vecy
	 */
	public void add(Module module, Point2D location, double vecx, double vecy) {
		Map<MapNode, MapNode> table = new HashMap<MapNode, MapNode>(0);
		AffineTransform tr = new AffineTransform();
		tr.translate(location.getX(), location.getY());
		tr.rotate(vecx, vecy);
		for (MapNode node : module.getNodes()) {
			MapNode simNode = (MapNode) node.clone();
			Point2D l = simNode.getLocation();
			tr.transform(l, l);
			simNode.setLocation(l);
			table.put(node, simNode);
			add(simNode);
		}
		for (MapEdge edge : module.getEdges()) {
			MapEdge newEdge = new MapEdge();
			newEdge.setPriority(edge.getPriority());
			newEdge.setSpeedLimit(edge.getSpeedLimit());
			MapNode begin = table.get(edge.getBegin());
			newEdge.setBegin(begin);
			MapNode end = table.get(edge.getEnd());
			newEdge.setEnd(end);
			add(newEdge);
		}
		init();
	}

	/**
	 * 
	 * @param path
	 */
	public void add(Path path) {
		this.path.add(path);
	}

	/**
	 * @param site
	 */
	public void add(SiteNode site) {
		nodes.add(site);
		sites.add(site);
	}

	/**
	 * @param point
	 */
	public void centerMap(Point2D point) {
		double x = point.getX();
		double y = point.getY();
		for (MapNode node : nodes) {
			Point2D location = node.getLocation();
			node.setLocation((location.getX() - x), (location.getY() - y));
		}
	}

	/**
	 * @param edge
	 * @param node
	 */
	public void changeBeginNode(MapEdge edge, MapNode node) {
		edge.setBegin(node);
		init();
	}

	/**
	 * @param edge
	 * @param node
	 */
	public void changeEndNode(MapEdge edge, MapNode node) {
		edge.setEnd(node);
		init();
	}

	/**
         * 
         */
	public void clear() {
		edges.clear();
		nodes.clear();
		sites.clear();
		veicleList.clear();
		path.clear();
	}

	/**
         * 
         */
	private void computeConnectionMatrix() {
		int n = nodes.size();
		for (int i = 0; i < n; ++i) {
			MapEdge[] mapEdgesi = edgeMap[i];
			double[] tmi = timeMatrix[i];
			int[] pmi = previousMatrix[i];
			for (int j = 0; j < n; ++j) {
				MapEdge edge = mapEdgesi[j];
				if (edge != null) {
					double t = edge.computeExpectedTransitTime();
					if (t >= NO_CONNECTION)
						t = MAX_TIME;
					tmi[j] = t;
					pmi[j] = i;
				} else {
					tmi[j] = NO_CONNECTION;
					pmi[j] = NO_NODE;
				}
			}
		}
		/*
		 * Floyd algorithm
		 */
		for (int k = 0; k < n; ++k) {
			double[] tmk = timeMatrix[k];
			int[] pmk = previousMatrix[k];
			for (int i = 0; i < n; ++i) {
				double[] tmi = timeMatrix[i];
				double tmik = tmi[k];
				int[] pmi = previousMatrix[i];
				for (int j = 0; j < n; ++j) {
					double d = tmik + tmk[j];
					if (d < tmi[j]) {
						tmi[j] = d;
						pmi[j] = pmk[j];
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param bound
	 */
	public void computeMapBound(Rectangle2D bound) {
		if (nodes.isEmpty()) {
			bound.setFrame(DEFAULT_MAP_BOUND);
		} else if (nodes.size() == 1) {
			Point2D location = nodes.get(0).getLocation();
			bound.setFrameFromCenter(location.getX(), location.getY(),
					DEFAULT_MAP_SIZE * 0.5, DEFAULT_MAP_SIZE * 0.5);
		} else {
			Point2D location = nodes.get(0).getLocation();
			bound.setFrame(location.getX(), location.getY(), 0, 0);
			for (MapNode node : nodes) {
				bound.add(node.getLocation());
			}
		}
	}

	/**
	 * 
	 * @param infos
	 */
	public void computeRouteInfos(RouteInfos infos) {
		infos.computeInfos(this);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#computeSiteCount()
	 */
	public int computeSiteCount() {
		return getSiteCount();
	}

	/**
	 * Computes the traffic information map for the different destinations
	 * 
	 * @param infos
	 *            the result list of traffic information
	 */
	public void computeTrafficInfos(List<TrafficInfo> infos) {
		infos.clear();
		Map<SiteNode, TrafficInfo> map = new HashMap<SiteNode, TrafficInfo>();
		for (Veicle v : veicleList) {
			SiteNode site = (SiteNode) v.getDestination();
			TrafficInfo info = map.get(site);
			if (info == null) {
				info = new TrafficInfo();
				info.setDestination(site);
				map.put(site, info);
				infos.add(info);
			}
			info.setVeicleCount(info.getVeicleCount() + 1);
			if (v.isDeleyed()) {
				info.setDelayCount(info.getDelayCount() + 1);
				info.setTotalDelayTime(info.getTotalDelayTime() + v.getDelay());
			}
		}
	}

	/**
	 * Computes the transit time among the sites
	 */
	private void computeTransitTime() {
		int n = nodes.size();
		double[][] time = new double[n][n];
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				if (i != j) {
					MapEdge egde = edgeMap[i][j];
					if (egde != null)
						time[i][j] = egde.computeTransitTime();
					else
						time[i][j] = NO_CONNECTION;
				}
			}
		}
		/*
		 * Floyd algorithm
		 */
		for (int k = 0; k < n; ++k) {
			for (int i = 0; i < n; ++i) {
				for (int j = 0; j < n; ++j) {
					double d = time[i][k] + time[k][j];
					if (d < time[i][j]) {
						time[i][j] = d;
					}
				}
			}
		}
		/*
		 * Convert in map
		 */
		transitTime.clear();
		for (int i = 0; i < n; ++i) {
			MapNode from = getNode(i);
			if (from instanceof SiteNode) {
				for (int j = 0; j < n; ++j) {
					if (i != j) {
						MapNode to = getNode(j);
						if (to instanceof SiteNode
								&& time[i][j] != NO_CONNECTION) {
							Map<SiteNode, Double> map1 = transitTime.get(from);
							if (map1 == null) {
								map1 = new HashMap<SiteNode, Double>();
								transitTime.put((SiteNode) from, map1);
							}
							map1.put((SiteNode) to, time[i][j]);
						}
					}
				}
			}
		}
	}

	/**
	 * @param begin
	 * @param end
	 * @return
	 */
	public MapEdge createEdge(Point2D begin, Point2D end) {
		MapNode bnode = findNode(begin, DEFAULT_PRECISION);
		boolean isNewEdge = false;
		if (bnode == null) {
			bnode = new MapNode();
			bnode.setLocation(begin);
			add(bnode);
			isNewEdge = true;
		}
		MapNode enode = findNode(end, DEFAULT_PRECISION);
		if (enode == null) {
			enode = new MapNode();
			enode.setLocation(end);
			add(enode);
			isNewEdge = true;
		}
		MapEdge edge = null;
		if (!isNewEdge)
			edge = findEdge(bnode, enode);
		if (edge == null) {
			edge = edgeTemplate.createClone();
			edge.setBegin(bnode);
			edge.setEnd(enode);
			add(edge);
			init();
		}
		return edge;
	}

	/**
	 * 
	 * @param departure
	 * @param destination
	 * @param minProfile
	 */
	private void createPath(SiteNode departure, SiteNode destination,
			double minProfile) {
		Path path = new Path();
		path.setDeparture(departure);
		path.setDestination(destination);
		double scale = 1. - minProfile;
		double fr = random.nextDouble() * scale + minProfile;
		path.setWeight(fr);
		add(path);
	}

	/**
	 * 
	 * @param site
	 */
	private void createPathForSite(SiteNode site) {
		for (SiteNode other : sites) {
			if (!site.equals(other)) {
				createPath(site, other, DEFAULT_MIN_WEIGHT);
				createPath(other, site, DEFAULT_MIN_WEIGHT);
			}
		}
	}

	/**
	 * 
	 * @param profile
	 */
	public void createRandomMap(MapProfile profile) {
		clear();
		int n = profile.getSiteCount();
		double mapWidth = profile.getWidth();
		double mapHeight = profile.getHeight();
		for (int i = 0; i < n; ++i) {
			SiteNode site = new SiteNode();
			double x = random.nextDouble() * mapWidth;
			double y = random.nextDouble() * mapHeight;
			site.setLocation(x, y);
			add(site);
		}
		randomize(profile);
	}

	/**
	 * 
	 * @param departure
	 * @param destination
	 */
	private void createVeicleFrom(SiteNode departure, SiteNode destination) {
		MapEdge edge = findNextEdge(departure, destination);
		if (edge != null && !edge.isBusy()) {
			Veicle veicle = new Veicle();
			if (!destination.equals(departure)) {
				Itinerary it = new Itinerary();
				it.setDestination(departure);
				it.setExpectedTime(getExpectedTime(destination, departure));
				veicle.pushDestination(it);
			}
			veicle.setDestination(destination);
			veicle.setExpectedTravelingTime(getExpectedTime(departure,
					destination));
			veicleList.add(veicle);
			edge.push(veicle);
		}
	}

	/**
	 * @param context
	 */
	private void createVeicles() {
		double freq = context.getTime() * frequence / (getSiteCount() - 1) / 2;
		for (Path path : this.path) {
			double lambda = path.getWeight() * freq;
			SiteNode from = path.getDeparture();
			SiteNode to = path.getDestination();
			int m = random.nextPoison(lambda);
			for (int j = 0; j < m; ++j) {
				createVeicleFrom(from, to);
			}
		}
	}

	/**
	 * @param root
	 */
	public void dump(Element root) {
		Dumper utl = Dumper.getInstance();
		utl.dumpObject(root, "context", context);
		utl.dumpObject(root, "nodes", nodes);
		utl.dumpObject(root, "edges", edges);
		utl.dumpObject(root, "veicleList", veicleList);
		utl.dumpObject(root, "edgeTemplate", edgeTemplate);
		utl.dumpObject(root, "siteTemplate", siteTemplate);
		utl.dumpReference(root, "sites", sites);
		Element el = utl.createElement(root, "edgeMap");
		for (int i = 0; i < edgeMap.length; ++i) {
			for (int j = 0; j < edgeMap[i].length; ++j) {
				if (edgeMap[i][j] != null) {
					utl
							.dumpReference(el, "edgeMap", i + "," + j,
									edgeMap[i][j]);
				}
			}
		}
		el = utl.createElement(root, "timeMatrix");
		for (int i = 0; i < timeMatrix.length; ++i) {
			for (int j = 0; j < timeMatrix[i].length; ++j) {
				utl.dumpValue(el, "timeMatrix", i + "," + j, timeMatrix[i][j]);
			}
		}
		el = utl.createElement(root, "previousMatrix");
		for (int i = 0; i < previousMatrix.length; ++i) {
			for (int j = 0; j < previousMatrix[i].length; ++j) {
				utl.dumpValue(el, "previousMatrix", i + "," + j,
						previousMatrix[i][j]);
			}
		}
		utl.dumpReference(root, "temporaryList", temporaryList);
	}

	/**
	 * 
	 * @param begin
	 * @param end
	 * @return
	 */
	private MapEdge findEdge(MapNode begin, MapNode end) {
		return edgeMap[getNodeIndex(begin)][getNodeIndex(end)];
	}

	/**
	 * @param point
	 * @param precision
	 * @return
	 */
	public MapElement findElement(Point2D point, double precision) {
		MapElement element = findNode(point, precision);
		if (element == null) {
			double dist = precision * precision;
			for (MapEdge e : edges) {
				double d = e.getDistanceSq(point);
				if (d <= dist) {
					dist = d;
					element = e;
				}
			}
		}
		return element;
	}

	/**
	 * @param from
	 * @param to
	 * @return
	 */
	public MapEdge findNextEdge(MapNode from, MapNode to) {
		int i = getNodeIndex(from);
		if (i < 0)
			return null;
		int j = getNodeIndex(to);
		if (j < 0)
			return null;
		MapEdge edge = null;
		do {
			int prev = previousMatrix[i][j];
			if (prev != NO_NODE) {
				edge = edgeMap[prev][j];
			}
			j = prev;
		} while (j != NO_NODE && j != i);
		return edge;
	}

	/**
	 * @param point
	 * @param precision
	 * @return
	 */
	public MapNode findNode(Point2D point, double precision) {
		double dist = precision * precision;
		MapNode element = null;
		for (MapNode n : nodes) {
			double d = n.getDistanceSq(point);
			if (d <= dist) {
				dist = d;
				element = n;
			}
		}
		return element;
	}

	/**
	 * 
	 * @param source
	 * @param dest
	 * @return
	 */
	private double getExpectedTime(SiteNode source, SiteNode dest) {
		Map<SiteNode, Double> map = transitTime.get(source);
		if (map == null)
			return NO_CONNECTION;
		Double value = map.get(dest);
		if (value == null)
			return NO_CONNECTION;
		return value;
	}

	/**
	 * @return the frequence
	 */
	public double getFrequence() {
		return frequence;
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getMapEdges()
	 */
	public Iterable<MapEdge> getMapEdges() {
		return edges;
	}

	/**
	 * @return
	 */
	public Iterable<MapNode> getMapNodes() {
		return nodes;
	}

	/**
	 * @param index
	 * @return
	 */
	private MapNode getNode(int index) {
		return nodes.get(index);
	}

	/**
	 * @param site
	 * @return
	 */
	public int getNodeIndex(MapNode site) {
		return nodes.indexOf(site);
	}

	/**
	 * 
	 * @return
	 */
	public Iterable<Path> getPaths() {
		return path;
	}

	/**
	 * @return
	 */
	private int getSiteCount() {
		return sites.size();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getSiteNodes()
	 */
	public Iterable<SiteNode> getSiteNodes() {
		return sites;
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getVeicles()
	 */
	public Iterable<Veicle> getVeicles() {
		return veicleList;
	}

	/**
         * 
         */
	public void init() {
		int n = nodes.size();
		timeMatrix = new double[n][n];
		previousMatrix = new int[n][n];
		edgeMap = new MapEdge[n][n];
		for (MapEdge edge : this.edges) {
			int i = nodes.indexOf(edge.getBegin());
			int j = nodes.indexOf(edge.getEnd());
			edgeMap[i][j] = edge;
		}
		computeConnectionMatrix();
		computeTransitTime();
	}

	/**
         * 
         */
	public void optimizeNodes() {
		List<MapNode> nodes = new ArrayList<MapNode>(0);
		int n = previousMatrix.length;
		for (int i = 0; i < n; ++i) {
			boolean connected = false;
			for (int j = 0; j < n; ++j) {
				if (previousMatrix[i][j] != NO_NODE
						|| previousMatrix[j][i] != NO_NODE) {
					connected = true;
					break;
				}
			}
			MapNode node = getNode(i);
			if (!connected && !(node instanceof SiteNode)) {
				nodes.add(node);
			}
		}
		for (MapNode node : nodes) {
			remove(node);
		}
		init();
	}

	/**
	 * @param speedLimit
	 */
	public void optimizeSpeed(double speedLimit) {
		for (MapEdge edge : edges) {
			edge.setSpeedLimit(speedLimit);
		}
		computeConnectionMatrix();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#performSimulation()
	 */
	public void performSimulation() {
		SimContext context = this.context;
		double time = context.getTime();
		for (MapEdge edge : edges) {
			edge.updateTransitTime(time);
		}
		computeConnectionMatrix();
		temporaryList.clear();
		temporaryList.addAll(veicleList);
		for (Veicle veicle : temporaryList) {
			veicle.move(context);
		}
		temporaryList.clear();
		createVeicles();
		for (MapEdge edge : edges) {
			edge.dequeue(context);
		}
	}

	/**
	 * Randomize the path
	 * 
	 * @param profile
	 */
	public void randomize(MapProfile profile) {
		path.clear();
		setFrequence(profile.getFrequence());
		for (SiteNode departure : sites) {
			for (SiteNode destination : sites) {
				if (departure != destination) {
					createPath(departure, destination, profile.getMinWeight());
				}
			}
		}
	}

	/**
	 * @param edge
	 */
	public void remove(MapEdge edge) {
		removeSingleEdge(edge);
		init();
	}

	/**
	 * @param node
	 */
	public void remove(MapNode node) {
		removeEdgeByNode(node);
		nodes.remove(node);
		sites.remove(node);
		if (node instanceof SiteNode)
			removePathForSite((SiteNode) node);
		init();
	}

	/**
	 * @param veicle
	 */
	public void remove(Veicle veicle) {
		veicleList.remove(veicle);
	}

	/**
	 * @param node
	 */
	private void removeEdgeByNode(MapNode node) {
		List<MapEdge> list = new ArrayList<MapEdge>(0);
		for (MapEdge edge : edges) {
			if (edge.getBegin().equals(node) || edge.getEnd().equals(node)) {
				list.add(edge);
			}
		}
		for (MapEdge edge : list) {
			removeSingleEdge(edge);
		}
	}

	/**
	 * 
	 * @param site
	 */
	private void removePathForSite(SiteNode site) {
		for (Iterator<Path> it = path.iterator(); it.hasNext();) {
			Path path = it.next();
			if (site.equals(path.getDeparture())
					|| site.equals(path.getDestination())) {
				it.remove();
			}
		}
	}

	/**
	 * @param edge
	 */
	private void removeSingleEdge(MapEdge edge) {
		edge.reset(context);
		edge.setEnd(null);
		edges.remove(edge);
	}

	/**
	 * @param oldNode
	 * @param newNode
	 */
	private void replaceNodeInEdges(MapNode oldNode, MapNode newNode) {
		for (MapEdge edge : edges) {
			edge.replaceNode(oldNode, newNode);
		}
	}

	/**
	 * @param oldNode
	 * @param newNode
	 */
	private void replaceNodeInVeicles(MapNode oldNode, MapNode newNode) {
		for (Veicle veicle : veicleList) {
			veicle.replaceNode(oldNode, newNode);
		}
	}

	/**
	 * @param edgeTemplate
	 *            the edgeTemplate to set
	 */
	public void setEdgeTemplate(MapEdge edgeTemplate) {
		this.edgeTemplate = edgeTemplate;
	}

	/**
	 * @param frequence
	 *            the frequence to set
	 */
	public void setFrequence(double frequence) {
		this.frequence = frequence;
	}

	/**
	 * @param siteTemplate
	 *            the siteTemplate to set
	 */
	public void setSiteTemplate(SiteNode siteTemplate) {
		this.siteTemplate = siteTemplate;
	}

	/**
	 * @param timeInterval
	 */
	public void setTimeInterval(double timeInterval) {
		context.setTime(timeInterval);
	}

	/**
	 * @param site
	 * @return
	 */
	public MapNode transformToNode(SiteNode site) {
		MapNode node = new MapNode();
		node.setLocation(site.getLocation());
		add(node);
		replaceNodeInEdges(site, node);
		replaceNodeInVeicles(site, node);
		removePathForSite(site);
		remove(site);
		return node;
	}

	/**
	 * @param node
	 * @return
	 */
	public SiteNode transformToSite(MapNode node) {
		SiteNode site = siteTemplate.createClone();
		site.setLocation(node.getLocation());
		add(site);
		replaceNodeInEdges(node, site);
		replaceNodeInVeicles(node, site);
		remove(node);
		createPathForSite(site);
		return site;
	}

	/**
	 * 
	 * @param paths
	 */
	public void loadPaths(Collection<Path> paths) {
		path.clear();
		for (Path p : paths) {
			path.add(p.clone());
		}
	}
}
