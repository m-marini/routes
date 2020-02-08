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
import static org.mmarini.routes.swing.v2.SwingUtils.createJToggleButton;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * The MapViewPane allows the user to view and interact with the map.
 * <p>
 * It contains a tool bar to select the tool to change the map or the view scale
 * or view mode.
 * </p>
 * <p>
 * The tools to change the map allow to select elements, insert edges, insert
 * modules or set the center of the map.
 * </p>
 */
public class MapViewPane extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JToggleButton selectButton;
	private final JToggleButton edgeButton;
	private final JToggleButton moduleButton;
	private final ButtonGroup toolGroup;
	private final JToggleButton normalViewButton;
	private final JToggleButton trafficViewButton;
	private final ButtonGroup viewGroup;
	private final JButton zoomDefaultButton;
	private final JButton zoomInButton;
	private final JButton zoomOutButton;
	private final JButton fitInWindowAction;
	private final Observable<ActionEvent> zoomInObs;
	private final Observable<ActionEvent> zoomOutObs;
	private final Observable<ActionEvent> fitInWindowObs;
	private final Observable<ActionEvent> zoomDefaultObs;
	private final Observable<ActionEvent> selectModeObs;
	private final Observable<ActionEvent> edgeModeObs;
	private final Observable<ActionEvent> normalViewObs;
	private final Observable<ActionEvent> trafficViewObs;

	/**
	 * Create the component
	 */
	public MapViewPane(final Component content) {
		selectButton = createJToggleButton("MapViewPane.selectAction"); //$NON-NLS-1$
		edgeButton = createJToggleButton("MapViewPane.edgeAction"); //$NON-NLS-1$
		moduleButton = createJToggleButton("MapViewPane.moduleAction"); //$NON-NLS-1$
		zoomDefaultButton = createJButton("MapViewPane.zoomDefaultAction"); //$NON-NLS-1$
		zoomInButton = createJButton("MapViewPane.zoomInAction"); //$NON-NLS-1$
		zoomOutButton = createJButton("MapViewPane.zoomOutAction"); //$NON-NLS-1$
		fitInWindowAction = createJButton("MapViewPane.fitInWindowAction"); //$NON-NLS-1$
		normalViewButton = createJToggleButton("MapViewPane.normalViewAction"); //$NON-NLS-1$
		trafficViewButton = createJToggleButton("MapViewPane.trafficViewAction"); //$NON-NLS-1$
		toolGroup = new ButtonGroup();
		viewGroup = new ButtonGroup();
		normalViewButton.setSelected(true);

		zoomDefaultObs = SwingObservable.actions(zoomDefaultButton);
		zoomInObs = SwingObservable.actions(zoomInButton);
		zoomOutObs = SwingObservable.actions(zoomOutButton);
		fitInWindowObs = SwingObservable.actions(fitInWindowAction);
		selectModeObs = SwingObservable.actions(selectButton);
		edgeModeObs = SwingObservable.actions(edgeButton);
		normalViewObs = SwingObservable.actions(normalViewButton);
		trafficViewObs = SwingObservable.actions(trafficViewButton);
		init(content);
		setOpaque(false);
	}

	/**
	 * Returns the panel with content
	 *
	 * @param content the content
	 */
	private MapViewPane createContent(final Component content) {
		setLayout(new BorderLayout());
		add(createToolbar(), BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
		return this;
	}

	/**
	 * Create the tool bar
	 *
	 * @return the tool bar
	 */
	private JToolBar createToolbar() {
		final JToolBar bar = new JToolBar();
		bar.add(selectButton);
		bar.add(edgeButton);
		bar.add(moduleButton);
		bar.add(new JSeparator(SwingConstants.VERTICAL));

		bar.add(zoomDefaultButton);
		bar.add(zoomInButton);
		bar.add(zoomOutButton);
		bar.add(fitInWindowAction);

		bar.add(new JSeparator(SwingConstants.VERTICAL));
		bar.add(normalViewButton);
		bar.add(trafficViewButton);
		return bar;
	}

	/**
	 * @return the edgeModeObs
	 */
	public Observable<ActionEvent> getEdgeModeObs() {
		return edgeModeObs;
	}

	/**
	 * @return the fitInWindowObs
	 */
	public Observable<ActionEvent> getFitInWindowObs() {
		return fitInWindowObs;
	}

	/**
	 * @return the normalViewObs
	 */
	public Observable<ActionEvent> getNormalViewObs() {
		return normalViewObs;
	}

	/**
	 * @return the selectModeObs
	 */
	public Observable<ActionEvent> getSelectModeObs() {
		return selectModeObs;
	}

	/**
	 * @return the trafficViewObs
	 */
	public Observable<ActionEvent> getTrafficViewObs() {
		return trafficViewObs;
	}

	/**
	 * @return the zoomDefaultObs
	 */
	public Observable<ActionEvent> getZoomDefaultObs() {
		return zoomDefaultObs;
	}

	/**
	 * @return the zoomInObs
	 */
	public Observable<ActionEvent> getZoomInObs() {
		return zoomInObs;
	}

	/**
	 * @return the zoomOutObs
	 */
	public Observable<ActionEvent> getZoomOutObs() {
		return zoomOutObs;
	}

	/**
	 * Initialize the component
	 *
	 * @param content
	 * @return
	 */
	private MapViewPane init(final Component content) {
		toolGroup.add(selectButton);
		toolGroup.add(edgeButton);
		toolGroup.add(moduleButton);

		viewGroup.add(normalViewButton);
		viewGroup.add(trafficViewButton);

		createContent(content);
		return this;
	}
}
