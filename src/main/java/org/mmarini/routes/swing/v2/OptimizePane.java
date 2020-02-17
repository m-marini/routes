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
 * @author marco.marini@mmarini.org
 * @version $Id: OptimizePane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 */
public class OptimizePane extends Box implements Constants {

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
		speedField.setModel(new SpinnerNumberModel(130., 10., 300., 10.));
		optimizeSpeed.setSelected(true);
		speedField.setValue(130.);
		createContext();
	}

	/**
	     *
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

	/**
	 * Returns the speed limit in MPS
	 */
	public double getSpeedLimit() {
		return ((Number) speedField.getValue()).doubleValue() * KMH_TO_MPS;
	}

	/**
	 *
	 * Returns true if speed optimization
	 */
	public boolean isOptimizeSpeed() {
		return optimizeSpeed.isSelected();
	}

	/**
	 *
	 * @param speedLimit speed limit in MPS
	 */
	public OptimizePane setSpeedLimit(final double speedLimit) {
		speedField.setValue(speedLimit * MPS_TO_KMH);
		return this;
	}
}
