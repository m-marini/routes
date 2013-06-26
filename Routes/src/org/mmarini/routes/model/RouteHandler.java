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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.mmarini.routes.xml.Dumper;
import org.mmarini.routes.xml.Path;
import org.mmarini.routes.xml.SaxMapParser;
import org.mmarini.routes.xml.XmlMapBuilder;
import org.xml.sax.SAXException;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: RouteHandler.java,v 1.16 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class RouteHandler {

	private Simulator simulator;

	private SaxMapParser parser;

	private XmlMapBuilder builder;

	private Dumper dumper;

	private ModuleBuilder moduleBuilder;

	/**
         * 
         */
	public RouteHandler() {
		simulator = new Simulator();
		builder = new XmlMapBuilder();
		parser = SaxMapParser.getInstance();
		dumper = Dumper.getInstance();
		moduleBuilder = new ModuleBuilder();
	}

	/**
	 * @param vecy
	 * @see org.mmarini.routes.model.RouteHandler#addModule(org.mmarini.routes.model.Module,
	 *      java.awt.geom.Point2D, double)
	 */
	public void addModule(Module module, Point2D location, double vecx,
			double vecy) {
		simulator.add(module, location, vecx, vecy);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#centerMap(java.awt.geom.Point2D)
	 */
	public void centerMap(Point2D point) {
		simulator.centerMap(point);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changeBeginNode(org.mmarini.routes.model.MapEdge)
	 */
	public void changeBeginNode(MapEdge edge, MapNode node) {
		simulator.changeBeginNode(edge, node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changeEndNode(org.mmarini.routes.model.MapEdge,
	 *      org.mmarini.routes.model.MapNode)
	 */
	public void changeEndNode(MapEdge edge, MapNode node) {
		simulator.changeEndNode(edge, node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changePriority(org.mmarini.routes.model.MapEdge,
	 *      int)
	 */
	public void changePriority(MapEdge edge, int priority) {
		edge.setPriority(priority);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#changeSpeedLimit(org.mmarini.routes.model.MapEdge,
	 *      double)
	 */
	public void changeSpeedLimit(MapEdge edge, double speedLimit) {
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
	public void computeMapBound(Rectangle2D bound) {
		simulator.computeMapBound(bound);
	}

	/**
	 * Computes the route informations
	 * 
	 * @param infos
	 *            the routes information
	 */
	public void computeRouteInfos(RouteInfos infos) {
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
	 * @param infos
	 *            the result list of traffic information
	 */
	public void computeTrafficInfos(List<TrafficInfo> infos) {
		simulator.computeTrafficInfos(infos);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#createEdge(java.awt.geom.Point2D,
	 *      java.awt.geom.Point2D)
	 */
	public MapEdge createEdge(Point2D begin, Point2D end) {
		return simulator.createEdge(begin, end);
	}

	/**
         * 
         */
	public void createRandomMap(MapProfile profile) {
		simulator.createRandomMap(profile);
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @see org.mmarini.routes.model.RouteHandler#dump()
	 */
	public void dump() throws ParserConfigurationException, SAXException,
			IOException {
		String filename = MessageFormat.format(
				"dump-{0,date,yyyyMMdd-hhmmss}.xml",
				new Object[] { new Date() });
		dumper.dump(new File(filename), simulator);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#findElement(java.awt.geom.Point2D)
	 */
	public MapElement findElement(Point2D point, double precision) {
		return simulator.findElement(point, precision);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#findNode(java.awt.geom.Point2D,
	 *      double)
	 */
	public MapNode findNode(Point2D point, double precision) {
		return simulator.findNode(point, precision);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getMapEdges()
	 */
	public Iterable<MapEdge> getMapEdges() {
		return simulator.getMapEdges();
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#getNodes()
	 */
	public Iterable<MapNode> getNodes() {
		return simulator.getMapNodes();
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
	public void load(File file) throws ParserConfigurationException,
			SAXException, IOException {
		SimulatorBuilder builder = new SimulatorBuilder();
		builder.setSimulator(simulator);
		parser.parse(file, builder);
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @see org.mmarini.routes.model.RouteHandler#load(java.net.URL)
	 */
	public void load(URL url) throws ParserConfigurationException,
			SAXException, IOException {
		SimulatorBuilder builder = new SimulatorBuilder();
		builder.setSimulator(simulator);
		parser.parse(builder, url);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#optimize(boolean, bdouble)
	 */
	public void optimize(boolean optimizeNodes, boolean optimizeSpeed,
			double speedLimit) {
		if (optimizeNodes)
			simulator.optimizeNodes();
		if (optimizeSpeed)
			simulator.optimizeSpeed(speedLimit);
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
	public void randomize(MapProfile profile) {
		simulator.randomize(profile);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#remove(org.mmarini.routes.model.MapEdge)
	 */
	public void remove(MapEdge edge) {
		simulator.remove(edge);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#delete(org.mmarini.routes.model.MapNode)
	 */
	public void remove(MapNode node) {
		simulator.remove(node);
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	public void retrieveModule(List<Module> modules)
			throws ParserConfigurationException, SAXException, IOException {
		File path = new File("modules");
		if (path.isDirectory()) {
			File[] listFiles = path.listFiles();
			Arrays.sort(listFiles, new Comparator<File>() {

				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}

			});
			for (File file : listFiles) {
				if (file.isFile() && file.canRead()) {
					Module module = new Module();
					moduleBuilder.setModule(module);
					parser.parse(file, moduleBuilder);
					modules.add(module);
				}
			}
		}
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#save(java.io.File)
	 */
	public void save(File file) throws ParserConfigurationException,
			TransformerException, SAXException {
		builder.build(file, simulator);
	}

	/**
	 * 
	 * @param frequence
	 */
	public void setFrequence(double frequence) {
		simulator.setFrequence(frequence);
	}

	/**
         * 
         */
	public void setTemplate(MapEdge edge) {
		simulator.setEdgeTemplate(edge);
	}

	/**
         * 
         */
	public void setTemplate(SiteNode node) {
		simulator.setSiteTemplate(node);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#setTimeInterval(double)
	 */
	public void setTimeInterval(double timeInterval) {
		simulator.setTimeInterval(timeInterval);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#transformToNode(org.mmarini.routes.model.SiteNode)
	 */
	public MapNode transformToNode(SiteNode site) {
		return simulator.transformToNode(site);
	}

	/**
	 * @see org.mmarini.routes.model.RouteHandler#transformToSite(org.mmarini.routes.model.MapNode)
	 */
	public SiteNode transformToSite(MapNode node) {
		return simulator.transformToSite(node);
	}

	/**
	 * 
	 * @return
	 */
	public double getFrequence() {
		return simulator.getFrequence();
	}

	/**
	 * 
	 * @return
	 */
	public Iterable<Path> getPaths() {
		return simulator.getPaths();
	}

	/**
	 * 
	 * @param paths
	 */
	public void loadPaths(List<Path> paths) {
		simulator.loadPaths(paths);
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public int getNodeIndex(MapNode node) {
		return simulator.getNodeIndex(node);
	}
}
