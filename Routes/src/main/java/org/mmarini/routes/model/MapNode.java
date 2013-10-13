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

import org.mmarini.routes.xml.Dumpable;
import org.mmarini.routes.xml.Dumper;
import org.w3c.dom.Element;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapNode.java,v 1.9 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class MapNode implements MapElement, Dumpable, Cloneable {
	private Point2D location;

	private List<MapEdge> incomes;

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
	public MapNode(MapNode node) {
		this();
		location.setLocation(node.location);

	}

	/**
	 * @param edge
	 */
	public void addIncome(MapEdge edge) {
		incomes.add(edge);
	}

	/**
	 * @see org.mmarini.routes.model.MapElement#apply(org.mmarini.routes.model.MapElementVisitor)
	 */
	@Override
	public void apply(MapElementVisitor visitor) {
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
		for (MapEdge edge : incomes) {
			int p = edge.getPriority();
			if (p > priority && edge.isVeicleExiting()) {
				priority = p;
			}
		}
		return priority;
	}

	/**
	 * @see org.mmarini.routes.xml.Dumpable#dump(org.w3c.dom.Element)
	 */
	@Override
	public void dump(Element root) {
		Dumper dumper = Dumper.getInstance();
		dumper.dumpObject(root, "location", location);
		dumper.dumpReference(root, "incomes", incomes);
	}

	/**
	 * @param point
	 * @return
	 */
	public double getDistanceSq(Point2D point) {
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
	public void removeIncome(MapEdge edge) {
		incomes.remove(edge);
	}

	/**
	 * @param x
	 * @param y
	 */
	public void setLocation(double x, double y) {
		location.setLocation(x, y);
	}

	/**
	 * @param location
	 */
	public void setLocation(Point2D location) {
		this.location.setLocation(location);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node(");
		builder.append(location.getX());
		builder.append(",");
		builder.append(location.getY());
		builder.append(")");
		return builder.toString();
	}
}
