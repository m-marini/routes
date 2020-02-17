/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Tuple2;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @author us00852
 *
 */
public class ExplorerPaneController implements Constants {

	private final ExplorerPane explorerPane;
	private final RouteMap routeMap;
	private final MapElementPane mapElementPane;
	private final BehaviorSubject<UIStatus> uiStatusSubj;
	private final Observable<UIStatus> uiStatusObs;
	private final ControllerFunctions controller;

	/**
	 * @param explorerPane
	 * @param routeMap
	 * @param mapElementPane
	 * @param uiStatusSubj
	 * @param uiStatusObs
	 * @param controller
	 */
	public ExplorerPaneController(final ExplorerPane explorerPane, final RouteMap routeMap,
			final MapElementPane mapElementPane, final BehaviorSubject<UIStatus> uiStatusSubj,
			final Observable<UIStatus> uiStatusObs, final ControllerFunctions controller) {
		this.explorerPane = explorerPane;
		this.routeMap = routeMap;
		this.mapElementPane = mapElementPane;
		this.uiStatusSubj = uiStatusSubj;
		this.uiStatusObs = uiStatusObs;
		this.controller = controller;
	}

	/**
	 *
	 * @return
	 */
	public ExplorerPaneController build() {
		explorerPane.getSiteObs().withLatestFrom(uiStatusObs, (site, st) -> {
			return new Tuple2<>(st, site);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode site = t.get2();
			final UIStatus newStatus = st.setSelectedElement(MapElement.create(site));
			mapElementPane.setNode(site);
			routeMap.setSelectedSite(Optional.of(site));
			controller.centerMapTo(newStatus, site.getLocation());
			uiStatusSubj.onNext(newStatus);
		}, controller::showError);

		explorerPane.getNodeObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return new Tuple2<>(st, node);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode node = t.get2();
			final UIStatus newStatus = st.setSelectedElement(MapElement.create(node));
			mapElementPane.setNode(node);
			routeMap.setSelectedNode(Optional.of(node));
			controller.centerMapTo(newStatus, node.getLocation());
			uiStatusSubj.onNext(newStatus);
		}, controller::showError);

		explorerPane.getEdgeObs().withLatestFrom(uiStatusObs, (edge, st) -> {
			return new Tuple2<>(st, edge);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapEdge edge = t.get2();
			final UIStatus newStatus = st.setSelectedElement(MapElement.create(edge));
			mapElementPane.setEdge(edge);
			routeMap.setSelectedEdge(Optional.of(edge));
			controller.centerMapTo(newStatus, edge.getBeginLocation());
			uiStatusSubj.onNext(newStatus);
		}, controller::showError);
		return this;
	}
}
