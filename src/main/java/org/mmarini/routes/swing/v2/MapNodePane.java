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

import static org.mmarini.routes.swing.v2.SwingUtils.createFieldConstraints;
import static org.mmarini.routes.swing.v2.SwingUtils.createGridConstraints;
import static org.mmarini.routes.swing.v2.SwingUtils.createJButton;
import static org.mmarini.routes.swing.v2.SwingUtils.createLabelConstraints;
import static org.mmarini.routes.swing.v2.SwingUtils.withGridBagConstraints;

import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.util.Optional;

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

import org.mmarini.routes.model.v2.MapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * Panel with the information of a node and its user actions.
 */
public class MapNodePane extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(MapNodePane.class);

	private final JTextField nameField;
	private final JFormattedTextField xField;
	private final JFormattedTextField yField;
	private final JButton changeButton;
	private final JButton deleteButton;
	private final Observable<MapNode> changeObs;
	private final Observable<MapNode> deleteObs;
	private Optional<MapNode> node;

	/** Creates the panel. */
	public MapNodePane() {
		nameField = new JTextField(6);
		xField = new JFormattedTextField(new NumberFormatter());
		yField = new JFormattedTextField(new NumberFormatter());
		changeButton = createJButton("MapNodePane.changeAction"); //$NON-NLS-1$
		deleteButton = createJButton("MapNodePane.deleteAction"); //$NON-NLS-1$
		node = Optional.empty();
		changeObs = SwingObservable.actions(changeButton).map(ev -> getNode()).filter(node -> node.isPresent())
				.map(ed -> ed.get()).doOnNext(ev -> logger.debug("on next change {}", ev));
		deleteObs = SwingObservable.actions(deleteButton).map(ev -> getNode()).filter(node -> node.isPresent())
				.map(ed -> ed.get()).doOnNext(ev -> logger.debug("on next delete {}", ev));

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
	 * Creates the content.
	 *
	 * @return the panel
	 */
	private MapNodePane createContent() {
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		add(createInfoPane(), BorderLayout.CENTER);
		return this;
	}

	/** Returns the info panel. */
	private JComponent createInfoPane() {
		final JPanel pane = withGridBagConstraints(new JPanel())
				.add(new JLabel(Messages.getString("MapNodePane.name.label")), //$NON-NLS-1$
						createLabelConstraints(0, 0, 1, 1).build())
				.add(nameField, createFieldConstraints(1, 0, 1, 1).build())
				.add(Box.createGlue(), createGridConstraints(2, 0, 1, 4).weightx(1).inset(2).build())
				.add(new JLabel(Messages.getString("MapNodePane.x.label")), //$NON-NLS-1$
						createLabelConstraints(0, 1, 1, 1).weightx(1).build())
				.add(xField, createFieldConstraints(1, 1, 1, 1).build())
				.add(new JLabel(Messages.getString("MapNodePane.y.label")), //$NON-NLS-1$
						createLabelConstraints(0, 2, 1, 1).weightx(1).build())
				.add(yField, createFieldConstraints(1, 2, 1, 1).build())
				.add(Box.createGlue(), createGridConstraints(0, 3, 2, 1).weighty(1).inset(2).build()).getContainer();
		return pane;
	}

	/** Returns the tool bar. */
	private JComponent createToolBar() {
		final JToolBar toolbar = new JToolBar();

		toolbar.add(changeButton);
		toolbar.add(deleteButton);
		return toolbar;
	}

	/** Returns the observable of change node button. */
	public Observable<MapNode> getChangeObs() {
		return changeObs;
	}

	/** Returns the observable of delete node button. */
	public Observable<MapNode> getDeleteObs() {
		return deleteObs;
	}

	/** Returns the node. */
	Optional<MapNode> getNode() {
		return node;
	}

	/**
	 * Sets the shown node.
	 *
	 * @param node the node
	 * @return the panel
	 */
	public MapNodePane setNode(final MapNode node) {
		this.node = Optional.of(node);
		final Point2D location = node.getLocation();
		xField.setValue(location.getX());
		yField.setValue(location.getY());
		nameField.setText(node.getShortName());
		return this;
	}
}
