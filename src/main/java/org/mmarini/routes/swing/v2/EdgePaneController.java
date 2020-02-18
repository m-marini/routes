/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;

/**
 * @author us00852
 *
 */
public class EdgePaneController implements Constants {

	private static final Logger logger = LoggerFactory.getLogger(EdgePaneController.class);

	private final EdgePane edgePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final Observable<UIStatus> uiStatusObs;
	private final ControllerFunctions controller;

	/**
	 * @param edgePane
	 * @param routeMap
	 * @param explorerPane
	 * @param uiStatusObs
	 * @param controller
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
	 *
	 * @return
	 */
	public EdgePaneController build() {
		edgePane.getDeleteObs().withLatestFrom(uiStatusObs, (edge, st) -> {
			return new Tuple2<>(st, edge);
		}).subscribe(t -> {
			controller.withStopSimulator(st1 -> {
				final UIStatus status = t.get1();
				final MapEdge edge = t.get2();
				logger.debug("delete edge {}", edge.getShortName());
				final UIStatus nextStatus = controller.deleteEdge(status, edge);
				controller.mapChanged(nextStatus);
				explorerPane.clearSelection();
				routeMap.clearSelection();
				return nextStatus;
			});
		});

		edgePane.getPriorityObs().withLatestFrom(uiStatusObs, (p, st) -> {
			// add last ui status
			return new Tuple2<>(st, p);
		}).filter(t -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getPriority() != t.get2();
			}).orElse(false);
		}).subscribe(t -> {
			controller.withStopSimulator(tr -> {
				final MapEdge edge = edgePane.getEdge().get();
				final int priority = t.get2();
				logger.debug("changePriority {} {}", edge.getShortName(), priority); //$NON-NLS-1$
				final MapEdge newEdge = edge.setPriority(priority);
				final UIStatus status = t.get1();
				final UIStatus newStatus = status.setTraffics(status.getTraffics().change(newEdge));
				controller.mapChanged(newStatus);
				routeMap.setSelectedEdge(Optional.of(newEdge)).repaint();
				explorerPane.setSelectedEdge(newEdge);
				return newStatus;
			});
		}, controller::showError);

		edgePane.getSpeedLimitObs().withLatestFrom(uiStatusObs, (speed, st) -> {
			// add last ui status
			return new Tuple2<>(st, speed);
		}).filter(t -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getSpeedLimit() != t.get2();
			}).orElse(false);
		}).subscribe(t -> {
			controller.withStopSimulator(tr -> {
				// change priority
				final double speedLimit = t.get2() * KMH_TO_MPS;
				final UIStatus uiStatus = t.get1();
				final MapEdge edge = edgePane.getEdge().get();
				logger.debug("changeSpeedLimit {} {}", edge.getShortName(), speedLimit); //$NON-NLS-1$
				final MapEdge newEdge = edge.setSpeedLimit(speedLimit);
				final UIStatus newStatus = uiStatus.setTraffics(uiStatus.getTraffics().change(newEdge));
				controller.mapChanged(newStatus);
				routeMap.setSelectedEdge(Optional.of(newEdge)).repaint();
				explorerPane.setSelectedEdge(newEdge);
				return newStatus;
			});
		}, controller::showError);
		return this;
	}
}
