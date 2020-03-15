/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.awt.event.KeyEvent;
import java.util.Optional;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapNode;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Controller for the keyboard command.
 * <p>
 * The controller manages all the user interactions from the keyboard to the
 * main controller and other components
 * </p>
 */
public class KeyController implements Constants {

	private final RouteMap routeMap;
	private final ControllerFunctions controller;
	private final Flowable<UIStatus> uiStatusFlow;

	/**
	 * Creates the controller.
	 *
	 * @param routeMap     the route map
	 * @param uiStatusFlow the flowable of ui status
	 * @param controller   the main controller
	 */
	public KeyController(final RouteMap routeMap, final Flowable<UIStatus> uiStatusFlow,
			final ControllerFunctions controller) {
		this.routeMap = routeMap;
		this.controller = controller;
		this.uiStatusFlow = uiStatusFlow;
	}

	/**
	 * Create the binding to the keyboard.
	 *
	 * @return the controller
	 */
	public KeyController build() {
		// flowable of delete keys
		routeMap.getKeyboardFlow().filter(ev -> {
			// Filter for delete keys pressed
			return ev.getID() == KeyEvent.KEY_PRESSED
					&& (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE || ev.getKeyCode() == KeyEvent.VK_DELETE);
		}).withLatestFrom(uiStatusFlow, (ev, st) -> st).subscribe(st -> {
			final Optional<MapNode> mapNode = routeMap.getSelectedSite().or(() -> {
				return routeMap.getSelectedNode();
			});
			final Optional<UIStatus> deleteNodeProcess = mapNode.map(node -> {
				return controller.deleteNode(st, node);
			});
			final Optional<UIStatus> deleteProcess = deleteNodeProcess.or(() -> {
				final Optional<UIStatus> sta = routeMap.getSelectedEdge().map(edge -> controller.deleteEdge(st, edge));
				return sta;
			});
			final UIStatus nextStatus = deleteProcess.orElse(st);
			controller.mapChanged(nextStatus);
		}, controller::showError);

		return this;
	}
}
