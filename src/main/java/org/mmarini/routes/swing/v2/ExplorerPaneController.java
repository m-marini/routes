/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;

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
	private final ControllerFunctions controller;

	/**
	 * Creates the controller for the explorer panel.
	 *
	 * @param explorerPane   the explorer panel
	 * @param routeMap       the route map
	 * @param mapElementPane the map element panel
	 * @param controller     the main controller
	 */
	public ExplorerPaneController(final ExplorerPane explorerPane, final RouteMap routeMap,
			final MapElementPane mapElementPane, final ControllerFunctions controller) {
		this.explorerPane = explorerPane;
		this.routeMap = routeMap;
		this.mapElementPane = mapElementPane;
		this.controller = controller;
	}

	/**
	 * Builds the subscriptions that manage the user interactions.
	 *
	 * @return the controller
	 */
	public ExplorerPaneController build() {
		explorerPane.getSiteFlow().subscribe(site -> {
			mapElementPane.setNode(site);
			routeMap.setSelectedSite(Optional.of(site));
			controller.centerMapTo(site.getLocation());
		}, controller::showError);

		explorerPane.getNodeFlow().subscribe(node -> {
			mapElementPane.setNode(node);
			routeMap.setSelectedNode(Optional.of(node));
			controller.centerMapTo(node.getLocation());
		}, controller::showError);

		explorerPane.getEdgeFlow().subscribe(edge -> {
			mapElementPane.setEdge(edge);
			routeMap.setSelectedEdge(Optional.of(edge));
			controller.centerMapTo(edge.getBeginLocation());
		}, controller::showError);
		return this;
	}
}
