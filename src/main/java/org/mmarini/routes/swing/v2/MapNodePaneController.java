/**
 *
 */
package org.mmarini.routes.swing.v2;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;

/**
 * Controller for map node panel.
 * <p>
 * The controller manages all the user interactions from the map node panel to
 * the main controller and other components.
 * </p>
 */
public class MapNodePaneController implements Constants {

	private static final Logger logger = LoggerFactory.getLogger(MapNodePaneController.class);

	private final MapNodePane nodePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final ControllerFunctions controller;
	private final Observable<UIStatus> uiStatusObs;

	/**
	 * Creates the controller.
	 *
	 * @param nodePane     the node panel
	 * @param routeMap     the route panel
	 * @param explorerPane the explorer panel
	 * @param uiStatusObs  the observable of UIStatus
	 * @param controller   the main controller
	 */
	public MapNodePaneController(final MapNodePane nodePane, final RouteMap routeMap, final ExplorerPane explorerPane,
			final Observable<UIStatus> uiStatusObs, final ControllerFunctions controller) {
		this.nodePane = nodePane;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.controller = controller;
		this.uiStatusObs = uiStatusObs;
	}

	/**
	 * Builds the subscribers.
	 *
	 * @return the controller
	 */
	public MapNodePaneController build() {
		// Change node type
		nodePane.getChangeObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return Tuple.of(st, node);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MapNode node = t.get2();
			logger.debug("changeNode {} ", node); //$NON-NLS-1$
			final Traffics nextSt = st.getTraffics().changeNode(node, (a, b) -> 1);
			final UIStatus nextUiStatus = st.setTraffics(nextSt);
			final GeoMap map = nextUiStatus.getTraffics().getMap();
			routeMap.setSelectedSite(map.getSite(node));
			routeMap.setSelectedNode(map.getNode(node));
			explorerPane.setSelectedNode(node);
			controller.mapChanged(nextUiStatus);
		}, controller::showError);

		// delete node type
		nodePane.getDeleteObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return Tuple.of(st, node);
		}).subscribe(t -> {
			final UIStatus nextStatus = controller.deleteNode(t.get1(), t.get2());
			explorerPane.clearSelection();
			routeMap.clearSelection();
			controller.mapChanged(nextStatus);
		}, controller::showError);
		return this;
	}
}
