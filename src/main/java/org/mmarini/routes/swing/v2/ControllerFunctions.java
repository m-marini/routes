//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.swing.v2;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;

import io.reactivex.rxjava3.functions.Action;

/**
 * Controller functions
 */
public interface ControllerFunctions {

	/**
	 * Centered map view to a given point.
	 *
	 * @param status the ui status the ui status
	 * @param center the center point
	 * @return the controller
	 */
	public ControllerFunctions centerMapTo(UIStatus status, Point2D center);

	/**
	 * Change the UI status
	 *
	 * @param status the status
	 * @return the controller
	 */
	public ControllerFunctions changeStatus(UIStatus status);

	/**
	 * Returns the ui status deleting an edge.
	 *
	 * @param status the initial status
	 * @param edge   the edge to delete
	 */
	public UIStatus deleteEdge(final UIStatus status, final MapEdge edge);

	/**
	 * Returns the ui status deleting a node.
	 *
	 * @param status the initial status
	 * @param node   the node to delete
	 */
	public UIStatus deleteNode(final UIStatus status, final MapNode node);

	/**
	 * Upgrades the ui components due to map change.
	 *
	 * @param status the ui status
	 * @return the controller
	 */
	public ControllerFunctions mapChanged(final UIStatus status);

	/**
	 * Changes the scale
	 * <p>
	 * It computes the viewport position and zoom of scroll map.
	 * </p>
	 *
	 * @param status the initial status
	 * @param scale  the scale factor
	 * @param pivot  the pivot point of scale function
	 */
	public ControllerFunctions scaleTo(final UIStatus status, final double scale, final Point pivot);

	/**
	 * Shows an error message.
	 *
	 * @param pattern    the pattern message to show
	 * @param parameters the parameters
	 * @return the controller
	 */
	public ControllerFunctions showError(String pattern, Object... parameters);

	/**
	 * Shows an error message from an exception.
	 *
	 * @param e the exception
	 * @return the controller
	 */
	public ControllerFunctions showError(final Throwable e);

	/**
	 * Upgrades the head up display.
	 *
	 * @param status the ui status
	 * @param point  the cursor location
	 * @return the controller
	 */
	public ControllerFunctions updateHud(UIStatus status, Point2D point);

	/**
	 * Executes an action with stopped simulator
	 *
	 * @param action the action
	 * @return the controller
	 * @throws Throwable in case of error
	 */
	public ControllerFunctions withSimulationStop(Action action) throws Throwable;
}
