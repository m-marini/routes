//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.model.v2;

/**
 * Pair of elements.
 * <p>
 * The instance can be created with {@link Tuple#of(Object, Object)}
 * </p>
 */
public class Tuple2<T1, T2> implements Tuple {

	private final T1 elem1;
	private final T2 elem2;

	/**
	 * Create a pair of object.
	 *
	 * @param elem1 first element
	 * @param elem2 second element
	 */
	protected Tuple2(final T1 elem1, final T2 elem2) {
		super();
		assert elem1 != null;
		assert elem2 != null;
		this.elem1 = elem1;
		this.elem2 = elem2;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
		if (!elem1.equals(other.elem1)) {
			return false;
		}
		if (!elem2.equals(other.elem2)) {
			return false;
		}
		return true;
	}

	/** Returns the first element. */
	public T1 get1() {
		return elem1;
	}

	/** Returns the second element. */
	public T2 get2() {
		return elem2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elem1.hashCode();
		result = prime * result + elem2.hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("(").append(elem1).append(", ").append(elem2).append(")");
		return builder.toString();
	}
}
