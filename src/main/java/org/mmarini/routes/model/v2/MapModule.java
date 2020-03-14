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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Module consisting of a set of edges the can be dropped during editing of map.
 */
public class MapModule {

	private static final MapModule EMPTY = new MapModule(Set.of());

	/** Returns empty module. */
	public static MapModule create() {
		return EMPTY;
	}

	/**
	 * Returns the module from the edges of map.
	 *
	 * @param map the map
	 */
	public static MapModule create(final GeoMap map) {
		return new MapModule(map.getEdges());
	}

	private final Set<MapEdge> edges;

	/**
	 * Creates the map module.
	 *
	 * @param edges the edges of module
	 */
	public MapModule(final Set<MapEdge> edges) {
		assert edges != null;
		this.edges = edges;
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
		final MapModule other = (MapModule) obj;
		if (!edges.equals(other.edges)) {
			return false;
		}
		return true;
	}

	/** Returns the bound rectangle of the module. */
	public Rectangle2D getBound() {
		if (edges.isEmpty()) {
			return new Rectangle2D.Double();
		} else {
			final Double bound = edges.stream().findAny().map(e -> {

				final double xb = e.getBeginLocation().getX();
				final double yb = e.getBeginLocation().getY();
				final double xe = e.getEndLocation().getX();
				final double ye = e.getEndLocation().getY();

				final double x0 = min(xb, xe);
				final double y0 = min(yb, ye);
				final double x1 = max(xb, xe);
				final double y1 = max(yb, ye);
				return new Rectangle2D.Double(x0, y0, x1 - x0, y1 - x0);
			}).get();
			for (final MapEdge e : edges) {
				bound.add(e.getBeginLocation());
				bound.add(e.getEndLocation());
			}
			return bound;
		}
	}

	/** Returns the edges of module. */
	public Set<MapEdge> getEdges() {
		return edges;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + edges.hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Module [edges=").append(edges).append("]");
		return builder.toString();
	}

	/**
	 * Returns the module applying the affine transformation.
	 *
	 * @param trans the affine transformation
	 */
	public MapModule transform(final AffineTransform trans) {
		final Map<MapNode, MapNode> nodeMap = edges.parallelStream().flatMap(edge -> {
			return Stream.of(edge.getBegin(), edge.getEnd());
		}).distinct().collect(Collectors.toMap(Function.identity(), node -> {
			final MapNode result = node.transform(trans);
			return result;
		}));
		final Set<MapEdge> newEdges = edges.parallelStream().map(edge -> {
			final MapNode newBegin = nodeMap.get(edge.getBegin());
			final MapNode newEnd = nodeMap.get(edge.getEnd());
			final MapEdge newEdge = edge.setEnds(newBegin, newEnd);
			return newEdge;
		}).collect(Collectors.toSet());
		return new MapModule(newEdges);
	}
}
