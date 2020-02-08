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

package org.mmarini.routes.swing.v2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.swing.JComponent;

import org.mmarini.routes.model.Constants;
import org.mmarini.routes.model.v2.EdgeTraffic;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 */
public class RouteMap extends JComponent implements Constants {

	private class Painter {
		private final Graphics2D graphics;
		private final Map<MapNode, Color> colorMap;
		private final Rectangle2D bound;
		private final boolean borderPainted;

		/**
		 * @param graphics
		 * @param bound
		 * @param colorMap
		 * @param borderPainted
		 */
		public Painter(final Graphics2D graphics, final Rectangle2D bound, final Map<MapNode, Color> colorMap,
				final boolean borderPainted) {
			super();
			this.graphics = graphics;
			this.bound = bound;
			this.colorMap = colorMap;
			this.borderPainted = borderPainted;
		}

		/**
		 * Returns the painter with map of colors of sites
		 */
		protected Painter computeSiteColorMap() {
			final Map<MapNode, Color> map = status.map(s -> {
				return SwingUtils.buildColorMap(s.getMap().getSites());
			}).orElseGet(() -> Collections.emptyMap());
			return new Painter(graphics, bound, map, borderPainted);
		}

		/**
		 * Returns the painter with painted canvas
		 */
		public Painter paint() {
			return computeSiteColorMap().paintGrid().paintEdges().paintSites().paintSelectedEdge().paintVehicles()
					.paintSelectedNode().paintSelectedSite().paintDragEdge();
		}

		/**
		 *
		 * @return
		 */
		private Painter paintDragEdge() {
			dragEdge.ifPresent(line -> {
				graphics.setColor(EDGE_DRAGING_COLOR);
				graphics.setStroke(STROKE);
				graphics.draw(new Line2D.Double(line.getElem1(), line.getElem2()));
			});
			return this;
		}

		/**
		 * Returns the painter with painted edge
		 *
		 * @param edge the edge
		 */
		private Painter paintEdge(final MapEdge edge) {
			graphics.setStroke(STROKE);
			graphics.draw(new Line2D.Double(edge.getBeginLocation(), edge.getEndLocation()));
			return this;
		}

		/**
		 * Returns the painter with painted edges
		 */
		private Painter paintEdges() {
			status.ifPresent(s -> {
				if (trafficView) {
					s.getTraffics().forEach(this::paintTraffic);
				} else {
					graphics.setColor(EDGE_COLOR);
					s.getMap().getEdges().forEach(this::paintEdge);
				}
			});
			return this;
		}

		/**
		 * Returns the painter with painted grid
		 */
		private Painter paintGrid() {
			final Color minorColor = trafficView ? MINOR_GRID_REVERSED_COLOR : MINOR_GRID_COLOR;
			final Color majorColor = trafficView ? MAJOR_GRID_REVERSED_COLOR : MAJOR_GRID_COLOR;
			graphics.setStroke(THIN_STROKE);

			final long i0 = (long) Math.floor(bound.getMinX() / gridSize);
			final long i1 = (long) Math.floor(bound.getMaxX() / gridSize) + 1;
			final long j0 = (long) Math.floor(bound.getMinY() / gridSize);
			final long j1 = (long) Math.floor(bound.getMaxY() / gridSize) + 1;

			final double x0 = i0 * gridSize;
			final double x1 = i1 * gridSize;
			final double y0 = j0 * gridSize;
			final double y1 = j1 * gridSize;
			for (long i = i0; i <= i1; ++i) {
				graphics.setColor((i % 10) == 0 ? majorColor : minorColor);
				final double x = i * gridSize;
				graphics.draw(new Line2D.Double(x, y0, x, y1));

			}
			for (long j = j0; j <= j1; ++j) {
				graphics.setColor((j % 10) == 0 ? majorColor : minorColor);
				final double y = j * gridSize;
				graphics.draw(new Line2D.Double(x0, y, x1, y));

			}
			return this;
		}

