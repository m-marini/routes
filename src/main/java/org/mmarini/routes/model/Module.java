/*
 * Module.java
 *
 * $Id: Module.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 *
 * 22/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Module.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class Module {
	private final List<MapNode> nodeList;

	private final List<MapEdge> edgeList;

	private final Rectangle2D bound;

	/**
	     * 
	     */
	public Module() {
		nodeList = new ArrayList<MapNode>(0);
		edgeList = new ArrayList<MapEdge>(0);
		bound = new Rectangle2D.Double(0, 0, 0, 0);
	}

	/**
	 * @param edge
	 */
	public void add(final MapEdge edge) {
		getEdgeList().add(edge);
	}

	/**
	 * @param node
	 */
	public void add(final MapNode node) {
		final Point2D l = node.getLocation();
		if (getNodeList().isEmpty()) {
			bound.setFrame(l.getX(), l.getY(), 0, 0);
		}
		getNodeList().add(node);
		bound.add(l);
	}

	/**
	 * 
	 * @return
	 */
	public Rectangle2D getBound() {
		return bound;
	}

	/**
	 * @return the edgeList
	 */
	private List<MapEdge> getEdgeList() {
		return edgeList;
	}

	/**
	 * 
	 * @return
	 */
	public Iterable<MapEdge> getEdges() {
		return getEdgeList();
	}

	/**
	 * @return the nodeList
	 */
	private List<MapNode> getNodeList() {
		return nodeList;
	}

	/**
	 * @return
	 */
	public Iterable<MapNode> getNodes() {
		return getNodeList();
	}
}
