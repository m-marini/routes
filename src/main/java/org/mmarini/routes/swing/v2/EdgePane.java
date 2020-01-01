//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.swing.v2;

import static org.mmarini.routes.swing.v2.SwingUtils.createJButton;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;

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

import org.mmarini.routes.model.v2.MapEdge;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 *
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
	private final JButton browseBeginNodeButton;
	private final JButton browseEndNodeButton;
	private final JButton deleteButton;
	private final Observable<ActionEvent> browseBeginObs;
	private final Observable<ActionEvent> browseEndObs;
	private final Observable<ActionEvent> deleteObs;
	private final Observable<PropertyChangeEvent> priorityObs;
	private final Observable<PropertyChangeEvent> speedLimitObs;

	/**
	 *
	 */
	public EdgePane() {
		priorityField = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance()));
		speedLimitField = new JFormattedTextField(new NumberFormatter(NumberFormat.getNumberInstance()));
		incomeQueueField = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance()));
		distanceField = new JFormattedTextField(new NumberFormatter(NumberFormat.getNumberInstance()));
		nameField = new JTextField(6);
		beginField = new JTextField(6);
		endField = new JTextField(6);
		browseBeginNodeButton = createJButton("EdgePane.browseBeginNodeAction"); //$NON-NLS-1$
		browseEndNodeButton = createJButton("EdgePane.browseEndNodeAction"); //$NON-NLS-1$
		deleteButton = createJButton("EdgePane.deleteAction"); //$NON-NLS-1$

		browseBeginObs = SwingObservable.actions(browseBeginNodeButton);
		browseEndObs = SwingObservable.actions(browseBeginNodeButton);
		deleteObs = SwingObservable.actions(deleteButton);
		priorityObs = SwingObservable.propertyChange(priorityField, "value");
		speedLimitObs = SwingObservable.propertyChange(speedLimitField, "value");
		init().createContent();
	}

	/**
	 * Returns the edge panel with content
	 */
	private EdgePane createContent() {
		setLayout(new BorderLayout());
		add(createToolbar(), BorderLayout.NORTH);
		add(createInfoPane(), BorderLayout.CENTER);
		return this;
	}

	/**
	 * @return
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

		browseBeginNodeButton.setBorder(BorderFactory.createEmptyBorder());
		c = browseBeginNodeButton;
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

		browseEndNodeButton.setBorder(BorderFactory.createEmptyBorder());
		c = browseEndNodeButton;
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
		final JToolBar bar = new JToolBar();
		final JButton deleteButton = createJButton("EdgePane.deleteAction"); //$NON-NLS-1$
		bar.add(deleteButton);
		return bar;
	}

	/**
	 * @return the browseBeginObs
	 */
	public Observable<ActionEvent> getBrowseBeginObs() {
		return browseBeginObs;
	}

	/**
	 * @return the browseEndObs
	 */
	public Observable<ActionEvent> getBrowseEndObs() {
		return browseEndObs;
	}

	/**
	 * @return the deleteObs
	 */
	public Observable<ActionEvent> getDeleteObs() {
		return deleteObs;
	}

	/**
	 * @return the priorityObs
	 */
	public Observable<PropertyChangeEvent> getPriorityObs() {
		return priorityObs;
	}

	/**
	 * @return the speedLimitObs
	 */
	public Observable<PropertyChangeEvent> getSpeedLimitObs() {
		return speedLimitObs;
	}

	/**
	 * @return
	 *
	 */
	private EdgePane init() {
		setBorder(BorderFactory.createTitledBorder(Messages.getString("EdgePane.title"))); //$NON-NLS-1$

		priorityField.setHorizontalAlignment(SwingConstants.RIGHT);
		priorityField.setColumns(5);
//		priorityField.addPropertyChangeListener("value", //$NON-NLS-1$
//				new PropertyChangeListener() {
//
//					@Override
//					public void propertyChange(final PropertyChangeEvent evt) {
//						setPriority(((Number) evt.getNewValue()).intValue());
//					}
//				});

		speedLimitField.setHorizontalAlignment(SwingConstants.RIGHT);
		speedLimitField.setColumns(5);
//		speedLimitField.addPropertyChangeListener("value", //$NON-NLS-1$
//				new PropertyChangeListener() {
//
//					@Override
//					public void propertyChange(final PropertyChangeEvent evt) {
//						setSpeedLimit(((Number) evt.getNewValue()).doubleValue() / 3.6f);
//					}
//				});
		incomeQueueField.setHorizontalAlignment(SwingConstants.RIGHT);
		incomeQueueField.setColumns(3);
		incomeQueueField.setEditable(false);

		distanceField.setHorizontalAlignment(SwingConstants.RIGHT);
		distanceField.setColumns(5);
		distanceField.setEditable(false);

		nameField.setEditable(false);
		beginField.setEditable(false);
		endField.setEditable(false);

		return this;
	}

	/**
	 *
	 * @param edge
	 */
	public EdgePane setEdge(final MapEdge edge) {
		priorityField.setValue(edge.getPriority());
		speedLimitField.setValue(edge.getSpeedLimit() * 3.6);
		beginField.setText(edge.getBegin().getShortName());
		endField.setText(edge.getEnd().getShortName());
		distanceField.setValue(edge.getDistance());
		nameField.setText(edge.getShortName());
		return this;
	}
}
