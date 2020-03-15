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
import org.mmarini.routes.model.v2.MapModule;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.model.v2.Tuple2;
import org.mmarini.routes.model.v2.Tuple3;
import org.mmarini.routes.swing.v2.UIStatus.MapMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;

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
	private final Flowable<UIStatus> uiStatusFlow;
	private final ControllerFunctions controller;

	/**
	 * Create the mouse controller.
	 *
	 * @param scrollMap      the scroll map
	 * @param routeMap       the route map
	 * @param mapElementPane the map element panel
	 * @param explorerPane   the explorer panel
	 * @param uiStatusFlow   the flowable of ui status
	 * @param controller     the main controller
	 */
	public MouseController(final ScrollMap scrollMap, final RouteMap routeMap, final MapElementPane mapElementPane,
			final ExplorerPane explorerPane, final Flowable<UIStatus> uiStatusFlow,
			final ControllerFunctions controller) {
		this.scrollMap = scrollMap;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.uiStatusFlow = uiStatusFlow;
		this.controller = controller;
		this.mapElementPane = mapElementPane;
	}

	/**
	 * Binds for edge dragging.
	 *
	 * @param withPointFlow the flowable of cursor map point
	 * @return the controller
	 */
	private MouseController bindOnDragEdge(final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow) {
		withPointFlow.filter(t -> {
			final UIStatus st = t.get1();
			return st.getMode().equals(MapMode.DRAG_EDGE);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MouseEvent ev = t.get3();
			final Point2D endPoint = routeMap.snapToNode(t.get2());
			final Point2D startPoint = st.getDragEdge().get().get1();
			switch (ev.getID()) {
			case MouseEvent.MOUSE_ENTERED:
			case MouseEvent.MOUSE_EXITED:
			case MouseEvent.MOUSE_DRAGGED:
			case MouseEvent.MOUSE_MOVED: {
				final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(Tuple.of(startPoint, endPoint));
				routeMap.setDragEdge(dragEdge);
				controller.changeStatus(st.setDragEdge(dragEdge));
			}
				break;
			case MouseEvent.MOUSE_PRESSED:
				logger.debug("bindOnDragEdge addEdge status {} {}", st, st.getTraffics().getTraffics().size()); //$NON-NLS-1$
				logger.debug("bindOnDragEdge addEdge {}", st); //$NON-NLS-1$
				final UIStatus statusWithEdge = st.createEdge(startPoint, endPoint);
				final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(Tuple.of(endPoint, endPoint));
				final UIStatus newStatus = statusWithEdge.setDragEdge(dragEdge);
				logger.debug("bindOnDragEdge addEdge new status {} {}", newStatus, //$NON-NLS-1$
						newStatus.getTraffics().getTraffics().size());
				controller.mapChanged(newStatus);
				break;
			}
		}, controller::showError);
		return this;
	}

	/**
	 * Binds for module dragging.
	 *
	 * @param withPointFlow the flowable of cursor map point
	 * @return the controller
	 */
	private MouseController bindOnDragModule(final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow) {
		withPointFlow.filter(t -> {
			final UIStatus st = t.get1();
			return st.getMode().equals(MapMode.DRAG_MODULE);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final Point2D pivot = t.get2();
			final MouseEvent ev = t.get3();
			switch (ev.getID()) {
			case MouseEvent.MOUSE_ENTERED:
			case MouseEvent.MOUSE_EXITED:
			case MouseEvent.MOUSE_DRAGGED:
			case MouseEvent.MOUSE_MOVED:
				routeMap.setPivot(Optional.of(pivot));
				scrollMap.repaint();
				break;
			case MouseEvent.MOUSE_PRESSED:
				routeMap.setPivot(Optional.of(pivot)).setAngle(0.0);
				scrollMap.repaint();
				logger.debug("bindOnRotateModule drop pivot={}", pivot);
				controller.changeStatus(st.setMode(MapMode.ROTATE_MODULE));
			}
			routeMap.requestFocus();
		}, controller::showError);
		return this;
	}

	/**
	 * Binds for mouse to head up display.
	 *
	 * @param withPointFlow the flowable of cursor map point
	 * @return the controller
	 */
	private MouseController bindOnMouseForHud(final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow) {
		withPointFlow.subscribe(t -> {
			controller.updateHud(t.get1(), t.get2());
		}, controller::showError);
		return this;
	}

	/**
	 * Binds for mouse selection.
	 *
	 * @param withPointFlow the flowable of cursor map point
	 * @return the controller
	 */
	private MouseController bindOnMouseSelection(final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow) {
		withPointFlow.filter(t -> {
			final UIStatus st = t.get1();
			final MouseEvent ev = t.get3();
			return st.getMode().equals(MapMode.SELECTION) && ev.getID() == MouseEvent.MOUSE_PRESSED;
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final Point2D point = t.get2();
			final MapElement elem = st.findElementAt(point);
			elem.getNode().ifPresent(explorerPane::setSelectedNode);
			elem.getEdge().ifPresent(explorerPane::setSelectedEdge);
			if (elem.isEmpty()) {
				routeMap.clearSelection();
				explorerPane.clearSelection();
				controller.centerMapTo(st, t.get2());
				mapElementPane.clearSelection();
			}
			routeMap.requestFocus();
		}, controller::showError);
		return this;
	}

	/**
	 * Binds for module rotation.
	 *
	 * @param withPointFlow the flowable of cursor map point
	 * @return the controller
	 */
	private MouseController bindOnRotateModule(final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow) {
		withPointFlow.filter(t -> {
			final UIStatus st = t.get1();
			return st.getMode().equals(MapMode.ROTATE_MODULE);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MouseEvent ev = t.get3();
			final Point2D pt = t.get2();
			final Point2D pivot = routeMap.getPivot().get();
			final double angle = Math.atan2(pt.getY() - pivot.getY(), pt.getX() - pivot.getX());
			switch (ev.getID()) {
			case MouseEvent.MOUSE_ENTERED:
			case MouseEvent.MOUSE_EXITED:
			case MouseEvent.MOUSE_DRAGGED:
			case MouseEvent.MOUSE_MOVED:
				routeMap.setAngle(angle);
				scrollMap.repaint();
				break;
			case MouseEvent.MOUSE_PRESSED:
				final Optional<MapModule> module = routeMap.getModule().map(m -> {
					final AffineTransform tr = AffineTransform.getTranslateInstance(pivot.getX(), pivot.getY());
					tr.rotate(angle);
					return m.transform(tr);
				});
				final Traffics newTraffics = st.getTraffics()
						.addEdges(module.map(MapModule::getEdges).orElse(Set.of()));
				logger.debug("bindOnRotateModule drop angle={}", angle);
				final UIStatus newStatus = st.setMode(MapMode.DRAG_MODULE).setTraffics(newTraffics);
				routeMap.setPivot(Optional.of(pt)).setAngle(0.0);
				controller.mapChanged(newStatus);
			}
			routeMap.requestFocus();
		}, controller::showError);
		return this;
	}

	/**
	 * Binds for edge start.
	 *
	 * @param withPointFlow the flowable of cursor map point
	 * @return the controller
	 */
	private MouseController bindOnStartEdge(final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow) {
		withPointFlow.filter(t -> {
			final UIStatus st = t.get1();
			final MouseEvent ev = t.get3();
			return st.getMode().equals(MapMode.START_EDGE) && ev.getID() == MouseEvent.MOUSE_PRESSED;
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final Point2D startPoint = routeMap.snapToNode(t.get2());
			final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(Tuple.of(startPoint, startPoint));

			routeMap.setSelectedEdge(Optional.empty()).setSelectedNode(Optional.empty())
					.setSelectedSite(Optional.empty()).setDragEdge(dragEdge);
			scrollMap.repaint();
			controller.changeStatus(st.setDragEdge(dragEdge).setMode(MapMode.DRAG_EDGE));
			routeMap.requestFocus();
		}, controller::showError);
		return this;
	}

	/**
	 * Builds the subscription to mouse events.
	 *
	 * @return the controller
	 */
	public MouseController build() {
//		final MulticastProcessor<Tuple3<UIStatus, Point2D, MouseEvent>> withPointProc = MulticastProcessor.create();
//		withPointProc.start();
		final PublishProcessor<Tuple3<UIStatus, Point2D, MouseEvent>> withPointProc = PublishProcessor.create();
		routeMap.getMouseFlow().withLatestFrom(uiStatusFlow, (ev, status) -> {
			final Point2D pt = routeMap.toMapPoint(ev.getPoint());
			return Tuple.of(status, pt, ev);
		}).subscribe(withPointProc);
		final Flowable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointFlow = withPointProc;
		return bindOnMouseForHud(withPointFlow).bindOnMouseSelection(withPointFlow).bindOnStartEdge(withPointFlow)
				.bindOnRotateModule(withPointFlow).bindOnDragModule(withPointFlow).bindOnDragEdge(withPointFlow);
	}
}
