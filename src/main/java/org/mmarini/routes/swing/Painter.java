/*
 * RouteMap.java
 *
 * $Id: Painter.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.Module;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Painter.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 */
public class Painter {

	private static final Color END_NODE_COLOR = Color.RED;

	private static final Color BEGIN_NODE_COLOR = Color.GREEN;

	private static final Color SELECTED_SITE_COLOR = Color.WHITE;

	private static final Color EDGE_COLOR = Color.LIGHT_GRAY;

	private static final Color MAJOR_GRID_COLOR = new Color(0xd0d0d0);

	private static final Color MINOR_GRID_COLOR = new Color(0xe0e0e0);

	private static final Color MAJOR_GRID_REVERSED_COLOR = new Color(0x202020);

	private static final Color MINOR_GRID_REVERSED_COLOR = new Color(0x101010);

	private static final Color SELECTED_NODE_COLOR = Color.RED;

	private static final Color SELECTED_EDGE_COLOR = Color.YELLOW;

	private static final double VEICLE_LENGTH = 5;

	private static final double VEICLE_WIDTH = 3;

	private static final double EDGE_WIDTH = 5;

	private static final double NODE_SIZE = 10;

	private Graphics2D graphics;

	private final BasicStroke thinStroke;

	private final Line2D line;

	private final BasicStroke stroke;

	private boolean borderPainted;

	private final Shape veicleShape;

	private final Shape siteShape;

	private final Shape edgeEndPoint;

	private boolean reversed;

	/**
	     *
	     */
	public Painter() {
		stroke = new BasicStroke((float) EDGE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		thinStroke = new BasicStroke(0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		line = new Line2D.Double();
		veicleShape = new Rectangle2D.Double(-VEICLE_LENGTH * 0.5, -VEICLE_WIDTH * 0.5, VEICLE_LENGTH, VEICLE_WIDTH);
		siteShape = new Ellipse2D.Double(-NODE_SIZE * 0.5, -NODE_SIZE * 0.5, NODE_SIZE, NODE_SIZE);
		edgeEndPoint = new Ellipse2D.Double(-EDGE_WIDTH * 0.5, -EDGE_WIDTH * 0.5, EDGE_WIDTH, EDGE_WIDTH);
	}

	/**
	 * @param g
	 * @param tr
	 * @param edge
	 */
	public void paintCursorEdge(final MapEdge edge) {
		final Point2D beginLocation = edge.getBeginLocation();
		final Point2D endLocation = edge.getEndLocation();
		paintEdge(beginLocation, endLocation, SELECTED_EDGE_COLOR);
	}

	/**
	 *
	 * @param edge
	 */
	public void paintCursorEdgeEnds(final MapEdge edge) {
		final Point2D beginLocation = edge.getBeginLocation();
		final Point2D endLocation = edge.getEndLocation();
		paintShape(edgeEndPoint, beginLocation, BEGIN_NODE_COLOR);
		paintShape(edgeEndPoint, endLocation, END_NODE_COLOR);
	}

	/**
	 *
	 * @param edge
	 */
	public void paintEdge(final MapEdge edge) {
		paintEdge(edge, EDGE_COLOR);
	}

	/**
	 *
	 * @param edge
	 * @param color
	 */
	public void paintEdge(final MapEdge edge, final Color color) {
		paintEdge(edge.getBeginLocation(), edge.getEndLocation(), color);
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @param color
	 */
	public void paintEdge(final Point2D from, final Point2D to, final Color color) {
		line.setLine(from, to);
		graphics.setColor(color);
		graphics.setStroke(stroke);
		graphics.draw(line);
	}

	/**
	 *
	 * @param bound
	 * @param size
	 */
	public void paintGrid(final Rectangle2D bound, final double size) {
		final double x0 = bound.getMinX();
		final double x1 = bound.getMaxX();
		final double y1 = bound.getMaxY();
		final double y0 = bound.getMinY();
		final Color minorColor = reversed ? MINOR_GRID_REVERSED_COLOR : MINOR_GRID_COLOR;
		final Color majorColor = reversed ? MAJOR_GRID_REVERSED_COLOR : MAJOR_GRID_COLOR;
		graphics.setColor(minorColor);
		graphics.setStroke(thinStroke);
		for (double x = Math.floor(x0 / size) * size; x <= x1; x += size) {
			final double xg = Math.floor(x / size / 10.) * 10. * size;
			if (x == xg) {
				graphics.setColor(majorColor);
			} else {
				graphics.setColor(minorColor);
			}
			line.setLine(x, y0, x, y1);
			graphics.draw(line);
		}
		for (double y = Math.floor(y0 / size) * size; y <= y1; y += size) {
			final double xg = Math.floor(y / size / 10.) * 10. * size;
			if (y == xg) {
				graphics.setColor(majorColor);
			} else {
				graphics.setColor(minorColor);
			}
			line.setLine(x0, y, x1, y);
			graphics.draw(line);
		}
	}

	/**
	 * @param module
	 * @param location
	 * @param vecx
	 * @param vecy
	 */
	public void paintModule(final Module module, final Point2D location, final double vecx, final double vecy) {
		final AffineTransform old = graphics.getTransform();
		final AffineTransform tr = graphics.getTransform();
		tr.translate(location.getX(), location.getY());
		tr.rotate(vecx, vecy);
		graphics.setTransform(tr);
		for (final MapEdge edge : module.getEdges()) {
			paintEdge(edge);
		}
		graphics.setTransform(old);
	}

	/**
	 *
	 * @param center
	 */
	public void paintNodeCursor(final Point2D center) {
		paintShape(edgeEndPoint, center, SELECTED_NODE_COLOR);
	}

	/**
	 *
	 * @param shape
	 * @param location
	 * @param color
	 */
	private void paintShape(final Shape shape, final Point2D location, final Color color) {
		graphics.setColor(color);
		final AffineTransform tr = graphics.getTransform();
		graphics.translate(location.getX(), location.getY());
		graphics.fill(shape);
		if (borderPainted) {
			graphics.setStroke(thinStroke);
			graphics.setColor(reversed ? Color.WHITE : Color.BLACK);
			graphics.draw(shape);
		}
		graphics.setTransform(tr);
	}

	/**
	 *
	 * @param location
	 * @param color
	 */
	public void paintSite(final Point2D location, final Color color) {
		paintShape(siteShape, location, color);
	}

	/**
	 *
	 * @param center
	 */
	public void paintSiteCursor(final Point2D center) {
		paintSite(center, SELECTED_SITE_COLOR);
	}

	/**
	 *
	 * @param location
	 * @param vec
	 * @param color
	 */
	public void paintVeicle(final Point2D location, final Point2D vec, final Color color) {
		graphics.setColor(color);
		final AffineTransform old = graphics.getTransform();
		final AffineTransform tr = graphics.getTransform();
		tr.setToTranslation(location.getX(), location.getY());
		tr.rotate(vec.getX(), vec.getY());
		graphics.transform(tr);
		graphics.fill(veicleShape);
		if (borderPainted) {
			graphics.setStroke(thinStroke);
			graphics.setColor(Color.BLACK);
			graphics.draw(veicleShape);
		}
		graphics.setTransform(old);
	}

	/**
	 * @param borderPainted the borderPainted to set
	 */
	public void setBorderPainted(final boolean borderPainted) {
		this.borderPainted = borderPainted;
	}

	/**
	 * @param graphics the graphics to set
	 */
	public void setGraphics(final Graphics2D graphics) {
		this.graphics = graphics;
	}

	/**
	 * @param reversed the reversed to set
	 */
	public void setReversed(final boolean reversed) {
		this.reversed = reversed;
	}
}
