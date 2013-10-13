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
	public int nextBinomial(double p, int n) {
		double x = nextDouble();
		double pdf = 1;
		double p1 = 1 - p;
		for (int i = 0; i < n; ++i) {
			pdf *= p1;
		}
		double pk = p / p1;
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
	public double nextKumaraswamy(double a, double b) {
		return Math.pow(1 - Math.pow(1 - nextDouble(), 1 / b), 1 / a);
	}

	/**
	 * @param lambda
	 * @return
	 */
	public int nextPoison(double lambda) {
		int k = -1;
		double p = 1;
		double l = Math.exp(-lambda);
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
	public double nextRayleigh(double sigma) {
		return sigma * Math.sqrt(-2 * Math.log(1 - nextDouble()));
	}

	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public double nextUniform(double average, double range) {
		return (nextDouble() - 0.5f) * range + average;
	}
}