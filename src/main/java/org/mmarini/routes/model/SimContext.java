/*
 * SimContext.java
 *
 * $Id: SimContext.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 30/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SimContext.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 */
public interface SimContext {

	/**
	 * @param from
	 * @param to
	 * @return
	 */
	public abstract MapEdge findNextEdge(MapNode from, MapNode to);

	/**
	 * @return
	 */
	public abstract double getTime();

	/**
	 * @param veicle
	 */
	public abstract void removeVeicle(Veicle veicle);

}
