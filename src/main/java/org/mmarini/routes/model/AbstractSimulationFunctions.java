/*
 * ABstractSimulationFunctions.java
 *
 * $Id: AbstractSimulationFunctions.java,v 1.9 2010/10/19 20:32:59 marco Exp $
 *
 * 01/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: AbstractSimulationFunctions.java,v 1.2 2009/01/05 17:04:20
 *          marco Exp $
 *
 */
public abstract class AbstractSimulationFunctions implements Constants {
	private static AbstractSimulationFunctions instance = new LinearSimulationFunctions();

	/**
	 * @return the instance
	 */
	public static AbstractSimulationFunctions createInstance() {
		return instance;
	}

	/**
	     *
	     */
	protected AbstractSimulationFunctions() {
	}

	/**
	 * @param speed
	 * @return
	 */
	public abstract double computeDistanceBySpeed(double speed);

	/**
	 *
	 * @param distance
	 * @return
	 */
	public abstract double computeSpeed(double distance);

	/**
	 * @param distance
	 * @param veicleCount
	 * @return
	 */
	public abstract double computeSpeedByVeicles(double distance, int veicleCount);

	/**
	 * @param distance
	 * @param veicleCount
	 * @param speedLimit
	 * @return
	 */
	public abstract double computeTrafficLevel(double distance, int veicleCount, double speedLimit);

	/**
	 * @param distance
	 * @param speedLimit
	 * @param veicleCount
	 * @return
	 */
	public abstract double computeTransitTime(double distance, double speedLimit, int veicleCount);

	/**
	 * @param time
	 * @param length
	 * @return
	 */
	public abstract double computeVeicleMovement(double time, double length);
}
