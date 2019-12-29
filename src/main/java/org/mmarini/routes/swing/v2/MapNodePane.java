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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

/**
 *
 */
public class MapNodePane extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JTextField nameField;
	private final JFormattedTextField xField;
	private final JFormattedTextField yField;

	private final JLabel nameLabel;
	private final JLabel xLabel;
	private final JLabel yLabel;

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

		setBorder(BorderFactory.createTitledBorder(Messages.getString("MapNodePane.title"))); //$NON-NLS-1$

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
	 * Returns the map node panel with content
	 */
	private MapNodePane createContent() {
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		add(createInfoPane(), BorderLayout.CENTER);
		return this;
	}

	/**
	 * Returns the info panel
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
	 * Returns the tool bar
	 */
	private JComponent createToolBar() {
		final JToolBar toolbar = new JToolBar();
		final JButton changeButton = createJButton("MapNodePane.changeAction"); //$NON-NLS-1$
		final JButton deleteButton = createJButton("MapNodePane.deleteAction"); //$NON-NLS-1$

		toolbar.add(changeButton);
		toolbar.add(deleteButton);
		return toolbar;
	}
}
