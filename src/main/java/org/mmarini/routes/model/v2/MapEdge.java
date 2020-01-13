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
import java.util.UUID;

import org.mmarini.routes.model.Constants;

/**
 * Al tratto di strada associamo la velocità corrente dei veicoli. La velocità
 * corrente è determinanata dal primo veicolo entrato nel tratto di strada. Se
 * il veicolo è in prossimità dell'uscita la velocità è limitata dalla velocità
 * del tratto sucessivo. Il veicolo è in prossimità se alla velocità corrente il
 * veicolo per il dato intervallo il veicolo esce dal tratto stradale. Se il
 * veicolo non è in prossimità la velocità è il limite del tratto stradale.
 * <p>
 * Durante la simulazione vengono dapprima calcolate le velocità correnti di
 * ogni tratto stradale, poi vengono calcolate le posizioni dei veicoli per ogni
 * tratto stradale.
 * </p>
 * <p>
 * Il numero massimo di veicoli presenti nel tratto è dato da<br>
 * n = Sl / Vl<br>
 * dove Vl = 5 è la lunghezza del veicolo.<br>
 * </p>
 */
public class MapEdge implements Comparable<MapEdge>, Constants {
	/**
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	public static MapEdge create(final MapNode begin, final MapNode end) {
		final UUID id = UUID.nameUUIDFromBytes((begin.getId().toString() + end.getId().toString()).getBytes());
		return new MapEdge(id, begin, end, DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS, DEFAULT_PRIORITY);
	}

	private final UUID id;
	private final MapNode begin;
	private final MapNode end;
	private final double speedLimit;
	private final int priority;

	/**
	 *
	 * @param begin
	 * @param end
	 * @param speedLimit
	 * @param priority
	 */
	protected MapEdge(final UUID id, final MapNode begin, final MapNode end, final double speedLimit,
			final int priority) {
		super();
		this.id = id;
		this.begin = begin;
		this.end = end;
		this.speedLimit = speedLimit;
		this.priority = priority;
	}

	/**
	 * Returns the map edge with a changed node
	 *
	 * @param node    orignal node
	 * @param newNode new node
	 */
	public MapEdge changeNode(final MapNode node, final MapNode newNode) {
		if (node.equals(begin)) {
			return create(newNode, end).setSpeedLimit(speedLimit).setPriority(priority);
		} else if (node.equals(end)) {
			return create(begin, newNode).setSpeedLimit(speedLimit).setPriority(priority);
		} else {
			return this;
		}
	}

	@Override
	public int compareTo(final MapEdge other) {
		return UUIDComparator.compareTo(id, other.id);
	}

	/**
	 * Returns ka for a given edge
	 * <p>
	 * The ka is distance from the begin node along the edge direction.<br>
	 * It is computed as the ratio between the scalar product of edge direction and
	 * point direction and the length of edge.
	 * </p>
	 *
	 * <pre>
	 * ka = &lt;A, B> / lA = lB * cos(a)
	 * &lt;A, B> = lA * lB * cos(a)
	 * </pre>
	 *
	 * @param point the point
	 */
	private double computeKa(final Point2D point) {
		final Point2D begin = getBeginLocation();
		final double x0 = begin.getX();
		final double y0 = begin.getY();
		final Point2D ev = getDirection();
		final double xe = ev.getX();
		final double ye = ev.getY();
		final double xp = point.getX() - x0;
		final double yp = point.getY() - y0;
		final double ep = xe * xp + ye * yp;
		final double ka = ep / getLength();
		return ka;
	}

	/**
	 * Returns the sign of cross product of direction by other edge direction.
	 * <ul>
	 * <li>>0 if the other edge direction is coming from left</li>
	 * <li>0 if the other edge has the same direction of the edge</li>
	 * <li>&lt;0 if the other edge direction is coming from right</li>
	 * </ul>
	 *
	 * @param edge the other edge
	 */
	public int cross(final MapEdge edge) {
		final double dx = getEndLocation().getX() - getBeginLocation().getX();
		final double dy = getEndLocation().getY() - getBeginLocation().getY();

		final double dx1 = edge.getEndLocation().getX() - edge.getBeginLocation().getX();
		final double dy1 = edge.getEndLocation().getY() - edge.getBeginLocation().getY();

		final double cross = dx * dy1 - dy * dx1;
		return cross > 0.0 ? 1 : cross < -0.0 ? -1 : 0;
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
		final MapEdge other = (MapEdge) obj;
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
	 * @return the begin
	 */
	public MapNode getBegin() {
		return begin;
	}

	/**
	 * @return
	 */
	public Point2D getBeginLocation() {
		return begin.getLocation();
	}

	/**
	 * Returns the closest edge point from a given point
	 *
	 * @param point
	 * @return
	 */
	Point2D getClosest(final Point2D point) {
		final double ka = computeKa(point);
		if (ka <= 0) {
			return begin.getLocation();
		} else if (ka >= getLength()) {
			return end.getLocation();
		} else {
			return getLocation(ka);
		}
	}

	/**
	 * Returns the direction of the edge
	 */
	public Point2D getDirection() {
		final double dx = getEnd().getX() - getBegin().getX();
		final double dy = getEnd().getY() - getBegin().getY();
		return new Point2D.Double(dx, dy);
	}

	/**
	 * Returns the distance of a point from the edge
	 *
	 * @param point the point
	 */
	public double getDistance(final Point2D point) {
		final Point2D closer = getClosest(point);
		final double result = point.distance(closer);
		return result;
	}

	/**
	 * Returns the end node
	 */
	public MapNode getEnd() {
		return end;
	}

	/**
	 * Returns the end point
	 */
	public Point2D getEndLocation() {
		return end.getLocation();
	}

	/**
	 * Returns the id og edge
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Returns the length of edge
	 */
	public double getLength() {
		return begin.getLocation().distance(end.getLocation());
	}

	/**
	 * Returns the location in the edge from a give distance of origin
	 *
	 * @param distance the distance
	 */
	public Point2D getLocation(final double distance) {
		final Point2D dir = getDirection();
		final double lambda = distance / getLength();
		final double x = dir.getX() * lambda + getBeginLocation().getX();
		final double y = dir.getY() * lambda + getBeginLocation().getY();
		final Point2D result = new Point2D.Double(x, y);
		return result;
	}

	/**
	 * Return the name (the uuid)
	 */
	private String getName() {
		return id.toString();
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Returns the short name (first 6 characters of uuid)
	 */
	public String getShortName() {
		return getName().substring(0, 6);
	}

	/**
	 * @return the speedLimit
	 */
	public double getSpeedLimit() {
		return speedLimit;
	}

	/**
	 * Computes the transite time.
	 * <p>
	 * The best transtit time is the distance / speedLimit
	 * </p>
	 *
	 * @return the transit time
	 */
	public double getTransitTime() {
		return getLength() / speedLimit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Returns true if the edge is crossing with other
	 *
	 * @param other the other edge
	 */
	public boolean isCrossing(final MapEdge other) {
		return end.equals(other.end);
	}

	/**
	 *
	 * @param priority
	 * @return
	 */
	public MapEdge setPriority(final int priority) {
		return new MapEdge(id, begin, end, speedLimit, priority);
	}

	/**
	 *
	 * @param priority
	 * @return
	 */
	public MapEdge setSpeedLimit(final double speedLimit) {
		return new MapEdge(id, begin, end, speedLimit, priority);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MapEdge [").append(id).append("]");
		return builder.toString();
	}
}
