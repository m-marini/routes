package org.mmarini.routes.model.v2;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class MockRandomBuilder {

	class SequenceRandom extends Random {
		private static final long serialVersionUID = 1L;
		private int intIndex;
		private int doubleIndex;

		@Override
		public double nextDouble() {
			final double value = doubles[doubleIndex];
			doubleIndex = (doubleIndex + 1) % doubles.length;
			return value;
		}

		@Override
		public int nextInt() {
			final int value = ints[intIndex];
			intIndex = (intIndex + 1) % ints.length;
			return value;
		}

		@Override
		public int nextInt(final int n) {
			return nextInt() % n;
		}
	}

	private static final MockRandomBuilder EMPTY = new MockRandomBuilder(new double[0], new int[0]);

	public static MockRandomBuilder empty() {
		return EMPTY;
	}

	public static MockRandomBuilder just(final double... values) {
		return new MockRandomBuilder(values, new int[0]);
	}

	public static MockRandomBuilder just(final int... values) {
		return new MockRandomBuilder(new double[0], values);
	}

	public static MockRandomBuilder range(final int intCount, final int doubleCount) {
		final int[] ints = IntStream.range(0, intCount).toArray();
		final double[] doubles = IntStream.range(0, doubleCount).mapToDouble(i -> (double) i / doubleCount).toArray();
		return new MockRandomBuilder(doubles, ints);
	}

	private final double[] doubles;

	private final int[] ints;

	/**
	 * @param doubles
	 * @param ints
	 */
	protected MockRandomBuilder(final double[] doubles, final int[] ints) {
		super();
		this.doubles = doubles;
		this.ints = ints;
	}

	public Random build() {
		return new SequenceRandom();
	}

	public MockRandomBuilder concat(final MockRandomBuilder other) {
		final int[] newInts = IntStream.concat(IntStream.of(ints), IntStream.of(other.ints)).toArray();
		final double[] newDoubles = DoubleStream.concat(DoubleStream.of(doubles), DoubleStream.of(other.doubles))
				.toArray();
		return new MockRandomBuilder(newDoubles, newInts);
	}
}