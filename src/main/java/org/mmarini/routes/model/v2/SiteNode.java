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

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author mmarini
 *
 */
public class SiteNode extends MapNode {
	/**
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public static SiteNode create(final double x, final double y) {
		return create(new Point2D.Double(x, y));
	}

	/**
	 *
	 * @param location
	 * @return
	 */
	public static SiteNode create(final Point2D location) {
		final ByteBuffer name = ByteBuffer.allocate(Double.SIZE * 2 / 8).putDouble(location.getX())
				.putDouble(location.getY());
		final UUID id = UUID.nameUUIDFromBytes(name.array());
		return new SiteNode(id, location);
	}

	/**
	 *
	 * @param id
	 * @param location
	 */
	protected SiteNode(final UUID id, final Point2D location) {
		super(id, location);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SiteNode [").append(getId()).append(", ").append(getX()).append(", ").append(getY())
				.append("]");
		return builder.toString();
	}
}
