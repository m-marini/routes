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
import java.awt.Point;
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

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.EdgeTraffic;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapModule;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Component that renders the traffics map.
 */
public class RouteMap extends JComponent implements Constants {
	/** The mouse modes */
	static enum MouseMode {
		SELECTION, START_EDGE, DRAG_EDGE, DRAG_MODULE, ROTATE_MODULE
	}

	private class Painter {
		private final Graphics2D graphics;
		private final Map<MapNode, Color> colorMap;
		private final Rectangle2D bound;
		private final boolean borderPainted;

		/**
		 * Creates the painter.
		 *
		 * @param graphics      the graphics context
		 * @param bound         the map bound
		 * @param colorMap      the site color map
		 * @param borderPainted true id border painted
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
		 * Sets the site color map.
		 *
		 * @return the painter
		 */
		protected Painter computeSiteColorMap() {
			final Map<MapNode, Color> map = traffics.map(s -> {
				return SwingUtils.buildColorMap(s.getMap().getSites());
			}).orElseGet(() -> Collections.emptyMap());
			return new Painter(graphics, bound, map, borderPainted);
		}

		/**
		 * Paints the whole map.
		 *
		 * @return the painter
		 */
		public Painter paint() {
			return computeSiteColorMap().paintGrid().paintEdges().paintSites().paintSelectedEdge().paintVehicles()
					.paintSelectedNode().paintSelectedSite().paintModule().paintDragEdge();
		}

		/**
		 * Paints the drag edge.
		 *
		 * @return the painter
		 */
		private Painter paintDragEdge() {
			dragEdge.ifPresent(line -> {
				graphics.setColor(EDGE_DRAGING_COLOR);
				graphics.setStroke(STROKE);
				graphics.draw(new Line2D.Double(line.get1(), line.get2()));
			});
			return this;
		}

		/**
		 * Paints the edge.
		 *
		 * @param edge the edge
		 * @return the painter
		 */
		private Painter paintEdge(final MapEdge edge) {
			graphics.setStroke(STROKE);
			graphics.draw(new Line2D.Double(edge.getBeginLocation(), edge.getEndLocation()));
			return this;
		}

