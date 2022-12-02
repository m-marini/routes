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

import static hu.akarnokd.rxjava3.swing.SwingObservable.change;
import static org.mmarini.routes.model2.Constants.DEFAULT_SPEED_LIMIT_KMH;
import static org.mmarini.routes.model2.Constants.KMPHSPM;

/**
 * @author marco.marini@mmarini.org
 */
public class OptimizePane extends Box {

    public static final double MINIMUM_SPEED_LIMIT_KMH = 10.;
    public static final double MAXIMUM_SPEED_LIMIT_KMH = 300.;
    public static final double STEP_SPEED_LIMIT_KMH = 10.;
    private static final long serialVersionUID = 1L;
    private final JCheckBox optimizeSpeed;
    private final JSpinner speedField;
    private final JLabel speedLabel;

    /**
     *
     */
    public OptimizePane() {
        super(BoxLayout.PAGE_AXIS);
        optimizeSpeed = new JCheckBox(Messages.getString("OptimizePane.optimizeSpeed.label")); //$NON-NLS-1$
        speedLabel = new JLabel(Messages.getString("OptimizePane.speed.label")); //$NON-NLS-1$
        speedField = new JSpinner();

        init();
        createContent();
        createFlows();
    }

    /**
     *
     */
    private void createContent() {
        Box box = Box.createHorizontalBox();
        box.add(optimizeSpeed);
        box.add(Box.createGlue());
        add(box);

        box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder(Messages.getString("OptimizePane.speedPane.title"))); //$NON-NLS-1$
        box.add(speedLabel);
        box.add(speedField);
        box.add(Box.createGlue());
        add(box);
    }

    /**
     *
     */
    private void createFlows() {
        change(optimizeSpeed)
                .doOnNext(e -> handleOptimizeSpeedChange())
                .subscribe();
    }

    /**
     *
     */
    public double getSpeedLimit() {
        return ((Number) speedField.getValue()).doubleValue() / KMPHSPM;
    }

    /**
     *
     */
    public OptimizePane setSpeedLimit(double speedLimit) {
        speedField.setValue(speedLimit * KMPHSPM);
        return this;
    }

    /**
     *
     */
    private void handleOptimizeSpeedChange() {
        final boolean selected = isOptimizeSpeed();
        speedLabel.setEnabled(selected);
        speedField.setEnabled(selected);
    }

    /**
     *
     */
    private void init() {
        speedField.setModel(new SpinnerNumberModel(DEFAULT_SPEED_LIMIT_KMH, MINIMUM_SPEED_LIMIT_KMH, MAXIMUM_SPEED_LIMIT_KMH, STEP_SPEED_LIMIT_KMH));
        optimizeSpeed.setSelected(true);
        speedField.setValue(DEFAULT_SPEED_LIMIT_KMH);
    }

    /**
     *
     */
    public boolean isOptimizeSpeed() {
        return optimizeSpeed.isSelected();
    }
}
