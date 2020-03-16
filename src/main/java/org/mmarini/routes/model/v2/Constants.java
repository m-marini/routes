/*
 * Constants.java
 *
 * $Id: Constants.java,v 1.5 2009/05/29 20:47:24 marco Exp $
 *
 * 31/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model.v2;

/**
 * Common constants of simulation.
 */
public interface Constants {
	/** Multiplicative factor from meters/second to kilometers/hour */
	public static final double MPS_TO_KMH = 3.6;

	/** Multiplicative factor from kilometers/hour to meters/second */
	public static final double KMH_TO_MPS = 1 / MPS_TO_KMH;

	/** Lengh of vehicle in meters */
	public static final double VEHICLE_LENGTH = 5;

	/** Reaction time of vehicle */
	public static final double REACTION_TIME = 1;

	/** Default speed limit in kilometers/hour */
	public static final double DEFAULT_SPEED_LIMIT_KMH = 130;

	/** Default weight of path between site nodes */
	public static final double DEFAULT_WEIGHT = 1.0;

	/** Default frequency of vehicle creation */
	public static final double DEFAULT_FREQUENCE = 1.0;

	/** Default priority of edges */
	public static final int DEFAULT_PRIORITY = 0;
}
