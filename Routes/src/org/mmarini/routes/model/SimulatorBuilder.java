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

import org.mmarini.routes.xml.MapBuilder;
import org.mmarini.routes.xml.Path;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SimulatorBuilder.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class SimulatorBuilder implements MapBuilder {
	private Simulator simulator;

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#add(org.mmarini.routes.model.MapEdge)
	 */
	public void add(MapEdge edge) {
		getSimulator().add(edge);
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#add(org.mmarini.routes.model.MapNode)
	 */
	public void add(MapNode node) {
		getSimulator().add(node);
	}

	/**
	 * 
	 */
	@Override
	public void add(Path path) {
		getSimulator().add(path);
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#add(org.mmarini.routes.model.SiteNode)
	 */
	public void add(SiteNode site) {
		getSimulator().add(site);
	}

	@Override
	public void applyFrequence(double frequence) {
		getSimulator().setFrequence(frequence);
	}

	/**
	 * @see org.mmarini.routes.xml.MapBuilder#clear()
	 */
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
	 * @see org.mmarini.routes.xml.MapBuilder#init()
	 */
	public void init() {
		getSimulator().init();
	}

	/**
	 * @param simulator
	 *            the simulator to set
	 */
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}
}
