/*
 * SimulatorBuilder.java
 *
 * $Id: SimulatorBuilder.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 22/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SimulatorBuilder.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 */
public class SimulatorBuilder implements MapBuilder {
	private Simulator simulator;

	/**
	 * @see org.mmarini.routes.model.MapBuilder#add(org.mmarini.routes.model.MapEdge)
	 */
	@Override
	public void add(final MapEdge edge) {
		getSimulator().add(edge);
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#add(org.mmarini.routes.model.MapNode)
	 */
	@Override
	public void add(final MapNode node) {
		getSimulator().add(node);
	}

	/**
	 *
	 */
	@Override
	public void add(final Path path) {
		getSimulator().add(path);
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#add(org.mmarini.routes.model.SiteNode)
	 */
	@Override
	public void add(final SiteNode site) {
		getSimulator().add(site);
	}

	@Override
	public void applyFrequence(final double frequence) {
		getSimulator().setFrequence(frequence);
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#clear()
	 */
	@Override
	public void clear() {
		getSimulator().clear();
	}

	/**
	 * @return the simulator
	 */
	private Simulator getSimulator() {
		return simulator;
	}

	/**
	 * @see org.mmarini.routes.model.MapBuilder#init()
	 */
	@Override
	public void init() {
		getSimulator().init();
	}

	/**
	 * @param simulator the simulator to set
	 */
	public void setSimulator(final Simulator simulator) {
		this.simulator = simulator;
	}
}
