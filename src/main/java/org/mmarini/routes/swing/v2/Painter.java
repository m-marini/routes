/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mmarini.routes.model.v2.EdgeTraffic;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.SimulationStatus;
import org.mmarini.routes.model.v2.SiteNode;
import org.mmarini.routes.model.v2.Tuple2;

/**
 * @author mmarini
 *
 */
public class Painter {
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

	private static final Color END_NODE_COLOR = Color.RED;
	private static final Color BEGIN_NODE_COLOR = Color.GREEN;
	private static final Color DEFAULT_SITE_COLOR = Color.GRAY;
	private static final Color DEFAULT_VEHICLE_COLOR = Color.GRAY;
	private static final Color SELECTED_SITE_COLOR = Color.WHITE;
	private static final Color EDGE_COLOR = Color.LIGHT_GRAY;
	private static final Color MAJOR_GRID_COLOR = new Color(0xc0c0c0);
	private static final Color MINOR_GRID_COLOR = new Color(0xe0e0e0);
	private static final Color MAJOR_GRID_REVERSED_COLOR = new Color(0x202020);
	private static final Color MINOR_GRID_REVERSED_COLOR = new Color(0x101010);
	private static final Color SELECTED_NODE_COLOR = Color.RED;
	private static final Color SELECTED_EDGE_COLOR = Color.YELLOW;
	private static final double VEICLE_LENGTH = 5;
	private static final double VEICLE_WIDTH = 3;
	private static final double EDGE_WIDTH = 5;
	private static final double NODE_SIZE = 10;
	private static final double NODE_SATURATION = 1;
	private static final BasicStroke STROKE = new BasicStroke((float) EDGE_WIDTH, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	private static final BasicStroke THIN_STROKE = new BasicStroke(0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Rectangle2D VEHICLE_SHAPE = new Rectangle2D.Double(-VEICLE_LENGTH, -VEICLE_WIDTH / 2,
			VEICLE_LENGTH, VEICLE_WIDTH);

	/**
	 * Returns the comparison between two nodes
	 *
	 * @param a first node
	 * @param b second node
	 */
	private static int compareNodes(final MapNode a, final MapNode b) {
		return a.getId().compareTo(b.getId());
	}

	/**
	 *
	 * @param gr
	 * @return
	 */
	public static Painter create(final Graphics2D gr) {
		return new Painter(gr, false, 1, Optional.empty(), new Rectangle2D.Double(), Collections.emptyMap());
	}

	private final Graphics2D graphics;
	private final Optional<SimulationStatus> status;
	private final Rectangle2D bound;
	private final boolean reversed;
	private final double gridSize;
	private final Map<SiteNode, Color> colorMap;

	private boolean borderPainted;

	/**
	 * @param graphics
	 * @param reversed
	 * @param gridSize
	 * @param status
	 * @param bound
	 * @param colorMap
	 */
	public Painter(final Graphics2D graphics, final boolean reversed, final double gridSize,
			final Optional<SimulationStatus> status, final Rectangle2D bound, final Map<SiteNode, Color> colorMap) {
		super();
		this.reversed = reversed;
		this.gridSize = gridSize;
		this.graphics = graphics;
		this.status = status;
		this.bound = bound;
		this.colorMap = colorMap;
	}

	/**
	 * Returns the painter with map of colors of sites
	 */
	private Painter computeSiteColorMap() {
		final Map<SiteNode, Color> map = status.map(s -> {
			final List<SiteNode> sites = s.getMap().getSites().stream().sorted(Painter::compareNodes)
					.collect(Collectors.toList());
			final int n = sites.size();
			if (n > 1) {
				final Map<SiteNode, Color> map1 = IntStream.range(0, n).mapToObj(i -> {
					final double value = (double) i / (n - 1);
					final Color color = SwingUtils.computeColor(value, NODE_SATURATION);
					return new Tuple2<SiteNode, Color>(sites.get(i), color);
				}).collect(Collectors.toMap(Tuple2::getElem1, Tuple2::getElem2));
				return map1;
			} else {
				return Map.of(sites.get(0), SwingUtils.computeColor(0, NODE_SATURATION));
			}
		}).orElseGet(() -> Collections.emptyMap());
		return new Painter(graphics, reversed, gridSize, status, bound, map);
	}

	/**
	 * Returns the painter with painted canvas
	 */
	public Painter paint() {
		return computeSiteColorMap().paintGrid().paintEdges().paintSites().paintVehicles();
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
			graphics.setColor(EDGE_COLOR);
			s.getMap().getEdges().forEach(this::paintEdge);
		});
		return this;
	}

