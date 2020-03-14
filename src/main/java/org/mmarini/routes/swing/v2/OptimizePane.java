/*
 * OptimizePane.java
 *
 * $Id: OptimizePane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 11/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing.v2;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mmarini.routes.model.v2.Constants;

/**
 * The panel with optimization parameters
 */
public class OptimizePane extends Box implements Constants {

	private static final long serialVersionUID = 1L;

	private final JCheckBox optimizeSpeed;
	private final JSpinner speedField;
	private final JLabel speedLabel;

	/** Creates the panel */
	public OptimizePane() {
		super(BoxLayout.PAGE_AXIS);
		optimizeSpeed = new JCheckBox(Messages.getString("OptimizePane.optimizeSpeed.label")); //$NON-NLS-1$
		speedLabel = new JLabel(Messages.getString("OptimizePane.speed.label")); //$NON-NLS-1$
		speedField = new JSpinner();
		speedField.setModel(new SpinnerNumberModel(130., 10., 300., 10.));
		optimizeSpeed.setSelected(true);
		speedField.setValue(130.);
		createContext();
	}

	/**
	 * Creates the content
	 *
	 * @return the panel
	 */
	private void createContext() {
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

	/** Returns the speed limit in meters/second */
	public double getSpeedLimit() {
		return ((Number) speedField.getValue()).doubleValue() * KMH_TO_MPS;
	}

	/** Returns true if speed optimization */
	public boolean isOptimizeSpeed() {
		return optimizeSpeed.isSelected();
	}

	/**
	 * Sets the speed limit for optimization
	 *
	 * @param speedLimit speed limit in meters/second
	 * @return the panel
	 */
	public OptimizePane setSpeedLimit(final double speedLimit) {
		speedField.setValue(speedLimit * MPS_TO_KMH);
		return this;
	}
}
