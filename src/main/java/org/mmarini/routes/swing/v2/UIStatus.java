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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class UIStatus implements Constants {
	static enum MapMode {
		SELECTION, START_EDGE, DRAG_EDGE, DRAG_MODULE, ROTATE_MODULE
	}

	public static final int MAP_INSETS = 60;
	public static final double DEFAULT_SCALE = 1;
	private static final double MIN_GRID_SIZE_METERS = 1;
	private static final int MIN_GRID_SIZE_PIXELS = 10;
	private static final double CURSOR_SELECTION_PRECISION = 10;

	private static final Logger logger = LoggerFactory.getLogger(UIStatus.class);

	/**
	 * Returns default status
	 */
	public static UIStatus create() {
		return new UIStatus(DEFAULT_SCALE, Traffics.create(), MapMode.SELECTION, Optional.empty(), MapElement.empty(),
				DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS);
	}

	/**
	 * Returns true if location is in range of an edge
	 *
	 * @param node  edge
	 * @param point point
	 */
	private static boolean isInRange(final MapEdge edge, final Point2D point) {
		final double distance = edge.getDistance(point);
		return distance <= RouteMap.EDGE_WIDTH / 2;
	}

	/**
	 * Returns true if location is in range of site
	 *
	 * @param node  site
	 * @param point point
	 */
	private static boolean isInRange(final MapNode node, final Point2D point, final double maxDistance) {
		final double distance = node.getLocation().distance(point);
		return distance <= maxDistance;
	}

	/** The scale of route map (pixels/m) */
	private final double scale;
	private final Traffics traffics;
	private final MapMode mode;
	private final Optional<Tuple2<Point2D, Point2D>> dragEdge;
	private final MapElement selectedElement;
	private final int priority;
	private final double speedLimit;

	/**
	 * @param scale
	 * @param traffics
	 * @param mode
	 * @param dragEdge
	 * @param selectedElement
	 * @param speedLimit
	 */
	protected UIStatus(final double scale, final Traffics traffics, final MapMode mode,
			final Optional<Tuple2<Point2D, Point2D>> dragEdge, final MapElement selectedElement,
			final double speedLimit) {
		this.scale = scale;
		this.traffics = traffics;
		this.mode = mode;
		this.dragEdge = dragEdge;
		this.selectedElement = selectedElement;
		this.priority = DEFAULT_PRIORITY;
		this.speedLimit = speedLimit;
	}

	/**
	 * Returns the location for a pivot point
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

	/**
	 *
	 * @param startPoint
	 * @param endPoint
	 * @return
	 */
	public UIStatus createEdge(final Point2D startPoint, final Point2D endPoint) {
		logger.debug("createEdge {}, {}", startPoint, endPoint);
		final MapNode startNode = findAnyNodeAt(startPoint).orElseGet(() -> MapNode.create(startPoint));
		final MapNode endNode = findAnyNodeAt(endPoint).orElseGet(() -> MapNode.create(endPoint));
		final MapEdge edge = MapEdge.create(startNode, endNode).setPriority(priority).optimizedSpeedLimit(speedLimit);
		final Traffics newStatus = traffics.addEdge(edge);
		return setTraffics(newStatus);
	}

	/**
	 *
	 * @param pt
	 * @return
	 */
	public Optional<MapNode> findAnyNodeAt(final Point2D pt) {
		return findSiteAt(pt).map(site -> site).or(() -> findNodeAt(pt));
	}

	/**
	 * Returns the edge at location
	 *
	 * @param pt the location
	 */
	private Optional<MapEdge> findEdgeAt(final Point2D pt) {
		final Optional<MapEdge> result = traffics.getMap().getEdges().stream().filter(s -> {
			return isInRange(s, pt);
		}).findAny();
		return result;
	}

	/**
	 * Returns the map element at location
	 *
	 * @param pt the location
	 */
	public MapElement findElementAt(final Point2D pt) {
		final MapElement result = findSiteAt(pt).map(site -> MapElement.create(site))
				.or(() -> findNodeAt(pt).map(node -> MapElement.create(node)))
				.or(() -> findEdgeAt(pt).map(edge -> MapElement.create(edge))).orElse(MapElement.empty());
		return result;
	}

	/**
	 * Returns the node at location
	 *
	 * @param pt the location
	 */
	private Optional<MapNode> findNodeAt(final Point2D pt) {
		final Optional<MapNode> result = traffics.getMap().getNodes().parallelStream().filter(s -> {
			return isInRange(s, pt, RouteMap.NODE_SIZE / 2);
		}).findAny();
		return result;
	}

	/**
	 * Returns the node at location
	 *
	 * @param pt the location
	 */
	private Optional<MapNode> findSiteAt(final Point2D pt) {
		final Optional<MapNode> result = traffics.getMap().getSites().parallelStream().filter(s -> {
			return isInRange(s, pt, RouteMap.SITE_SIZE / 2);
		}).findAny();
		return result;
	}

	/**
	 * @return the dragEdge
	 */
	public Optional<Tuple2<Point2D, Point2D>> getDragEdge() {
		return dragEdge;
	}

	/**
	 * Returns the grid size in meters
	 */
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

	/**
	 * Returns the transformation from screen coordinates to map coordinates
	 */
	public AffineTransform getInverseTransform() {
		try {
			return getTransform().createInverse();
		} catch (final NoninvertibleTransformException e) {
			logger.error(e.getMessage(), e);
			return new AffineTransform();
		}
	}

	/**
	 * Returns the map bound
	 *
	 * @param map the map
	 */
	public Rectangle2D getMapBound() {
		final GeoMap map = traffics.getMap();
		final Set<MapNode> all = Stream.concat(map.getSites().parallelStream(), map.getNodes().parallelStream())
				.collect(Collectors.toSet());
		final OptionalDouble x0 = all.parallelStream().mapToDouble(n -> n.getX()).min();
		final OptionalDouble x1 = all.parallelStream().mapToDouble(n -> n.getX()).max();
		final OptionalDouble y0 = all.parallelStream().mapToDouble(n -> n.getY()).min();
		final OptionalDouble y1 = all.parallelStream().mapToDouble(n -> n.getY()).max();

		final Rectangle2D result = (x0.isPresent() && x1.isPresent() && y0.isPresent() && y1.isPresent())
				? new Rectangle2D.Double(x0.getAsDouble(), y0.getAsDouble(), x1.getAsDouble() - x0.getAsDouble(),
						y1.getAsDouble() - y0.getAsDouble())
				: new Rectangle2D.Double();
		return result;
	}

	/**
	 * @return the mode
	 */
	public MapMode getMode() {
		return mode;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Returns the map size
	 */
	public Dimension getScreenMapSize() {
		final Rectangle2D bound = getMapBound();
		final int width = (int) Math.round(bound.getWidth() * scale) + MAP_INSETS * 2;
		final int height = (int) Math.round(bound.getHeight() * scale) + MAP_INSETS * 2;
		final Dimension result = new Dimension(width, height);
		return result;
	}

	/**
	 * @return the selectedElement
	 */
	public MapElement getSelectedElement() {
		return selectedElement;
	}

	/**
	 * @return the speedLimit
	 */
	public double getSpeedLimit() {
		return speedLimit;
	}

	/**
	 * @return the status
	 */
	public Traffics getTraffics() {
		return traffics;
	}

	/**
	 * Returns the transformation from map coordinates to screen coordinates
	 */
	public AffineTransform getTransform() {
		return getTransform(scale);
	}

	/**
	 * Returns the transformation from map coordinates to screen coordinate
	 *
	 * @param scale the scale
	 */
	private AffineTransform getTransform(final double scale) {
		final Rectangle2D mapBound = getMapBound();
		final AffineTransform result = AffineTransform.getTranslateInstance(MAP_INSETS, MAP_INSETS);
		result.scale(scale, scale);
		result.translate(-mapBound.getMinX(), -mapBound.getMinY());
		return result;
	}

	/**
	 *
	 * @param speedLimit
	 * @return
	 */
	public UIStatus optimizeSpeed() {
		return setTraffics(traffics.optimizeSpeed(speedLimit));
	}

	/**
	 *
	 * @param minWeight
	 * @return
	 */
	public UIStatus randomize(final double minWeight) {
		return setTraffics(traffics.randomize(minWeight));
	}

	/**
	 * Returns the UIStatus with a new drage edge
	 *
	 * @param dragEdge
	 */
	public UIStatus setDragEdge(final Optional<Tuple2<Point2D, Point2D>> dragEdge) {
		return new UIStatus(scale, traffics, mode, dragEdge, selectedElement, speedLimit);
	}

	/**
	 * Returns the status with new frequence of traffics
	 *
	 * @param frequence frequence
	 */
	public UIStatus setFrequence(final double frequence) {
		final Traffics newTraffics = traffics.setFrequence(frequence);
		final UIStatus result = setTraffics(newTraffics);
		return result;
	}

	/**
	 * Returns the UIStatus with a new mode
	 *
	 * @param mode the mode
	 */
	public UIStatus setMode(final MapMode mode) {
		return new UIStatus(scale, traffics, mode, dragEdge, selectedElement, speedLimit);
	}

	/**
	 * Returns the UIStatus with a new scale
	 *
	 * @param scale the scale
	 */
	public UIStatus setScale(final double scale) {
		return new UIStatus(scale, traffics, mode, dragEdge, selectedElement, speedLimit);
	}

	/**
	 * Returns the UIStatus with a new selected element
	 *
	 * @param selectedElement the selected element
	 */
	public UIStatus setSelectedElement(final MapElement selectedElement) {
		return new UIStatus(scale, traffics, mode, dragEdge, selectedElement, speedLimit);
	}

	/**
	 *
	 * @param speedLimit
	 * @return
	 */
	public UIStatus setSpeedLimit(final double speedLimit) {
		return new UIStatus(scale, traffics, mode, dragEdge, selectedElement, speedLimit);
	}

	/**
	 * Returns the UIStatus with a new Simulation status
	 *
	 * @param traffics the status
	 */
	public UIStatus setTraffics(final Traffics traffics) {
		return new UIStatus(scale, traffics, mode, dragEdge, selectedElement, speedLimit);
	}

	/**
	 *
	 * @param weights
	 * @return
	 */
	public UIStatus setWeights(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		return setTraffics(traffics.setWeights(weights));
	}

	/**
	 *
	 * @param point
	 * @return
	 */
	public Point2D snapToNode(final Point2D point) {
		final double precision = CURSOR_SELECTION_PRECISION / scale;
		final Optional<MapNode> node = traffics.getMap().findNearst(point, precision);
		final Point2D result = node.map(MapNode::getLocation).orElse(point);
		return result;
	}

	/**
	 *
	 * @param point
	 * @return
	 */
	public Point2D toMapPoint(final Point2D point) {
		try {
			return getTransform().inverseTransform(point, new Point2D.Double());
		} catch (final NoninvertibleTransformException e) {
			logger.error(e.getMessage(), e);
			return point;
		}
	}

	/**
	 *
	 * @param point
	 * @return
	 */
	public Point2D toScreenPoint(final Point2D point) {
		return getTransform().transform(point, new Point2D.Double());
	}
}
