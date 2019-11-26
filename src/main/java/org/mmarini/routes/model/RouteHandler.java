/*
 * RouteHandler.java
 *
 * $Id: RouteHandler.java,v 1.16 2010/10/19 20:33:00 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: RouteHandler.java,v 1.16 2010/10/19 20:33:00 marco Exp $
 *
 */
public class RouteHandler {

	private final Simulator simulator;

	/**
	     *
	     */
	public RouteHandler() {
		simulator = new Simulator();
	}

	/**
	 * @param vecy
	 * @see org.mmarini.routes.model.RouteHandler#addModule(org.mmarini.routes.model.Module,
	 *      java.awt.geom.Point2D, double)
	 */
	public void addModule(final Module module, final Point2D location, final double vecx, final double vecy) {
		simulator.add(module, location, vecx, vecy);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#centerMap(java.awt.geom.Point2D)
	 */
	public void centerMap(final Point2D point) {
		simulator.centerMap(point);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changeBeginNode(org.mmarini.routes.model.MapEdge)
	 */
	public void changeBeginNode(final MapEdge edge, final MapNode node) {
		simulator.changeBeginNode(edge, node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changeEndNode(org.mmarini.routes.model.MapEdge,
	 *      org.mmarini.routes.model.MapNode)
	 */
	public void changeEndNode(final MapEdge edge, final MapNode node) {
		simulator.changeEndNode(edge, node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changePriority(org.mmarini.routes.model.MapEdge,
	 *      int)
	 */
	public void changePriority(final MapEdge edge, final int priority) {
		edge.setPriority(priority);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changeSpeedLimit(org.mmarini.routes.model.MapEdge,
	 *      double)
	 */
	public void changeSpeedLimit(final MapEdge edge, final double speedLimit) {
		edge.setSpeedLimit(speedLimit);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#clearMap()
	 */
	public void clearMap() {
		simulator.clear();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#computeMapBound(Rectangle2D)
	 */
	public void computeMapBound(final Rectangle2D bound) {
		simulator.computeMapBound(bound);
	}

	/**
	 * Computes the route informations
	 *
	 * @param infos the routes information
	 */
	public void computeRouteInfos(final RouteInfos infos) {
		simulator.computeRouteInfos(infos);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#computeSiteCount()
	 */
	public int computeSiteCount() {
		return simulator.computeSiteCount();
	}

	/**
	 * Computes the traffic information map for the different destinations
	 *
	 * @param infos the result list of traffic information
	 */
	public void computeTrafficInfos(final List<TrafficInfo> infos) {
		simulator.computeTrafficInfos(infos);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#createEdge(java.awt.geom.Point2D,
	 *      java.awt.geom.Point2D)
	 */
	public MapEdge createEdge(final Point2D begin, final Point2D end) {
		return simulator.createEdge(begin, end);
	}

	/**
	     *
	     */
	public void createRandomMap(final MapProfile profile) {
		simulator.createRandomMap(profile);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#findElement(java.awt.geom.Point2D)
	 */
	public MapElement findElement(final Point2D point, final double precision) {
		return simulator.findElement(point, precision);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#findNode(java.awt.geom.Point2D,
	 *      double)
	 */
	public MapNode findNode(final Point2D point, final double precision) {
		return simulator.findNode(point, precision);
	}

	/**
	 *
	 * @return
	 */
	public double getFrequence() {
		return simulator.getFrequence();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getMapEdges()
	 */
	public Iterable<MapEdge> getMapEdges() {
		return simulator.getMapEdges();
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public int getNodeIndex(final MapNode node) {
		return simulator.getNodeIndex(node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getNodes()
	 */
	public Iterable<MapNode> getNodes() {
		return simulator.getMapNodes();
	}

	/**
	 *
	 * @return
	 */
	public Iterable<Path> getPaths() {
		return simulator.getPaths();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getSiteNodes()
	 */
	public Iterable<SiteNode> getSiteNodes() {
		return simulator.getSiteNodes();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getVeicles()
	 */
	public Iterable<Veicle> getVeicles() {
		return simulator.getVeicles();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#load(java.io.File)
	 */
	public void load(final File file) throws ParserConfigurationException, SAXException, IOException {
		SimulatorLoader.load(file, simulator);
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @see org.mmarini.routes.model.RouteHandler#load(java.net.URL)
	 */
	public void load(final URL url) throws ParserConfigurationException, SAXException, IOException {
		SimulatorLoader.load(url, simulator);
	}

	/**
	 *
	 * @param paths
	 */
	public void loadPaths(final List<Path> paths) {
		simulator.loadPaths(paths);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#optimize(boolean, bdouble)
	 */
	public void optimize(final boolean optimizeNodes, final boolean optimizeSpeed, final double speedLimit) {
		if (optimizeNodes) {
			simulator.optimizeNodes();
		}
		if (optimizeSpeed) {
			simulator.optimizeSpeed(speedLimit);
		}
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#performSimulation()
	 */
	public void performSimulation() {
		simulator.performSimulation();
	}

	/**
	 * Randomize the traffic generator
	 *
	 * @param profile
	 */
	public void randomize(final MapProfile profile) {
		simulator.randomize(profile);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#remove(org.mmarini.routes.model.MapEdge)
	 */
	public void remove(final MapEdge edge) {
		simulator.remove(edge);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#delete(org.mmarini.routes.model.MapNode)
	 */
	public void remove(final MapNode node) {
		simulator.remove(node);
	}

	/**
	 * @throws JsonProcessingException
	 * @throws IOException
	 *
	 */
	public void retrieveModule(final List<Module> modules) throws JsonProcessingException, IOException {
		final File path = new File("modules");
		if (path.isDirectory()) {
			final Map<String, Module> moduleMap = Arrays.stream(path.listFiles())
					.filter(file -> file.isFile() && file.canRead() && file.getName().endsWith(".yml"))
					.collect(Collectors.<File, String, Module>toMap(file -> file.getName(),
							arg0 -> ModuleLoader.load(arg0)));
			final TreeSet<String> names = new TreeSet<String>(moduleMap.keySet());
			names.stream().map(name -> moduleMap.get(name)).forEach(modules::add);
		}
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#save(java.io.File)
	 */
	public void save(final File file) throws ParserConfigurationException, TransformerException, SAXException {
		SimulatorWriter.write(file, simulator);
	}

	/**
	 *
	 * @param frequence
	 */
	public void setFrequence(final double frequence) {
		simulator.setFrequence(frequence);
	}

	/**
	     *
	     */
	public void setTemplate(final MapEdge edge) {
		simulator.setEdgeTemplate(edge);
	}

	/**
	     *
	     */
	public void setTemplate(final SiteNode node) {
		simulator.setSiteTemplate(node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#setTimeInterval(double)
	 */
	public void setTimeInterval(final double timeInterval) {
		simulator.setTimeInterval(timeInterval);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#transformToNode(org.mmarini.routes.model.SiteNode)
	 */
	public MapNode transformToNode(final SiteNode site) {
		return simulator.transformToNode(site);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#transformToSite(org.mmarini.routes.model.MapNode)
	 */
	public SiteNode transformToSite(final MapNode node) {
		return simulator.transformToSite(node);
	}
}