		/**
		 *
		 * @param node
		 * @return
		 */
		private Painter paintNodeShape(final MapNode node) {
			final Ellipse2D shape = new Ellipse2D.Double(-NODE_SIZE * 0.5 + node.getX(), -NODE_SIZE * 0.5 + node.getY(),
					NODE_SIZE, NODE_SIZE);
			graphics.fill(shape);
			if (borderPainted) {
				graphics.setStroke(THIN_STROKE);
				graphics.setColor(trafficView ? Color.WHITE : Color.BLACK);
				graphics.draw(shape);
			}
			return this;
		}

		private Painter paintSelectedEdge() {
			selectedEdge.filter(s -> isBlink()).ifPresent(edge -> {
				graphics.setColor(SELECTED_EDGE_COLOR);
				paintEdge(edge);
				graphics.setColor(BEGIN_NODE_COLOR);
				paintNodeShape(edge.getBegin());
				graphics.setColor(END_NODE_COLOR);
				paintNodeShape(edge.getEnd());
			});
			return this;
		}

		private Painter paintSelectedNode() {
			selectedNode.filter(s -> isBlink()).ifPresent(node -> {
				graphics.setColor(SELECTED_NODE_COLOR);
				paintNodeShape(node);
			});
			return this;
		}

		private Painter paintSelectedSite() {
			selectedSite.filter(s -> isBlink()).ifPresent(site -> {
				graphics.setColor(SELECTED_SITE_COLOR);
				paintSiteShape(site);
			});
			return this;
		}

		/**
		 * @param site
		 */
		private Painter paintSite(final MapNode site) {
			final Color color = colorMap.getOrDefault(site, DEFAULT_SITE_COLOR);
			graphics.setColor(color);
			return paintSiteShape(site);
		}

		/**
		 * Returns the painter with painted sites
		 */
		private Painter paintSites() {
			status.ifPresent(st -> {
				st.getMap().getSites().forEach(this::paintSite);
			});
			return this;
		}

		/**
		 * Returns the painter with a painted node
		 *
		 * @param node the node
		 */
		private Painter paintSiteShape(final MapNode node) {
			final Ellipse2D shape = new Ellipse2D.Double(-SITE_SIZE * 0.5 + node.getX(), -SITE_SIZE * 0.5 + node.getY(),
					SITE_SIZE, SITE_SIZE);
			graphics.fill(shape);
			if (borderPainted) {
				graphics.setStroke(THIN_STROKE);
				graphics.setColor(trafficView ? Color.WHITE : Color.BLACK);
				graphics.draw(shape);
			}
			return this;
		}

		private Painter paintTraffic(final EdgeTraffic traffic) {
			final double tc = traffic.getTrafficCongestion();
			final Color trafficColor = SwingUtils.computeColor(tc, 1.0);
			graphics.setColor(trafficColor);
			paintEdge(traffic.getEdge());
			return this;
		}

		/**
		 * Returns the painter with the painted vehicle
		 *
		 * @param v the vehicle
		 */
		private Painter paintVehicle(final VehicleInfo v) {
			graphics.setColor(v.color);
			final AffineTransform rotTr = AffineTransform.getRotateInstance(v.direction.getX(), v.direction.getY());
			final AffineTransform translateTr = AffineTransform.getTranslateInstance(v.location.getX(),
					v.location.getY());
			translateTr.concatenate(rotTr);
			final Shape shape = translateTr.createTransformedShape(VEHICLE_SHAPE);
			graphics.fill(shape);
			if (borderPainted) {
				graphics.setStroke(THIN_STROKE);
				graphics.setColor(Color.BLACK);
				graphics.draw(shape);
			}
			return this;
		}

