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
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.SiteNode;

import javax.swing.*;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author marco.marini@mmarini.org
 */
public class ExplorerPane extends JTabbedPane {
    private static final int EDGE_TAB_INDEX = 1;
    private static final int NODE_TAB_INDEX = 0;
    private static final long serialVersionUID = 1L;

    private final JList<NodeView> nodeJList;
    private final JList<EdgeView> edgeJList;
    private final String title;
    private final String nodeTabTitle;
    private final String edgeTabTitle;
    private final Flowable<EdgeView> edgeFlowable;
    private final Flowable<SiteNode> siteFlowable;
    private final Flowable<MapNode> nodeFlowable;

    /**
     *
     */
    public ExplorerPane() {
        this.nodeJList = new JList<>(new DefaultListModel<>());
        this.edgeJList = new JList<>(new DefaultListModel<>());
        title = Messages.getString("ExplorerPane.title"); //$NON-NLS-1$
        nodeTabTitle = Messages.getString("ExplorerPane.nodeTabe.title"); //$NON-NLS-1$
        edgeTabTitle = Messages.getString("ExplorerPane.edgeTab.title"); //$NON-NLS-1$
        edgeFlowable = SwingObservable.listSelection(edgeJList.getSelectionModel())
                .map(ev -> getSelectedEdge())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toFlowable(BackpressureStrategy.MISSING);
        Observable<MapNode> nodeObservable = SwingObservable.listSelection(nodeJList.getSelectionModel())
                .map(ev -> getSelectedNode())
                .filter(Optional::isPresent)
                .map(Optional::get);
        siteFlowable = nodeObservable
                .filter(n -> (n instanceof SiteNode))
                .map(n -> (SiteNode) n)
                .toFlowable(BackpressureStrategy.MISSING);
        nodeFlowable = nodeObservable
                .filter(n -> !(n instanceof SiteNode))
                .toFlowable(BackpressureStrategy.MISSING);
        init();
        createContent();
    }

    /**
     *
     */
    public void clearSelection() {
        edgeJList.setSelectedIndex(edgeJList.getSelectedIndex());
    }

    /**
     *
     */
    private void createContent() {
        addTab(nodeTabTitle, new JScrollPane(nodeJList));
        addTab(edgeTabTitle, new JScrollPane(edgeJList));
    }

    /**
     * @param model list model
     * @param test  predicate
     * @param <T>   item type
     */
    private <T> int findIndex(ListModel<T> model, Predicate<T> test) {
        int idx = -1;
        final int n = model.getSize();
        for (int i = 0; i < n; i++) {
            if (test.test(model.getElementAt(i))) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    /**
     *
     */
    public Flowable<EdgeView> getEdgeFlowable() {
        return edgeFlowable;
    }

    /**
     *
     */
    public DefaultListModel<EdgeView> getEdgeListModel() {
        return (DefaultListModel<EdgeView>) edgeJList.getModel();
    }

    /**
     *
     */
    public Flowable<MapNode> getNodeFlowable() {
        return nodeFlowable;
    }

    /**
     *
     */
    public DefaultListModel<NodeView> getNodeListModel() {
        return (DefaultListModel<NodeView>) nodeJList.getModel();
    }

    /**
     *
     */
    public Optional<EdgeView> getSelectedEdge() {
        return Optional.ofNullable(edgeJList.getSelectedValue());
    }

    /**
     *
     */
    public void setSelectedEdge(EdgeView edge) {
        assert edge != null;
        final int idx = findIndex(getEdgeListModel(), view -> view.equals(edge));
        if (idx >= 0) {
            edgeJList.setSelectedIndex(idx);
            setSelectedIndex(EDGE_TAB_INDEX);
        } else {
            clearSelection();
        }
    }

    /**
     *
     */
    public Optional<MapNode> getSelectedNode() {
        return Optional.ofNullable(nodeJList.getSelectedValue())
                .map(NodeView::getNode);
    }

    /**
     *
     */
    public void setSelectedNode(final NodeView node) {
        assert node != null;
        int idx = findIndex(getNodeListModel(), view -> view.equals(node));
        if (idx >= 0) {
            nodeJList.setSelectedIndex(idx);
            setSelectedIndex(NODE_TAB_INDEX);
        } else {
            clearSelection();
        }
    }

    /**
     *
     */
    public Flowable<SiteNode> getSiteFlowable() {
        return siteFlowable;
    }

    /**
     *
     */
    private void init() {
        setBorder(BorderFactory.createTitledBorder(title));
        nodeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        edgeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
