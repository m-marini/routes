/*
 * MainFrame.java
 *
 * $Id: MainFrame.java,v 1.14 2010/10/19 20:32:59 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main frame of the simulation.
 * <p>
 * It contains the menu actions, the route map panel, the edge panel and the
 * explorer panel. It comunicates with RouteMediator to manage the simualtion.
 * All the menu actions are redirected to the mediator to be resolved.
 * </p>
 * 
 * @author marco.marini@mmarini.org
 * @version $Id: MainFrame.java,v 1.14 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class MainFrame extends JFrame {
	private static Logger log = LoggerFactory.getLogger(MainFrame.class);

	private static final String IMAGES_ROUTES = "/images/routes.gif";
	private static final String DUMP_PROPERTY_NAME = "org.mmarini.routes.swing.MainFrame.dump";
	private static final long serialVersionUID = 1L;

	/**
	 * Application entry point
	 * 
	 * @param args unused
	 */
	public static void main(final String[] args) throws Throwable {
		log.info("MainFrame started.");
		try {
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			// UIManager
			// .setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private final MapViewPane routeMap;
	private final AbstractAction newRandomAction;
	private final AbstractAction newAction;
	private final AbstractAction infosAction;
	private final AbstractAction veicleInfosAction;
	private final AbstractAction exitAction;
	private final AbstractAction openAction;
	private final AbstractAction saveAction;
	private final AbstractAction saveAsAction;
	private final AbstractAction optimizeAction;
	private final AbstractAction frequenceAction;
	private final AbstractAction randomizeAction;
	private final AbstractAction routesAction;
	private final AbstractAction stopAction;
	private final AbstractAction speedx1Action;
	private final AbstractAction speedx2Action;
	private final AbstractAction speedx5Action;
	private final AbstractAction speedx10Action;
	private final AbstractAction dumpAction;
	private final MapElementPane mapElementPane;
	private final JSplitPane splitPane;
	private final JSplitPane rightSplitPane;
	private final ExplorerPane explorerPane;

	private final RouteMediator mediator;

	/**
	 * @throws HeadlessException
	 */
	public MainFrame() throws HeadlessException {
		routeMap = new MapViewPane();
		mapElementPane = new MapElementPane();
		splitPane = new JSplitPane();
		rightSplitPane = new JSplitPane();
		explorerPane = new ExplorerPane();
		mediator = new RouteMediator();

		infosAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.showInfos();
			}
		};
		veicleInfosAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.showTrafficInfos();
			}
		};
		dumpAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.dump();
			}
		};
		stopAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.toogleSimulation();
			}
		};
		speedx1Action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.setSpeedSimulation(1f);
			}
		};
		speedx2Action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.setSpeedSimulation(2f);
			}
		};
		speedx5Action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.setSpeedSimulation(5f);
			}
		};
		speedx10Action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.setSpeedSimulation(10f);
			}
		};
		exitAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.exit();
			}
		};
		optimizeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.optimize();
			}
		};
		randomizeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.randomize();
			}
		};
		frequenceAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.setFrequence();
			}
		};
		routesAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.setRouteSetting();
			}
		};
		newAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.newMap();
				resetTitle();
			}
		};
		newRandomAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.newRandomMap();
				resetTitle();
			}
		};
		openAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.open();
			}
		};
		saveAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.save();
			}
		};
		saveAsAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				mediator.saveAs();
			}
		};
		addWindowListener(new WindowAdapter() {

			/**
			 * @see java.awt.event.WindowAdapter#windowOpened(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowOpened(final WindowEvent arg0) {
				mediator.start();
			}
		});
		final URL url = getClass().getResource(IMAGES_ROUTES);
		if (url != null) {
			final ImageIcon img = new ImageIcon(url);
			setIconImage(img.getImage());
		}

		init();
	}

	/**
	     * 
	     */
	private void createContent() {
		final Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(createToolBar(), BorderLayout.NORTH);
		pane.add(splitPane, BorderLayout.CENTER);
	}

	/**
	     * 
	     */
	private void createMenuBar() {
		final JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu(Messages.getString("MainFrame.fileMenu.text")); //$NON-NLS-1$
		menu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.fileMenu.mnemonic").charAt(0))); //$NON-NLS-1$
		JMenuItem item = new JMenuItem(newAction);
		menu.add(item);
		item = new JMenuItem(newRandomAction);
		menu.add(item);
		item = new JMenuItem(openAction);
		menu.add(item);
		menu.add(new JSeparator());
		item = new JMenuItem(saveAction);
		menu.add(item);
		item = new JMenuItem(saveAsAction);
		menu.add(item);
		menu.add(new JSeparator());
		item = new JMenuItem(exitAction);
		menu.add(item);
		bar.add(menu);

		menu = new JMenu(Messages.getString("MainFrame.viewMenu.text")); //$NON-NLS-1$
		menu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.viewMenu.mnemonic").charAt(0))); //$NON-NLS-1$
		item = new JMenuItem(infosAction);
		menu.add(item);
		item = new JMenuItem(veicleInfosAction);
		menu.add(item);
		bar.add(menu);

		menu = new JMenu(Messages.getString("MainFrame.optionMenu.text")); //$NON-NLS-1$
		menu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.optionMenu.mnemonic").charAt(0))); //$NON-NLS-1$

		item = new JMenuItem(optimizeAction);
		menu.add(item);
		item = new JMenuItem(randomizeAction);
		menu.add(item);
		item = new JMenuItem(frequenceAction);
		menu.add(item);
		item = new JMenuItem(routesAction);
		menu.add(item);

		menu.add(new JSeparator());

		final JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(stopAction);
		menu.add(item2);

		final ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem item1 = new JRadioButtonMenuItem(speedx1Action);
		menu.add(item1);
		group.add(item1);
		item1.setSelected(true);
		item1 = new JRadioButtonMenuItem(speedx2Action);
		menu.add(item1);
		group.add(item1);
		item1 = new JRadioButtonMenuItem(speedx5Action);
		menu.add(item1);
		group.add(item1);
		item1 = new JRadioButtonMenuItem(speedx10Action);
		menu.add(item1);
		group.add(item1);
		bar.add(menu);

		if (System.getProperty(DUMP_PROPERTY_NAME) != null) {
			menu.add(new JSeparator());

			item = new JMenuItem(dumpAction);
			menu.add(item);
		}
		setJMenuBar(bar);
	}

	/**
	 * @return
	 */
	private Component createToolBar() {
		final JToolBar toolBar = new JToolBar();
		toolBar.add(newAction);
		toolBar.add(openAction);
		toolBar.add(saveAction);
		return toolBar;
	}

	/**
	 * Initialize the content of the main frame
	 */
	private void init() {
		resetTitle();
		final SwingUtils utils = SwingUtils.getInstance();
		utils.initAction(newRandomAction, "MainFrame.newRandomAction"); //$NON-NLS-1$
		utils.initAction(newAction, "MainFrame.newAction"); //$NON-NLS-1$
		utils.initAction(exitAction, "MainFrame.exitAction"); //$NON-NLS-1$
		utils.initAction(openAction, "MainFrame.openAction"); //$NON-NLS-1$
		utils.initAction(saveAction, "MainFrame.saveAction"); //$NON-NLS-1$
		utils.initAction(saveAsAction, "MainFrame.saveAsAction"); //$NON-NLS-1$
		utils.initAction(optimizeAction, "MainFrame.optimizeAction"); //$NON-NLS-1$
		utils.initAction(randomizeAction, "MainFrame.randomizeAction"); //$NON-NLS-1$
		utils.initAction(frequenceAction, "MainFrame.frequenceAction"); //$NON-NLS-1$
		utils.initAction(routesAction, "MainFrame.routesAction"); //$NON-NLS-1$
		utils.initAction(stopAction, "MainFrame.stopAction"); //$NON-NLS-1$
		utils.initAction(speedx1Action, "MainFrame.speedx1Action"); //$NON-NLS-1$
		utils.initAction(speedx2Action, "MainFrame.speedx2Action"); //$NON-NLS-1$
		utils.initAction(speedx5Action, "MainFrame.speedx5Action"); //$NON-NLS-1$
		utils.initAction(speedx10Action, "MainFrame.speedx10Action"); //$NON-NLS-1$
		utils.initAction(dumpAction, "MainFrame.dumpAction"); //$NON-NLS-1$
		utils.initAction(infosAction, "MainFrame.infosAction"); //$NON-NLS-1$
		utils.initAction(veicleInfosAction, "MainFrame.veicleInfosAction"); //$NON-NLS-1$

		saveAction.setEnabled(false);

		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1);
		splitPane.setLeftComponent(routeMap);
		splitPane.setRightComponent(rightSplitPane);

		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setResizeWeight(1);
		rightSplitPane.setTopComponent(explorerPane);
		rightSplitPane.setBottomComponent(mapElementPane);

		createMenuBar();
		createContent();

		mediator.setMainFrame(this);
		mediator.setExplorerPane(explorerPane);
		mediator.setMapViewPane(routeMap);
		mediator.setMapElementPane(mapElementPane);

		mediator.init();
		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.height -= 50;
		setSize(size);
	}

	/**
	     * 
	     */
	private void resetTitle() {
		setTitle(Messages.getString("MainFrame.title")); //$NON-NLS-1$
	}

	/**
	 * @param b
	 */
	public void setSaveActionEnabled(final boolean b) {
		saveAction.setEnabled(true);
	}
}
