/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */
package org.mmarini.routes.swing;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.net.URL;

/**
 * This is the main frame of the simulation.
 * <p>
 * It contains the menu actions, the route map panel, the edge panel and the
 * explorer panel. It communicates with RouteMediator to manage the simulation.
 * All the menu actions are redirected to the mediator to be resolved.
 * </p>
 *
 * @author marco.marini@mmarini.org
 */
public class MainFrame extends JFrame {
    private static final String IMAGES_ROUTES = "/images/routes.gif";
    private static final long serialVersionUID = 1L;

    private final MapViewPane mapViewPane;

    private final JMenuItem vehicleInfoMenuItem;
    private final JMenuItem infosMenuItem;
    private final JMenuItem newRandomAction;
    private final JMenuItem newMenuItem;
    private final JMenuItem openMenuItem;
    private final JMenuItem saveMenuItem;
    private final JMenuItem exitMenuItem;
    private final JMenuItem saveAsMenuItem;
    private final JMenuItem optimizeMenuItem;
    private final JMenuItem frequencyMenuItem;
    private final JMenuItem randomizeMenuItem;
    private final JMenuItem routesMenuItem;

    private final JCheckBoxMenuItem stopMenuItem;
    private final JRadioButtonMenuItem speedx1MenuItem;
    private final JRadioButtonMenuItem speedx2MenuItem;
    private final JRadioButtonMenuItem speedx5MenuItem;
    private final JRadioButtonMenuItem speedx10MenuItem;
    private final MapElementPane mapElementPane;
    private final JSplitPane splitPane;
    private final JSplitPane rightSplitPane;
    private final ExplorerPane explorerPane;

    private final JButton newButton;
    private final JButton openButton;
    private final JButton saveButton;

    private final Flowable<ActionEvent> infosFlowable;
    private final Flowable<ActionEvent> vehicleInfoFlowable;
    private final Flowable<ActionEvent> stopFlowable;
    private final Flowable<Float> simSpeedFlowable;
    private final Flowable<ActionEvent> newMapFlowable;
    private final Flowable<ActionEvent> openMapFlowable;
    private final Flowable<ActionEvent> saveMapFlowable;
    private final Flowable<ActionEvent> exitFlowable;
    private final Flowable<ActionEvent> optimizeFlowable;
    private final Flowable<ActionEvent> randomizeFlowable;
    private final Flowable<ActionEvent> frequencyFlowable;
    private final Flowable<ActionEvent> routesFlowable;
    private final Flowable<ActionEvent> newRandomFlowable;
    private final Flowable<ActionEvent> saveAsFlowable;
    private final Flowable<WindowEvent> windowFlowable;

