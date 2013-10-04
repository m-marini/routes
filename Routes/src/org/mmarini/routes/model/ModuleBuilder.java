/*
 * SimulatorBuilder.java
 *
 * $Id: ModuleBuilder.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 22/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import org.mmarini.routes.xml.MapBuilder;
import org.mmarini.routes.xml.Path;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: ModuleBuilder.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class ModuleBuilder implements MapBuilder {
	private Module module;

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#add(org.mmarini.routes.model.MapEdge)
	 */
	public void add(MapEdge edge) {
		getModule().add(edge);
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#add(org.mmarini.routes.model.MapNode)
	 */
	public void add(MapNode node) {
		getModule().add(node);
	}

	/**
	 * 
	 */
	public void add(Path path) {
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#add(org.mmarini.routes.model.SiteNode)
	 */
	public void add(SiteNode site) {
		getModule().add(site);
	}

	/**
	 * 
	 */
	public void applyFrequence(double frequence) {
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#clear()
	 */
	public void clear() {
	}

	/**
	 * @return the simulator
	 */
	private Module getModule() {
		return module;
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#init()
	 */
	public void init() {
	}

	/**
	 * @param module
	 *            the module to set
	 */
	public void setModule(Module module) {
		this.module = module;
	}
}
