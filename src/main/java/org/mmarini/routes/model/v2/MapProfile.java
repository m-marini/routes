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
 * Profile used during random map generation.
 * <p>
 * It includes
 * <ul>
 * <li>Number of sites</li>
 * <li>Map width</li>
 * <li>Map height</li>
 * <li>Minimum traffic weight</li>
 * <li>Traffic frequency</li>
 * </ul>
 * </p>
 */
public class MapProfile {
	private final int siteCount;
	private final double width;
	private final double height;
	private final double minWeight;
	private final double frequence;

	/**
	 * Creates a map profile.
	 *
	 * @param siteCount number of sites
	 * @param width     width of map in meters
	 * @param height    height of map in meters
	 * @param minWeight minimum traffic weight
	 * @param frequence traffic frequency
	 */
	public MapProfile(final int siteCount, final double width, final double height, final double minWeight,
			final double frequence) {
		this.siteCount = siteCount;
		this.width = width;
		this.height = height;
		this.minWeight = minWeight;
		this.frequence = frequence;
	}

	/** Returns the frequency. */
	public double getFrequence() {
		return frequence;
	}

	/** Returns the height in meters. */
	public double getHeight() {
		return height;
	}

	/** Returns the minimum traffic weight. */
	public double getMinWeight() {
		return minWeight;
	}

	/** Returns the number of sites. */
	public int getSiteCount() {
		return siteCount;
	}

	/** Returns the map width in meters. */
	public double getWidth() {
		return width;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MapProfile [siteCount=").append(siteCount).append(", width=").append(width).append(", height=")
				.append(height).append(", minWeight=").append(minWeight).append(", frequence=").append(frequence)
				.append("]");
		return builder.toString();
	}
}
