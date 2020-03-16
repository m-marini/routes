/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.Traffics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the edge panel.
 * <p>
 * The controller manages all the user interactions from the edge panel to the
 * main controller and other components
 * </p>
 */
public class EdgePaneController implements Constants {

	private static final Logger logger = LoggerFactory.getLogger(EdgePaneController.class);

	private final EdgePane edgePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final ControllerFunctions controller;

	/**
	 * Creates the controller.
	 *
	 * @param edgePane     the edge panel
	 * @param routeMap     the route map panel
	 * @param explorerPane the explorer panel
	 * @param controller   the main controller
	 */
	public EdgePaneController(final EdgePane edgePane, final RouteMap routeMap, final ExplorerPane explorerPane,
			final ControllerFunctions controller) {
		this.edgePane = edgePane;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.controller = controller;
	}

	/**
	 * Builds the subscriptions.
	 *
	 * @return the controller
	 */
	public EdgePaneController build() {
		edgePane.getDeleteFlow().subscribe(edge -> {
			logger.debug("delete edge {}", edge.getShortName());
			routeMap.getTraffics().ifPresentOrElse(tr -> {
				explorerPane.clearSelection();
				controller.mapChanged(tr.removeEdge(edge));
			}, () -> {
				logger.error("Missing traffics", new Error());
			});
		});
		edgePane.getPriorityFlow().filter(p -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getPriority() != p;
			}).orElse(false);
		}).subscribe(priority -> {
			edgePane.getEdge().ifPresentOrElse(edge -> {
				logger.debug("changePriority {} {}", edge.getShortName(), priority);
				routeMap.getTraffics().ifPresentOrElse(tr -> {
					final MapEdge newEdge = edge.setPriority(priority);
					routeMap.setSelectedEdge(Optional.of(newEdge)).repaint();
					explorerPane.setSelectedEdge(newEdge);
					final Traffics newStatus = tr.change(newEdge);
					controller.mapChanged(newStatus);
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			}, () -> {
				logger.error("Missing edge", new Error());
			});
		}, controller::showError);

		edgePane.getSpeedLimitFlow().filter(speed -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getSpeedLimit() != speed;
			}).orElse(false);
		}).subscribe(s -> {
			// change priority
			routeMap.getTraffics().ifPresentOrElse(tr -> {
				final double speedLimit = s * KMH_TO_MPS;
				final MapEdge edge = edgePane.getEdge().get();
				logger.debug("changeSpeedLimit {} {}", edge.getShortName(), speedLimit); //$NON-NLS-1$
				final MapEdge newEdge = edge.setSpeedLimit(speedLimit);
				routeMap.setSelectedEdge(Optional.of(newEdge)).repaint();
				explorerPane.setSelectedEdge(newEdge);
				controller.mapChanged(tr.change(newEdge));
			}, () -> {
				logger.error("Missing edge", new Error());
			});
		}, controller::showError);
		return this;
	}
}
