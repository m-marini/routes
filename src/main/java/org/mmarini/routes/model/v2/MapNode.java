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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Node at a location point in the map.
 */
public class MapNode implements Comparable<MapNode> {
	/**
	 * Returns the map node at point coordinates.
	 *
	 * @param x horizontal coordinates
	 * @param y vertical coordinates
	 */
	public static MapNode create(final double x, final double y) {
		return create(new Point2D.Double(x, y));
	}

	/**
	 * Returns the map node at a given point.
	 *
	 * @param location the location point
	 */
	public static MapNode create(final Point2D location) {
		return new MapNode(location);
	}

	private final Point2D location;
	private final UUID id;

	/**
	 * Creates a map node for a location.
	 *
	 * @param location the location point
	 */
	protected MapNode(final Point2D location) {
		super();
		assert location != null;
		this.location = location;
		final ByteBuffer name = ByteBuffer.allocate(Double.SIZE * 2 / 8).putDouble(location.getX())
				.putDouble(location.getY());
		id = UUID.nameUUIDFromBytes(name.array());
	}

	@Override
	public int compareTo(final MapNode other) {
		return UUIDComparator.compareTo(id, other.id);
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
		if (!location.equals(other.location)) {
			return false;
		}
		return true;
	}

	/** Returns the unique node identifier. */
	public UUID getId() {
		return id;
	}

	/** Returns the location point of node. */
	public Point2D getLocation() {
		return location;
	}

	/** Returns the name (the uuid). */
	public String getName() {
		return id.toString();
	}

	/** Returns the short name (first 6 characters of uuid). */
	public String getShortName() {
		return getName().substring(0, 6);
	}

	/** Returns the horizontal coordinate of node in meters. */
	public double getX() {
		return location.getX();
	}

	/** Returns the vertical coordinate of node in meters. */
	public double getY() {
		return location.getY();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (location.hashCode());
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MapNode [").append(id).append(", ").append(getX()).append(", ").append(getY()).append("]");
		return builder.toString();
	}

	/**
	 * Returns the node by applying the transformation.
	 *
	 * @param trans the transformation
	 */
	public MapNode transform(final AffineTransform trans) {
		final Point2D newPt = trans.transform(location, new Point2D.Double());
		final MapNode result = create(newPt);
		return result;
	}
}
