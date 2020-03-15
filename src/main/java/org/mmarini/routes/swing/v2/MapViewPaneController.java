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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import org.mmarini.routes.model.v2.MapModule;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.swing.v2.UIStatus.MapMode;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Controller for map view panel.
 * <p>
 * The controller manages all the user interactions from the map view panel to
 * the main controller and other components.
 * </p>
 */
public class MapViewPaneController {
	/**
	 * Returns the Point from a Point2D
	 *
	 * @param point the Point2D
	 */
	private static Point toPoint(final Point2D point) {
		return new Point((int) Math.round(point.getX()), (int) Math.round(point.getY()));
	}

	private final MapViewPane mapViewPane;
	private final ScrollMap scrollMap;
	private final RouteMap routeMap;
	private final Flowable<UIStatus> uiStatusFlow;
	private final ControllerFunctions controller;

	/**
	 * Creates the controller.
	 *
	 * @param mapViewPane  the map view panel
	 * @param scrollMap    the scroll map panel
	 * @param routeMap     the route map panel
	 * @param uiStatusFlow the flowable of ui status
	 * @param controller   the controller
	 */
	public MapViewPaneController(final MapViewPane mapViewPane, final ScrollMap scrollMap, final RouteMap routeMap,
			final Flowable<UIStatus> uiStatusFlow, final ControllerFunctions controller) {
		this.mapViewPane = mapViewPane;
		this.scrollMap = scrollMap;
		this.routeMap = routeMap;
		this.uiStatusFlow = uiStatusFlow;
		this.controller = controller;
	}

	/**
	 * Builds the subscribers
	 *
	 * @return the controller
	 */
	public MapViewPaneController build() {
		mapViewPane.getEdgeModeFlow().withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			routeMap.setModule(Optional.empty()).setDragEdge(Optional.empty())
					.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			final UIStatus newStatus = st.setMode(MapMode.START_EDGE).setDragEdge(Optional.empty());
			controller.changeStatus(newStatus);
		}, controller::showError);

		mapViewPane.getSelectModeFlow().withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			routeMap.setModule(Optional.empty()).setDragEdge(Optional.empty())
					.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			final UIStatus newStatus = st.setMode(MapMode.SELECTION).setDragEdge(Optional.empty());
			controller.changeStatus(newStatus);
		}, controller::showError);

		mapViewPane.getModuleModeFlow().withLatestFrom(uiStatusFlow, (m, st) -> {
			return Tuple.of(st, m);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapModule module = t.get2();
			final UIStatus newStatus = st.setMode(MapMode.DRAG_MODULE);
			final Optional<MapModule> moduleOpt = Optional.of(module);
			routeMap.setDragEdge(Optional.empty()).setModule(moduleOpt);
			controller.changeStatus(newStatus);
		}, controller::showError);

		mapViewPane.getZoomDefaultFlow().withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			controller.scaleTo(st, RouteMap.DEFAULT_SCALE, pivot);
		}, controller::showError);

		mapViewPane.getZoomInFlow().withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			controller.scaleTo(st, routeMap.getScale() * Controller.SCALE_FACTOR, pivot);
		}, controller::showError);

		mapViewPane.getZoomOutFlow().withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			controller.scaleTo(st, routeMap.getScale() / Controller.SCALE_FACTOR, pivot);
		}, controller::showError);

		mapViewPane.getFitInWindowFlow().withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			final Rectangle2D mapRect = st.getMapBound();
			final Dimension screenSize = scrollMap.getViewport().getExtentSize();
			final double sx = (screenSize.getWidth() - RouteMap.MAP_INSETS * 2) / mapRect.getWidth();
			final double sy = (screenSize.getHeight() - RouteMap.MAP_INSETS * 2) / mapRect.getHeight();
			final double newScale1 = Math.min(sx, sy);
			final double scaleStep = Math.floor(Math.log(newScale1) / Math.log(Controller.SCALE_FACTOR));
			final double newScale = Math.pow(Controller.SCALE_FACTOR, scaleStep);
			controller.scaleTo(st, newScale, new Point());
		}, controller::showError);

		mapViewPane.getTrafficViewFlow().subscribe(ev -> {
			routeMap.setTrafficView(true);
		}, controller::showError);

		mapViewPane.getNormalViewFlow().subscribe(ev -> {
			routeMap.setTrafficView(false);
		}, controller::showError);
		return this;
	}

}
