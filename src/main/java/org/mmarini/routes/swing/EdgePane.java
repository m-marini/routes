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
import org.mmarini.routes.model.MapEdge;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;

/**
 * @author marco.marini@mmarini.org
 */
public class EdgePane extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JFormattedTextField priorityField;
    private final JFormattedTextField speedLimitField;
    private final JFormattedTextField incomeQueueField;
    private final JFormattedTextField distanceField;
    private final JTextField nameField;
    private final JTextField beginField;
    private final JTextField endField;
    private final JButton deleteAction;
    private final JButton browseBeginNodeAction;
    private final JButton browseEndNodeAction;
    private final Flowable<EdgeView> changeFlowable;
    private final Flowable<EdgeView> deleteFlowable;
    private final Flowable<EdgeView> endNodeFlowable;
    private final Flowable<EdgeView> beginNodeFlowable;
    private EdgeView edgeModel;

    /**
     *
     */
    public EdgePane() {
        priorityField = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance()));
        speedLimitField = new JFormattedTextField(new NumberFormatter(NumberFormat.getNumberInstance()));
        incomeQueueField = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance()));
        distanceField = new JFormattedTextField(new NumberFormatter(NumberFormat.getNumberInstance()));
        nameField = new JTextField(20);
        beginField = new JTextField(10);
        endField = new JTextField(10);
        deleteAction = new JButton();
        browseEndNodeAction = new JButton();
        browseBeginNodeAction = new JButton();
        Observable<EdgeView> priorityObs = SwingObservable.propertyChange(priorityField, "value")
                .map(evt -> {
                    int p = ((Number) evt.getNewValue()).intValue();
                    return edgeModel.setPriority(p);
                });
        Observable<EdgeView> speedObs = SwingObservable.propertyChange(speedLimitField, "value")
                .map(evt -> {
                    double speedLimit = ((Number) evt.getNewValue()).doubleValue() / 3.6;
                    return edgeModel.setSpeedLimit(speedLimit);
                });
        this.changeFlowable = priorityObs.mergeWith(speedObs)
                .toFlowable(BackpressureStrategy.MISSING);
        this.deleteFlowable = SwingObservable.actions(deleteAction).map(ev -> edgeModel)
                .toFlowable(BackpressureStrategy.MISSING);
        this.endNodeFlowable = SwingObservable.actions(browseEndNodeAction).map(ev -> edgeModel)
                .toFlowable(BackpressureStrategy.MISSING);
        this.beginNodeFlowable = SwingObservable.actions(browseBeginNodeAction).map(ev -> edgeModel)
                .toFlowable(BackpressureStrategy.MISSING);
        createContent();
        init();
    }

    /**
     *
     */
    private void createContent() {
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.NORTH);
        add(createInfoPane(), BorderLayout.CENTER);
    }

    /**
     *
     */
    private Component createInfoPane() {
        final JPanel pane = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        pane.setLayout(layout);
        final GridBagConstraints cons = new GridBagConstraints();
        cons.insets = new Insets(2, 2, 2, 2);

        Component c;
        c = new JLabel(Messages.getString("EdgePane.name.label")); //$NON-NLS-1$
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
        cons.gridwidth = 2;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = Box.createGlue();
        cons.gridx = 3;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 7;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 1;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = new JLabel(Messages.getString("EdgePane.begin.label")); //$NON-NLS-1$
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

        c = beginField;
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

        JButton btn = browseBeginNodeAction;
        btn.setBorder(BorderFactory.createEmptyBorder());
        c = btn;
        cons.gridx = 2;
        cons.gridy = 1;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = new JLabel(Messages.getString("EdgePane.end.label")); //$NON-NLS-1$
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

        c = endField;
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

        btn = browseEndNodeAction;
        btn.setBorder(BorderFactory.createEmptyBorder());
        c = btn;
        cons.gridx = 2;
        cons.gridy = 2;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = new JLabel(Messages.getString("EdgePane.distance.label")); //$NON-NLS-1$
        cons.gridx = 0;
        cons.gridy = 3;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = distanceField;
        cons.gridx = 1;
        cons.gridy = 3;
        cons.gridwidth = 2;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = new JLabel(Messages.getString("EdgePane.speedLimit.label")); //$NON-NLS-1$
        cons.gridx = 0;
        cons.gridy = 4;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = speedLimitField;
        cons.gridx = 1;
        cons.gridy = 4;
        cons.gridwidth = 2;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = new JLabel(Messages.getString("EdgePane.priority.label")); //$NON-NLS-1$
        cons.gridx = 0;
        cons.gridy = 5;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = priorityField;
        cons.gridx = 1;
        cons.gridy = 5;
        cons.gridwidth = 2;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = 0;
        cons.weighty = 0;
        layout.setConstraints(c, cons);
        pane.add(c);

        c = Box.createGlue();
        cons.gridx = 0;
        cons.gridy = 6;
        cons.gridwidth = 3;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.EAST;
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
    private JToolBar createToolbar() {
        final JToolBar bar = new JToolBar();
        bar.add(deleteAction);
        return bar;
    }

    public Flowable<EdgeView> getBeginNodeFlowable() {
        return beginNodeFlowable;
    }

    public Flowable<EdgeView> getChangeFlowable() {
        return changeFlowable;
    }

    public Flowable<EdgeView> getDeleteFlowable() {
        return deleteFlowable;
    }

    public Flowable<EdgeView> getEndNodeFlowable() {
        return endNodeFlowable;
    }

    /**
     *
     */
    private void init() {
        setBorder(BorderFactory.createTitledBorder(Messages.getString("EdgePane.title"))); //$NON-NLS-1$
        final SwingUtils utils = SwingUtils.getInstance();
        utils.initButton(deleteAction, "EdgePane.deleteAction"); //$NON-NLS-1$
        utils.initButton(browseBeginNodeAction, "EdgePane.browseBeginNodeAction"); //$NON-NLS-1$
        utils.initButton(browseEndNodeAction, "EdgePane.browseEndNodeAction"); //$NON-NLS-1$

        priorityField.setHorizontalAlignment(SwingConstants.RIGHT);
        priorityField.setColumns(5);

        speedLimitField.setHorizontalAlignment(SwingConstants.RIGHT);
        speedLimitField.setColumns(5);
        incomeQueueField.setHorizontalAlignment(SwingConstants.RIGHT);
        incomeQueueField.setColumns(3);
        incomeQueueField.setEditable(false);

        distanceField.setHorizontalAlignment(SwingConstants.RIGHT);
        distanceField.setColumns(5);
        distanceField.setEditable(false);

        nameField.setEditable(false);

        beginField.setEditable(false);

        endField.setEditable(false);
    }

    /**
     * @param edgeModel the edge model of the panel
     */
    public void setEdge(final EdgeView edgeModel) {
        this.edgeModel = edgeModel;
        final MapEdge edge = edgeModel.getEdge();
        nameField.setText(edgeModel.getName());
        beginField.setText(edgeModel.getBeginName());
        endField.setText(edgeModel.getEndName());
        priorityField.setValue(edge.getPriority());
        speedLimitField.setValue(edge.getSpeedLimit() * 3.6);
        distanceField.setValue(edge.getDistance());
    }
}
