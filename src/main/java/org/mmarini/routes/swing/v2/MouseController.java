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
import org.mmarini.routes.model.v2.Tuple2;
import org.mmarini.routes.model.v2.Tuple3;
import org.mmarini.routes.swing.v2.UIStatus.MapMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 *
 */
public class MouseController implements Constants {
	private static final Logger logger = LoggerFactory.getLogger(MouseController.class);

	private final ScrollMap scrollMap;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final BehaviorSubject<UIStatus> uiStatusSubj;
	private final Observable<UIStatus> uiStatusObs;
	private final ControllerFunctions controller;

	/**
	 * @param scrollMap
	 * @param routeMap
	 * @param explorerPane
	 * @param uiStatusSubj
	 * @param uiStatusObs
	 * @param controller
	 */
	public MouseController(final ScrollMap scrollMap, final RouteMap routeMap, final ExplorerPane explorerPane,
			final BehaviorSubject<UIStatus> uiStatusSubj, final Observable<UIStatus> uiStatusObs,
			final ControllerFunctions controller) {
		this.scrollMap = scrollMap;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.uiStatusSubj = uiStatusSubj;
		this.uiStatusObs = uiStatusObs;
		this.controller = controller;
	}

	/**
	 *
	 * @param withPointObs
	 * @return
	 */
	private MouseController bindOnDragEdge(final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs) {
		withPointObs.filter(t -> {
			final UIStatus st = t.get1();
			return st.getMode().equals(MapMode.DRAG_EDGE);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MouseEvent ev = t.get3();
			final Point2D endPoint = st.snapToNode(t.get2());
			final Point2D startPoint = st.getDragEdge().get().get1();
			switch (ev.getID()) {
			case MouseEvent.MOUSE_ENTERED:
			case MouseEvent.MOUSE_EXITED:
			case MouseEvent.MOUSE_DRAGGED:
			case MouseEvent.MOUSE_MOVED: {
				final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(startPoint, endPoint));
				routeMap.setDragEdge(dragEdge);
				uiStatusSubj.onNext(st.setDragEdge(dragEdge));
			}
				break;
			case MouseEvent.MOUSE_PRESSED:
				logger.debug("bindOnDragEdge addEdge status {} {}", st, st.getTraffics().getTraffics().size()); //$NON-NLS-1$
				controller.withStopSimulator(tr -> {
					logger.debug("bindOnDragEdge addEdge {}", st); //$NON-NLS-1$
					final UIStatus statusWithEdge = st.createEdge(startPoint, endPoint);
					final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(endPoint, endPoint));
					final UIStatus newStatus = statusWithEdge.setDragEdge(dragEdge);
					logger.debug("bindOnDragEdge addEdge new status {} {}", newStatus, //$NON-NLS-1$
							newStatus.getTraffics().getTraffics().size());
					controller.mapChanged(newStatus);
					return newStatus;
				});
				break;
			}
		}, controller::showError);
		return this;
	}

	/**
	 *
	 * @param withPointObs
	 * @return
	 */
	private MouseController bindOnDragModule(final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs) {
		withPointObs.filter(t -> {
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
				uiStatusSubj.onNext(st.setMode(MapMode.ROTATE_MODULE));
			}
		}, controller::showError);
		return this;
	}

	/**
	 * @param withPointObs
	 */
	private MouseController bindOnMouseForHud(final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs) {
		withPointObs.subscribe(t -> {
			controller.updateHud(t.get1(), t.get2());
		}, controller::showError);
		return this;
	}

	/**
	 * @param withPointObs
	 * @return
	 */
	private MouseController bindOnMouseSelection(final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs) {
		withPointObs.filter(t -> {
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
				explorerPane.clearSelection();
				controller.centerMapTo(st, t.get2());
			}
			if (!st.getSelectedElement().equals(elem)) {
				uiStatusSubj.onNext(st.setSelectedElement(elem));
			}
		}, controller::showError);
		return this;
	}

	/**
	 *
	 * @param withPointObs
	 * @return
	 */
	private MouseController bindOnRotateModule(final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs) {
		withPointObs.filter(t -> {
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
				controller.withStopSimulator(tr1 -> {
					final Optional<MapModule> module = routeMap.getModule().map(m -> {
						final AffineTransform tr = AffineTransform.getTranslateInstance(pivot.getX(), pivot.getY());
						tr.rotate(angle);
						return m.transform(tr);
					});
					final Traffics newTraffics = st.getTraffics()
							.addEdges(module.map(MapModule::getEdges).orElse(Set.of()));
					final UIStatus newStatus = st.setMode(MapMode.DRAG_MODULE).setTraffics(newTraffics);
					routeMap.setPivot(Optional.of(pt)).setAngle(0.0);
					controller.mapChanged(newStatus);
					return newStatus;
				});
			}
		}, controller::showError);
		return this;
	}

	/**
	 *
	 * @param withPointObs
	 * @return
	 */
	private MouseController bindOnStartEdge(final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs) {
		withPointObs.filter(t -> {
			final UIStatus st = t.get1();
			final MouseEvent ev = t.get3();
			return st.getMode().equals(MapMode.START_EDGE) && ev.getID() == MouseEvent.MOUSE_PRESSED;
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final Point2D startPoint = st.snapToNode(t.get2());
			final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(startPoint, startPoint));

			routeMap.setSelectedEdge(Optional.empty()).setSelectedNode(Optional.empty())
					.setSelectedSite(Optional.empty()).setDragEdge(dragEdge);
			scrollMap.repaint();
			uiStatusSubj.onNext(st.setDragEdge(dragEdge).setMode(MapMode.DRAG_EDGE));
		}, controller::showError);
		return this;
	}

	/**
	 *
	 * @return
	 */
	public MouseController build() {
		final PublishSubject<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs1 = PublishSubject.create();
		routeMap.getMouseObs().withLatestFrom(uiStatusObs, (ev, status) -> {
			final Point2D pt = status.toMapPoint(ev.getPoint());
			return new Tuple3<>(status, pt, ev);
		}).subscribe(withPointObs1);
		final Observable<Tuple3<UIStatus, Point2D, MouseEvent>> withPointObs = withPointObs1;
		return bindOnMouseForHud(withPointObs).bindOnMouseSelection(withPointObs).bindOnStartEdge(withPointObs)
				.bindOnRotateModule(withPointObs).bindOnDragModule(withPointObs).bindOnDragEdge(withPointObs);
	}
}
