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

import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.Tuple2;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 *
 */
public class ScrollMap extends JScrollPane implements Constants {
	private static final long serialVersionUID = 1L;

	private static final Point LEGEND_LOCATION = new Point(5, 5);
	private static final Insets LEGEND_INSETS = new Insets(3, 3, 3, 3);

	private final Observable<ActionEvent> scaleToObs;
	private final Observable<ChangeEvent> changeObs;
	private final List<String> pointLegendPattern;
	private final List<String> edgeLegendPattern;
	private List<String> hud;

	/**
	 * @param content
	 */
	public ScrollMap(final Component content) {
		hud = List.of("Head Up", "Display");
		this.pointLegendPattern = SwingUtils.loadPatterns("ScrollMap.pointLegendPattern"); //$NON-NLS-1$
		this.edgeLegendPattern = SwingUtils.loadPatterns("ScrollMap.edgeLegendPattern"); //$NON-NLS-1$
		setViewportView(content);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		final JButton lowerLeftCornerButton = new JButton();
		scaleToObs = SwingObservable.actions(lowerLeftCornerButton);
		changeObs = SwingObservable.change(getViewport());
		setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, lowerLeftCornerButton);

		setDoubleBuffered(false);
		setOpaque(false);
	}

	/**
	 * Returns the head up display text
	 *
	 * @param patterns
	 * @param gridSize
	 * @param point
	 * @param dragEdge
	 * @param maxSpeed
	 */
	private List<String> computeHud(final List<String> patterns, final double gridSize, final Point2D point,
			final Optional<Tuple2<Point2D, Point2D>> dragEdge, final double maxSpeed) {
		final Optional<Double> lengthOpt = dragEdge.map(t -> t.get1().distance(t.get2()));
		final Double length = dragEdge.map(t -> t.get1().distance(t.get2())).orElse(null);
		final Double speed = lengthOpt.map(l -> {
			return min(MapEdge.computeSpeedLimit(l), maxSpeed) * MPS_TO_KMH;
		}).orElse(null);
		final List<String> texts = patterns.stream()
				.map(pattern -> String.format(pattern, gridSize, point.getX(), point.getY(), length, speed))
				.collect(Collectors.toList());
		return texts;
	}

	/**
	 * Returns the rectangle containing the HUD
	 *
	 * @param messages the messages in the HUD
	 */
	private Rectangle computeRect(final List<String> messages) {
		final FontMetrics fm = getFontMetrics(getFont());
		final int w = messages.stream().mapToInt(text -> fm.stringWidth(text)).max().orElseGet(() -> 0);
		final Rectangle result = new Rectangle(LEGEND_LOCATION.x, LEGEND_LOCATION.y,
				w + LEGEND_INSETS.left + LEGEND_INSETS.right,
				fm.getHeight() * messages.size() + LEGEND_INSETS.bottom + LEGEND_INSETS.top);
		return result;
	}

	/**
	 * @return the changeObs
	 */
	public Observable<ChangeEvent> getChangeObs() {
		return changeObs;
	}

	/**
	 * Returns the scaleToObs
	 */
	public Observable<ActionEvent> getScaleToObs() {
		return scaleToObs;
	}

	/**
	 * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
	 */
	@Override
	protected void paintChildren(final Graphics g) {
		super.paintChildren(g);
		paintHud(g, hud);
		repaint();
	}

	/**
	 *
	 * @param g
	 * @param messages
	 * @return
	 */
	private ScrollMap paintHud(final Graphics g, final List<String> messages) {
		final Rectangle rect = computeRect(messages);
		final FontMetrics fm = getFontMetrics(getFont());
		g.setColor(Color.WHITE);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(Color.BLACK);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		final int x = rect.x + LEGEND_INSETS.left;
		final int fh = fm.getHeight();
		int y = rect.y + LEGEND_INSETS.top + fh - fm.getDescent();
		for (final String text : messages) {
			g.drawString(text, x, y);
			y += fh;
		}
		return this;
	}

	/**
	 * Set hud for edge
	 *
	 * @param gridSize grid size
	 * @param point    mouse position
	 * @param dragEdge drag edge
	 * @param maxSpeed max speed
	 */
	public ScrollMap setEdgeHud(final double gridSize, final Point2D point,
			final Optional<Tuple2<Point2D, Point2D>> dragEdge, final double maxSpeed) {
		return setHud(computeHud(edgeLegendPattern, gridSize, point, dragEdge, maxSpeed));
	}

	/**
	 * Returns the scroll map with changed head up display
	 *
	 * @param hud the head up display
	 */
	public ScrollMap setHud(final List<String> hud) {
		this.hud = hud;
		repaint();
		return this;
	}

	/**
	 * Set hud for edge
	 *
	 * @param gridSize grid size
	 * @param point    mouse position
	 * @param dragEdge drag edge
	 * @param maxSpeed max speed
	 */
	public ScrollMap setPointHud(final double gridSize, final Point2D point) {
		return setHud(computeHud(pointLegendPattern, gridSize, point, Optional.empty(), 0));
	}
}
