/*
 * MapElementVisitor.java
 *
 * $Id: MapElementVisitor.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 *
 * 05/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapElementVisitor.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 *
 */
public interface MapElementVisitor {

	/**
	 * @param edge
	 */
	public abstract void visit(MapEdge edge);

	/**
	 * @param node
	 */
	public abstract void visit(MapNode node);

	/**
	 * @param node
	 */
	public abstract void visit(SiteNode node);

}
