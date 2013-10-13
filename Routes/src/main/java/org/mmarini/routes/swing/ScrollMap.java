/*
 * ScrollMap.java
 *
 * $Id: ScrollMap.java,v 1.15 2010/10/19 20:32:59 marco Exp $
 *
 * 06/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapElement;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.Module;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: ScrollMap.java,v 1.15 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class ScrollMap extends JScrollPane {

	private static final double SCALE_FACTOR = Math.sqrt(2);

	private static final long serialVersionUID = 1L;

	private static final Point LEGEND_LOCATION = new Point(5, 5);

	private static final Insets LEGEND_INSETS = new Insets(3, 3, 3, 3);

	private RouteMap routeMap;

	private Point point;

	private Point2D mapPoint;

	private Rectangle rect;

	private String[] pointLegendPattern;

	private String[] gridLegendPattern;

	private String[] edgeLegendPattern;

	/**
         * 
         */
	public ScrollMap() {
		routeMap = new RouteMap();
		point = new Point();
		mapPoint = new Point2D.Double();
		rect = new Rectangle();
		gridLegendPattern = loadPatterns("ScrollMap.gridLegendPattern");
		pointLegendPattern = loadPatterns("ScrollMap.pointLegendPattern");
		edgeLegendPattern = loadPatterns("ScrollMap.edgeLegendPattern");

		routeMap.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				handleMouseWheelMoved(e);
			}

		});

		setViewportView(routeMap);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, new JButton(
				new AbstractAction() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						scaleToFit();
					}
				}));
		routeMap.addMouseMotionListener(new MouseMotionAdapter() {

			/**
			 * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseMoved(MouseEvent e) {
				repaint();
			}

		});

		setDoubleBuffered(true);
		setOpaque(false);
	}

	/**
	 * Add a MapElementListener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addMapElementListener(MapElementListener listener) {
		routeMap.addMapElementListener(listener);
	}

	/**
	 * @param location
	 */
	private void centerTo(Point2D location) {
		routeMap.computeViewLocation(point, location);
		JViewport viewport = getViewport();
		Rectangle rec = viewport.getVisibleRect();
		point.x -= rec.width / 2;
		point.y -= rec.height / 2;
		validateView(point);
		viewport.setViewPosition(point);
	}

	/**
	 * @param location
	 * @param insets
	 * @param text
	 */
	private void computeRect(String[] text) {
		FontMetrics fm = getFontMetrics(getFont());
		rect.x = LEGEND_LOCATION.x;
		rect.y = LEGEND_LOCATION.y;
		int n = text.length;
		int w = 0;
		for (String tx : text) {
			w = Math.max(w, fm.stringWidth(tx));
		}
		rect.width = w + LEGEND_INSETS.left + LEGEND_INSETS.right;
		rect.height = fm.getHeight() * n + LEGEND_INSETS.bottom
				+ LEGEND_INSETS.top;
	}

	/**
	 * Return the current view scale
	 * 
	 * @return the current view scale (real px/virtual px)
	 */
	public double getScale() {
		return routeMap.getScale();
	}

	/**
	 * 
	 * @param e
	 */
	protected void handleMouseWheelMoved(MouseWheelEvent e) {
		double scale = Math.pow(SCALE_FACTOR, e.getWheelRotation());
		scale(e.getPoint(), getScale() * scale);
		repaint();
	}

	/**
	 * @param edge
	 * @return
	 */
	private boolean isShown(MapEdge edge) {
		Rectangle rect = getViewport().getViewRect();
		/*
		 * Top right visibile point
		 */
		routeMap.computeMapLocation(mapPoint, rect.getLocation());
		double x0 = mapPoint.getX();
		double y0 = mapPoint.getY();

		/*
		 * Bottom left visible point
		 */
		point.x = (int) rect.getMaxX();
		point.y = (int) rect.getMaxY();
		routeMap.computeMapLocation(mapPoint, point);
		double x1 = mapPoint.getX();
		double y1 = mapPoint.getY();

		/*
		 * begin point
		 */
		Point2D pt = edge.getBeginLocation();
		double x2 = pt.getX();
		double y2 = pt.getY();

		/*
		 * end point
		 */
		pt = edge.getEndLocation();
		double x3 = pt.getX();
		double y3 = pt.getY();

		double dx = x3 - x2;
		double dy = y3 - y2;
		if (Math.abs(dx) >= Math.abs(dy)) {
			double k = dy / dx;
			if (x3 < x2) {
				double t = x3;
				x3 = x2;
				x2 = t;
				t = y3;
				y3 = y2;
				y2 = t;
			}
			if (x3 < x0 || x2 > x1)
				return false;
			if (x2 < x0) {
				y2 = (x0 - x2) * k + y2;
				x2 = x0;
			}
			if (x3 > x1) {
				y3 = (x1 - x2) * k + y2;
			}
			return Math.max(y2, y3) >= y0 && Math.min(y2, y3) <= y1;
		} else {
			double k = dx / dy;
			if (y3 < y2) {
				double t = x3;
				x3 = x2;
				x2 = t;
				t = y3;
				y3 = y2;
				y2 = t;
			}
			if (y3 < y0 || y2 > y1)
				return false;
			if (y2 < y0) {
				x2 = (y0 - y2) * k + x2;
				y2 = y0;
			}
			if (y3 > y1) {
				x3 = (y1 - y2) * k + x2;
			}
			return Math.max(x2, x3) >= x0 && Math.min(x2, x3) <= x1;
		}
	}

	/**
	 * @param node
	 * @return
	 */
	private boolean isShown(MapNode node) {
		Rectangle rect = getViewport().getViewRect();
		routeMap.computeViewLocation(point, node.getLocation());
		return rect.contains(point);
	}

	/**
	 * @param key
	 * @return
	 */
	private String[] loadPatterns(String key) {
		List<String> list = new ArrayList<String>(0);
		int i = 0;
		for (;;) {
			String text = Messages.getString(key + "." + i);
			if (text.startsWith("!")) {
				break;
			}
			list.add(text);
			++i;
		}
		return list.toArray(new String[0]);
	}

	/**
	 * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
	 */
	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);
		paintInfo(g);
	}

	/**
	 * @param g
	 */
	private void paintInfo(Graphics g) {
		/*
		 * Compute the paramters
		 */
		Point pt = routeMap.getMousePosition();
		if (pt != null) {
			routeMap.computeMapLocation(mapPoint, pt);
		}
		Object[] parms = new Object[] { routeMap.getGridSize(),
				mapPoint.getX(), mapPoint.getY(), routeMap.getEdgeLength() };
		/*
		 * Compute the pattern
		 */
		String[] pattern;
		if (pt == null) {
			pattern = gridLegendPattern;
		} else if (routeMap.isSelectingEnd()) {
			pattern = edgeLegendPattern;
		} else {
			pattern = pointLegendPattern;
		}
		int n = pattern.length;
		String[] text = new String[n];
		for (int i = 0; i < n; ++i) {
			text[i] = MessageFormat.format(pattern[i], parms);
		}
		paintMessageBox(g, text);
	}

	/**
	 * 
	 * @param g
	 * @param messages
	 */
	private void paintMessageBox(Graphics g, String[] messages) {
		computeRect(messages);
		FontMetrics fm = getFontMetrics(getFont());
		g.setColor(Color.WHITE);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(Color.BLACK);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		int x = rect.x + LEGEND_INSETS.left;
		int fh = fm.getHeight();
		int y = rect.y + LEGEND_INSETS.top + fh - fm.getDescent();
		for (String text : messages) {
			g.drawString(text, x, y);
			y += fh;
		}
	}

	/**
	 * Reset the status of the view.<br>
	 * This method reset the map view. It must be call when a map is changed.
	 */
	public void reset() {
		routeMap.reset();
	}

	/**
	 * 
	 * @param ref
	 * @param scale
	 */
	private void scale(Point ref, double scale) {
		JViewport viewport = getViewport();
		Point pt = viewport.getViewPosition();
		int dx = ref.x - pt.x;
		int dy = ref.y - pt.y;
		routeMap.computeMapLocation(mapPoint, ref);
		routeMap.setScale(scale);
		routeMap.computeViewLocation(point, mapPoint);
		point.x -= dx;
		point.y -= dy;
		viewport.setViewPosition(point);
	}

	/**
	 * Scale the view to fit the current component size
	 */
	public void scaleToFit() {
		routeMap.scaleToFit(getViewport().getSize());
		repaint();
	}

	/**
	 * Scroll the map view and center to an edge
	 * 
	 * @param edge
	 *            the edge element to center the view to
	 */
	public void scrollTo(MapEdge edge) {
		if (!isShown(edge)) {
			Point2D b = edge.getBeginLocation();
			Point2D e = edge.getEndLocation();
			double x = (b.getX() + e.getX()) * 0.5;
			double y = (b.getY() + e.getY()) * 0.5;
			mapPoint.setLocation(x, y);
			centerTo(mapPoint);
		}
	}

	/**
	 * @param node
	 */
	public void scrollTo(MapNode node) {
		if (!isShown(node)) {
			centerTo(node.getLocation());
		}
	}

	/**
	 * Set the mediator.<br>
	 * The mediator is used by RouteMap subcomponent of this component.
	 * 
	 * @param mediator
	 *            the mediator
	 */
	public void setMediator(RouteMediator handler) {
		routeMap.setMediator(handler);
	}

	/**
	 * Sets the view scale
	 * 
	 * @param scale
	 *            the scale (real px/virtual px)
	 */
	public void setScale(double scale) {
		routeMap.setScale(scale);
	}

	/**
	 * @param element
	 */
	public void setSelectedElement(MapElement element) {
		routeMap.setSelectedElement(element);
	}

	/**
	 * @param trafficView
	 */
	public void setTrafficView(boolean trafficView) {
		routeMap.setTrafficView(trafficView);
	}

	/**
         * 
         */
	public void startCenterMode() {
		routeMap.startCenterMode();
	}

	/**
         * 
         * 
         */
	public void startEdgeMode() {
		routeMap.startEdgeMode();
	}

	/**
	 * 
	 * @param module
	 */
	public void startModuleMode(Module module) {
		routeMap.startModuleMode(module);
	}

	/**
         * 
         * 
         */
	public void startSelectMode() {
		routeMap.startSelectMode();
	}

	/**
	 * @param point2
	 */
	private void validateView(Point point) {
		JViewport viewport = getViewport();
		Dimension size = viewport.getViewSize();
		Rectangle rec = viewport.getVisibleRect();
		if (point.x + rec.width >= size.width) {
			point.x = size.width - rec.width;
		}
		if (point.x < 0) {
			point.x = 0;
		}
		if (point.y + rec.height >= size.height) {
			point.y = size.height - rec.height;
		}
		if (point.y < 0) {
			point.y = 0;
		}
	}

	/**
         * 
         */
	public void zoomIn() {
		Rectangle rect = getViewport().getViewRect();
		point.x = (int) Math.round(rect.getCenterX());
		point.y = (int) Math.round(rect.getCenterY());
		scale(point, getScale() * SCALE_FACTOR);
	}

	/**
         * 
         */
	public void zoomOut() {
		Rectangle rect = getViewport().getViewRect();
		point.x = (int) Math.round(rect.getCenterX());
		point.y = (int) Math.round(rect.getCenterY());
		scale(point, getScale() / SCALE_FACTOR);
	}
}
