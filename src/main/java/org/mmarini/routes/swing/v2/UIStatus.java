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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Status of user interface of simulator.
 */
public class UIStatus implements Constants {
	/** The map modes */
	static enum MapMode {
		SELECTION, START_EDGE, DRAG_EDGE, DRAG_MODULE, ROTATE_MODULE
	}

	private static final Logger logger = LoggerFactory.getLogger(UIStatus.class);

	/** Returns default status. */
	public static UIStatus create() {
		return new UIStatus(Traffics.create(), MapMode.SELECTION, Optional.empty(),
				DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS);
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

	/** The scale of route map (pixels/m). */
	private final Traffics traffics;
	private final MapMode mode;
	private final Optional<Tuple2<Point2D, Point2D>> dragEdge;
	private final int priority;
	private final double speedLimit;

	/**
	 * Creates the status.
	 * 
	 * @param traffics   the traffics
	 * @param mode       the selection mode
	 * @param dragEdge   the drag edge ends
	 * @param speedLimit the speed limit
	 */
	protected UIStatus(final Traffics traffics, final MapMode mode, final Optional<Tuple2<Point2D, Point2D>> dragEdge,
			final double speedLimit) {
		this.traffics = traffics;
		this.mode = mode;
		this.dragEdge = dragEdge;
		this.priority = DEFAULT_PRIORITY;
		this.speedLimit = speedLimit;
	}

	/**
	 * Returns the UIStatus with a new edge.
	 *
	 * @param startPoint start point
	 * @param endPoint   end point
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
		final Optional<MapEdge> result = traffics.getMap().getEdges().stream().filter(s -> {
			return isInRange(s, pt);
		}).findAny();
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
	private Optional<MapNode> findNodeAt(final Point2D pt) {
		final Optional<MapNode> result = traffics.getMap().getNodes().parallelStream().filter(s -> {
			return isInRange(s, pt, RouteMap.NODE_SIZE / 2);
		}).findAny();
		return result;
	}

	/**
	 * Returns the site at a given point.
	 *
	 * @param pt the point in the map
	 */
	private Optional<MapNode> findSiteAt(final Point2D pt) {
		final Optional<MapNode> result = traffics.getMap().getSites().parallelStream().filter(s -> {
			return isInRange(s, pt, RouteMap.SITE_SIZE / 2);
		}).findAny();
		return result;
	}

	/** Returns the drag edge. */
	public Optional<Tuple2<Point2D, Point2D>> getDragEdge() {
		return dragEdge;
	}

	/** Returns the map bound. */
	public Rectangle2D getMapBound() {
		final Rectangle2D result = traffics.getMap().getBound();
		return result;
	}

	/** Returns the mode of mouse cursor. */
	public MapMode getMode() {
		return mode;
	}

	/** Return the default edge priority. */
	public int getPriority() {
		return priority;
	}

	/** Return the speed limit. */
	public double getSpeedLimit() {
		return speedLimit;
	}

	/** Return the traffics. */
	public Traffics getTraffics() {
		return traffics;
	}

	/** Returns the ui status with optimized traffics. */
	public UIStatus optimizeSpeed() {
		return setTraffics(traffics.optimizeSpeed(speedLimit));
	}

	/**
	 * Returns the status with randomized vehicle generation parameters.
	 *
	 * @param minWeight the minimum weights
	 * @param random    the random generator
	 */
	public UIStatus randomize(final double minWeight, final Random random) {
		return setTraffics(traffics.randomize(minWeight, random));
	}

	/**
	 * Returns the UIStatus with a new drage edge.
	 *
	 * @param dragEdge the drag edge ends
	 */
	public UIStatus setDragEdge(final Optional<Tuple2<Point2D, Point2D>> dragEdge) {
		return new UIStatus(traffics, mode, dragEdge, speedLimit);
	}

	/**
	 * Returns the status with new frequency of traffics.
	 *
	 * @param frequence frequency
	 */
	public UIStatus setFrequence(final double frequence) {
		final Traffics newTraffics = traffics.setFrequence(frequence);
		final UIStatus result = setTraffics(newTraffics);
		return result;
	}

	/**
	 * Returns the UIStatus with a new mode.
	 *
	 * @param mode the mode
	 */
	public UIStatus setMode(final MapMode mode) {
		return new UIStatus(traffics, mode, dragEdge, speedLimit);
	}

	/**
	 * Returns the UIStatus with a speed limit.
	 *
	 * @param speedLimit the speed limit in meters/second
	 */
	public UIStatus setSpeedLimit(final double speedLimit) {
		return new UIStatus(traffics, mode, dragEdge, speedLimit);
	}

	/**
	 * Returns the UIStatus with a new traffics.
	 *
	 * @param traffics the traffics
	 */
	public UIStatus setTraffics(final Traffics traffics) {
		return new UIStatus(traffics, mode, dragEdge, speedLimit);
	}

	/**
	 * Returns the UIStatus with a new vehicle generation weights.
	 *
	 * @param weights the weights
	 */
	public UIStatus setWeights(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		return setTraffics(traffics.setWeights(weights));
	}
}