	/**
	 * Returns the painter with painted grid
	 */
	private Painter paintGrid() {
		final Color minorColor = reversed ? MINOR_GRID_REVERSED_COLOR : MINOR_GRID_COLOR;
		final Color majorColor = reversed ? MAJOR_GRID_REVERSED_COLOR : MAJOR_GRID_COLOR;
		graphics.setColor(minorColor);
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
			graphics.setColor(i % 10 == 0 ? majorColor : minorColor);
			final double x = i * gridSize;
			graphics.draw(new Line2D.Double(x, y0, x, y1));

		}
		for (long j = j0; j <= j1; ++j) {
			graphics.setColor(j % 10 == 0 ? majorColor : minorColor);
			final double y = j * gridSize;
			graphics.draw(new Line2D.Double(x0, y, x1, y));

		}
		return this;
	}

	/**
	 * Returns the painter with a painted node
	 *
	 * @param node the node
	 */
	private Painter paintNode(final MapNode node) {
		graphics.fill(new Ellipse2D.Double(-NODE_SIZE * 0.5 + node.getX(), -NODE_SIZE * 0.5 + node.getY(), NODE_SIZE,
				NODE_SIZE));
		return this;
	}

	/**
	 * Returns the painter with painted sites
	 */
	private Painter paintSites() {
		status.ifPresent(st -> {
			st.getMap().getSites().forEach(site -> {
				graphics.setColor(colorMap.getOrDefault(site, DEFAULT_SITE_COLOR));
				paintNode(site);
			});
		});
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
		final AffineTransform translateTr = AffineTransform.getTranslateInstance(v.location.getX(), v.location.getY());
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
		if (!reversed) {
			status.stream().<EdgeTraffic>flatMap(st -> st.getTraffic().stream())
					.<VehicleInfo>flatMap(te -> te.getVehicles().stream().map(v -> {
						final Point2D location = te.getEdge().getLocation(v.getLocation());
						final Point2D direction = te.getEdge().getDirection();
						final Color color = colorMap.getOrDefault(v.getTarget(), DEFAULT_VEHICLE_COLOR);
						return new VehicleInfo(location, direction, color);
					})).forEach(this::paintVehicle);
		}
		return this;

	}

	/**
	 * Returns the painter with set border painted
	 *
	 * @param borderPainted true if border painted
	 */
	public Painter setBorderPainted(final boolean borderPainted) {
		this.borderPainted = borderPainted;
		return this;
	}

	/**
	 * Returns the painter for a given bound
	 *
	 * @param bound the bound
	 */
	public Painter setBound(final Rectangle2D bound) {
		return new Painter(graphics, reversed, gridSize, status, bound, colorMap);
	}

	/**
	 * Returns the painter for a given grid size
	 *
	 * @param gridSize the gris size
	 */
	public Painter setGridSize(final double gridSize) {
		return new Painter(graphics, reversed, gridSize, status, bound, colorMap);
	}

	/**
	 * Returns the painter for a status
	 *
	 * @param status
	 */
	public Painter setStatus(final Optional<SimulationStatus> status) {
		return new Painter(graphics, reversed, gridSize, status, bound, colorMap);
	}

	/**
	 *
	 * @param transform
	 * @return
	 */
	public Painter setTransform(final AffineTransform transform) {
		graphics.transform(transform);
		return this;
	}
}
