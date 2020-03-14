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

import java.util.UUID;

/**
 * UUID comparator.
 */
public interface UUIDComparator {
	/**
	 * Returns an integer indicating the order of two uuids.
	 * <p>
	 * <ul>
	 * <li>a positive number if first UUID is after the second</li>
	 * <li>a negative number if first UUID is before the second</li>
	 * <li>zeroif first UUID is equal to the second</li>
	 * </ul>
	 * </p>
	 *
	 * @param a first uuid
	 * @param b second uuid
	 */
	public static int compareTo(final UUID a, final UUID b) {
		final int msl = Long.compareUnsigned(a.getMostSignificantBits(), b.getMostSignificantBits());
		if (msl != 0) {
			return msl;
		}
		final int lsl = Long.compareUnsigned(a.getLeastSignificantBits(), b.getLeastSignificantBits());
		return lsl;
	}
}
