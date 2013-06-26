/*
 * MapElementEvent.java
 *
 * $Id: MapElementEvent.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 05/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.util.EventObject;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.SiteNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapElementEvent.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class MapElementEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private MapNode node;

	private MapEdge edge;

	private SiteNode site;

	/**
	 * @param source
	 */
	public MapElementEvent(Object source) {
		super(source);
	}

	/**
	 * @return the edge
	 */
	public MapEdge getEdge() {
		return edge;
	}

	/**
	 * @return the node
	 */
	public MapNode getNode() {
		return node;
	}

	/**
	 * @return the site
	 */
	public SiteNode getSite() {
		return site;
	}

	/**
	 * @param edge
	 *            the edge to set
	 */
	public void setEdge(MapEdge edge) {
		this.edge = edge;
	}

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(MapNode node) {
		this.node = node;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(SiteNode site) {
		this.site = site;
	}

}
