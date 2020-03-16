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

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Optional;
import java.util.Set;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapModule;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.model.v2.Tuple2;
import org.mmarini.routes.swing.v2.RouteMap.MouseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the mouse.
 * <p>
 * The controller manages all the user interactions from mouse to the main
 * controller and other components
 * </p>
 */
public class MouseController implements Constants {
	private static final Logger logger = LoggerFactory.getLogger(MouseController.class);

	private final ScrollMap scrollMap;
	private final RouteMap routeMap;
	private final MapElementPane mapElementPane;
	private final ExplorerPane explorerPane;
	private final ControllerFunctions controller;

	/**
	 * Create the mouse controller.
	 *
	 * @param scrollMap      the scroll map
	 * @param routeMap       the route map
	 * @param mapElementPane the map element panel
	 * @param explorerPane   the explorer panel
	 * @param controller     the main controller
	 */
	public MouseController(final ScrollMap scrollMap, final RouteMap routeMap, final MapElementPane mapElementPane,
			final ExplorerPane explorerPane, final ControllerFunctions controller) {
		this.scrollMap = scrollMap;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.controller = controller;
		this.mapElementPane = mapElementPane;
	}

	/**
	 * Builds the subscription to mouse events.
	 *
	 * @return the controller
	 */
	public MouseController build() {
		routeMap.getMouseFlow().subscribe(this::handleMouseEvent, controller::showError);
		return this;
	}

	/**
	 * Handles drag edge.
	 * 
	 * @param point the cursor map point
	 * @param event the mouse event
	 * @return the controller
	 */
	private MouseController handleDragEdge(final Point2D point, final MouseEvent event) {
		routeMap.getDragEdge().ifPresentOrElse(ed -> {
			final Point2D endPoint = routeMap.snapToNode(point);
			final Point2D startPoint = ed.get1();
			if (event.getID() == MouseEvent.MOUSE_PRESSED) {
				logger.debug("bindOnDragEdge: endPoint {}", endPoint);
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(Tuple.of(endPoint, endPoint));
					routeMap.setDragEdge(dragEdge);
					final MapNode startNode = routeMap.findAnyNodeAt(startPoint)
							.orElseGet(() -> MapNode.create(startPoint));
					final MapNode endNode = routeMap.findAnyNodeAt(endPoint).orElseGet(() -> MapNode.create(endPoint));
					final MapEdge edge = MapEdge.create(startNode, endNode).setPriority(DEFAULT_PRIORITY)
							.optimizedSpeedLimit(DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS);
					controller.mapChanged(traffics.addEdge(edge));
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			} else {
				routeMap.setDragEdge(Optional.of(Tuple.of(startPoint, endPoint)));
			}
		}, () -> {
			logger.error("Missing drage edge", new Error());
		});
		return this;
	}

	/**
	 * Handles drag module.
	 * 
	 * @param point the cursor map point
	 * @param event the mouse event
	 * @return the controller
	 */
	private MouseController handleDragM(final Point2D point, final MouseEvent event) {
		routeMap.setPivot(Optional.of(point));
		if (event.getID() == MouseEvent.MOUSE_PRESSED) {
			logger.debug("bindOnDragModule drop pivot={}", point);
			routeMap.setAngle(0.0).setMode(MouseMode.ROTATE_MODULE);
		}
		scrollMap.repaint();
		routeMap.requestFocus();
		return this;
	}

	/**
	 * Handles the mouse event.
	 * 
	 * @param event the mouse event
	 * @return the controller
	 */
	private MouseController handleMouseEvent(final MouseEvent event) {
		final Point2D point = routeMap.toMapPoint(event.getPoint());
		controller.updateHud(point);

		switch (routeMap.getMode()) {
		case SELECTION:
			return handleSelection(point, event);
		case START_EDGE:
			return handleStartEdge(point, event);
		case DRAG_EDGE:
			return handleDragEdge(point, event);
		case DRAG_MODULE:
			return handleDragM(point, event);
		case ROTATE_MODULE:
			return handleRotateModule(point, event);
		default:
			logger.error("Unrecognized mouse mode " + routeMap.getMode(), new Error());
			return this;
		}
	}

	/**
	 * Handles rotate module.
	 * 
	 * @param point the cursor map point
	 * @param event the mouse event
	 * @return the controller
	 */
	private MouseController handleRotateModule(final Point2D point, final MouseEvent event) {
		routeMap.getPivot().ifPresentOrElse(pivot -> {
			final double angle = Math.atan2(point.getY() - pivot.getY(), point.getX() - pivot.getX());
			if (event.getID() == MouseEvent.MOUSE_PRESSED) {
				logger.debug("bindOnRotateModule drop point {} angle={}", point, angle);
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final Optional<MapModule> module = routeMap.getModule().map(m -> {
						final AffineTransform tr = AffineTransform.getTranslateInstance(pivot.getX(), pivot.getY());
						tr.rotate(angle);
						return m.transform(tr);
					});
					final Traffics newTraffics = traffics.addEdges(module.map(MapModule::getEdges).orElse(Set.of()));
					routeMap.setPivot(Optional.of(point)).setAngle(0.0).setMode(MouseMode.DRAG_MODULE);
					controller.mapChanged(newTraffics);
					routeMap.requestFocus();
				}, () -> {
					logger.error("Error missing traffics", new Error());
				});
			} else {
				routeMap.setAngle(angle);
				scrollMap.repaint();
			}
		}, () -> {
			logger.error("Error missing pivot", new Error());
		});
		return null;
	}

	/**
	 * Handles the mouse event when in selection mode.
	 * 
	 * @param point the cursor map point
	 * @param event the mouse event
	 * @return the controller
	 */
	private MouseController handleSelection(final Point2D point, final MouseEvent event) {
		if (event.getID() == MouseEvent.MOUSE_PRESSED) {
			logger.debug("bindOnMouseSelection: select at {}", point);
			final MapElement elem = routeMap.findElementAt(point);
			elem.getNode().ifPresent(explorerPane::setSelectedNode);
			elem.getEdge().ifPresent(explorerPane::setSelectedEdge);
			if (elem.isEmpty()) {
				routeMap.clearSelection();
				explorerPane.clearSelection();
				controller.centerMapTo(point);
				mapElementPane.clearSelection();
			}
			routeMap.requestFocus();
		}
		return this;
	}

	/**
	 * Handles start edge.
	 * 
	 * @param point the cursor map point
	 * @param event the mouse event
	 * @return the controller
	 */
	private MouseController handleStartEdge(final Point2D point, final MouseEvent event) {
		if (event.getID() == MouseEvent.MOUSE_PRESSED) {
			final Point2D startPoint = routeMap.snapToNode(point);
			logger.debug("bindOnStartEdge: point at {}", startPoint);
			final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(Tuple.of(startPoint, startPoint));
			routeMap.setSelectedEdge(Optional.empty()).setSelectedNode(Optional.empty())
					.setSelectedSite(Optional.empty()).setDragEdge(dragEdge).setMode(MouseMode.DRAG_EDGE);
			scrollMap.repaint();
			routeMap.requestFocus();
		}
		return this;
	}
}
