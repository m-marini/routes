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
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import javax.swing.JComponent;

import org.mmarini.routes.model.v2.SimulationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class RouteMap extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(RouteMap.class);

	private boolean trafficView;
	private Optional<SimulationStatus> status;
	private final double scale;
	private final Point2D offset;

	/**
	 *
	 */
	public RouteMap() {
		super();
		status = Optional.empty();
		offset = new Point2D.Double();
		scale = 3;
		setBackground(Color.WHITE);
		setDoubleBuffered(true);
		logger.debug("RouteMap created");
	}

	/**
	 * Returns the grid size
	 */
	private double getGridSize() {
		final double size = 10 / scale;
		double gridSize = 1;
		while (size > gridSize) {
			gridSize *= 10;
		}
		return gridSize;
	}

	/**
	 * Returns the transformation
	 */
	private AffineTransform getTransform() {
		final AffineTransform scaleTr = AffineTransform.getScaleInstance(scale, scale);
		final AffineTransform offsetTr = AffineTransform.getTranslateInstance(-offset.getX(), -offset.getY());
		scaleTr.concatenate(offsetTr);
		return scaleTr;
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
		final AffineTransform tr = getTransform();
		try {
			final Rectangle2D bound = new Rectangle2D.Double(0, 0, size.width, size.height);
			final Rectangle2D realBound = tr.createInverse().createTransformedShape(bound).getBounds2D();
			Painter.create((Graphics2D) g.create()).setBound(realBound).setStatus(status).setTransform(tr)
					.setGridSize(getGridSize()).paint();
		} catch (final NoninvertibleTransformException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param status the status to set
	 */
	public RouteMap setStatus(final SimulationStatus status) {
		this.status = Optional.ofNullable(status);
		repaint();
		return this;
	}

}
