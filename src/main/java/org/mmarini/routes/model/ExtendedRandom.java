/*
 * ExtendedRandom.java
 *
 * $Id: ExtendedRandom.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 *
 * 22/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import java.util.Random;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: ExtendedRandom.java,v 1.4 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class ExtendedRandom extends Random {
	/**
	     * 
	     */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param p
	 * @param n
	 * @return
	 */
	public int nextBinomial(final double p, final int n) {
		final double x = nextDouble();
		double pdf = 1;
		final double p1 = 1 - p;
		for (int i = 0; i < n; ++i) {
			pdf *= p1;
		}
		final double pk = p / p1;
		int k = 0;
		double cdf = pdf;
		while (k < n && cdf <= x) {
			++k;
			pdf *= pk * (n - k + 1) / k;
			cdf += pdf;
		}
		return k;
	}

	/**
	 * @param lambda
	 * @return
	 */
	public double nextKumaraswamy(final double a, final double b) {
		return Math.pow(1 - Math.pow(1 - nextDouble(), 1 / b), 1 / a);
	}

	/**
	 * @param lambda
	 * @return
	 */
	public int nextPoison(final double lambda) {
		int k = -1;
		double p = 1;
		final double l = Math.exp(-lambda);
		do {
			++k;
			p *= nextDouble();
		} while (p > l);
		return k;
	}

	/**
	 * 
	 * @param sigma
	 * @return
	 */
	public double nextRayleigh(final double sigma) {
		return sigma * Math.sqrt(-2 * Math.log(1 - nextDouble()));
	}

	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public double nextUniform(final double average, final double range) {
		return (nextDouble() - 0.5f) * range + average;
	}
}