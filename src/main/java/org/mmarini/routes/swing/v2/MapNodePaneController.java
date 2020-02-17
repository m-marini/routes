/**
 *
 */
package org.mmarini.routes.swing.v2;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;

/**
 * @author us00852
 *
 */
public class MapNodePaneController implements Constants {

	private static final Logger logger = LoggerFactory.getLogger(MapNodePaneController.class);

	private final MapNodePane nodePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final ControllerFunctions controller;
	private final Observable<UIStatus> uiStatusObs;

	/**
	 * @param nodePane
	 * @param routeMap
	 * @param explorerPane
	 * @param uiStatusObs
	 * @param controller
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
	 *
	 * @return
	 */
	public MapNodePaneController build() {
		// Change node type
		nodePane.getChangeObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return new Tuple2<>(st, node);
		}).subscribe(t -> {
			controller.withStopSimulator(tr -> {
				final UIStatus st = t.get1();
				final MapNode node = t.get2();
				logger.debug("changeNode {} ", node); //$NON-NLS-1$
				final Traffics nextSt = st.getTraffics().changeNode(node, (a, b) -> 1);
				final UIStatus nextUiStatus = st.setTraffics(nextSt).setSelectedElement(MapElement.empty());
				controller.mapChanged(nextUiStatus);
				final GeoMap map = nextUiStatus.getTraffics().getMap();
				routeMap.setSelectedSite(map.getSite(node));
				routeMap.setSelectedNode(map.getNode(node));
				explorerPane.setSelectedNode(node);
				return nextUiStatus;
			});
		}, controller::showError);

		// delete node type
		nodePane.getDeleteObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return new Tuple2<>(st, node);
		}).subscribe(t -> {
			controller.withStopSimulator(tr -> {
				final UIStatus nextStatus = controller.deleteNode(t.get1(), t.get2());
				controller.mapChanged(nextStatus);
				explorerPane.clearSelection();
				routeMap.clearSelection();
				return nextStatus;
			});
		}, controller::showError);
		return this;
	}
}
