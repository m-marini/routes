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

/**
 * @author marco.marini@mmarini.org
 */
public class MapElementPane extends JPanel {

    private static final String EMPTY_CARD = "Empty"; //$NON-NLS-1$
    private static final String NODE_CARD = "Node"; //$NON-NLS-1$
    private static final String EDGE_CARD = "Edge"; //$NON-NLS-1$
    private static final String SITE_CARD = "Site"; //$NON-NLS-1$

    private static final long serialVersionUID = 1L;
    private final CardLayout cardLayout;
    private final SitePane sitePane;
    private final NodePane nodePane;
    private final EdgePane edgePane;

    /**
     *
     */
    public MapElementPane() {
        cardLayout = new CardLayout();
        nodePane = new NodePane();
        sitePane = new SitePane();
        edgePane = new EdgePane();
        createContent();
    }

    /**
     *
     */
    public void clearPanel() {
        cardLayout.show(this, EMPTY_CARD);
    }

    /**
     *
     */
    private void createContent() {
        setLayout(cardLayout);
        final Box empty = Box.createVerticalBox();
        empty.setBorder(BorderFactory.createTitledBorder(Messages.getString("MapElementPane.emptyPane.title"))); //$NON-NLS-1$
        add(empty, EMPTY_CARD);
        add(nodePane, NODE_CARD);
        add(sitePane, SITE_CARD);
        add(edgePane, EDGE_CARD);
    }

    public EdgePane getEdgePane() {
        return edgePane;
    }

    public NodePane getMapNodePane() {
        return nodePane;
    }

    public SitePane getSiteNodePane() {
        return sitePane;
    }

    public void setSelectedEdge(final EdgeView edge) {
        assert edge != null;
        edgePane.setEdge(edge);
        cardLayout.show(this, EDGE_CARD);
    }

    public void setSelectedNode(final NodeView node) {
        assert node != null;
        nodePane.setNode(node);
        cardLayout.show(this, NODE_CARD);
    }

    public void setSelectedSite(final NodeView node) {
        assert node != null;
        sitePane.setNode(node);
        cardLayout.show(this, SITE_CARD);
    }
}
