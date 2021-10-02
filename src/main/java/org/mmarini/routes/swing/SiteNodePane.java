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

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author marco.marini@mmarini.org
 */
public class SiteNodePane extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JFormattedTextField xField;
    private final JFormattedTextField yField;
    private final JLabel yLabel;
    private final JLabel xLabel;
    private final JLabel nameLabel;
    private final JTextField nameField;
    private final JButton changeButton;
    private final JButton deleteButton;
    private final Flowable<MapNodeEntry> changeFlowable;
    private final Flowable<MapNodeEntry> deleteFlowable;
    private MapNodeEntry node;

    /**
     *
     */
    public SiteNodePane() {
        xField = new JFormattedTextField(new NumberFormatter());
        yField = new JFormattedTextField(new NumberFormatter());
        nameField = new JTextField(10);
        xLabel = new JLabel(Messages.getString("SiteNodePane.x.label")); //$NON-NLS-1$
        yLabel = new JLabel(Messages.getString("SiteNodePane.y.label")); //$NON-NLS-1$
        nameLabel = new JLabel(Messages.getString("SiteNodePane.name.label")); //$NON-NLS-1$
        changeButton = new JButton();
        deleteButton = new JButton();

        this.changeFlowable = SwingObservable.actions(changeButton)
                .map(ev -> node)
                .toFlowable(BackpressureStrategy.MISSING);
        this.deleteFlowable = SwingObservable.actions(deleteButton)
                .map(ev -> node)
                .toFlowable(BackpressureStrategy.MISSING);
        init();
        createContent();
    }

    /**
     *
     */
    private void createContent() {
        setLayout(new BorderLayout());
        add(createToolBar(), BorderLayout.NORTH);
        add(createInfoPane(), BorderLayout.CENTER);
    }

    /**
     *
     */
    private JComponent createInfoPane() {
        final JPanel pane = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        pane.setLayout(layout);
        final GridBagConstraints cons = new GridBagConstraints();
        cons.insets = new Insets(2, 2, 2, 2);

        Component c;
        c = nameLabel;
        cons.gridx = 0;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = nameField;
        cons.gridx = 1;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = Box.createGlue();
        cons.gridx = 2;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 6;
        cons.anchor = GridBagConstraints.CENTER;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 1;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = xLabel;
        cons.gridx = 0;
        cons.gridy = 1;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = xField;
        cons.gridx = 1;
        cons.gridy = 1;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = yLabel;
        cons.gridx = 0;
        cons.gridy = 2;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = yField;
        cons.gridx = 1;
        cons.gridy = 2;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = Box.createGlue();
        cons.gridx = 0;
        cons.gridy = 3;
        cons.gridwidth = 2;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.CENTER;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 1;
        layout.setConstraints(c, cons);
        pane.add(c);

        return pane;
    }

    /**
     *
     */
    private JComponent createToolBar() {
        final JToolBar toolbar = new JToolBar();
        toolbar.add(changeButton);
        toolbar.add(deleteButton);
        return toolbar;
    }

    public Flowable<MapNodeEntry> getChangeFlowable() {
        return changeFlowable;
    }

    public Flowable<MapNodeEntry> getDeleteFlowable() {
        return deleteFlowable;
    }

    private void init() {
        final SwingUtils utils = SwingUtils.getInstance();
        utils.initButton(changeButton, "SiteNodePane.changeAction"); //$NON-NLS-1$
        utils.initButton(deleteButton, "SiteNodePane.deleteAction"); //$NON-NLS-1$

        setBorder(BorderFactory.createTitledBorder(Messages.getString("SiteNodePane.title"))); //$NON-NLS-1$

        nameField.setEditable(false);

        xField.setColumns(5);
        xField.setEditable(false);
        xField.setHorizontalAlignment(SwingConstants.RIGHT);

        yField.setColumns(5);
        yField.setEditable(false);
        yField.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    /**
     * @param node the node to set
     */
    public void setNode(final MapNodeEntry node) {
        this.node = node;
        final Point2D location = node.getNode().getLocation();
        xField.setValue(location.getX());
        yField.setValue(location.getY());
        nameField.setText(node.getName());
    }
}