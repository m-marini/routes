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

import org.mmarini.routes.model.MapNode;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * @author marco.marini@mmarini.org
 */
public class NodeChooser extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JList<MapNodeEntry> nodeList;

    /**
     *
     */
    public NodeChooser() {
        nodeList = new JList<>();
        nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        createContent();
    }

    /**
     *
     */
    public void clearSelection() {
        final int idx = nodeList.getSelectedIndex();
        nodeList.setSelectedIndex(idx);
    }

    /**
     *
     */
    private void createContent() {
        setLayout(new BorderLayout());
        add(new JScrollPane(nodeList), BorderLayout.CENTER);
    }

    /**
     *
     */
    public Optional<MapNode> getSelectedNode() {
        return Optional.ofNullable(nodeList.getSelectedValue()).map(MapNodeEntry::getNode);
    }

    /**
     * MapNodeEntry
     *
     * @param nodeList2
     */
    public void setNodeList(final DefaultListModel<MapNodeEntry> nodeList2) {
        nodeList.setModel(nodeList2);
    }
}