    /**
     * @param mapViewPane    the map view panel
     * @param mapElementPane the element panel
     * @param explorerPane   the explorer panel
     * @throws HeadlessException in caso of error
     */
    public MainFrame(MapViewPane mapViewPane, MapElementPane mapElementPane, ExplorerPane explorerPane) throws HeadlessException {
        this.mapViewPane = mapViewPane;
        this.mapElementPane = mapElementPane;
        this.explorerPane = explorerPane;
        splitPane = new JSplitPane();
        rightSplitPane = new JSplitPane();

        infosMenuItem = new JMenuItem();
        vehicleInfoMenuItem = new JMenuItem();
        stopMenuItem = new JCheckBoxMenuItem();
        speedx1MenuItem = new JRadioButtonMenuItem();
        speedx2MenuItem = new JRadioButtonMenuItem();
        speedx5MenuItem = new JRadioButtonMenuItem();
        speedx10MenuItem = new JRadioButtonMenuItem();
        newMenuItem = new JMenuItem();
        openMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        newRandomAction = new JMenuItem();
        exitMenuItem = new JMenuItem();
        saveAsMenuItem = new JMenuItem();
        optimizeMenuItem = new JMenuItem();
        frequencyMenuItem = new JMenuItem();
        randomizeMenuItem = new JMenuItem();
        routesMenuItem = new JMenuItem();

        newButton = new JButton();
        openButton = new JButton();
        saveButton = new JButton();

        infosFlowable = SwingObservable.actions(infosMenuItem).toFlowable(BackpressureStrategy.MISSING);
        vehicleInfoFlowable = SwingObservable.actions(vehicleInfoMenuItem).toFlowable(BackpressureStrategy.MISSING);
        stopFlowable = SwingObservable.actions(stopMenuItem).toFlowable(BackpressureStrategy.MISSING);
        final Observable<Float> x1 = SwingObservable.actions(speedx1MenuItem).map(e -> 1f);
        final Observable<Float> x2 = SwingObservable.actions(speedx2MenuItem).map(e -> 2f);
        final Observable<Float> x5 = SwingObservable.actions(speedx5MenuItem).map(e -> 5f);
        final Observable<Float> x10 = SwingObservable.actions(speedx10MenuItem).map(e -> 10f);
        simSpeedFlowable = x1.mergeWith(x2).mergeWith(x5).mergeWith(x10)
                .toFlowable(BackpressureStrategy.MISSING);
        newMapFlowable = SwingObservable.actions(newMenuItem)
                .mergeWith(SwingObservable.actions(newButton))
                .toFlowable(BackpressureStrategy.MISSING);
        openMapFlowable = SwingObservable.actions(openMenuItem)
                .mergeWith(SwingObservable.actions(openButton))
                .toFlowable(BackpressureStrategy.MISSING);
        saveMapFlowable = SwingObservable.actions(saveMenuItem)
                .mergeWith(SwingObservable.actions(saveButton))
                .toFlowable(BackpressureStrategy.MISSING);
        exitFlowable = SwingObservable.actions(exitMenuItem)
                .toFlowable(BackpressureStrategy.MISSING);
        optimizeFlowable = SwingObservable.actions(optimizeMenuItem)
                .toFlowable(BackpressureStrategy.MISSING);
        randomizeFlowable = SwingObservable.actions(randomizeMenuItem)
                .toFlowable(BackpressureStrategy.MISSING);
        frequencyFlowable = SwingObservable.actions(frequencyMenuItem)
                .toFlowable(BackpressureStrategy.MISSING);
        routesFlowable = SwingObservable.actions(routesMenuItem)
                .toFlowable(BackpressureStrategy.MISSING);
        newRandomFlowable = SwingObservable.actions(newRandomAction)
                .toFlowable(BackpressureStrategy.MISSING);
        saveAsFlowable = SwingObservable.actions(saveAsMenuItem)
                .toFlowable(BackpressureStrategy.MISSING);
        windowFlowable = SwingObservable.window(this)
                .toFlowable(BackpressureStrategy.MISSING);
        final URL url = getClass().getResource(IMAGES_ROUTES);
        if (url != null) {
            final ImageIcon img = new ImageIcon(url);
            setIconImage(img.getImage());
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        JMenuItem item = newMenuItem;
        menu.add(item);
        item = newRandomAction;
        menu.add(item);
        item = openMenuItem;
        menu.add(item);
        menu.add(new JSeparator());
        item = saveMenuItem;
        menu.add(item);
        item = saveAsMenuItem;
        menu.add(item);
        menu.add(new JSeparator());
        item = exitMenuItem;
        menu.add(item);
        bar.add(menu);

        menu = new JMenu(Messages.getString("MainFrame.viewMenu.text")); //$NON-NLS-1$
        menu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.viewMenu.mnemonic").charAt(0))); //$NON-NLS-1$
        item = infosMenuItem;
        menu.add(item);
        item = vehicleInfoMenuItem;
        menu.add(item);
        bar.add(menu);

        menu = new JMenu(Messages.getString("MainFrame.optionMenu.text")); //$NON-NLS-1$
        menu.setMnemonic(Integer.valueOf(Messages.getString("MainFrame.optionMenu.mnemonic").charAt(0))); //$NON-NLS-1$

        menu.add(optimizeMenuItem);
        menu.add(randomizeMenuItem);
        menu.add(frequencyMenuItem);
        menu.add(routesMenuItem);

        menu.add(new JSeparator());
        menu.add(stopMenuItem);

        final ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem item1 = speedx1MenuItem;
        menu.add(item1);
        group.add(item1);
        item1.setSelected(true);
        item1 = speedx2MenuItem;
        menu.add(item1);
        group.add(item1);
        item1 = speedx5MenuItem;
        menu.add(item1);
        group.add(item1);
        item1 = speedx10MenuItem;
        menu.add(item1);
        group.add(item1);
        bar.add(menu);
        setJMenuBar(bar);
    }

    /**
     *
     */
    private Component createToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        return toolBar;
    }

    public Flowable<ActionEvent> getExitFlowable() {
        return exitFlowable;
    }

