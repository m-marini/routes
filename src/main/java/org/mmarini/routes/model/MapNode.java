/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapNode.java,v 1.9 2010/10/19 20:33:00 marco Exp $
 *
 */
public class MapNode implements MapElement, Cloneable {
	private final Point2D location;

	private final List<MapEdge> incomes;

	/**
	     *
	     */
	public MapNode() {
		location = new Point2D.Double();
		incomes = new ArrayList<MapEdge>(0);
	}

	/**
	 * @param node
	 */
	public MapNode(final MapNode node) {
		this();
		location.setLocation(node.location);

	}

	/**
	 * @param edge
	 */
	public void addIncome(final MapEdge edge) {
		incomes.add(edge);
	}

	/**
	 * @see org.mmarini.routes.model.MapElement#apply(org.mmarini.routes.model.MapElementVisitor)
	 */
	@Override
	public void apply(final MapElementVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new MapNode(this);
	}

	/**
	 * @return
	 */
	public int computeIncomeMaxPriority() {
		int priority = Integer.MIN_VALUE;
		for (final MapEdge edge : incomes) {
			final int p = edge.getPriority();
			if (p > priority && edge.isVeicleExiting()) {
				priority = p;
			}
		}
		return priority;
	}

	/**
	 * @param point
	 * @return
	 */
	public double getDistanceSq(final Point2D point) {
		return location.distanceSq(point);
	}

	/**
	 * @return the location
	 */
	public Point2D getLocation() {
		return location;
	}

	/**
	 * @param edge
	 */
	public void removeIncome(final MapEdge edge) {
		incomes.remove(edge);
	}

	/**
	 * @param x
	 * @param y
	 */
	public void setLocation(final double x, final double y) {
		location.setLocation(x, y);
	}

	/**
	 * @param location
	 */
	public void setLocation(final Point2D location) {
		this.location.setLocation(location);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Node(");
		builder.append(location.getX());
		builder.append(",");
		builder.append(location.getY());
		builder.append(")");
		return builder.toString();
	}
}
