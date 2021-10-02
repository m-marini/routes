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
package org.mmarini.routes.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Simulator.java,v 1.18 2010/10/19 20:32:59 marco Exp $
 */
public class Simulator implements Constants {

    private static final double DEFAULT_MIN_WEIGHT = 1.;
    private static final double NO_CONNECTION = Double.POSITIVE_INFINITY;
    private static final double MAX_TIME = 1e10f;
    private static final int NO_NODE = -1;
    private static final double DEFAULT_MAP_SIZE = 5000f;
    private static final double DEFAULT_PRECISION = 0.5f;
    private static final double TIME_INTERVAL = 40e-3f;
    private static final Rectangle2D DEFAULT_MAP_BOUND = new Rectangle2D.Double(0, 0, DEFAULT_MAP_SIZE,
            DEFAULT_MAP_SIZE);

    private final List<MapNode> nodes;
    private final List<SiteNode> sites;
    private final List<MapEdge> edges;
    private final List<Vehicle> veicleList;
    private final SimContextImpl context;
    private final ExtendedRandom random;
    private final List<Vehicle> temporaryList;
    private final Map<SiteNode, Map<SiteNode, Double>> transitTime;
    private final List<Path> path;
    private double[][] timeMatrix;
    private int[][] previousMatrix;
    private MapEdge[][] edgeMap;
    private MapEdge edgeTemplate;
    private SiteNode siteTemplate;
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
        veicleList = new ArrayList<Vehicle>(0);
        temporaryList = new ArrayList<Vehicle>(0);
        context = new SimContextImpl();
        context.setSimulator(this);
        random = new ExtendedRandom();
        siteTemplate = new SiteNode();
        edgeTemplate = new MapEdge();
        frequence = DEFAULT_FREQUENCE;

