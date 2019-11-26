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

/**
 * @author marco.marini@mmarini.org
 * @version $Id: ModuleBuilder.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 */
public class ModuleBuilder implements MapBuilder {
	private Module module;

	/**
	 * @see org.mmarini.routes.model.MapBuilder#add(org.mmarini.routes.model.MapEdge)
	 */
	@Override
	public void add(final MapEdge edge) {
		getModule().add(edge);
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#add(org.mmarini.routes.model.MapNode)
	 */
	@Override
	public void add(final MapNode node) {
		getModule().add(node);
	}

	/**
	 *
	 */
	@Override
	public void add(final Path path) {
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#add(org.mmarini.routes.model.SiteNode)
	 */
	@Override
	public void add(final SiteNode site) {
		getModule().add(site);
	}

	/**
	 *
	 */
	@Override
	public void applyFrequence(final double frequence) {
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#clear()
	 */
	@Override
	public void clear() {
	}

	/**
	 * @return the simulator
	 */
	private Module getModule() {
		return module;
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#init()
	 */
	@Override
	public void init() {
	}

	/**
	 * @param module the module to set
	 */
	public void setModule(final Module module) {
		this.module = module;
	}
}
