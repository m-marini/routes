/*
 * SimContextImpl.java
 *
 * $Id: SimContextImpl.java,v 1.6 2010/10/19 20:33:00 marco Exp $
 *
 * 30/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SimContextImpl.java,v 1.6 2010/10/19 20:33:00 marco Exp $
 *
 */
public class SimContextImpl implements SimContext {
	private double time;

	private Simulator simulator;

	/**
	     *
	     */
	public SimContextImpl() {
	}

	/**
	 * @see org.mmarini.routes.model.SimContext#findNextEdge(org.mmarini.routes.model.MapNode,
	 *      org.mmarini.routes.model.MapNode)
	 */
	@Override
	public MapEdge findNextEdge(final MapNode from, final MapNode to) {
		return getSimulator().findNextEdge(from, to);
	}

	/**
	 * @return the simulator
	 */
	private Simulator getSimulator() {
		return simulator;
	}

	/**
	 * @see org.mmarini.routes.model.SimContext#getTime()
	 */
	@Override
	public double getTime() {
		return time;
	}

	/**
	 * @see org.mmarini.routes.model.SimContext#removeVeicle(org.mmarini.routes.model.Veicle)
	 */
	@Override
	public void removeVeicle(final Veicle veicle) {
		getSimulator().remove(veicle);
	}

	/**
	 * @param simulator the simulator to set
	 */
	public void setSimulator(final Simulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(final double time) {
		this.time = time;
	}
}
