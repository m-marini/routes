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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import javax.swing.JComponent;

import org.mmarini.routes.model.v2.SimulationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 */
public class RouteMap extends JComponent {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(RouteMap.class);

	private final Observable<MouseEvent> mouseObs;
	private final Observable<MouseWheelEvent> mouseWheelObs;
	private boolean trafficView;
	private Optional<SimulationStatus> status;
	private AffineTransform transform;
	private double gridSize;
	private boolean borderPainted;

	/**
	 *
	 */
	public RouteMap() {
		super();
		transform = new AffineTransform();
		gridSize = 10;
		mouseObs = SwingObservable.mouse(this);
		mouseWheelObs = SwingObservable.mouseWheel(this);
		status = Optional.empty();
		setBackground(Color.WHITE);
		setDoubleBuffered(true);
		logger.debug("RouteMap created");
	}

	/**
	 * Returns the grid size in meter
	 */
	public double getGridSize() {
		return gridSize;
	}

	/**
	 * @return the mouseObs
	 */
	public Observable<MouseEvent> getMouseObs() {
		return mouseObs;
	}

	/**
	 * @return the mouseWeelObs
	 */
	public Observable<MouseWheelEvent> getMouseWheelObs() {
		return mouseWheelObs;
	}

	/**
	 * @return the status
	 */
	public Optional<SimulationStatus> getStatus() {
		return status;
	}

	/**
	 * @return the transform
	 */
	public AffineTransform getTransform() {
		return transform;
	}

	/**
	 * @return the trafficView
	 */
	public boolean isTrafficView() {
		return trafficView;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		final Dimension size = getSize();
		Color bg;
		if (trafficView) {
			bg = Color.BLACK;
		} else {
			bg = getBackground();
		}
		g.setColor(bg);
		g.fillRect(0, 0, size.width, size.height);
		final AffineTransform tr = transform;
		try {
			final Rectangle2D bound = new Rectangle2D.Double(0, 0, size.width, size.height);
			final Rectangle2D realBound = tr.createInverse().createTransformedShape(bound).getBounds2D();
			Painter.create((Graphics2D) g.create()).setBound(realBound).setStatus(status).setTransform(tr)
					.setGridSize(gridSize).setBorderPainted(borderPainted).paint();
		} catch (final NoninvertibleTransformException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Returns the route map with set border painted
	 *
	 * @param borderPainted true if border painted
	 */
	public RouteMap setBorderPainted(final boolean borderPainted) {
		this.borderPainted = borderPainted;
		return this;
	}

	/**
	 * Returns the route map with grid size set
	 *
	 * @param gridSize the grid size in meters
	 */
	public RouteMap setGridSize(final double gridSize) {
		this.gridSize = gridSize;
		return this;
	}

	/**
	 * @param status the status to set
	 * @return
	 */
	public RouteMap setStatus(final Optional<SimulationStatus> status) {
		this.status = status;
		return this;
	}

	/**
	 * @param status the status to set
	 */
	public RouteMap setStatus(final SimulationStatus status) {
		this.status = Optional.ofNullable(status);
		return this;
	}

	/**
	 * @param trafficView the trafficView to set
	 * @return
	 */
	public RouteMap setTrafficView(final boolean trafficView) {
		this.trafficView = trafficView;
		return this;
	}

	/**
	 * Returns the route map with set transform
	 *
	 * @param transform the transform from map coordinate to screen coordinate
	 */
	public RouteMap setTransform(final AffineTransform transform) {
		this.transform = transform;
		return this;
	}
}
