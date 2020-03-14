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
import static org.mmarini.routes.swing.v2.SwingUtils.createJCheckBoxMenuItem;
import static org.mmarini.routes.swing.v2.SwingUtils.createJMenuItem;
import static org.mmarini.routes.swing.v2.SwingUtils.createJRadioButtonMenuItem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * Main frame of the simulation and application entry point.
 * <p>
 * It contains the menu actions, the route map panel, the edge panel and the
 * explorer panel.
 * </p>
 *
 */
public class MainFrame extends JFrame {
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

	private static final String IMAGES_ROUTES = "/images/routes.gif";
	private static final long serialVersionUID = 1L;

	/**
	 * Application entry point.
	 *
	 * @param args unused
	 */
	public static void main(final String[] args) throws Throwable {
		logger.info("MainFrame started.");
		new Controller();
	}

	private final JSplitPane splitPane;
	private final JSplitPane rightSplitPane;

	private final JMenuItem newMenuItem;
	private final JMenuItem newRandomMenuItem;
	private final JMenuItem openMenuItem;
	private final JMenuItem saveMenuItem;
	private final JMenuItem saveAsMenuItem;
	private final JMenuItem exitMenuItem;

	private final JMenuItem veicleInfosMenuItem;

	private final JMenuItem optimizeMenuItem;
	private final JMenuItem randomizeMenuItem;
	private final JMenuItem frequenceMenuItem;
	private final JMenuItem routesMenuItem;

	private final JCheckBoxMenuItem stopMenuItem;
	private final JRadioButtonMenuItem speedx1MenuItem;
	private final JRadioButtonMenuItem speedx2MenuItem;
	private final JRadioButtonMenuItem speedx5MenuItem;
	private final JRadioButtonMenuItem speedx10MenuItem;

	private final JButton newButton;
	private final JButton openButton;
	private final JButton saveButton;

	private final Observable<ActionEvent> newMapObs;
	private final Observable<ActionEvent> newRandomObs;
	private final Observable<ActionEvent> openMapObs;
	private final Observable<ActionEvent> saveMapObs;
	private final Observable<ActionEvent> saveMapAsObs;
	private final Observable<ActionEvent> exitObs;
	private final Observable<ActionEvent> vehicleInfoObs;
	private final Observable<ActionEvent> optimizeObs;
	private final Observable<ActionEvent> randomizeObs;
	private final Observable<ActionEvent> frequenceObs;
	private final Observable<ActionEvent> routesObs;
	private final Observable<ActionEvent> stopObs;
	private final Observable<Double> speedObs;

