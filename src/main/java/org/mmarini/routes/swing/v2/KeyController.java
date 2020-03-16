/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.awt.event.KeyEvent;

import org.mmarini.routes.model.v2.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the keyboard command.
 * <p>
 * The controller manages all the user interactions from the keyboard to the
 * main controller and other components
 * </p>
 */
public class KeyController implements Constants {
	private static final Logger logger = LoggerFactory.getLogger(KeyController.class);

	private final RouteMap routeMap;
	private final ControllerFunctions controller;

	/**
	 * Creates the controller.
	 *
	 * @param routeMap   the route map
	 * @param controller the main controller
	 */
	public KeyController(final RouteMap routeMap, final ControllerFunctions controller) {
		this.routeMap = routeMap;
		this.controller = controller;
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
		}).subscribe(ev -> {
			routeMap.getTraffics().ifPresentOrElse(tr -> {
				routeMap.getSelectedSite().or(() -> {
					return routeMap.getSelectedNode();
				}).map(node -> {
					// remove the node or site
					return tr.removeNode(node);
				}).or(() -> {
					// Remove the edge
					return routeMap.getSelectedEdge().map(edge -> {
						return tr.removeEdge(edge);
					});
				}).ifPresent(tr1 -> {
					// Update the traffics
					controller.mapChanged(tr1);
				});
			}, () -> {
				logger.error("Missing traffics", new Error());
			});
		}, controller::showError);

		return this;
	}

}
