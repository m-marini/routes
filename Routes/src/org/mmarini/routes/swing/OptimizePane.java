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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: OptimizePane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class OptimizePane extends Box {

	private static final long serialVersionUID = 1L;

	private JCheckBox optimizeSpeed;

	private JCheckBox optimizeNodes;

	private JSpinner speedField;

	private JLabel speedLabel;

	/**
         * 
         */
	public OptimizePane() {
		super(BoxLayout.PAGE_AXIS);
		optimizeSpeed = new JCheckBox(Messages
				.getString("OptimizePane.optimizeSpeed.label")); //$NON-NLS-1$
		optimizeNodes = new JCheckBox(Messages
				.getString("OptimizePane.optimizeNodes.label")); //$NON-NLS-1$
		speedLabel = new JLabel(Messages.getString("OptimizePane.speed.label")); //$NON-NLS-1$
		speedField = new JSpinner();

		speedField.setModel(new SpinnerNumberModel(130., 10., 300., 10.));

		optimizeSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				handleOptimizeSpeedChange();
			}

		});
		optimizeSpeed.setSelected(true);
		optimizeNodes.setSelected(true);
		speedField.setValue(130.);
		createContext();
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
		box.setBorder(BorderFactory.createTitledBorder(Messages
				.getString("OptimizePane.speedPane.title"))); //$NON-NLS-1$
		box.add(speedLabel);
		box.add(speedField);
		box.add(Box.createGlue());
		add(box);
	}

	/**
	 * 
	 * @return
	 */
	public double getSpeedLimit() {
		return ((Number) speedField.getValue()).doubleValue() / 3.6f;
	}

	/**
         * 
         * 
         */
	private void handleOptimizeSpeedChange() {
		boolean selected = isOptimizeSpeed();
		speedLabel.setEnabled(selected);
		speedField.setEnabled(selected);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isOptimizeNodes() {
		return optimizeNodes.isSelected();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isOptimizeSpeed() {
		return optimizeSpeed.isSelected();
	}
}
