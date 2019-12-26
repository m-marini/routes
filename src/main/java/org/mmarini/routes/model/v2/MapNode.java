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
 */
public class MapNode {
	/**
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public static MapNode create(final double x, final double y) {
		return create(new Point2D.Double(x, y));
	}

	/**
	 *
	 * @param location
	 * @return
	 */
	public static MapNode create(final Point2D location) {
		final ByteBuffer name = ByteBuffer.allocate(Double.SIZE * 2 / 8).putDouble(location.getX())
				.putDouble(location.getY());
		final UUID id = UUID.nameUUIDFromBytes(name.array());
		return new MapNode(id, location);
	}

	private final UUID id;
	private final Point2D location;

	/**
	 *
	 * @param id
	 * @param location
	 */
	protected MapNode(final UUID id, final Point2D location) {
		super();
		this.id = id;
		this.location = location;
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
		final MapNode other = (MapNode) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * @return
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @return the location
	 */
	public Point2D getLocation() {
		return location;
	}

	/**
	 *
	 * @return
	 */
	public double getX() {
		return location.getX();
	}

	/**
	 *
	 * @return
	 */
	public double getY() {
		return location.getY();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MapNode [").append(id).append(", ").append(getX()).append(", ").append(getY()).append("]");
		return builder.toString();
	}
}
