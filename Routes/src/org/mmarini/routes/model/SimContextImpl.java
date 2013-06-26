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

import org.mmarini.routes.xml.Dumpable;
import org.mmarini.routes.xml.Dumper;
import org.w3c.dom.Element;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SimContextImpl.java,v 1.6 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class SimContextImpl implements SimContext, Dumpable {
	private double time;

	private Simulator simulator;

	/**
         * 
         */
	public SimContextImpl() {
	}

	/**
	 * @param root
	 */
	public void dump(Element root) {
		Dumper dumper = Dumper.getInstance();
		dumper.dumpReference(root, "simulator", getSimulator());
		dumper.dumpValue(root, "time", getTime());
	}

	/**
	 * @see org.mmarini.routes.model.SimContext#findNextEdge(org.mmarini.routes.model.MapNode,
	 *      org.mmarini.routes.model.MapNode)
	 */
	public MapEdge findNextEdge(MapNode from, MapNode to) {
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
	public double getTime() {
		return time;
	}

	/**
	 * @see org.mmarini.routes.model.SimContext#removeVeicle(org.mmarini.routes.model.Veicle)
	 */
	public void removeVeicle(Veicle veicle) {
		getSimulator().remove(veicle);
	}

	/**
	 * @param simulator
	 *            the simulator to set
	 */
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}
}
