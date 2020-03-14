/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Tuple;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * The controller for the explorer panel.
 * <p>
 * The controller manages all the user interactions from the explorer panel to
 * the main controller and other components
 * </p>
 */
public class ExplorerPaneController implements Constants {

	private final ExplorerPane explorerPane;
	private final RouteMap routeMap;
	private final MapElementPane mapElementPane;
	private final BehaviorSubject<UIStatus> uiStatusSubj;
	private final Observable<UIStatus> uiStatusObs;
	private final ControllerFunctions controller;

	/**
	 * Creates the controller for the explorer panel
	 *
	 * @param explorerPane   the explorer panel
	 * @param routeMap       the route map
	 * @param mapElementPane the map element panel
	 * @param uiStatusSubj   the status subject
	 * @param uiStatusObs    the status observables
	 * @param controller     the main controller
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
	 * Builds the subscriptions that manage the user interactions.
	 *
	 * @return the controller
	 */
	public ExplorerPaneController build() {
		explorerPane.getSiteObs().withLatestFrom(uiStatusObs, (site, st) -> {
			return Tuple.of(st, site);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode site = t.get2();
			mapElementPane.setNode(site);
			routeMap.setSelectedSite(Optional.of(site));
			controller.centerMapTo(st, site.getLocation());
			uiStatusSubj.onNext(st);
		}, controller::showError);

		explorerPane.getNodeObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return Tuple.of(st, node);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode node = t.get2();
			mapElementPane.setNode(node);
			routeMap.setSelectedNode(Optional.of(node));
			controller.centerMapTo(st, node.getLocation());
			uiStatusSubj.onNext(st);
		}, controller::showError);

		explorerPane.getEdgeObs().withLatestFrom(uiStatusObs, (edge, st) -> {
			return Tuple.of(st, edge);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapEdge edge = t.get2();
			mapElementPane.setEdge(edge);
			routeMap.setSelectedEdge(Optional.of(edge));
			controller.centerMapTo(st, edge.getBeginLocation());
			uiStatusSubj.onNext(st);
		}, controller::showError);
		return this;
	}
}
