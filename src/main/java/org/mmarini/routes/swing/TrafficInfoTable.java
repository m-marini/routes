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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.NumberFormat;

/**
 * @author marco.marini@mmarini.org
 */
public class TrafficInfoTable extends JTable {

    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     */
    public TrafficInfoTable(final TableModel model) {
        super(model);
        setRowSelectionAllowed(false);
        final JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1L;

            /**
             * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
             *      java.lang.Object, boolean, boolean, int, int)
             */
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                           final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                NumberFormat formatter = NumberFormat.getNumberInstance();
                if (column == 3) {
                    formatter = NumberFormat.getPercentInstance();
                }
                setText(formatter.format(value));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
                setBackground(TrafficInfoTable.this.getBackground());
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }

        });
        setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                           final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final NumberFormat formatter = NumberFormat.getIntegerInstance();
                setText(formatter.format(value));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
                setBackground(TrafficInfoTable.this.getBackground());
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }

        });
        setDefaultRenderer(TrafficInfoView.class, new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                           final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final NodeView entry = ((TrafficInfoView) value).getDestination();
                setText(entry.getName());
                setBackground(entry.getColor());
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }

        });
        setAutoCreateRowSorter(true);
    }
}
