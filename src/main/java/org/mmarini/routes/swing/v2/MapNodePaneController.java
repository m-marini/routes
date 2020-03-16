/**
 *
 */
package org.mmarini.routes.swing.v2;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.Traffics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/**
	 * Creates the controller.
	 *
	 * @param nodePane     the node panel
	 * @param routeMap     the route panel
	 * @param explorerPane the explorer panel
	 * @param controller   the main controller
	 */
	public MapNodePaneController(final MapNodePane nodePane, final RouteMap routeMap, final ExplorerPane explorerPane,
			final ControllerFunctions controller) {
		this.nodePane = nodePane;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.controller = controller;
	}

	/**
	 * Builds the subscribers.
	 *
	 * @return the controller
	 */
	public MapNodePaneController build() {
		// Change node type
		nodePane.getChangeFlow().subscribe(node -> {
			routeMap.getTraffics().ifPresentOrElse(traffics -> {
				logger.debug("changeNode {} ", node); //$NON-NLS-1$
				final Traffics nextTraffics = traffics.changeNode(node, (a, b) -> 1);
				final GeoMap map = nextTraffics.getMap();
				routeMap.setSelectedSite(map.getSite(node));
				routeMap.setSelectedNode(map.getNode(node));
				explorerPane.setSelectedNode(node);
				controller.mapChanged(nextTraffics);
			}, () -> {
				logger.error("Missing traffics", new Error());
			});
		}, controller::showError);

		// delete node type
		nodePane.getDeleteFlow().subscribe(node -> {
			routeMap.getTraffics().ifPresentOrElse(tr -> {
				explorerPane.clearSelection();
				controller.mapChanged(tr.removeNode(node));
			}, () -> {
				logger.error("Missing traffics", new Error());
			});
		}, controller::showError);
		return this;
	}
}
