/*
 * ExplorerPane.java
 *
 * $Id: MapNodeEntry.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 *
 * 06/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import org.mmarini.routes.model.MapNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapNodeEntry.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class MapNodeEntry {
	private final MapNode node;

	private final String name;

	/**
	 * @param name
	 * @param node
	 */
	public MapNodeEntry(final String name, final MapNode node) {
		this.node = node;
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the node
	 */
	public MapNode getNode() {
		return node;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

}