	/**
	 * Creates the main frame.
	 *
	 * @param left   the left component
	 * @param top    the top component
	 * @param bottom the bottom component
	 * @throws HeadlessException in case of error
	 */
	public MainFrame(final Component left, final Component top, final Component bottom) throws HeadlessException {
		this.splitPane = new JSplitPane();
		this.rightSplitPane = new JSplitPane();
		this.saveMenuItem = createJMenuItem("MainFrame.saveAction"); //$NON-NLS-1$

		this.optimizeMenuItem = createJMenuItem("MainFrame.optimizeAction"); //$NON-NLS-1$
		this.randomizeMenuItem = createJMenuItem("MainFrame.randomizeAction"); //$NON-NLS-1$
		this.frequenceMenuItem = createJMenuItem("MainFrame.frequenceAction"); //$NON-NLS-1$
		this.routesMenuItem = createJMenuItem("MainFrame.routesAction"); //$NON-NLS-1$
		this.stopMenuItem = createJCheckBoxMenuItem("MainFrame.stopAction"); //$NON-NLS-1$
		this.speedx1MenuItem = createJRadioButtonMenuItem("MainFrame.speedx1Action"); //$NON-NLS-1$
		this.speedx2MenuItem = createJRadioButtonMenuItem("MainFrame.speedx2Action"); //$NON-NLS-1$
		this.speedx5MenuItem = createJRadioButtonMenuItem("MainFrame.speedx5Action"); //$NON-NLS-1$
		this.speedx10MenuItem = createJRadioButtonMenuItem("MainFrame.speedx10Action"); //$NON-NLS-1$
		this.veicleInfosMenuItem = createJMenuItem("MainFrame.veicleInfosAction"); //$NON-NLS-1$

		this.saveButton = createJButton("MainFrame.saveAction");

		this.newMenuItem = createJMenuItem("MainFrame.newAction"); //$NON-NLS-1$
		this.exitMenuItem = createJMenuItem("MainFrame.exitAction"); //$NON-NLS-1$
		this.newRandomMenuItem = createJMenuItem("MainFrame.newRandomAction"); //$NON-NLS-1$
		this.openMenuItem = createJMenuItem("MainFrame.openAction"); //$NON-NLS-1$
		this.saveAsMenuItem = createJMenuItem("MainFrame.saveAsAction"); //$NON-NLS-1$
		this.newButton = createJButton("MainFrame.newAction");
		this.openButton = createJButton("MainFrame.openAction");

		this.newRandomObs = SwingObservable.actions(newRandomMenuItem);
		this.saveMapAsObs = SwingObservable.actions(saveAsMenuItem);
		this.exitObs = SwingObservable.actions(exitMenuItem);

		this.newMapObs = SwingObservable.actions(newButton).mergeWith(SwingObservable.actions(newMenuItem));
		this.openMapObs = SwingObservable.actions(openButton).mergeWith(SwingObservable.actions(openMenuItem));
		this.saveMapObs = SwingObservable.actions(saveButton).mergeWith(SwingObservable.actions(saveMenuItem));

		this.optimizeObs = SwingObservable.actions(optimizeMenuItem);
		this.frequenceObs = SwingObservable.actions(frequenceMenuItem);
		this.speedObs = SwingObservable.actions(speedx1MenuItem).map(ev -> {
			return 1.0;
		}).mergeWith(SwingObservable.actions(speedx2MenuItem).map(ev -> {
			return 2.0;
		})).mergeWith(SwingObservable.actions(speedx5MenuItem).map(ev -> {
			return 5.0;
		})).mergeWith(SwingObservable.actions(speedx10MenuItem).map(ev -> {
			return 10.0;
		}));
		this.stopObs = SwingObservable.actions(stopMenuItem);
		this.routesObs = SwingObservable.actions(routesMenuItem);
		this.randomizeObs = SwingObservable.actions(randomizeMenuItem);
		this.vehicleInfoObs = SwingObservable.actions(veicleInfosMenuItem);

		init(left, top, bottom);
	}

	/** Returns the main frame with the content. */
	private MainFrame createContent() {
		final Container pane = getContentPane();
		pane.setLayout(new BorderLayout());

		pane.add(createToolBar(), BorderLayout.NORTH);
		pane.add(splitPane, BorderLayout.CENTER);
		return this;
	}

	/** Returns the main frame with the menu bar. */
	private MainFrame createMenuBar() {
		final JMenuBar bar = new JMenuBar();
		final JMenu fileMenu = new JMenu(Messages.getString("MainFrame.fileMenu.text")); //$NON-NLS-1$

		fileMenu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.fileMenu.mnemonic").charAt(0))); //$NON-NLS-1$
		fileMenu.add(new JSeparator());
		fileMenu.add(newMenuItem);
		fileMenu.add(newRandomMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitMenuItem);

		final JMenu viewMenu = new JMenu(Messages.getString("MainFrame.viewMenu.text")); //$NON-NLS-1$
		viewMenu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.viewMenu.mnemonic").charAt(0))); //$NON-NLS-1$
		viewMenu.add(veicleInfosMenuItem);

		final JMenu optionMenu = new JMenu(Messages.getString("MainFrame.optionMenu.text")); //$NON-NLS-1$
		optionMenu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.optionMenu.mnemonic").charAt(0))); //$NON-NLS-1$
		optionMenu.add(optimizeMenuItem);
		optionMenu.add(randomizeMenuItem);
		optionMenu.add(frequenceMenuItem);
		optionMenu.add(routesMenuItem);
		optionMenu.add(new JSeparator());
		optionMenu.add(stopMenuItem);

		final ButtonGroup group = new ButtonGroup();
		optionMenu.add(speedx1MenuItem);
		group.add(speedx1MenuItem);
		optionMenu.add(speedx2MenuItem);
		group.add(speedx2MenuItem);
		optionMenu.add(speedx5MenuItem);
		group.add(speedx5MenuItem);
		optionMenu.add(speedx10MenuItem);
		group.add(speedx10MenuItem);

		speedx1MenuItem.setSelected(true);

		bar.add(fileMenu);
		bar.add(viewMenu);
		bar.add(optionMenu);
		setJMenuBar(bar);
		return this;
	}

