/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;

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
	private final Observable<UIStatus> uiStatusObs;
	private final ControllerFunctions controller;

	/**
	 * Creates the controller.
	 *
	 * @param edgePane     the edge panel
	 * @param routeMap     the route map panel
	 * @param explorerPane the explorer panel
	 * @param uiStatusObs  the ui status observable
	 * @param controller   the main controller
	 */
	public EdgePaneController(final EdgePane edgePane, final RouteMap routeMap, final ExplorerPane explorerPane,
			final Observable<UIStatus> uiStatusObs, final ControllerFunctions controller) {
		this.edgePane = edgePane;
		this.routeMap = routeMap;
		this.explorerPane = explorerPane;
		this.uiStatusObs = uiStatusObs;
		this.controller = controller;
	}

	/**
	 * Builds the subscriptions.
	 *
	 * @return the controller
	 */
	public EdgePaneController build() {
		edgePane.getDeleteObs().withLatestFrom(uiStatusObs, (edge, st) -> {
			return Tuple.of(st, edge);
		}).subscribe(t -> {
			final UIStatus status = t.get1();
			final MapEdge edge = t.get2();
			logger.debug("delete edge {}", edge.getShortName());
			explorerPane.clearSelection();
			routeMap.clearSelection();
			final UIStatus nextStatus = controller.deleteEdge(status, edge);
			controller.mapChanged(nextStatus);
		});

		edgePane.getPriorityObs().withLatestFrom(uiStatusObs, (p, st) -> {
			// add last ui status
			return Tuple.of(st, p);
		}).filter(t -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getPriority() != t.get2();
			}).orElse(false);
		}).subscribe(t -> {
			final MapEdge edge = edgePane.getEdge().get();
			final int priority = t.get2();
			logger.debug("changePriority {} {}", edge.getShortName(), priority); //$NON-NLS-1$
			final MapEdge newEdge = edge.setPriority(priority);
			final UIStatus status = t.get1();
			final UIStatus newStatus = status.setTraffics(status.getTraffics().change(newEdge));
			routeMap.setSelectedEdge(Optional.of(newEdge)).repaint();
			explorerPane.setSelectedEdge(newEdge);
			controller.mapChanged(newStatus);
		}, controller::showError);

		edgePane.getSpeedLimitObs().withLatestFrom(uiStatusObs, (speed, st) -> {
			// add last ui status
			return Tuple.of(st, speed);
		}).filter(t -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getSpeedLimit() != t.get2();
			}).orElse(false);
		}).subscribe(t -> {
			// change priority
			final double speedLimit = t.get2() * KMH_TO_MPS;
			final UIStatus uiStatus = t.get1();
			final MapEdge edge = edgePane.getEdge().get();
			logger.debug("changeSpeedLimit {} {}", edge.getShortName(), speedLimit); //$NON-NLS-1$
			final MapEdge newEdge = edge.setSpeedLimit(speedLimit);
			final UIStatus newStatus = uiStatus.setTraffics(uiStatus.getTraffics().change(newEdge));
			routeMap.setSelectedEdge(Optional.of(newEdge)).repaint();
			explorerPane.setSelectedEdge(newEdge);
			controller.mapChanged(newStatus);
		}, controller::showError);
		return this;
	}
}
