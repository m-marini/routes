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

import hu.akarnokd.rxjava3.swing.SwingObservable;

import javax.swing.*;

/**
 * Shows the parameters of the profile to generate random map.
 * Allows to enter the information
 *
 * @author marco.marini@mmarini.org
 */
public class RoutePane extends Box {
    private static final long serialVersionUID = 1L;
    private final DefaultListModel<MapNodeEntry> departureListModel;
    private final RouteTableModel routeTableModel;
    private final JList<MapNodeEntry> departureList;
    private final JTable routeTable;
    private SquareMatrixModel<MapNodeEntry> pathEntry;

    /**
     *
     */
    public RoutePane() {
        super(BoxLayout.LINE_AXIS);
        departureListModel = new DefaultListModel<>();
        routeTableModel = new RouteTableModel();
        departureList = new JList<>(departureListModel);
        routeTable = new JTable(routeTableModel);

        init();
        createContent();
        createFlows();
    }

    /**
     *
     */
    private void createContent() {
        final JScrollPane departureScrollPane = new JScrollPane(departureList);
        departureScrollPane
                .setBorder(BorderFactory.createTitledBorder(Messages.getString("RoutePane.departure.title"))); //$NON-NLS-1$

        final JScrollPane routeScrollPane = new JScrollPane(routeTable);
        routeScrollPane.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoutePane.route.title"))); //$NON-NLS-1$

        add(departureScrollPane);
        add(routeScrollPane);
    }

    /**
     *
     */
    private void createFlows() {
        SwingObservable.listSelection(departureList)
                .filter(ev -> !ev.getValueIsAdjusting())
                .doOnNext(ev ->
                        departureSelected()).subscribe();
    }

    /**
     *
     */
    private void departureSelected() {
        final int idx = departureList.getSelectedIndex();
        routeTableModel.setRow(idx);
    }

    /**
     *
     */
    public SquareMatrixModel<MapNodeEntry> getPathEntry() {
        return pathEntry;
    }

    /**
     * Sets the path entries
     *
     * @param entries the path entries
     */
    public void setPathEntry(SquareMatrixModel<MapNodeEntry> entries) {
        this.pathEntry = entries;
        departureListModel.clear();
        departureListModel.addAll(entries.getIndices());
        routeTableModel.setRow(0);
        routeTableModel.setPathEntry(entries);
        departureList.setSelectedIndex(0);
    }

    private void init() {
        routeTable.setDefaultRenderer(MapNodeEntry.class, new MapNodeEntryTableCellRenderer());
        departureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        departureList.setCellRenderer(new SiteListCellRenderer());
    }
}
