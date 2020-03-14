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
 * Tuple of variable elements.
 */
public interface Tuple {

	/**
	 * Returns a tuple of two elements.
	 *
	 * @param <T1> the type of first element
	 * @param <T2> the type of second element
	 * @param t1   the first element
	 * @param t2   the second element
	 */
	public static <T1, T2> Tuple2<T1, T2> of(final T1 t1, final T2 t2) {
		return new Tuple2<>(t1, t2);
	}

	/**
	 * Returns a tuple of three elements.
	 *
	 * @param <T1> the type of first element
	 * @param <T2> the type of second element
	 * @param <T3> the type of third element
	 * @param t1   the first element
	 * @param t2   the second element
	 * @param t3   the third element
	 */
	public static <T1, T2, T3> Tuple3<T1, T2, T3> of(final T1 t1, final T2 t2, final T3 t3) {
		return new Tuple3<>(t1, t2, t3);
	}
}
