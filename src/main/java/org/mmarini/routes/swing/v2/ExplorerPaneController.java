/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Tuple;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Controller for the explorer panel.
 * <p>
 * The controller manages all the user interactions from the explorer panel to
 * the main controller and other components
 * </p>
 */
public class ExplorerPaneController implements Constants {

	private final ExplorerPane explorerPane;
	private final RouteMap routeMap;
	private final MapElementPane mapElementPane;
	private final Flowable<UIStatus> uiStatusFlow;
	private final ControllerFunctions controller;

	/**
	 * Creates the controller for the explorer panel.
	 *
	 * @param explorerPane   the explorer panel
	 * @param routeMap       the route map
	 * @param mapElementPane the map element panel
	 * @param uiStatusFlow   the status flowable
	 * @param controller     the main controller
	 */
	public ExplorerPaneController(final ExplorerPane explorerPane, final RouteMap routeMap,
			final MapElementPane mapElementPane, final Flowable<UIStatus> uiStatusFlow,
			final ControllerFunctions controller) {
		this.explorerPane = explorerPane;
		this.routeMap = routeMap;
		this.mapElementPane = mapElementPane;
		this.uiStatusFlow = uiStatusFlow;
		this.controller = controller;
	}

	/**
	 * Builds the subscriptions that manage the user interactions.
	 *
	 * @return the controller
	 */
	public ExplorerPaneController build() {
		explorerPane.getSiteFlow().withLatestFrom(uiStatusFlow, (site, st) -> {
			return Tuple.of(st, site);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode site = t.get2();
			mapElementPane.setNode(site);
			routeMap.setSelectedSite(Optional.of(site));
			controller.centerMapTo(st, site.getLocation()).changeStatus(st);
		}, controller::showError);

		explorerPane.getNodeFlow().withLatestFrom(uiStatusFlow, (node, st) -> {
			return Tuple.of(st, node);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode node = t.get2();
			mapElementPane.setNode(node);
			routeMap.setSelectedNode(Optional.of(node));
			controller.centerMapTo(st, node.getLocation()).changeStatus(st);
		}, controller::showError);

		explorerPane.getEdgeFlow().withLatestFrom(uiStatusFlow, (edge, st) -> {
			return Tuple.of(st, edge);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapEdge edge = t.get2();
			mapElementPane.setEdge(edge);
			routeMap.setSelectedEdge(Optional.of(edge));
			controller.centerMapTo(st, edge.getBeginLocation()).changeStatus(st);
		}, controller::showError);
		return this;
	}
}
