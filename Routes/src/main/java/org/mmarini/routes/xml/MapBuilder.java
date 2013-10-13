/*
 * MapBuilder.java
 *
 * $Id: MapBuilder.java,v 1.8 2010/10/19 20:33:00 marco Exp $
 *
 * 22/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.SiteNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapBuilder.java,v 1.8 2010/10/19 20:33:00 marco Exp $
 * 
 */
public interface MapBuilder {

	/**
	 * @param edge
	 */
	public abstract void add(MapEdge edge);

	/**
	 * @param node
	 */
	public abstract void add(MapNode node);

	/**
	 * 
	 * @param path
	 */
	public abstract void add(Path path);

	/**
	 * @param site
	 */
	public abstract void add(SiteNode site);

	/**
	 * 
	 * @param frequence
	 */
	public abstract void applyFrequence(double frequence);

	/**
     * 
     */
	public abstract void clear();

	/**
         * 
         */
	public abstract void init();

}
