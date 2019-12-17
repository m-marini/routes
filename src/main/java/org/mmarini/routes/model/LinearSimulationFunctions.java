/*
 * LinearSimulationFunctions.java
 *
 * $Id: LinearSimulationFunctions.java,v 1.9 2010/10/19 20:33:00 marco Exp $
 *
 * 01/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: LinearSimulationFunctions.java,v 1.2 2009/01/05 17:04:20 marco
 *          Exp $
 *
 */
public class LinearSimulationFunctions extends AbstractSimulationFunctions {

	private static final double MIN_SPEED_LIMIT = 5 / 3.6;

	/**
	     *
	     */
	protected LinearSimulationFunctions() {
	}

	/**
	 * @param distance
	 * @param speedLimit
	 * @param veicleCount
	 * @return
	 */
	private double calculateCurrentSpeed(final double distance, final double speedLimit, final int veicleCount) {
		if (veicleCount < 1) {
			return speedLimit;
		}
		final double maxSpeed = (distance / veicleCount - VEHICLE_LENGTH) / REACTION_TIME;
		return Math.min(maxSpeed, speedLimit);
	}

	/**
	 * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeDistanceBySpeed(double)
	 */
	@Override
	public double computeDistanceBySpeed(final double speed) {
		return REACTION_TIME * speed + VEHICLE_LENGTH;
		// return ENERGY * speed * speed + REACTION
		// * speed + VEICLE_LENGTH;
	}

	/**
	     *
	     */
	@Override
	public double computeSpeed(final double distance) {
		return Math.max(distance / REACTION_TIME, MIN_SPEED_LIMIT);
	}

	/**
	 * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeSpeedByVeicles(double,
	 *      int)
	 */
	@Override
	public double computeSpeedByVeicles(final double distance, final int veicleCount) {
		return Math.max((distance - VEHICLE_LENGTH) / REACTION_TIME, 0f);
	}

	/**
	 * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeTrafficLevel(double,
	 *      int, double)
	 */
	@Override
	public double computeTrafficLevel(final double distance, final int veicleCount, final double speedLimit) {
		if (veicleCount == 0) {
			return 0;
		}
		final double speed = computeSpeedByVeicles(distance / veicleCount, veicleCount);
		if (speed >= speedLimit) {
			return 0;
		}
		return 1 - speed / speedLimit;
	}

	/**
	 * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeTransitTime(double,
	 *      double, int)
	 */
	@Override
	public double computeTransitTime(final double distance, final double speedLimit, final int veicleCount) {
		final double speed = calculateCurrentSpeed(distance, speedLimit, veicleCount);
		return distance / speed;
	}

	/**
	 * @see org.mmarini.routes.model.AbstractSimulationFunctions#computeVeicleMovement(double,
	 *      double)
	 */
	@Override
	public double computeVeicleMovement(final double time, final double length) {
		// double kr = REACTION;
		// double vl = VEICLE_LENGTH;
		// double ke = ENERGY;
		// double det = Math.pow(kr + t, 2) - 4 * kr * (vl - l);
		// return (double) ((Math.sqrt(det) - kr - t) / (2f * ke) * t);
		return (length - VEHICLE_LENGTH) / (1 + REACTION_TIME / time);
	}
}
