/*
 * MapNode.java
 *
 * $Id: MapNode.java,v 1.9 2010/10/19 20:33:00 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
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
