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
import java.awt.Component;
import java.awt.Container;
import java.text.NumberFormat;
import java.util.Optional;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Panel with edge information and user actions.
 */
public class EdgePane extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(EdgePane.class);

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
	private final Flowable<Object> browseBeginFlow;
	private final Flowable<MapEdge> browseEndFlow;
	private final Flowable<MapEdge> deleteFlow;
	private final Flowable<Integer> priorityFlow;
	private final Flowable<Double> speedLimitFlow;
	private Optional<MapEdge> edge;

	/** Creates an edge panel. */
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

		browseBeginFlow = SwingUtils.actions(browseBeginNodeButton).map(ev -> getEdge()).filter(ed -> ed.isPresent())
				.map(ed -> ed.get());
		browseEndFlow = SwingUtils.actions(browseBeginNodeButton).map(ev -> getEdge()).filter(ed -> ed.isPresent())
				.map(ed -> ed.get());
		deleteFlow = SwingUtils.actions(deleteButton).map(ev -> getEdge()).filter(ed -> ed.isPresent())
				.map(ed -> ed.get()).doOnNext(ev -> logger.debug("on next delete {}", ev));
		init().createContent();
		priorityFlow = SwingUtils.<Number>value(priorityField).map(v -> v.intValue());
		speedLimitFlow = SwingUtils.<Number>value(speedLimitField).map(v -> v.doubleValue());
	}

	/** Returns the edge panel with content. */
	private EdgePane createContent() {
		setLayout(new BorderLayout());
		add(createToolbar(), BorderLayout.NORTH);
		add(createInfoPane(), BorderLayout.CENTER);
		return this;
	}

	/** Returns the info panel. */
	private Component createInfoPane() {
		browseBeginNodeButton.setBorder(BorderFactory.createEmptyBorder());
		browseEndNodeButton.setBorder(BorderFactory.createEmptyBorder());

		final Container pane = withGridBagConstraints(new JPanel())
				.add(new JLabel(Messages.getString("EdgePane.name.label")), //$NON-NLS-1$
						createLabelConstraints(0, 0, 1, 1).build())
				.add(nameField, createFieldConstraints(1, 0, 1, 1).build())
				.add(Box.createGlue(), createGridConstraints(3, 0, 1, 1).weightx(1).inset(2).build())
				.add(new JLabel(Messages.getString("EdgePane.begin.label")), //$NON-NLS-1$
						createLabelConstraints(0, 1, 1, 1).weightx(1).build())
				.add(beginField, createFieldConstraints(1, 1, 1, 1).build())
				.add(browseBeginNodeButton, createGridConstraints().grid(2, 1, 1, 1).west().inset(2).build())
				.add(new JLabel(Messages.getString("EdgePane.end.label")), //$NON-NLS-1$
						createLabelConstraints(0, 2, 1, 1).build())
				.add(endField, createFieldConstraints(1, 2, 1, 1).build())
				.add(browseEndNodeButton, createGridConstraints().grid(2, 2, 1, 1).west().inset(2).build())
				.add(new JLabel(Messages.getString("EdgePane.distance.label")), //$NON-NLS-1$
						createLabelConstraints(0, 3, 1, 1).build())
				.add(distanceField, createFieldConstraints(1, 3, 1, 1).build())
				.add(new JLabel(Messages.getString("EdgePane.speedLimit.label")), //$NON-NLS-1$
						createLabelConstraints(0, 4, 1, 1).build())
				.add(speedLimitField, createFieldConstraints(1, 4, 1, 1).build())
				.add(new JLabel(Messages.getString("EdgePane.priority.label")), //$NON-NLS-1$
						createLabelConstraints(0, 5, 1, 1).build())
				.add(priorityField, createFieldConstraints(1, 5, 1, 1).build())
				.add(Box.createGlue(), createGridConstraints(0, 6, 3, 1).weighty(1).inset(2).build()).getContainer();
		return pane;
	}

	/** Returns the tool bar. */
	private JToolBar createToolbar() {
		final JToolBar bar = new JToolBar();
		bar.add(deleteButton);
		return bar;
	}

	/** Returns the flowable of begin node button selection. */
	public Flowable<Object> getBrowseBeginFlow() {
		return browseBeginFlow;
	}

	/** Returns the flowable of end node button selection. */
	public Flowable<MapEdge> getBrowseEndFlow() {
		return browseEndFlow;
	}

	/** Returns the flowable of delete edge button selection. */
	public Flowable<MapEdge> getDeleteFlow() {
		return deleteFlow;
	}

	/** Returns the edge. */
	Optional<MapEdge> getEdge() {
		return edge;
	}

	/** Returns the flowable of priority field changes. */
	public Flowable<Integer> getPriorityFlow() {
		return priorityFlow;
	}

	/** Returns the flowable of speed limit field changes. */
	public Flowable<Double> getSpeedLimitFlow() {
		return speedLimitFlow;
	}

	/**
	 * Initializes the panel.
	 *
	 * @return the panel
	 */
	private EdgePane init() {
		setBorder(BorderFactory.createTitledBorder(Messages.getString("EdgePane.title"))); //$NON-NLS-1$

		priorityField.setHorizontalAlignment(SwingConstants.RIGHT);
		priorityField.setColumns(5);

		speedLimitField.setHorizontalAlignment(SwingConstants.RIGHT);
		speedLimitField.setColumns(5);
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
	 * Sets the edge to manage.
	 *
	 * @param edge
	 * @return the panel
	 */
	public EdgePane setEdge(final MapEdge edge) {
		this.edge = Optional.ofNullable(edge);
		priorityField.setValue(edge.getPriority());
		speedLimitField.setValue(edge.getSpeedLimit() * 3.6);
		beginField.setText(edge.getBegin().getShortName());
		endField.setText(edge.getEnd().getShortName());
		distanceField.setValue(edge.getLength());
		nameField.setText(edge.getShortName());
		return this;
	}
}
