/*
 * SiteNode.java
 *
 * $Id: SiteNode.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SiteNode.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class SiteNode extends MapNode {

	/**
         * 
         */
	public SiteNode() {
	}

	/**
	 * @param node
	 */
	public SiteNode(SiteNode node) {
		super(node);
	}

	/**
	 * @see org.mmarini.routes.model.MapElement#apply(org.mmarini.routes.model.MapElementVisitor)
	 */
	@Override
	public void apply(MapElementVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @see org.mmarini.routes.model.MapNode#clone()
	 */
	@Override
	public Object clone() {
		return new SiteNode(this);
	}

	/**
	 * 
	 * @return
	 */
	public SiteNode createClone() {
		SiteNode site = new SiteNode();
		return site;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Site(");
		Point2D location = getLocation();
		builder.append(location.getX());
		builder.append(",");
		builder.append(location.getY());
		builder.append(")");
		return builder.toString();
	}
}
