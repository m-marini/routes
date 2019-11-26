/*
 * MapElementListener.java
 *
 * $Id: MapElementListener.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 05/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.util.EventListener;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapElementListener.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 */
public interface MapElementListener extends EventListener {

	/**
	 * @param mapElementEvent
	 */
	public abstract void edgeSelected(MapElementEvent mapElementEvent);

	/**
	 * @param mapElementEvent
	 */
	public abstract void nodeSelected(MapElementEvent mapElementEvent);

	/**
	 * @param mapElementEvent
	 */
	public abstract void siteSelected(MapElementEvent mapElementEvent);

}
