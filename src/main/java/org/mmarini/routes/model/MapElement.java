/*
 * MapElement.java
 *
 * $Id: MapElement.java,v 1.3 2009/05/08 21:28:51 marco Exp $
 *
 * 05/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapElement.java,v 1.3 2009/05/08 21:28:51 marco Exp $
 *
 */
public interface MapElement {
	/**
	 *
	 * @param visitor
	 */
	public abstract void apply(MapElementVisitor visitor);
}
