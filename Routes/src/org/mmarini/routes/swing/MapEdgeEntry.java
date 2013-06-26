/*
 * ExplorerPane.java
 *
 * $Id: MapEdgeEntry.java,v 1.3 2009/05/08 21:28:50 marco Exp $
 *
 * 06/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import org.mmarini.routes.model.MapEdge;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapEdgeEntry.java,v 1.3 2009/05/08 21:28:50 marco Exp $
 * 
 */
public class MapEdgeEntry {
	private MapEdge edge;

	private String name;

	/**
	 * @param name
	 * @param edge
	 */
	public MapEdgeEntry(String name, MapEdge edge) {
		this.edge = edge;
		this.name = name;
	}

	/**
	 * @return the edge
	 */
	public MapEdge getEdge() {
		return edge;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

}
