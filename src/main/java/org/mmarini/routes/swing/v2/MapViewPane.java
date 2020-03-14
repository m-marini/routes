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
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.mmarini.routes.model.v2.MapModule;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * Panel of map view that allows the user to view and interact with the map.
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
	private final JButton moduleSelector;
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
	private final Observable<MapModule> moduleModeObs;
	private Optional<MapModule> module;

	/**
	 * Creates the panel.
	 *
	 * @param content        the content
	 * @param moduleSelector the module selector
	 */
	public MapViewPane(final Component content, final JButton moduleSelector) {
		selectButton = createJToggleButton("MapViewPane.selectAction"); //$NON-NLS-1$
		edgeButton = createJToggleButton("MapViewPane.edgeAction"); //$NON-NLS-1$
		moduleButton = createJToggleButton("MapViewPane.moduleAction"); //$NON-NLS-1$
		zoomDefaultButton = createJButton("MapViewPane.zoomDefaultAction"); //$NON-NLS-1$
		zoomInButton = createJButton("MapViewPane.zoomInAction"); //$NON-NLS-1$
		zoomOutButton = createJButton("MapViewPane.zoomOutAction"); //$NON-NLS-1$
		fitInWindowAction = createJButton("MapViewPane.fitInWindowAction"); //$NON-NLS-1$
		normalViewButton = createJToggleButton("MapViewPane.normalViewAction"); //$NON-NLS-1$
		trafficViewButton = createJToggleButton("MapViewPane.trafficViewAction"); //$NON-NLS-1$
		this.moduleSelector = moduleSelector;
		toolGroup = new ButtonGroup();
		viewGroup = new ButtonGroup();
		normalViewButton.setSelected(true);
		module = Optional.empty();

		zoomDefaultObs = SwingObservable.actions(zoomDefaultButton);
		zoomInObs = SwingObservable.actions(zoomInButton);
		zoomOutObs = SwingObservable.actions(zoomOutButton);
		fitInWindowObs = SwingObservable.actions(fitInWindowAction);
		selectModeObs = SwingObservable.actions(selectButton);
		edgeModeObs = SwingObservable.actions(edgeButton);
		moduleModeObs = SwingObservable.actions(moduleButton).map(ev -> {
			return module;
		}).filter(m -> {
			return m.isPresent();
		}).map(m -> {
			return m.get();
		});
		normalViewObs = SwingObservable.actions(normalViewButton);
		trafficViewObs = SwingObservable.actions(trafficViewButton);
		init(content).setOpaque(false);
	}

	/**
	 * Returns the panel with content.
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
	 * Creates the tool bar.
	 *
	 * @return the tool bar
	 */
	private JToolBar createToolbar() {
		final JToolBar bar = new JToolBar();
		bar.add(selectButton);
		bar.add(edgeButton);
		bar.add(moduleButton);
		bar.add(moduleSelector);
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

	/** Returns the observable of edge mode button. */
	public Observable<ActionEvent> getEdgeModeObs() {
		return edgeModeObs;
	}

	/** Returns the observable of fit in window button. */
	public Observable<ActionEvent> getFitInWindowObs() {
		return fitInWindowObs;
	}

	/** Returns the observable of module mode button. */
	public Observable<MapModule> getModuleModeObs() {
		return moduleModeObs;
	}

	/** Returns the observable of normal view button. */
	public Observable<ActionEvent> getNormalViewObs() {
		return normalViewObs;
	}

	/** Returns the observable of select mode button. */
	public Observable<ActionEvent> getSelectModeObs() {
		return selectModeObs;
	}

	/** Return the observable of traffic view button. */
	public Observable<ActionEvent> getTrafficViewObs() {
		return trafficViewObs;
	}

	/** Return the observable of zoom to default button. */
	public Observable<ActionEvent> getZoomDefaultObs() {
		return zoomDefaultObs;
	}

	/** Returns the observable of zoom in button. */
	public Observable<ActionEvent> getZoomInObs() {
		return zoomInObs;
	}

	/** Returns the observable of zoom out button . */
	public Observable<ActionEvent> getZoomOutObs() {
		return zoomOutObs;
	}

	/**
	 * Initializes the component.
	 *
	 * @param content the content
	 * @return the panel
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

	/**
	 * Sets the module selection mode.
	 *
	 * @return the panel
	 */
	public MapViewPane selectModuleMode() {
		moduleButton.doClick();
		return this;
	}

	/**
	 * Sets the module.
	 *
	 * @param module the module
	 * @return the panel
	 */
	public MapViewPane setModule(final MapModule module) {
		this.module = Optional.of(module);
		return this;
	}

	/**
	 * Sets the module icon.
	 *
	 * @param icon the icon
	 * @return the panel
	 */
	public MapViewPane setModuleIcon(final Icon icon) {
		moduleButton.setIcon(icon);
		return this;
	}
}
