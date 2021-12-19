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

import org.mmarini.routes.model2.DoubleMatrix;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.NumberFormat;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author marco.marini@mmarini.org
 */
public class InfosTable extends JTable {

    private static final double CELL_COLOR_SATURATION = 0.3;
    private static final long serialVersionUID = 1L;
    private static final int PREFERRED_HEIGHT = 200;
    private static final int PREFERRED_COLUMN_WIDTH = 70;
    private static final int SCREEN_HORIZONTAL_INSETS = 300;
    private static final int SCROLL_BAR_WIDTH = 40;

    /**
     * @param frequencies the route information
     */
    public static InfosTable create(DoubleMatrix<NodeView> frequencies) {
        final RouteInfoModel model = new RouteInfoModel();
        model.setFrequencies(frequencies);
        return new InfosTable(model);
    }

    /**
     * @param model the table model
     */
    public InfosTable(final TableModel model) {
        super(model);
        NodeViewTableCellRenderer siteTableCellRenderer = new NodeViewTableCellRenderer();
        setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                           final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final RouteInfoModel model = (RouteInfoModel) table.getModel();
                final double v = ((Number) value).doubleValue();
                setText(NumberFormat.getNumberInstance().format(value));
                final double max = model.getMaximumFlux();
                final double min = model.getMinimumFlux();
                if (column == model.getColumnCount() - 1 || column == row + 1) {
                    setBackground(table.getBackground());
                } else {
                    double val;
                    if (max == min) {
                        val = 1;
                    } else {
                        val = (v - min) / (max - min);
                    }
                    setBackground(SwingUtils.getInstance().computeColor(val, CELL_COLOR_SATURATION));
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }

        });
        setDefaultRenderer(NodeView.class, siteTableCellRenderer);
        setRowSelectionAllowed(false);
        final JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                           final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final RouteInfoModel model = (RouteInfoModel) getModel();
                final JLabel label = new JLabel();
                final Color bg = label.getBackground();
                if (column == 0) {
                    setText(Messages.getString("InfosTable.flux.label")); //$NON-NLS-1$
                    setBackground(bg);
                    setBorder(BorderFactory.createRaisedBevelBorder());
                } else if (column == model.getColumnCount() - 1) {
                    setText(Messages.getString("InfosTable.total.label")); //$NON-NLS-1$
                    setBorder(BorderFactory.createRaisedBevelBorder());
                    setBackground(bg);
                } else {
                    final NodeView node = model.getFrequencies().getKeys().get(column - 1);
                    setText(node.getName());
                    setBackground(node.getColor());
                    setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }

        });
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        for (int i = 0; i < model.getColumnCount(); i++) {
            getColumnModel().getColumn(i).setMinWidth(PREFERRED_COLUMN_WIDTH);
        }
        int width = model.getColumnCount() * PREFERRED_COLUMN_WIDTH + SCROLL_BAR_WIDTH;
        int maxWidth = max(Toolkit.getDefaultToolkit().getScreenSize().width - SCREEN_HORIZONTAL_INSETS, 400);
        setPreferredScrollableViewportSize(new Dimension((min(width, maxWidth)), PREFERRED_HEIGHT));
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
}