        edgeTemplate.setPriority(DEFAULT_PRIORITY);
        edgeTemplate.setSpeedLimit(DEFAULT_SPEED_LIMIT_KMH * MPS_TO_KMH);
        context.setTime(TIME_INTERVAL);
    }

    /**
     * @param edge
     */
    public void add(final MapEdge edge) {
        edges.add(edge);
    }

    /**
     * @param node
     */
    public void add(final MapNode node) {
        nodes.add(node);
    }

    /**
     * @param module
     * @param location
     * @param vecx
     * @param vecy
     */
    public void add(final Module module, final Point2D location, final double vecx, final double vecy) {
        final Map<MapNode, MapNode> table = new HashMap<MapNode, MapNode>(0);
        final AffineTransform tr = new AffineTransform();
        tr.translate(location.getX(), location.getY());
        tr.rotate(vecx, vecy);
        for (final MapNode node : module.getNodes()) {
            final MapNode simNode = (MapNode) node.clone();
            final Point2D l = simNode.getLocation();
            tr.transform(l, l);
            simNode.setLocation(l);
            table.put(node, simNode);
            add(simNode);
        }
        for (final MapEdge edge : module.getEdges()) {
            final MapEdge newEdge = new MapEdge();
            newEdge.setPriority(edge.getPriority());
            newEdge.setSpeedLimit(edge.getSpeedLimit());
            final MapNode begin = table.get(edge.getBegin());
            newEdge.setBegin(begin);
            final MapNode end = table.get(edge.getEnd());
            newEdge.setEnd(end);
            add(newEdge);
        }
        init();
    }

    /**
     * @param path
     */
    public void add(final Path path) {
        this.path.add(path);
    }

    /**
     * @param site
     */
    public void add(final SiteNode site) {
        nodes.add(site);
        sites.add(site);
    }

    /**
     * @param point
     */
    public void centerMap(final Point2D point) {
        final double x = point.getX();
        final double y = point.getY();
        for (final MapNode node : nodes) {
            final Point2D location = node.getLocation();
            node.setLocation((location.getX() - x), (location.getY() - y));
        }
    }

    /**
     * @param edge
     * @param node
     */
    public void changeBeginNode(final MapEdge edge, final MapNode node) {
        edge.setBegin(node);
        init();
    }

    /**
     * @param edge
     * @param node
     */
    public void changeEndNode(final MapEdge edge, final MapNode node) {
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
        final int n = nodes.size();
        for (int i = 0; i < n; ++i) {
            final MapEdge[] mapEdgesi = edgeMap[i];
            final double[] tmi = timeMatrix[i];
            final int[] pmi = previousMatrix[i];
            for (int j = 0; j < n; ++j) {
                final MapEdge edge = mapEdgesi[j];
                if (edge != null) {
                    double t = edge.computeExpectedTransitTime();
                    if (t >= NO_CONNECTION) {
                        t = MAX_TIME;
                    }
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
            final double[] tmk = timeMatrix[k];
            final int[] pmk = previousMatrix[k];
            for (int i = 0; i < n; ++i) {
                final double[] tmi = timeMatrix[i];
                final double tmik = tmi[k];
                final int[] pmi = previousMatrix[i];
                for (int j = 0; j < n; ++j) {
                    final double d = tmik + tmk[j];
                    if (d < tmi[j]) {
                        tmi[j] = d;
                        pmi[j] = pmk[j];
                    }
                }
            }
        }
    }

    /**
     * @param bound
     */
    public void computeMapBound(final Rectangle2D bound) {
        if (nodes.isEmpty()) {
            bound.setFrame(DEFAULT_MAP_BOUND);
        } else if (nodes.size() == 1) {
            final Point2D location = nodes.get(0).getLocation();
            bound.setFrameFromCenter(location.getX(), location.getY(), DEFAULT_MAP_SIZE * 0.5, DEFAULT_MAP_SIZE * 0.5);
        } else {
            final Point2D location = nodes.get(0).getLocation();
            bound.setFrame(location.getX(), location.getY(), 0, 0);
            for (final MapNode node : nodes) {
                bound.add(node.getLocation());
            }
        }
    }

    /**
     * @param infos
     */
    public void computeRouteInfos(final RouteInfos infos) {
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
     * @param infos the result list of traffic information
     */
    public void computeTrafficInfos(final List<TrafficInfo> infos) {
        infos.clear();
        final Map<SiteNode, TrafficInfo> map = new HashMap<SiteNode, TrafficInfo>();
        for (final Vehicle v : veicleList) {
            final SiteNode site = (SiteNode) v.getDestination();
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
        final int n = nodes.size();
        final double[][] time = new double[n][n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != j) {
                    final MapEdge egde = edgeMap[i][j];
                    if (egde != null) {
                        time[i][j] = egde.computeTransitTime();
                    } else {
                        time[i][j] = NO_CONNECTION;
                    }
                }
            }
        }
        /*
         * Floyd algorithm
         */
        for (int k = 0; k < n; ++k) {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    final double d = time[i][k] + time[k][j];
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
            final MapNode from = getNode(i);
            if (from instanceof SiteNode) {
                for (int j = 0; j < n; ++j) {
                    if (i != j) {
                        final MapNode to = getNode(j);
                        if (to instanceof SiteNode && time[i][j] != NO_CONNECTION) {
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
    public MapEdge createEdge(final Point2D begin, final Point2D end) {
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
        if (!isNewEdge) {
            edge = findEdge(bnode, enode);
        }
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
     * @param departure
     * @param destination
     * @param minProfile
     */
    private void createPath(final SiteNode departure, final SiteNode destination, final double minProfile) {
        final Path path = new Path();
        path.setDeparture(departure);
        path.setDestination(destination);
        final double scale = 1. - minProfile;
        final double fr = random.nextDouble() * scale + minProfile;
        path.setWeight(fr);
        add(path);
    }

    /**
     * @param site
     */
    private void createPathForSite(final SiteNode site) {
        for (final SiteNode other : sites) {
            if (!site.equals(other)) {
                createPath(site, other, DEFAULT_MIN_WEIGHT);
                createPath(other, site, DEFAULT_MIN_WEIGHT);
            }
        }
    }

    /**
     * @param profile
     */
    public void createRandomMap(final MapProfile profile) {
        clear();
        final int n = profile.getSiteCount();
        final double mapWidth = profile.getWidth();
        final double mapHeight = profile.getHeight();
        for (int i = 0; i < n; ++i) {
            final SiteNode site = new SiteNode();
            final double x = random.nextDouble() * mapWidth;
            final double y = random.nextDouble() * mapHeight;
            site.setLocation(x, y);
            add(site);
        }
        randomize(profile);
    }

    /**
     * @param departure
     * @param destination
     */
    private void createVeicleFrom(final SiteNode departure, final SiteNode destination) {
        final MapEdge edge = findNextEdge(departure, destination);
        if (edge != null && !edge.isBusy()) {
            final Vehicle veicle = new Vehicle();
            if (!destination.equals(departure)) {
                final Itinerary it = new Itinerary();
                it.setDestination(departure);
                it.setExpectedTime(getExpectedTime(destination, departure));
                veicle.pushDestination(it);
            }
            veicle.setDestination(destination);
            veicle.setExpectedTravelingTime(getExpectedTime(departure, destination));
            veicleList.add(veicle);
            edge.push(veicle);
        }
    }

    /**
     *
     */
    private void createVeicles() {
        final double freq = context.getTime() * frequence / (getSiteCount() - 1) / 2;
        for (final Path path : this.path) {
            final double lambda = path.getWeight() * freq;
            final SiteNode from = path.getDeparture();
            final SiteNode to = path.getDestination();
            final int m = random.nextPoison(lambda);
            for (int j = 0; j < m; ++j) {
                createVeicleFrom(from, to);
            }
        }
    }

    /**
     * @param begin
     * @param end
     * @return
     */
    private MapEdge findEdge(final MapNode begin, final MapNode end) {
        return edgeMap[getNodeIndex(begin)][getNodeIndex(end)];
    }

    /**
     * @param point
     * @param precision
     * @return
     */
    public MapElement findElement(final Point2D point, final double precision) {
        MapElement element = findNode(point, precision);
        if (element == null) {
            double dist = precision * precision;
            for (final MapEdge e : edges) {
                final double d = e.getDistanceSq(point);
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
    public MapEdge findNextEdge(final MapNode from, final MapNode to) {
        final int i = getNodeIndex(from);
        if (i < 0) {
            return null;
        }
        int j = getNodeIndex(to);
        if (j < 0) {
            return null;
        }
        MapEdge edge = null;
        do {
            final int prev = previousMatrix[i][j];
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
    public MapNode findNode(final Point2D point, final double precision) {
        double dist = precision * precision;
        MapNode element = null;
        for (final MapNode n : nodes) {
            final double d = n.getDistanceSq(point);
            if (d <= dist) {
                dist = d;
                element = n;
            }
        }
        return element;
    }

    /**
     * @param source
     * @param dest
     * @return
     */
    private double getExpectedTime(final SiteNode source, final SiteNode dest) {
        final Map<SiteNode, Double> map = transitTime.get(source);
        if (map == null) {
            return NO_CONNECTION;
        }
        final Double value = map.get(dest);
        if (value == null) {
            return NO_CONNECTION;
        }
        return value;
    }

    /**
     * @return the frequence
     */
    public double getFrequence() {
        return frequence;
    }

    /**
     * @param frequence the frequence to set
     */
    public void setFrequence(final double frequence) {
        this.frequence = frequence;
    }

    /**
     * @see org.mmarini.routes.model.RouteHandler#getMapEdges()
     */
    public List<MapEdge> getMapEdges() {
        return edges;
    }

    /**
     * @return
     */
    public List<MapNode> getMapNodes() {
        return nodes;
    }

    /**
     * @param index
     * @return
     */
    private MapNode getNode(final int index) {
        return nodes.get(index);
    }

    /**
     * @param site
     * @return
     */
    public int getNodeIndex(final MapNode site) {
        return nodes.indexOf(site);
    }

    /**
     * @return
     */
    public List<Path> getPaths() {
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
    public List<SiteNode> getSiteNodes() {
        return sites;
    }

    /**
     * @see org.mmarini.routes.model.RouteHandler#getVeicles()
     */
    public Iterable<Vehicle> getVeicles() {
        return veicleList;
    }

    /**
     *
     */
    public void init() {
        final int n = nodes.size();
        timeMatrix = new double[n][n];
        previousMatrix = new int[n][n];
        edgeMap = new MapEdge[n][n];
        for (final MapEdge edge : this.edges) {
            final int i = nodes.indexOf(edge.getBegin());
            final int j = nodes.indexOf(edge.getEnd());
            edgeMap[i][j] = edge;
        }
        computeConnectionMatrix();
        computeTransitTime();
    }

    /**
     * @param paths
     */
    public void loadPaths(final Collection<Path> paths) {
        path.clear();
        for (final Path p : paths) {
            path.add(p.clone());
        }
    }

    /**
     *
     */
    public void optimizeNodes() {
        final List<MapNode> nodes = new ArrayList<MapNode>(0);
        final int n = previousMatrix.length;
        for (int i = 0; i < n; ++i) {
            boolean connected = false;
            for (int j = 0; j < n; ++j) {
                if (previousMatrix[i][j] != NO_NODE || previousMatrix[j][i] != NO_NODE) {
                    connected = true;
                    break;
                }
            }
            final MapNode node = getNode(i);
            if (!connected && !(node instanceof SiteNode)) {
                nodes.add(node);
            }
        }
        for (final MapNode node : nodes) {
            remove(node);
        }
        init();
    }

    /**
     * @param speedLimit
     */
    public void optimizeSpeed(final double speedLimit) {
        for (final MapEdge edge : edges) {
            edge.setSpeedLimit(speedLimit);
        }
        computeConnectionMatrix();
    }

    /**
     * @see org.mmarini.routes.model.RouteHandler#performSimulation()
     */
    public void performSimulation() {
        final SimContext context = this.context;
        final double time = context.getTime();
        for (final MapEdge edge : edges) {
            edge.updateTransitTime(time);
        }
        computeConnectionMatrix();
        temporaryList.clear();
        temporaryList.addAll(veicleList);
        for (final Vehicle veicle : temporaryList) {
            veicle.move(context);
        }
        temporaryList.clear();
        createVeicles();
        for (final MapEdge edge : edges) {
            edge.dequeue(context);
        }
    }

    /**
     * Randomize the path
     *
     * @param profile
     */
    public void randomize(final MapProfile profile) {
        path.clear();
        setFrequence(profile.getFrequency());
        for (final SiteNode departure : sites) {
            for (final SiteNode destination : sites) {
                if (departure != destination) {
                    createPath(departure, destination, profile.getMinWeight());
                }
            }
        }
    }

    /**
     * @param edge
     */
    public void remove(final MapEdge edge) {
        removeSingleEdge(edge);
        init();
    }

    /**
     * @param node
     */
    public void remove(final MapNode node) {
        removeEdgeByNode(node);
        nodes.remove(node);
        sites.remove(node);
        if (node instanceof SiteNode) {
            removePathForSite((SiteNode) node);
        }
        init();
    }

    /**
     * @param veicle
     */
    public void remove(final Vehicle veicle) {
        veicleList.remove(veicle);
    }

    /**
     * @param node
     */
    private void removeEdgeByNode(final MapNode node) {
        final List<MapEdge> list = new ArrayList<MapEdge>(0);
        for (final MapEdge edge : edges) {
            if (edge.getBegin().equals(node) || edge.getEnd().equals(node)) {
                list.add(edge);
            }
        }
        for (final MapEdge edge : list) {
            removeSingleEdge(edge);
        }
    }

    /**
     * @param site
     */
    private void removePathForSite(final SiteNode site) {
        for (final Iterator<Path> it = path.iterator(); it.hasNext(); ) {
            final Path path = it.next();
            if (site.equals(path.getDeparture()) || site.equals(path.getDestination())) {
                it.remove();
            }
        }
    }

    /**
     * @param edge
     */
    private void removeSingleEdge(final MapEdge edge) {
        edge.reset(context);
        edge.setEnd(null);
        edges.remove(edge);
    }

    /**
     * @param oldNode
     * @param newNode
     */
    private void replaceNodeInEdges(final MapNode oldNode, final MapNode newNode) {
        for (final MapEdge edge : edges) {
            edge.replaceNode(oldNode, newNode);
        }
    }

    /**
     * @param oldNode
     * @param newNode
     */
    private void replaceNodeInVeicles(final MapNode oldNode, final MapNode newNode) {
        for (final Vehicle veicle : veicleList) {
            veicle.replaceNode(oldNode, newNode);
        }
    }

    /**
     * @param edgeTemplate the edgeTemplate to set
     */
    public void setEdgeTemplate(final MapEdge edgeTemplate) {
        this.edgeTemplate = edgeTemplate;
    }

    /**
     * @param siteTemplate the siteTemplate to set
     */
    public void setSiteTemplate(final SiteNode siteTemplate) {
        this.siteTemplate = siteTemplate;
    }

    /**
     * @param timeInterval
     */
    public void setTimeInterval(final double timeInterval) {
        context.setTime(timeInterval);
    }

    /**
     * @param site
     * @return
     */
    public MapNode transformToNode(final SiteNode site) {
        final MapNode node = new MapNode();
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
    public SiteNode transformToSite(final MapNode node) {
        final SiteNode site = siteTemplate.createClone();
        site.setLocation(node.getLocation());
        add(site);
        replaceNodeInEdges(node, site);
        replaceNodeInVeicles(node, site);
        remove(node);
        createPathForSite(site);
        return site;
    }
}
