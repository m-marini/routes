/*
 * OptimizePane.java
 *
 * $Id: OptimizePane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 11/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import javax.swing.*;

import static hu.akarnokd.rxjava3.swing.SwingObservable.change;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: OptimizePane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 */
public class OptimizePane extends Box {

    private static final long serialVersionUID = 1L;
    private final JCheckBox optimizeSpeed;
    private final JCheckBox optimizeNodes;
    private final JSpinner speedField;
    private final JLabel speedLabel;

    /**
     *
     */
    public OptimizePane() {
        super(BoxLayout.PAGE_AXIS);
        optimizeSpeed = new JCheckBox(Messages.getString("OptimizePane.optimizeSpeed.label")); //$NON-NLS-1$
        optimizeNodes = new JCheckBox(Messages.getString("OptimizePane.optimizeNodes.label")); //$NON-NLS-1$
        speedLabel = new JLabel(Messages.getString("OptimizePane.speed.label")); //$NON-NLS-1$
        speedField = new JSpinner();

        speedField.setModel(new SpinnerNumberModel(130., 10., 300., 10.));

		/*
		optimizeSpeed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				handleOptimizeSpeedChange();
			}

		});
		*/
        optimizeSpeed.setSelected(true);
        optimizeNodes.setSelected(true);
        speedField.setValue(130.);
        createContext();
        change(optimizeSpeed)
                .doOnNext(e -> handleOptimizeSpeedChange())
                .subscribe();
    }

    /**
     *
     */
    private void createContext() {
        Box box = Box.createHorizontalBox();
        box.add(optimizeNodes);
        box.add(Box.createGlue());
        add(box);

        box = Box.createHorizontalBox();
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
     * @return
     */
    public double getSpeedLimit() {
        return ((Number) speedField.getValue()).doubleValue() / 3.6f;
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
     * @return
     */
    public boolean isOptimizeNodes() {
        return optimizeNodes.isSelected();
    }

    /**
     * @return
     */
    public boolean isOptimizeSpeed() {
        return optimizeSpeed.isSelected();
    }
}