		/**
		 * Returns the painter with painted vehicles
		 */
		private Painter paintVehicles() {
			if (!trafficView) {
				status.stream().<EdgeTraffic>flatMap(st -> st.getTraffics().stream())
						.<VehicleInfo>flatMap(te -> te.getVehicles().stream().map(v -> {
							final Point2D location = te.getEdge().getLocation(v.getLocation());
							final Point2D direction = te.getEdge().getDirection();
							final Color color = colorMap.getOrDefault(v.getTarget(), DEFAULT_VEHICLE_COLOR);
							return new VehicleInfo(location, direction, color);
						})).forEach(this::paintVehicle);
			}
			return this;

		}
	}

	static class VehicleInfo {
		public final Color color;
		public final Point2D location;
		public final Point2D direction;

		/**
		 * @param location
		 * @param direction
		 * @param color
		 */
		public VehicleInfo(final Point2D location, final Point2D direction, final Color color) {
			super();
			this.location = location;
			this.direction = direction;
			this.color = color;
		}
	}

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(RouteMap.class);

	public static final double VEHICLE_WIDTH = 3;
	public static final double EDGE_WIDTH = 5;
	public static final double SITE_SIZE = 10;
	public static final double NODE_SIZE = 5;
	private static final double BORDER_SCALE = 1;
	private static final int BLINKING_ON_TIME = 100;
	private static final int BLINKING_TIME = 300;
	private static final Color END_NODE_COLOR = Color.RED;
	private static final Color BEGIN_NODE_COLOR = Color.GREEN;
	private static final Color DEFAULT_SITE_COLOR = Color.GRAY;
	private static final Color DEFAULT_VEHICLE_COLOR = Color.GRAY;
	private static final Color SELECTED_SITE_COLOR = Color.WHITE;
	private static final Color EDGE_DRAGING_COLOR = Color.GRAY;
	private static final Color EDGE_COLOR = Color.LIGHT_GRAY;
	private static final Color MAJOR_GRID_COLOR = new Color(0xc0c0c0);
	private static final Color MINOR_GRID_COLOR = new Color(0xe0e0e0);
	private static final Color MAJOR_GRID_REVERSED_COLOR = new Color(0x202020);
	private static final Color MINOR_GRID_REVERSED_COLOR = new Color(0x101010);
	private static final Color SELECTED_NODE_COLOR = Color.RED;
	private static final Color SELECTED_EDGE_COLOR = Color.YELLOW;
	private static final BasicStroke STROKE = new BasicStroke((float) EDGE_WIDTH, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	private static final BasicStroke THIN_STROKE = new BasicStroke(0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Rectangle2D VEHICLE_SHAPE = new Rectangle2D.Double(-VEHICLE_LENGTH, -VEHICLE_WIDTH / 2,
			VEHICLE_LENGTH, VEHICLE_WIDTH);

	static boolean isBlink() {
		return System.currentTimeMillis() % BLINKING_TIME >= BLINKING_ON_TIME;
	}

	private final Observable<MouseEvent> mouseObs;
	private final Observable<MouseWheelEvent> mouseWheelObs;
	private final Observable<KeyEvent> keyboardObs;
	private boolean trafficView;
	private Optional<Traffics> status;
	private AffineTransform transform;
	private double gridSize;
	private Optional<MapNode> selectedNode;
	private Optional<MapEdge> selectedEdge;
	private Optional<MapNode> selectedSite;
	private Optional<Tuple2<Point2D, Point2D>> dragEdge;

	/**
	 *
	 */
	public RouteMap() {
		super();
		this.transform = new AffineTransform();
		this.gridSize = 10;
		this.mouseObs = SwingObservable.mouse(this);
		this.mouseWheelObs = SwingObservable.mouseWheel(this);
		this.keyboardObs = SwingObservable.keyboard(this);
		this.dragEdge = Optional.empty();
		setFocusable(true);
		setRequestFocusEnabled(true);
		requestFocus();

		this.status = Optional.empty();
		this.selectedNode = Optional.empty();
		this.selectedSite = Optional.empty();
		this.selectedEdge = Optional.empty();
		setBackground(Color.WHITE);
		setOpaque(true);
		setDoubleBuffered(true);
		logger.debug("RouteMap created");
	}

	public RouteMap clearSelection() {
		this.selectedSite = Optional.empty();
		this.selectedNode = Optional.empty();
		this.selectedEdge = Optional.empty();
		return this;
	}

	/**
	 * @return the keyboardObs
	 */
	public Observable<KeyEvent> getKeyboardObs() {
		return keyboardObs;
	}

	/**
	 * @return the mouseObs
	 */
	public Observable<MouseEvent> getMouseObs() {
		return mouseObs;
	}

	/**
	 * @return the mouseWeelObs
	 */
	public Observable<MouseWheelEvent> getMouseWheelObs() {
		return mouseWheelObs;
	}

	/**
	 * @return the selectedEdge
	 */
	Optional<MapEdge> getSelectedEdge() {
		return selectedEdge;
	}

	/**
	 * @return the selectedNode
	 */
	Optional<MapNode> getSelectedNode() {
		return selectedNode;
	}

	/**
	 * @return the selectedSite
	 */
	Optional<MapNode> getSelectedSite() {
		return selectedSite;
	}

	/**
	 * Returns true if border is painted
	 */
	boolean isBorderPainted() {
		final double scale = Math.max(transform.getScaleX(), transform.getScaleY());
		final boolean borderPainted = scale >= BORDER_SCALE;
		return borderPainted;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		final Dimension size = getPreferredSize();
		final Dimension rsize = getSize();
		Color bg;
		if (trafficView) {
			bg = Color.BLACK;
		} else {
			bg = getBackground();
		}
		g.setColor(bg);
		g.fillRect(0, 0, rsize.width, rsize.height);
		try {
			final Rectangle2D bound = new Rectangle2D.Double(0, 0, size.width, size.height);
			final Rectangle2D realBound = transform.createInverse().createTransformedShape(bound).getBounds2D();
			final Graphics2D gr = (Graphics2D) g.create();
			gr.transform(transform);
			new Painter(gr, realBound, Collections.emptyMap(), isBorderPainted()).paint();
		} catch (final NoninvertibleTransformException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Returns the route map with drag edge
	 *
	 * @param edge the edge
	 */
	public RouteMap setDragEdge(final Optional<Tuple2<Point2D, Point2D>> edge) {
		this.dragEdge = edge;
		repaint();
		return this;
	}

	/**
	 * Returns the route map with grid size set
	 *
	 * @param gridSize the grid size in meters
	 */
	public RouteMap setGridSize(final double gridSize) {
		logger.debug("setGridSize {}", gridSize);
		this.gridSize = gridSize;
		return this;
	}

	/**
	 * Returns the route map with selected edge
	 *
	 * @param edge the edge
	 */
	public RouteMap setSelectedEdge(final Optional<MapEdge> edge) {
		clearSelection();
		this.selectedEdge = edge;
		return this;
	}

	/**
	 * Returns the route map with selected node
	 *
	 * @param node the selected node
	 */
	public RouteMap setSelectedNode(final Optional<MapNode> node) {
		clearSelection();
		this.selectedNode = node;
		return this;
	}

	/**
	 * Returns the route map with selected site
	 *
	 * @param site the selected site
	 */
	public RouteMap setSelectedSite(final Optional<MapNode> site) {
		clearSelection();
		this.selectedSite = site;
		return this;
	}

	/**
	 * @param status the status to set
	 */
	public RouteMap setStatus(final Traffics status) {
		this.status = Optional.ofNullable(status);
		return this;
	}

	/**
	 * @param trafficView the trafficView to set
	 * @return
	 */
	public RouteMap setTrafficView(final boolean trafficView) {
		this.trafficView = trafficView;
		return this;
	}

	/**
	 * Returns the route map with set transform
	 *
	 * @param transform the transform from map coordinate to screen coordinate
	 */
	public RouteMap setTransform(final AffineTransform transform) {
		this.transform = transform;
		return this;
	}
}
