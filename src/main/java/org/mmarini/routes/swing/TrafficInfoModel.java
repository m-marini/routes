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
import java.util.List;

/**
 * @author marco.marini@mmarini.org
 */
public class TrafficInfoModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_LABEL_NAME = {"destination", "vehicleCount", "delayedCount", "delayedCountPerc",
            "delayedTime"};
    private static final Class<?>[] COLUMN_CLASS = {TrafficInfoView.class, Integer.class, Integer.class, Double.class, Double.class};
    private List<TrafficInfoView> info;

    /**
     *
     */
    public TrafficInfoModel() {
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return COLUMN_CLASS[columnIndex];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_LABEL_NAME.length;
    }

    @Override
    public String getColumnName(final int column) {
        return Messages.getString("TrafficInfoModel." + COLUMN_LABEL_NAME[column] + ".label"); //$NON-NLS-1$
    }

    /**
     * @param index the index
     */
    public NodeView getNode(final int index) {
        return info.get(index).getDestination();
    }

    @Override
    public int getRowCount() {
        return info.size();
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        final TrafficInfoView record = info.get(row);
        final int vehicleCount = record.getInfo().getVehicleCount();
        final int delayCount = record.getInfo().getDelayCount();
        switch (col) {
            case 0:
                return record;
            case 1:
                return vehicleCount;
            case 2:
                return delayCount;
            case 3:
                return vehicleCount > 0 ? (double) delayCount / vehicleCount : 0;
            case 4:
                return record.getInfo().getAverageDelayTime();
            default:
                return "?";
        }
    }

    /**
     * @param info the info
     */
    public void setInfo(final List<TrafficInfoView> info) {
        this.info = info;
        fireTableStructureChanged();
    }
}