	/** Returns the tool bar. */
	private Component createToolBar() {
		final JToolBar toolBar = new JToolBar();
		toolBar.add(newButton);
		toolBar.add(openButton);
		toolBar.add(saveButton);
		return toolBar;
	}

	/** Returns the observable of exit action. */
	public Observable<ActionEvent> getExitObs() {
		return exitObs;
	}

	/** Returns the observable of frequency action. */
	public Observable<ActionEvent> getFrequenceObs() {
		return frequenceObs;
	}

	/** Returns the observable of new map action. */
	public Observable<ActionEvent> getNewMapObs() {
		return newMapObs;
	}

	/** Returns the observable of new random map action. */
	public Observable<ActionEvent> getNewRandomObs() {
		return newRandomObs;
	}

	/** Returns the observable of open action. */
	public Observable<ActionEvent> getOpenMapObs() {
		return openMapObs;
	}

	/** Returns the observable of optimize action. */
	public Observable<ActionEvent> getOptimizeObs() {
		return optimizeObs;
	}

	/** Returns the observable of randomize action. */
	public Observable<ActionEvent> getRandomizeObs() {
		return randomizeObs;
	}

	/** Returns the observable of routes action. */
	public Observable<ActionEvent> getRoutesObs() {
		return routesObs;
	}

	/** Returns the observable of save as action. */
	public Observable<ActionEvent> getSaveMapAsObs() {
		return saveMapAsObs;
	}

	/** Returns the observable of save action */
	public Observable<ActionEvent> getSaveMapObs() {
		return saveMapObs;
	}

	/** Returns the observable of speed action. */
	public Observable<Double> getSpeedObs() {
		return speedObs;
	}

	/** Returns the observable of stop action. */
	public Observable<ActionEvent> getStopObs() {
		return stopObs;
	}

	/** Returns the observable of vehicle info action. */
	public Observable<ActionEvent> getVehicleInfoObs() {
		return vehicleInfoObs;
	}

	/**
	 * InitializeS main frame.
	 *
	 * @param left   left component
	 * @param top    right component
	 * @param bottom bottom component
	 * @return the main frame
	 */
	private MainFrame init(final Component left, final Component top, final Component bottom) {
		resetTitle();

		final URL url = getClass().getResource(IMAGES_ROUTES);
		if (url != null) {
			final ImageIcon img = new ImageIcon(url);
			setIconImage(img.getImage());
		}

		saveMenuItem.setEnabled(false);
		saveButton.setEnabled(false);

		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1);
		splitPane.setLeftComponent(left);
		splitPane.setRightComponent(rightSplitPane);

		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setResizeWeight(1);
		rightSplitPane.setTopComponent(top);
		rightSplitPane.setBottomComponent(bottom);

		createMenuBar();
		createContent();

		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.height -= 50;
		setSize(size);
		return this;
	}

	/** Returns true if simulation is stopped. */
	public boolean isStopped() {
		return stopMenuItem.isSelected();
	}

	/**
	 * Resets the title.
	 *
	 * @return the main frame
	 */
	public MainFrame resetTitle() {
		setTitle(Messages.getString("MainFrame.title")); //$NON-NLS-1$
		return this;
	}

	/**
	 * Set the enabling of save action.
	 *
	 * @param enabled true if enabled
	 * @return the main frame
	 */
	public MainFrame setSaveActionEnabled(final boolean enabled) {
		saveButton.setEnabled(enabled);
		saveMenuItem.setEnabled(enabled);
		return this;
	}
}
