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

import io.reactivex.rxjava3.core.Flowable;

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

	private final Flowable<ActionEvent> newMapFlow;
	private final Flowable<ActionEvent> newRandomFlow;
	private final Flowable<ActionEvent> openMapFlow;
	private final Flowable<ActionEvent> saveMapFlow;
	private final Flowable<ActionEvent> saveMapAsFlow;
	private final Flowable<ActionEvent> exitFlow;
	private final Flowable<ActionEvent> vehicleInfoFlow;
	private final Flowable<ActionEvent> optimizeFlow;
	private final Flowable<ActionEvent> randomizeFlow;
	private final Flowable<ActionEvent> frequenceFlow;
	private final Flowable<ActionEvent> routesFlow;
	private final Flowable<ActionEvent> stopFlow;
	private final Flowable<Double> speedFlow;

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

		this.newRandomFlow = SwingUtils.actions(newRandomMenuItem);
		this.saveMapAsFlow = SwingUtils.actions(saveAsMenuItem);
		this.exitFlow = SwingUtils.actions(exitMenuItem);

		this.newMapFlow = SwingUtils.actions(newButton).mergeWith(SwingUtils.actions(newMenuItem));
		this.openMapFlow = SwingUtils.actions(openButton).mergeWith(SwingUtils.actions(openMenuItem));
		this.saveMapFlow = SwingUtils.actions(saveButton).mergeWith(SwingUtils.actions(saveMenuItem));

		this.optimizeFlow = SwingUtils.actions(optimizeMenuItem);
		this.frequenceFlow = SwingUtils.actions(frequenceMenuItem);
		this.speedFlow = SwingUtils.actions(speedx1MenuItem).map(ev -> {
			return 1.0;
		}).mergeWith(SwingUtils.actions(speedx2MenuItem).map(ev -> {
			return 2.0;
		})).mergeWith(SwingUtils.actions(speedx5MenuItem).map(ev -> {
			return 5.0;
		})).mergeWith(SwingUtils.actions(speedx10MenuItem).map(ev -> {
			return 10.0;
		}));
		this.stopFlow = SwingUtils.actions(stopMenuItem);
		this.routesFlow = SwingUtils.actions(routesMenuItem);
		this.randomizeFlow = SwingUtils.actions(randomizeMenuItem);
		this.vehicleInfoFlow = SwingUtils.actions(veicleInfosMenuItem);

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

	/** Returns the flowable of exit action. */
	public Flowable<ActionEvent> getExitFlow() {
		return exitFlow;
	}

	/** Returns the flowable of frequency action. */
	public Flowable<ActionEvent> getFrequenceFlow() {
		return frequenceFlow;
	}

	/** Returns the flowable of new map action. */
	public Flowable<ActionEvent> getNewMapFlow() {
		return newMapFlow;
	}

	/** Returns the flowable of new random map action. */
	public Flowable<ActionEvent> getNewRandomFlow() {
		return newRandomFlow;
	}

	/** Returns the flowable of open action. */
	public Flowable<ActionEvent> getOpenMapFlow() {
		return openMapFlow;
	}

	/** Returns the flowable of optimize action. */
	public Flowable<ActionEvent> getOptimizeFlow() {
		return optimizeFlow;
	}

	/** Returns the flowable of randomize action. */
	public Flowable<ActionEvent> getRandomizeFlow() {
		return randomizeFlow;
	}

	/** Returns the flowable of routes action. */
	public Flowable<ActionEvent> getRoutesFlow() {
		return routesFlow;
	}

	/** Returns the flowable of save as action. */
	public Flowable<ActionEvent> getSaveMapAsFlow() {
		return saveMapAsFlow;
	}

	/** Returns the flowable of save action */
	public Flowable<ActionEvent> getSaveMapFlow() {
		return saveMapFlow;
	}

	/** Returns the flowable of speed action. */
	public Flowable<Double> getSpeedFlow() {
		return speedFlow;
	}

	/** Returns the flowable of stop action. */
	public Flowable<ActionEvent> getStopFlow() {
		return stopFlow;
	}

	/** Returns the flowable of vehicle info action. */
	public Flowable<ActionEvent> getVehicleInfoFlow() {
		return vehicleInfoFlow;
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
