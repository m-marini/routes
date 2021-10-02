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
