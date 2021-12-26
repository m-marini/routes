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

import static java.lang.String.format;
import static org.mmarini.routes.swing.SwingUtils.loadPatterns;

/**
 * @author marco.marini@mmarini.org
 */
public class InfoPane extends JComponent {
    public static final int MAX_NUM_COLUMNS = 10;
    public static final int SEC_PER_MIN = 60;
    public static final int SEC_PER_HOURS = 60 * 60;

    private static String timeFormat(long time) {
        if (time < SEC_PER_MIN) {
            return format("%d", time);
        }
        if (time < SEC_PER_HOURS) {
            return format("%d:%02d", time / SEC_PER_MIN, time % SEC_PER_MIN);
        } else {
            return format("%d:%02d:%02d",
                    time / SEC_PER_HOURS,
                    (time % SEC_PER_HOURS) / SEC_PER_MIN,
                    time % SEC_PER_MIN);
        }
    }

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
    private long time;

    /**
     *
     */
    public InfoPane() {
        mapPoint = new Point2D.Double();
        pointLegendPattern = loadPatterns("InfoPane.pointLegendPattern");
        edgeLegendPattern = loadPatterns("InfoPane.edgeLegendPattern");
        setBorder(BorderFactory.createEtchedBorder());
    }

    @Override
    public Dimension getPreferredSize() {
        final FontMetrics fm = getFontMetrics(getFont());
        int w = fm.getMaxAdvance() * MAX_NUM_COLUMNS;
        Insets insets = Optional.ofNullable(getBorder())
                .map(b -> b.getBorderInsets(this))
                .orElseGet(this::getInsets);
        return new Dimension(w + insets.left + insets.right,
                fm.getHeight() + insets.bottom + insets.top);
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
        String timeFmt = timeFormat(time);
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
                                numVehicles,
                                timeFmt)
                ).toArray(String[]::new);
        Dimension size = getSize();
        Insets insets = Optional.ofNullable(getBorder())
                .map(b -> b.getBorderInsets(this))
                .orElseGet(this::getInsets);

        final FontMetrics fm = getFontMetrics(getFont());
        int fh = fm.getHeight();
        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        int y = insets.top - fm.getDescent() + (fh + h) / 2;
        Graphics gr = g.create();
        gr.setColor(getForeground());
        Rectangle cb = gr.getClipBounds();
        for (int i = 0; i < text.length; i++) {
            int x = insets.left + i * w / text.length;
            gr.setClip(cb);
            gr.clipRect(x, insets.top, w / text.length, h);
            gr.drawString(text[i], x, y);
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

    /**
     * Sets the gris size
     *
     * @param gridSize the gris size
     */
    public void setGridSize(double gridSize) {
        boolean changed = gridSize != this.gridSize;
        this.gridSize = gridSize;
        if (changed) {
            repaint();
        }
    }

    public void setMapPoint(Point2D mapPoint) {
        this.mapPoint = mapPoint;
        repaint();
    }

    public void setNumVehicles(long numVehicles) {
        boolean changed = this.numVehicles != numVehicles;
        this.numVehicles = numVehicles;
        if (changed) {
            repaint();
        }
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
     * @param time the transitions per second
     */
    public void setTime(long time) {
        boolean changed = time != this.time;
        this.time = time;
        if (changed) {
            repaint();
        }
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
