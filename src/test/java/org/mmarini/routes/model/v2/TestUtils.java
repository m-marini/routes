/**
 *
 */
package org.mmarini.routes.model.v2;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * @author mmarini
 *
 */
public class TestUtils {
	private static final Random random = new Random();
	public static final int NUM_VALUES = 100;

	/**
	 * Returns a stream of integer from 0 to n
	 */
	public static IntStream genArguments() {
		return IntStream.range(0, NUM_VALUES);
	}

	/**
	 * Returns a stream of integer from 0 to n
	 *
	 * @param n the number of tests
	 */
	public static IntStream genArguments(final int n) {
		return IntStream.range(0, n);
	}

	/**
	 * Returns a random value included the limit
	 *
	 * @param i   iterator counter
	 * @param min minimum value
	 * @param max maximum value
	 */
	public static double genDouble(final int i, final double min, final double max) {
		switch (i) {
		case 0:
			return min;
		case 1:
			return max;
		default:
			return random.nextDouble() * (max - min) + min;
		}
	}
}