    public Flowable<ActionEvent> getFrequencyFlowable() {
        return frequencyFlowable;
    }

    public Flowable<ActionEvent> getInfosFlowable() {
        return infosFlowable;
    }

    public Flowable<ActionEvent> getNewMapFlowable() {
        return newMapFlowable;
    }

    public Flowable<ActionEvent> getNewRandomFlowable() {
        return newRandomFlowable;
    }

    public Flowable<ActionEvent> getOpenMapFlowable() {
        return openMapFlowable;
    }

    public Flowable<ActionEvent> getOptimizeFlowable() {
        return optimizeFlowable;
    }

    public Flowable<ActionEvent> getRandomizeFlowable() {
        return randomizeFlowable;
    }

    public Flowable<ActionEvent> getRoutesFlowable() {
        return routesFlowable;
    }

    public Flowable<ActionEvent> getSaveAsFlowable() {
        return saveAsFlowable;
    }

    public Flowable<ActionEvent> getSaveMapFlowable() {
        return saveMapFlowable;
    }

    public Flowable<Float> getSimSpeedFlowable() {
        return simSpeedFlowable;
    }

    public Flowable<ActionEvent> getStopFlowable() {
        return stopFlowable;
    }

    public Flowable<ActionEvent> getVehicleInfoFlowable() {
        return vehicleInfoFlowable;
    }

    public Flowable<WindowEvent> getWindowFlowable() {
        return windowFlowable;
    }

    /**
     * Initialize the content of the main frame
     */
    private void init() {
        resetTitle();
        final SwingUtils utils = SwingUtils.getInstance();
        utils.initMenuItem(newMenuItem, "MainFrame.newAction"); //$NON-NLS-1$
        utils.initMenuItem(openMenuItem, "MainFrame.openAction"); //$NON-NLS-1$
        utils.initMenuItem(saveMenuItem, "MainFrame.saveAction"); //$NON-NLS-1$

        utils.initButton(newButton, "MainFrame.newAction"); //$NON-NLS-1$
        utils.initButton(openButton, "MainFrame.openAction"); //$NON-NLS-1$
        utils.initButton(saveButton, "MainFrame.saveAction"); //$NON-NLS-1$

        utils.initMenuItem(newRandomAction, "MainFrame.newRandomAction"); //$NON-NLS-1$
        utils.initMenuItem(exitMenuItem, "MainFrame.exitAction"); //$NON-NLS-1$
        utils.initMenuItem(saveAsMenuItem, "MainFrame.saveAsAction"); //$NON-NLS-1$
        utils.initMenuItem(optimizeMenuItem, "MainFrame.optimizeAction"); //$NON-NLS-1$
        utils.initMenuItem(randomizeMenuItem, "MainFrame.randomizeAction"); //$NON-NLS-1$
        utils.initMenuItem(frequencyMenuItem, "MainFrame.frequenceAction"); //$NON-NLS-1$
        utils.initMenuItem(routesMenuItem, "MainFrame.routesAction"); //$NON-NLS-1$
        utils.initMenuItem(stopMenuItem, "MainFrame.stopAction"); //$NON-NLS-1$
        utils.initMenuItem(speedx1MenuItem, "MainFrame.speedx1Action"); //$NON-NLS-1$
        utils.initMenuItem(speedx2MenuItem, "MainFrame.speedx2Action"); //$NON-NLS-1$
        utils.initMenuItem(speedx5MenuItem, "MainFrame.speedx5Action"); //$NON-NLS-1$
        utils.initMenuItem(speedx10MenuItem, "MainFrame.speedx10Action"); //$NON-NLS-1$
        utils.initMenuItem(infosMenuItem, "MainFrame.infosAction"); //$NON-NLS-1$
        utils.initMenuItem(vehicleInfoMenuItem, "MainFrame.vehicleInfosAction"); //$NON-NLS-1$

        setSaveActionEnabled(false);

        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1);
        splitPane.setLeftComponent(mapViewPane);
        splitPane.setRightComponent(rightSplitPane);

        rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setOneTouchExpandable(true);
        rightSplitPane.setResizeWeight(1);
        rightSplitPane.setTopComponent(explorerPane);
        rightSplitPane.setBottomComponent(mapElementPane);

        createMenuBar();
        createContent();

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
     * @param b true if save action enabled
     */
    public void setSaveActionEnabled(final boolean b) {
        saveMenuItem.setEnabled(b);
        saveButton.setEnabled(b);
    }
}
