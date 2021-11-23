/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */
package org.mmarini.routes.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.Math.max;
import static org.mmarini.routes.swing.SwingUtils.loadPatterns;

/**
 * @author marco.marini@mmarini.org
 */
public class InfoPane extends JComponent {
    public static final int MAX_NUM_COLUMNS = 10;
    private final Rectangle rect;
    private final String[] edgeLegendPattern;
    private final String[] pointLegendPattern;
    private Point2D mapPoint;
    private long numVehicles;
    private double fps;
    private double tps;
    private double speed;
    private double gridSize;
    private double edgeLength;
    private boolean edgeLegend;

    /**
     *
     */
    public InfoPane() {
        mapPoint = new Point2D.Double();
        rect = new Rectangle();
        pointLegendPattern = loadPatterns("InfoPane.pointLegendPattern");
        edgeLegendPattern = loadPatterns("InfoPane.edgeLegendPattern");
        setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * @param text the text
     */
    private void computeRect(final String... text) {
        final FontMetrics fm = getFontMetrics(getFont());
        final int n = text.length;
        int w = 0;
        for (final String tx : text) {
            w = max(w, fm.stringWidth(tx));
        }
        Insets insets = Optional.ofNullable(getBorder())
                .map(b -> b.getBorderInsets(this))
                .orElseGet(this::getInsets);
        rect.x = insets.left;
        rect.y = insets.top;
        rect.width = w;
        rect.height = fm.getHeight() * n;
    }

    @Override
    public Dimension getPreferredSize() {
        final FontMetrics fm = getFontMetrics(getFont());
        final int noRows = max(pointLegendPattern.length, edgeLegendPattern.length);
        int w = fm.getMaxAdvance() * MAX_NUM_COLUMNS;
        Insets insets = Optional.ofNullable(getBorder())
                .map(b -> b.getBorderInsets(this))
                .orElseGet(this::getInsets);
        return new Dimension(w + insets.left + insets.right,
                fm.getHeight() * noRows + insets.bottom + insets.top);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        paintInfo(g);
    }

    /**
     * @param g graphics
     */
    private void paintInfo(final Graphics g) {
        /*
         * Compute the pattern
         */
        final String[] pattern;
        if (edgeLegend) {
            pattern = edgeLegendPattern;
        } else {
            pattern = pointLegendPattern;
        }
        /*
        String[] msg = new String[]{
                String.format("TPS: %.2f", tps),
                String.format("FPS: %.2f", fps),
                String.format("Speed: %-2.2f", speed),
                String.format("Vehicles: %d", this.numVehicles)
        };
*/
        final String[] text = Arrays.stream(pattern)
                .map(ptn ->
                        MessageFormat.format(ptn,
                                gridSize,
                                mapPoint.getX(),
                                mapPoint.getY(),
                                edgeLength,
                                tps,
                                fps,
                                speed,
                                numVehicles)
                ).toArray(String[]::new);

        paintMessageBox(g, text);
    }

    /**
     * @param g        graphics
     * @param messages messages
     */
    private void paintMessageBox(final Graphics g, final String... messages) {
        computeRect(messages);
        final FontMetrics fm = getFontMetrics(getFont());
        g.setColor(getForeground());
        Insets insets = getInsets();
        final int x = rect.x + insets.left;
        final int fh = fm.getHeight();
        int y = rect.y + insets.top + fh - fm.getDescent();
        for (final String text : messages) {
            g.drawString(text, x, y);
            y += fh;
        }
    }

    public void setEdgeLegend(boolean edgeLegend) {
        this.edgeLegend = edgeLegend;
    }

    public void setEdgeLength(double edgeLength) {
        this.edgeLength = edgeLength;
        repaint();
    }

    /**
     * Sets the frames per second
     *
     * @param fps the frames per second
     */
    public void setFps(double fps) {
        this.fps = fps;
        repaint();
    }

    public void setGridSize(double gridSize) {
        this.gridSize = gridSize;
    }

    public void setMapPoint(Point2D mapPoint) {
        this.mapPoint = mapPoint;
        repaint();
    }

    public void setNumVehicles(long numVehicles) {
        this.numVehicles = numVehicles;
        repaint();
    }

    /**
     * @param speed the speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
        repaint();
    }

    /**
     * Sets the transitions per second
     *
     * @param tps the transitions per second
     */
    public void setTps(double tps) {
        this.tps = tps;
        repaint();
    }
}
