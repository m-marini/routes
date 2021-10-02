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

import javax.swing.table.AbstractTableModel;

/**
 * @author Marco
 */
public class RouteInfoModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private double maximumFlux;
    private double minimumFlux;
    private SquareMatrixModel<NodeView> routeInfo;

    /**
     *
     */
    public RouteInfoModel() {
    }

    /**
     * @param row the row
     */
    private double computeColSum(final int row) {
        double sum = 0.;
        final double[][] values = routeInfo.getValues();
        for (int i = 0; i < values.length; ++i) {
            sum += values[row][i];
        }
        return sum;
    }

    /**
     *
     */
    private void computeMinMax() {
        final double[][] values = routeInfo.getValues();
        final int n = values.length;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                final double value = values[i][j];
                max = Math.max(max, value);
                min = Math.min(min, value);
            }
        }
        this.minimumFlux = min;
        this.maximumFlux = max;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 0 ? NodeView.class : Double.class;
    }

    @Override
    public int getColumnCount() {
        return routeInfo.getIndices().size() + 2;
    }

    /**
     *
     */
    public double getMaximumFlux() {
        return maximumFlux;
    }

    /**
     *
     */
    public double getMinimumFlux() {
        return minimumFlux;
    }

    /**
     *
     */
    public SquareMatrixModel<NodeView> getRouteInfo() {
        return routeInfo;
    }

    /**
     * @param routeInfo the route information
     */
    public void setRouteInfo(SquareMatrixModel<NodeView> routeInfo) {
        this.routeInfo = routeInfo;
        computeMinMax();
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return routeInfo.getIndices().size();
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        if (col == 0) {
            // First column legend
            return routeInfo.getIndices().get(row);
        } else if (col == routeInfo.getIndices().size() + 1) {
            // Last column total
            return computeColSum(row);
        } else {
            return routeInfo.getValues()[row][col - 1];
        }
    }
}
