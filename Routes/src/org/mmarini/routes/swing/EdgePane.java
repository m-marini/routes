/*
 * SiteNodePane.java
 *
 * $Id: EdgePane.java,v 1.11 2010/10/19 20:32:59 marco Exp $
 *
 * 05/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.mmarini.routes.model.MapEdge;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: EdgePane.java,v 1.11 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class EdgePane extends JPanel {
	private static final long serialVersionUID = 1L;

	private MapEdge edge;

	private JFormattedTextField priorityField;

	private JFormattedTextField speedLimitField;

	private JFormattedTextField incomeQueueField;

	private JFormattedTextField distanceField;

	private JTextField nameField;

	private JTextField beginField;

	private JTextField endField;

	private Action deleteAction;

	private RouteMediator mediator;

	private Action browseBeginNodeAction;

	private Action browseEndNodeAction;

	/**
         * 
         */
	public EdgePane() {
		priorityField = new JFormattedTextField(new NumberFormatter(
				NumberFormat.getIntegerInstance()));
		speedLimitField = new JFormattedTextField(new NumberFormatter(
				NumberFormat.getNumberInstance()));
		incomeQueueField = new JFormattedTextField(new NumberFormatter(
				NumberFormat.getIntegerInstance()));
		distanceField = new JFormattedTextField(new NumberFormatter(
				NumberFormat.getNumberInstance()));
		nameField = new JTextField(20);
		beginField = new JTextField(10);
		endField = new JTextField(10);

		deleteAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				delete();
			}

		};
		browseBeginNodeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				handleBrowseBegin();
			}

		};
		browseEndNodeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				handleBrowseEnd();
			}

		};
		init();
		createContent();
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
	 * @return
	 */
	private Component createInfoPane() {
		JPanel pane = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		pane.setLayout(layout);
		GridBagConstraints cons = new GridBagConstraints();
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

		JButton btn = new JButton(browseBeginNodeAction);
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

		btn = new JButton(browseEndNodeAction);
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
	 * @return
	 */
	private JToolBar createToolbar() {
		JToolBar bar = new JToolBar();
		bar.add(deleteAction);
		return bar;
	}

	/**
         * 
         */
	protected void delete() {
		mediator.remove(edge);
	}

	/**
         * 
         */
	protected void handleBrowseBegin() {
		mediator.changeBeginNode(edge);
	}

	/**
         * 
         */
	protected void handleBrowseEnd() {
		mediator.changeEndNode(edge);
	}

	/**
         * 
         */
	private void init() {
		setBorder(BorderFactory.createTitledBorder(Messages
				.getString("EdgePane.title"))); //$NON-NLS-1$
		SwingUtils utils = SwingUtils.getInstance();
		utils.initAction(deleteAction, "EdgePane.deleteAction"); //$NON-NLS-1$
		utils.initAction(browseBeginNodeAction,
				"EdgePane.browseBeginNodeAction"); //$NON-NLS-1$
		utils.initAction(browseEndNodeAction, "EdgePane.browseEndNodeAction"); //$NON-NLS-1$

		priorityField.setHorizontalAlignment(SwingConstants.RIGHT);
		priorityField.setColumns(5);
		priorityField.addPropertyChangeListener("value", //$NON-NLS-1$
				new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						setPriority(((Number) evt.getNewValue()).intValue());
					}
				});

		speedLimitField.setHorizontalAlignment(SwingConstants.RIGHT);
		speedLimitField.setColumns(5);
		speedLimitField.addPropertyChangeListener("value", //$NON-NLS-1$
				new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						setSpeedLimit(((Number) evt.getNewValue())
								.doubleValue() / 3.6f);
					}
				});
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
	 * @param edge
	 *            the edge to set
	 */
	public void setEdge(MapEdge edge) {
		this.edge = edge;
		priorityField.setValue(edge.getPriority());
		speedLimitField.setValue(edge.getSpeedLimit() * 3.6);
		beginField.setText(mediator.retrieveNodeName(edge.getBegin()));
		endField.setText(mediator.retrieveNodeName(edge.getEnd()));
		distanceField.setValue(edge.getDistance());
		nameField.setText(mediator.retrieveEdgeName(edge));
	}

	/**
	 * @param mediator
	 *            the mediator to set
	 */
	public void setMediator(RouteMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @param priority
	 */
	protected void setPriority(int priority) {
		mediator.changePriority(edge, priority);
	}

	/**
	 * @param speedLimit
	 */
	protected void setSpeedLimit(double speedLimit) {
		mediator.changeSpeedLimit(edge, speedLimit);
	}
}
