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
 * @author marco.marini@mmarini.org
 * @version $Id: Constants.java,v 1.5 2009/05/29 20:47:24 marco Exp $
 *
 */
public interface Constants {
	/**
	 *
	 */
	public static final double MPS_TO_KMH = 3.6;

	/**
	 *
	 */
	public static final double KMH_TO_MPS = 1 / MPS_TO_KMH;

	/**
	 *
	 */
	public static final double VEHICLE_LENGTH = 5;

	/**
	 *
	 */
	public static final double REACTION_TIME = 1;

	/**
	 * 0.01
	 */
	public static final double ENERGY = 0.01 * 3.6 * 3.6;

	/**
	 *
	 */
	public static final double DEFAULT_SPEED_LIMIT_KMH = 90;

	/**
	 *
	 */
	public static final double DEFAULT_WEIGHT = 1.0;

	/**
	 *
	 */
	public static final double DEFAULT_FREQUENCE = 1.0;

	/**
	 *
	 */
	public static final int DEFAULT_PRIORITY = 0;
}
