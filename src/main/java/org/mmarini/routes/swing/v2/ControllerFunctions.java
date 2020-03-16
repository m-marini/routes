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

import org.mmarini.routes.model.v2.Traffics;

import io.reactivex.rxjava3.functions.Action;

/**
 * Controller functions
 */
public interface ControllerFunctions {

	/**
	 * Centered map view to a given point.
	 * 
	 * @param center the center point
	 *
	 * @return the controller
	 */
	public ControllerFunctions centerMapTo(Point2D center);

	/**
	 * Upgrades the ui components due to map change.
	 * 
	 * @param traffics the traffics
	 *
	 * @return the controller
	 */
	public ControllerFunctions mapChanged(Traffics traffics);

	/**
	 * Changes the scale.
	 * <p>
	 * It computes the viewport position and zoom of scroll map.
	 * </p>
	 *
	 * @param scale the scale factor
	 * @param pivot the pivot point of scale function
	 * @return the controller
	 */
	public ControllerFunctions scaleTo(final double scale, final Point pivot);

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
	 * @param point the cursor location
	 * @return the controller
	 */
	public ControllerFunctions updateHud(Point2D point);

	/**
	 * Executes an action with stopped simulator.
	 *
	 * @param action the action
	 * @return the controller
	 * @throws Throwable in case of error
	 */
	public ControllerFunctions withSimulationStop(Action action) throws Throwable;
}