		/**
		 * Paints the edges.
		 *
		 * @return the painter
		 */
		private Painter paintEdges() {
			traffics.ifPresent(s -> {
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
		 * Paints the grid.
		 *
		 * @return the painter
		 */
		private Painter paintGrid() {
			final Color minorColor = trafficView ? MINOR_GRID_REVERSED_COLOR : MINOR_GRID_COLOR;
			final Color majorColor = trafficView ? MAJOR_GRID_REVERSED_COLOR : MAJOR_GRID_COLOR;
			graphics.setStroke(THIN_STROKE);

			final double gridSize = getGridSize();
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
		 * Paints the module.
		 *
		 * @return the painter
		 */
		private Painter paintModule() {
			module.ifPresent(m -> {
				pivot.ifPresent(p -> {
					final AffineTransform temp = graphics.getTransform();
					final AffineTransform tran = new AffineTransform(temp);
					tran.translate(p.getX(), p.getY());
					tran.rotate(angle);
					graphics.setTransform(tran);
					graphics.setColor(EDGE_COLOR);
					m.getEdges().forEach(this::paintEdge);
					graphics.setTransform(temp);
				});
			});
			return this;
		}

		/**
		 * Paints the shape of node.
		 *
		 * @param node the node
		 * @return the painter
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

		/**
		 * Paints the selected edge.
		 *
		 * @return the painter
		 */
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

		/**
		 * Paints the selected node.
		 *
		 * @return the painter
		 */
		private Painter paintSelectedNode() {
			selectedNode.filter(s -> isBlink()).ifPresent(node -> {
				graphics.setColor(SELECTED_NODE_COLOR);
				paintNodeShape(node);
			});
			return this;
		}

		/**
		 * Paints the selected site.
		 *
		 * @return the painter
		 */
		private Painter paintSelectedSite() {
			selectedSite.filter(s -> isBlink()).ifPresent(site -> {
				graphics.setColor(SELECTED_SITE_COLOR);
				paintSiteShape(site);
			});
			return this;
		}

		/**
		 * Paints the site.
		 *
		 * @param site the site
		 * @return the painter
		 */
		private Painter paintSite(final MapNode site) {
			final Color color = colorMap.getOrDefault(site, DEFAULT_SITE_COLOR);
			graphics.setColor(color);
			return paintSiteShape(site);
		}

		/**
		 * Paints the sites.
		 *
		 * @return the painter
		 */
		private Painter paintSites() {
			traffics.ifPresent(st -> {
				st.getMap().getSites().forEach(this::paintSite);
			});
			return this;
		}

		/**
		 * Paints the node.
		 *
		 * @param node the node
		 * @return the painter
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

		/**
		 * Paints the edge traffics.
		 *
		 * @param traffic the edge traffic
		 * @return the painter
		 */
		private Painter paintTraffic(final EdgeTraffic traffic) {
			final double tc = traffic.getTrafficCongestion();
			final Color trafficColor = SwingUtils.computeColor(tc, 1.0);
			graphics.setColor(trafficColor);
			paintEdge(traffic.getEdge());
			return this;
		}

		/**
		 * Paints the vehicle.
		 *
		 * @param v the vehicle
		 * @return the painter
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
		 * Paints the vehicles.
		 *
		 * @return the painter
		 */
		private Painter paintVehicles() {
			if (!trafficView) {
				traffics.stream().<EdgeTraffic>flatMap(st -> st.getTraffics().stream())
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

	/** The vehicle information. */
	static class VehicleInfo {
		public final Color color;
		public final Point2D location;
		public final Point2D direction;

		/**
		 * Creates the vehicle information.
		 *
		 * @param location  the location point
		 * @param direction the vehicle direction
		 * @param color     the vehicle color
		 */
		public VehicleInfo(final Point2D location, final Point2D direction, final Color color) {
			super();
			this.location = location;
			this.direction = direction;
			this.color = color;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(RouteMap.class);

	public static final int MAP_INSETS = 60;
	public static final double DEFAULT_SCALE = 1;
	private static final double MIN_GRID_SIZE_METERS = 1;
	private static final int MIN_GRID_SIZE_PIXELS = 10;
	private static final double CURSOR_SELECTION_PRECISION = 10;
	private static final long serialVersionUID = 1L;
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

	/** Returns true if blinking component is visible. */
	static boolean isBlink() {
		return System.currentTimeMillis() % BLINKING_TIME >= BLINKING_ON_TIME;
	}

	/**
	 * Returns true if location is in range of an edge.
	 *
	 * @param node  edge
	 * @param point point
	 */
	private static boolean isInRange(final MapEdge edge, final Point2D point) {
		final double distance = edge.getDistance(point);
		return distance <= RouteMap.EDGE_WIDTH / 2;
	}

	/**
	 * Returns true if location is in range of site.
	 *
	 * @param node  site
	 * @param point point
	 */
	private static boolean isInRange(final MapNode node, final Point2D point, final double maxDistance) {
		final double distance = node.getLocation().distance(point);
		return distance <= maxDistance;
	}

	private final Flowable<MouseEvent> mouseFlow;
	private final Flowable<MouseWheelEvent> mouseWheelFlow;
	private final Flowable<KeyEvent> keyboardFlow;
	private boolean trafficView;
	private Optional<Traffics> traffics;
	private AffineTransform transform;
	private Optional<MapNode> selectedNode;
	private Optional<MapEdge> selectedEdge;
	private Optional<MapNode> selectedSite;
	private Optional<Tuple2<Point2D, Point2D>> dragEdge;
	private Optional<MapModule> module;
	private Optional<Point2D> pivot;
	private double angle;

	private double scale;

	private MouseMode mode;

	/** Creates the component. */
	public RouteMap() {
		super();
		this.transform = new AffineTransform();
		this.mouseFlow = SwingUtils.mouse(this);
		this.mouseWheelFlow = SwingUtils.mouseWheel(this);
		this.keyboardFlow = SwingUtils.keyboard(this);
		this.dragEdge = Optional.empty();
		this.module = Optional.empty();
		this.pivot = Optional.empty();
		this.angle = 0;
		this.scale = DEFAULT_SCALE;
		this.mode = MouseMode.SELECTION;
		setFocusable(true);
		setRequestFocusEnabled(true);
		requestFocus();

		this.traffics = Optional.empty();
		this.selectedNode = Optional.empty();
		this.selectedSite = Optional.empty();
		this.selectedEdge = Optional.empty();
		setBackground(Color.WHITE);
		setOpaque(true);
		setDoubleBuffered(true);
		logger.debug("RouteMap created");
	}

	/**
	 * Clear all selected elements.
	 *
	 * @return the map component
	 */
	public RouteMap clearSelection() {
		this.selectedSite = Optional.empty();
		this.selectedNode = Optional.empty();
		this.selectedEdge = Optional.empty();
		return this;
	}

	/**
	 * Returns the location for a pivot point.
	 *
	 * @param viewportPosition the viewport point
	 * @param pivot            the pivot point
	 * @param newScale         the new scale
	 */
	public Point computeViewporPositionWithScale(final Point viewportPosition, final Point pivot,
			final double newScale) {
		final int x = Math.max(0,
				(int) Math.round((pivot.x - MAP_INSETS) * (newScale / scale - 1) + viewportPosition.x));
		final int y = Math.max(0,
				(int) Math.round((pivot.y - MAP_INSETS) * (newScale / scale - 1) + viewportPosition.y));
		final Point corner = new Point(x, y);
		return corner;
	}

	/** Returns the affine transform */
	private AffineTransform createTransform() {
		final AffineTransform result1 = traffics.map(tr -> {
			final Rectangle2D mapBound = tr.getMapBound();
			final AffineTransform result = AffineTransform.getTranslateInstance(MAP_INSETS, MAP_INSETS);
			result.scale(scale, scale);
			result.translate(-mapBound.getMinX(), -mapBound.getMinY());
			return result;
		}).orElseGet(() -> new AffineTransform());
		return result1;
	}

	/**
	 * Returns the node at a given point.
	 *
	 * @param pt point in the map
	 */
	public Optional<MapNode> findAnyNodeAt(final Point2D pt) {
		return findSiteAt(pt).map(site -> site).or(() -> findNodeAt(pt));
	}

	/**
	 * Returns the edge at given point.
	 *
	 * @param pt the point in the map
	 */
	private Optional<MapEdge> findEdgeAt(final Point2D pt) {
		final Optional<MapEdge> result = traffics.flatMap(tr -> {
			return tr.getMap().getEdges().stream().filter(s -> {
				return isInRange(s, pt);
			}).findAny();
		});
		return result;
	}

	/**
	 * Returns the map element at given point.
	 *
	 * @param pt the point in the map
	 */
	public MapElement findElementAt(final Point2D pt) {
		final MapElement result = findSiteAt(pt).map(site -> MapElement.create(site))
				.or(() -> findNodeAt(pt).map(node -> MapElement.create(node)))
				.or(() -> findEdgeAt(pt).map(edge -> MapElement.create(edge))).orElse(MapElement.empty());
		return result;
	}

	/**
	 * Returns the node at a given point.
	 *
	 * @param pt the point in the map
	 */
	public Optional<MapNode> findNodeAt(final Point2D pt) {
		final Optional<MapNode> result = traffics.flatMap(tr -> {
			return tr.getMap().getNodes().parallelStream().filter(s -> {
				return isInRange(s, pt, RouteMap.NODE_SIZE / 2);
			}).findAny();
		});
		return result;
	}

	/**
	 * Returns the site at a given point.
	 *
	 * @param pt the point in the map
	 */
	public Optional<MapNode> findSiteAt(final Point2D pt) {
		final Optional<MapNode> result = traffics.flatMap(tr -> {
			return tr.getMap().getSites().parallelStream().filter(s -> {
				return isInRange(s, pt, RouteMap.NODE_SIZE / 2);
			}).findAny();
		});
		return result;
	}

	/** Returns the angle of module rotation. */
	public double getAngle() {
		return angle;
	}

	/** Returns the drag edge. */
	public Optional<Tuple2<Point2D, Point2D>> getDragEdge() {
		return dragEdge;
	}

	public double getGridSize() {
		// size meters to have a grid of at least 10 pixels in the screen
		final double size = MIN_GRID_SIZE_PIXELS / scale;
		// Minimum grid of size 1 m
		double gridSize = MIN_GRID_SIZE_METERS;
		while (size > gridSize) {
			gridSize *= 10;
		}
		return gridSize;
	}

	/** Returns the flowable of keyboard. */
	public Flowable<KeyEvent> getKeyboardFlow() {
		return keyboardFlow;
	}

	/** Returns the mouse mode. */
	public MouseMode getMode() {
		return mode;
	}

	/** Returns the selected module. */
	public Optional<MapModule> getModule() {
		return module;
	}

	/** Returns the flowable of mouse. */
	public Flowable<MouseEvent> getMouseFlow() {
		return mouseFlow;
	}

	/** Returns the flowable of mouse wheel. */
	public Flowable<MouseWheelEvent> getMouseWheelFlow() {
		return mouseWheelFlow;
	}

	/** Returns the pivot point for the module. */
	public Optional<Point2D> getPivot() {
		return pivot;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension result1 = traffics.map(tr -> {
			final Rectangle2D bound = tr.getMapBound();
			final int width = (int) Math.round(bound.getWidth() * scale) + MAP_INSETS * 2;
			final int height = (int) Math.round(bound.getHeight() * scale) + MAP_INSETS * 2;
			final Dimension result = new Dimension(width, height);
			return result;
		}).orElseGet(super::getPreferredSize);
		return result1;
	}

	/** Returns the scale */
	public double getScale() {
		return scale;
	}

	/** Returns the selected edge. */
	Optional<MapEdge> getSelectedEdge() {
		return selectedEdge;
	}

	/** Returns the selected node. */
	Optional<MapNode> getSelectedNode() {
		return selectedNode;
	}

	/** Returns the selected site. */
	Optional<MapNode> getSelectedSite() {
		return selectedSite;
	}

	/** Returns the traffics */
	Optional<Traffics> getTraffics() {
		return traffics;
	}

	/** Returns true if border is painted. */
	boolean isBorderPainted() {
		final boolean borderPainted = scale >= BORDER_SCALE;
		return borderPainted;
	}

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
	 * Sets the module rotation angle.
	 *
	 * @param angle the angle
	 * @return the map component
	 */
	public RouteMap setAngle(final double angle) {
		this.angle = angle;
		return this;
	}

	/**
	 * Sets drag edge.
	 *
	 * @param edge the edge
	 * @return the map component
	 */
	public RouteMap setDragEdge(final Optional<Tuple2<Point2D, Point2D>> edge) {
		this.dragEdge = edge;
		repaint();
		return this;
	}

	/**
	 * Sets the mouse mode.
	 *
	 * @param mode the mode to set
	 * @return the panel
	 */
	public RouteMap setMode(final MouseMode mode) {
		logger.debug("setMode {}", mode);
		this.mode = mode;
		return this;
	}

	/**
	 * Sets the module.
	 *
	 * @param module the module
	 * @return the map component
	 */
	public RouteMap setModule(final Optional<MapModule> module) {
		this.module = module;
		return this;
	}

	/**
	 * Sets the pivot point for the module.
	 *
	 * @param pivot the pivot point
	 * @return the map component
	 */
	public RouteMap setPivot(final Optional<Point2D> pivot) {
		this.pivot = pivot;
		return this;
	}

	/**
	 * Sets the scale
	 *
	 * @param scale the scale
	 * @return the panel
	 *
	 */
	public RouteMap setScale(final double scale) {
		this.scale = scale;
		this.transform = createTransform();
		return this;
	}

	/**
	 * Sets the selected edge.
	 *
	 * @param edge the edge
	 * @return the map component
	 */
	public RouteMap setSelectedEdge(final Optional<MapEdge> edge) {
		clearSelection();
		this.selectedEdge = edge;
		return this;
	}

	/**
	 * Sets the deleted node.
	 *
	 * @param node the selected node
	 * @return the map component
	 */
	public RouteMap setSelectedNode(final Optional<MapNode> node) {
		clearSelection();
		this.selectedNode = node;
		return this;
	}

	/**
	 * Sets the selected site.
	 *
	 * @param site the selected site
	 * @return the map component
	 */
	public RouteMap setSelectedSite(final Optional<MapNode> site) {
		clearSelection();
		this.selectedSite = site;
		return this;
	}

	/**
	 * Sets the traffics.
	 *
	 * @param traffics the traffics
	 * @return the map component
	 */
	public RouteMap setTraffics(final Traffics traffics) {
		this.traffics = Optional.ofNullable(traffics);
		this.transform = createTransform();
		return this;
	}

	/**
	 * Sets the traffics view.
	 *
	 * @param trafficView true if showing traffics congestion
	 * @return the map component
	 */
	public RouteMap setTrafficView(final boolean trafficView) {
		this.trafficView = trafficView;
		return this;
	}

	/**
	 * Returns the point snap to the nearest node.
	 *
	 * @param point the point n the map
	 */
	public Point2D snapToNode(final Point2D point) {
		final double precision = CURSOR_SELECTION_PRECISION / scale;
		final Optional<MapNode> node = traffics.flatMap(tr -> {
			return tr.getMap().findNearst(point, precision);
		});
		final Point2D result = node.map(MapNode::getLocation).orElse(point);
		return result;
	}

	/**
	 * Returns the point in the map from point in the viewport..
	 *
	 * @param point viewport point
	 */
	public Point2D toMapPoint(final Point2D point) {
		try {
			return transform.inverseTransform(point, new Point2D.Double());
		} catch (final NoninvertibleTransformException e) {
			logger.error(e.getMessage(), e);
			return point;
		}
	}

	/**
	 * Returns the point in the viewport from map.
	 *
	 * @param point point in the map
	 */
	public Point2D toScreenPoint(final Point2D point) {
		return transform.transform(point, new Point2D.Double());
	}
}
