/*
 * SiteNodePane.java
 *
 * $Id: MapNodePane.java,v 1.9 2010/10/19 20:32:59 marco Exp $
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
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.mmarini.routes.model.MapNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapNodePane.java,v 1.9 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class MapNodePane extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTextField nameField;

	private JFormattedTextField xField;

	private JFormattedTextField yField;

	private MapNode node;

	private Action changeAction;

	private Action deleteAction;

	private RouteMediator mediator;

	private JLabel nameLabel;

	private JLabel xLabel;

	private JLabel yLabel;

	/**
         * 
         */
	public MapNodePane() {
		nameField = new JTextField(10);
		xField = new JFormattedTextField(new NumberFormatter());
		yField = new JFormattedTextField(new NumberFormatter());
		nameLabel = new JLabel(Messages.getString("MapNodePane.name.label")); //$NON-NLS-1$
		xLabel = new JLabel(Messages.getString("MapNodePane.x.label")); //$NON-NLS-1$
		yLabel = new JLabel(Messages.getString("MapNodePane.y.label")); //$NON-NLS-1$

		changeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				change();
			}

		};
		deleteAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				delete();
			}

		};

		SwingUtils utils = SwingUtils.getInstance();
		utils.initAction(changeAction, "MapNodePane.changeAction"); //$NON-NLS-1$
		utils.initAction(deleteAction, "MapNodePane.deleteAction"); //$NON-NLS-1$

		setBorder(BorderFactory.createTitledBorder(Messages
				.getString("MapNodePane.title"))); //$NON-NLS-1$

		nameField.setEditable(false);
		xField.setEditable(false);
		xField.setColumns(5);
		xField.setHorizontalAlignment(SwingConstants.RIGHT);
		yField.setEditable(false);
		yField.setColumns(5);
		yField.setHorizontalAlignment(SwingConstants.RIGHT);

		createContent();
	}

	/**
         * 
         */
	protected void change() {
		mediator.transformToSite(node);
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
	 * @return
	 */
	private JComponent createInfoPane() {
		JPanel pane = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		pane.setLayout(layout);
		GridBagConstraints cons = new GridBagConstraints();
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
		cons.gridheight = 4;
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
	 * @return
	 */
	private JComponent createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.add(changeAction);
		toolbar.add(deleteAction);
		return toolbar;
	}

	/**
         * 
         */
	protected void delete() {
		mediator.remove(node);
	}

	/**
	 * @param mediator
	 *            the mediator to set
	 */
	public void setMediator(RouteMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(MapNode node) {
		this.node = node;
		Point2D location = node.getLocation();
		xField.setValue(location.getX());
		yField.setValue(location.getY());
		nameField.setText(mediator.retrieveNodeName(node));
	}
}
