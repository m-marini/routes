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
import java.awt.event.WindowEvent;
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
import javax.swing.WindowConstants;

import org.mmarini.routes.swing.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * This is the main frame of the simulation.
 * <p>
 * It contains the menu actions, the route map panel, the edge panel and the
 * explorer panel. It communicates with RouteMediator to manage the simulation.
 * All the menu actions are redirected to the controller to be resolved.
 * </p>
 *
 */
public class MainFrame extends JFrame {
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

	private static final String IMAGES_ROUTES = "/images/routes.gif";
	private static final long serialVersionUID = 1L;

	/**
	 * Application entry point
	 *
	 * @param args unused
	 */
	public static void main(final String[] args) throws Throwable {
		logger.info("MainFrame started.");
		final MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private final JSplitPane splitPane;
	private final JSplitPane rightSplitPane;
	private final JMenuItem exitMenuItem;
	private final JMenuItem newMenuItem;
	private final JMenuItem newRandomMenuItem;
	private final JMenuItem openMenuItem;
	private final JMenuItem saveMenuItem;
	private final JMenuItem saveAsMenuItem;
	private final JMenuItem optimizeMenuItem;
	private final JMenuItem randomizeMenuItem;
	private final JMenuItem frequenceMenuItem;
	private final JMenuItem routesMenuItem;
	private final JCheckBoxMenuItem stopMenuItem;
	private final JRadioButtonMenuItem speedx1MenuItem;
	private final JRadioButtonMenuItem speedx2MenuItem;
	private final JRadioButtonMenuItem speedx5MenuItem;
	private final JRadioButtonMenuItem speedx10MenuItem;
	private final JMenuItem infosMenuItem;
	private final JMenuItem veicleInfosMenuItem;
	private final JButton newButton;
	private final JButton openButton;
	private final JButton saveButton;
	private final Controller controller;
	private final Observable<ActionEvent> newRandomObs;
	private final Observable<ActionEvent> saveMapAsObs;
	private final Observable<ActionEvent> openMapObs;
	private final Observable<ActionEvent> saveMapObs;
	private final Observable<ActionEvent> newMapObs;

	/**
	 * @throws HeadlessException
	 */
	public MainFrame() throws HeadlessException {
		splitPane = new JSplitPane();
		rightSplitPane = new JSplitPane();
		newMenuItem = createJMenuItem("MainFrame.newAction"); //$NON-NLS-1$
		exitMenuItem = createJMenuItem("MainFrame.exitAction"); //$NON-NLS-1$
		newRandomMenuItem = createJMenuItem("MainFrame.newRandomAction"); //$NON-NLS-1$
		openMenuItem = createJMenuItem("MainFrame.openAction"); //$NON-NLS-1$
		saveMenuItem = createJMenuItem("MainFrame.saveAction"); //$NON-NLS-1$
		saveAsMenuItem = createJMenuItem("MainFrame.saveAsAction"); //$NON-NLS-1$
		optimizeMenuItem = createJMenuItem("MainFrame.optimizeAction"); //$NON-NLS-1$
		randomizeMenuItem = createJMenuItem("MainFrame.randomizeAction"); //$NON-NLS-1$
		frequenceMenuItem = createJMenuItem("MainFrame.frequenceAction"); //$NON-NLS-1$
		routesMenuItem = createJMenuItem("MainFrame.routesAction"); //$NON-NLS-1$
		stopMenuItem = createJCheckBoxMenuItem("MainFrame.stopAction"); //$NON-NLS-1$
		speedx1MenuItem = createJRadioButtonMenuItem("MainFrame.speedx1Action"); //$NON-NLS-1$
		speedx2MenuItem = createJRadioButtonMenuItem("MainFrame.speedx2Action"); //$NON-NLS-1$
		speedx5MenuItem = createJRadioButtonMenuItem("MainFrame.speedx5Action"); //$NON-NLS-1$
		speedx10MenuItem = createJRadioButtonMenuItem("MainFrame.speedx10Action"); //$NON-NLS-1$
		infosMenuItem = createJMenuItem("MainFrame.infosAction"); //$NON-NLS-1$
		veicleInfosMenuItem = createJMenuItem("MainFrame.veicleInfosAction"); //$NON-NLS-1$

		newButton = createJButton("MainFrame.newAction");
		openButton = createJButton("MainFrame.openAction");
		saveButton = createJButton("MainFrame.saveAction");

		newMapObs = SwingObservable.actions(newButton).mergeWith(SwingObservable.actions(newMenuItem));
		newRandomObs = SwingObservable.actions(newRandomMenuItem);
		saveMapAsObs = SwingObservable.actions(saveAsMenuItem);
		openMapObs = SwingObservable.actions(openButton).mergeWith(SwingObservable.actions(openMenuItem));
		saveMapObs = SwingObservable.actions(saveButton).mergeWith(SwingObservable.actions(saveMenuItem));

		controller = new Controller(this);
		init().createSubscriptions();
	}

	/**
	 * Returns the main frame with the content
	 *
	 * @return
	 */
	private MainFrame createContent() {
		final Container pane = getContentPane();
		pane.setLayout(new BorderLayout());

		pane.add(createToolBar(), BorderLayout.NORTH);
		pane.add(splitPane, BorderLayout.CENTER);
		return this;
	}

	/**
	 * Returns the main frame with the menu bar
	 */
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
		viewMenu.add(infosMenuItem);
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

	/**
	 * Returns the main frame with subscriptions
	 *
	 * @return
	 */
	private MainFrame createSubscriptions() {
		SwingObservable.actions(exitMenuItem).subscribe(ev -> {
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		});
		return this;
	}

	/**
	 * Returns the tool bar
	 */
	private Component createToolBar() {
		final JToolBar toolBar = new JToolBar();
		toolBar.add(newButton);
		toolBar.add(openButton);
		toolBar.add(saveButton);
		return toolBar;
	}

	/**
	 * Returns the newMapObs
	 */
	public Observable<ActionEvent> getNewMapObs() {
		return newMapObs;
	}

	/**
	 * Returns the new random map observer
	 */
	public Observable<ActionEvent> getNewRandomObs() {
		return newRandomObs;
	}

	/**
	 * Returns the openMapObs
	 */
	public Observable<ActionEvent> getOpenMapObs() {
		return openMapObs;
	}

	/**
	 * Return the saveMapAsObs
	 */
	public Observable<ActionEvent> getSaveMapAsObs() {
		return saveMapAsObs;
	}

	/**
	 * Returns the saveMapObs
	 */
	public Observable<ActionEvent> getSaveMapObs() {
		return saveMapObs;
	}

	/**
	 * Returns the initialized main frame
	 */
	private MainFrame init() {
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
		splitPane.setLeftComponent(controller.getRouteMap());
		splitPane.setRightComponent(rightSplitPane);

		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setResizeWeight(1);
		rightSplitPane.setTopComponent(controller.getExplorerPane());
		rightSplitPane.setBottomComponent(controller.getMapElementPane());

		createMenuBar();
		createContent();

		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.height -= 50;
		setSize(size);
		return this;
	}

	/**
	 * Returns the main frame with title
	 *
	 * @return
	 */
	public MainFrame resetTitle() {
		setTitle(Messages.getString("MainFrame.title")); //$NON-NLS-1$
		return this;
	}

	/**
	 * Returns the main frame with save action enabled or disabled
	 *
	 * @param enabled true if enabled
	 */
	public MainFrame setSaveActionEnabled(final boolean enabled) {
		saveButton.setEnabled(enabled);
		saveMenuItem.setEnabled(enabled);
		return this;
	}
}
