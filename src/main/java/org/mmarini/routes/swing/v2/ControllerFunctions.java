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
import java.util.function.Function;

import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Traffics;

/**
 *
 */
public interface ControllerFunctions {

	/**
	 *
	 * @param st
	 * @param elem1
	 */
	public ControllerFunctions centerMapTo(UIStatus st, Point2D elem1);

	/**
	 *
	 * @param uiStatus
	 * @param edge
	 * @return
	 */
	public UIStatus deleteEdge(final UIStatus uiStatus, final MapEdge edge);

	/**
	 *
	 * @param uiStatus
	 * @param node
	 * @return
	 */
	public UIStatus deleteNode(final UIStatus uiStatus, final MapNode node);

	/**
	 *
	 * @param uiStatus
	 * @return
	 */
	public ControllerFunctions mapChanged(final UIStatus uiStatus);

	/**
	 *
	 * @param status
	 * @param scale
	 * @param pivot
	 * @return
	 */
	public UIStatus scaleTo(final UIStatus status, final double scale, final Point pivot);

	/**
	 * Returns the controller with error message
	 *
	 * @param message
	 * @param parameters
	 */
	public ControllerFunctions showError(String message, Object... parameters);

	/**
	 * Returns the controller with error message from exception
	 *
	 * @param e the exception
	 */
	public ControllerFunctions showError(final Throwable e);

	/**
	 *
	 * @param elem1
	 * @param elem12
	 * @return
	 */
	public ControllerFunctions updateHud(UIStatus elem1, Point2D elem12);

	/**
	 *
	 * @param changeStatus
	 * @return
	 */
	public Controller withStopSimulator(Function<Traffics, UIStatus> changeStatus);
}
