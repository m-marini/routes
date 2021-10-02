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