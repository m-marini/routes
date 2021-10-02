/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */
package org.mmarini.routes.model;

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
	public static final double VEICLE_LENGTH = 5;

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
