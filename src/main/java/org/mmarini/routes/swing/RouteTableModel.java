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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;

/**
 *
 */
public class RouteTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -2634066472823732066L;
    private static final String[] COLUMN_NAMES = {"Destination", "Weight"};
    private static final Logger logger = LoggerFactory.getLogger(RouteTableModel.class);
    private SquareMatrixModel<MapNodeEntry> pathEntry;
    private int row;

    /**
     *
     */
    public RouteTableModel() {
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 1 ? Double.class : MapNodeEntry.class;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(final int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getRowCount() {
        return pathEntry == null ? 0 : pathEntry.getIndices().size();
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        if (col == 0) {
            return pathEntry.getIndices().get(row);
        } else {
            return pathEntry.getValues()[this.row][row];
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    /**
     * @param pathEntry the path entry
     */
    public void setPathEntry(SquareMatrixModel<MapNodeEntry> pathEntry) {
        this.pathEntry = pathEntry;
        fireTableDataChanged();
    }

    /**
     * @param row the departure index
     */
    public void setRow(int row) {
        this.row = row;
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        assert columnIndex == 1;
        logger.info("set value={}, rowIndex={}, row={}", value, rowIndex, this.row);
        pathEntry.getValues()[this.row][rowIndex] = (Double) value;
    }
}
