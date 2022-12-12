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
import org.mmarini.routes.model2.MapModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * The MapViewPane allows the user to view and interact with the map.
 * <p>
 * It contains a toolbar to select the tool to change the map or the view scale
 * or view mode.
 * </p>
 * <p>
 * The tools to change the map allow to select elements, insert edges, insert
 * modules or set the center of the map.
 * </p>
 *
 * @author marco.marini@mmarini.org
 */
public class MapViewPane extends JPanel {

    private static final long serialVersionUID = 1L;
    private final RouteMapViewport scrollMap;
    private final JToggleButton selectButton;
    private final JToggleButton edgeButton;
    private final JToggleButton moduleButton;
    private final JToggleButton centerButton;
    private final ButtonGroup toolGroup;
    private final ModuleSelector moduleSelector;
    private final JToggleButton normalViewButton;
    private final JToggleButton trafficViewButton;
    private final ButtonGroup viewGroup;
    private final JButton zoomInButton;
    private final JButton zoomOutButton;
    private final JButton fitInWindowButton;
    private final JButton zoomDefaultButton;
    private final JComponent infoPane;
    private final Flowable<ActionEvent> normalViewFlowable;
    private final Flowable<ActionEvent> zoomDefaultFlowable;
    private final Flowable<ActionEvent> zoomOutFlowable;
    private final Flowable<ActionEvent> zoomInFlowable;
    private final Flowable<ActionEvent> fitInWindowFlowable;
    private final Flowable<ActionEvent> trafficViewFlowable;
    private final Flowable<ActionEvent> selectFlowable;
    private final Flowable<ActionEvent> edgeFlowable;
    private final Flowable<MapModule> moduleFlowable;
    private final Flowable<ActionEvent> centerFlowable;

    /**
     * Create the component
     *
     * @param scrollMap the scroll map
     * @param infoPane  the info panel
     */
    public MapViewPane(final RouteMapViewport scrollMap, JComponent infoPane) {
        this.scrollMap = scrollMap;
        this.infoPane = infoPane;
        selectButton = new JToggleButton();
        edgeButton = new JToggleButton();
        moduleButton = new JToggleButton();
        centerButton = new JToggleButton();
        toolGroup = new ButtonGroup();
        viewGroup = new ButtonGroup();
        normalViewButton = new JToggleButton();
        trafficViewButton = new JToggleButton();
        moduleSelector = new ModuleSelector();
        moduleSelector.addListSelectionListener(e -> handleModuleSelected());
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        fitInWindowButton = new JButton();
        zoomDefaultButton = new JButton();
        this.normalViewFlowable = SwingObservable.actions(normalViewButton).toFlowable(BackpressureStrategy.MISSING);
        this.trafficViewFlowable = SwingObservable.actions(trafficViewButton).toFlowable(BackpressureStrategy.MISSING);
        this.zoomDefaultFlowable = SwingObservable.actions(zoomDefaultButton).toFlowable(BackpressureStrategy.MISSING);
        this.zoomOutFlowable = SwingObservable.actions(zoomOutButton).toFlowable(BackpressureStrategy.MISSING);
        this.zoomInFlowable = SwingObservable.actions(zoomInButton).toFlowable(BackpressureStrategy.MISSING);
        this.fitInWindowFlowable = SwingObservable.actions(fitInWindowButton).toFlowable(BackpressureStrategy.MISSING);
        this.selectFlowable = SwingObservable.actions(selectButton).toFlowable(BackpressureStrategy.MISSING);
        this.edgeFlowable = SwingObservable.actions(edgeButton).toFlowable(BackpressureStrategy.MISSING);
        this.moduleFlowable = SwingObservable.actions(moduleButton)
                .map(e -> moduleSelector.getSelectedEntry().getModule())
                .toFlowable(BackpressureStrategy.MISSING);
        this.centerFlowable = SwingObservable.actions(centerButton).toFlowable(BackpressureStrategy.MISSING);

        init();
        setOpaque(false);
    }

    /**
     * Create the content
     */
    private void createContent() {
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.NORTH);
        add(scrollMap, BorderLayout.CENTER);
        add(infoPane, BorderLayout.SOUTH);
    }

    /**
     * Create the toolbar
     *
     * @return the toolbar
     */
    private JToolBar createToolbar() {
        final JToolBar bar = new JToolBar();
        bar.add(selectButton);
        bar.add(edgeButton);
        bar.add(centerButton);
        bar.add(moduleButton);
        bar.add(moduleSelector);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(zoomDefaultButton);
        bar.add(zoomInButton);
        bar.add(zoomOutButton);
        bar.add(fitInWindowButton);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(normalViewButton);
        bar.add(trafficViewButton);
        return bar;
    }

    public Flowable<ActionEvent> getCenterFlowable() {
        return centerFlowable;
    }

    public Flowable<ActionEvent> getEdgeFlowable() {
        return edgeFlowable;
    }

    public Flowable<ActionEvent> getFitInWindowFlowable() {
        return fitInWindowFlowable;
    }

    public Flowable<MapModule> getModuleFlowable() {
        return moduleFlowable;
    }

    public Flowable<ActionEvent> getNormalViewFlowable() {
        return normalViewFlowable;
    }

    public Flowable<ActionEvent> getSelectFlowable() {
        return selectFlowable;
    }

    public Flowable<ActionEvent> getTrafficViewFlowable() {
        return trafficViewFlowable;
    }

    public Flowable<ActionEvent> getZoomDefaultFlowable() {
        return zoomDefaultFlowable;
    }

    public Flowable<ActionEvent> getZoomInFlowable() {
        return zoomInFlowable;
    }

    public Flowable<ActionEvent> getZoomOutFlowable() {
        return zoomOutFlowable;
    }

    /**
     * Handle of the selection of a module. <br>
     * It is call whenever the insert module button is pressed.
     */
    private void handleModuleSelected() {
        final ModuleView entry = moduleSelector.getSelectedEntry();
        moduleButton.setIcon(entry.getIcon());
        moduleButton.doClick();
    }

    /**
     * Initialize the component
     */
    private void init() {
        final SwingUtils utils = SwingUtils.getInstance();
        utils.initButton(zoomInButton, "MapViewPane.zoomInAction"); //$NON-NLS-1$
        utils.initButton(zoomOutButton, "MapViewPane.zoomOutAction"); //$NON-NLS-1$
        utils.initButton(zoomDefaultButton, "MapViewPane.zoomDefaultAction"); //$NON-NLS-1$
        utils.initButton(fitInWindowButton, "MapViewPane.fitInWindowAction"); //$NON-NLS-1$
        utils.initButton(selectButton, "MapViewPane.selectAction"); //$NON-NLS-1$
        utils.initButton(edgeButton, "MapViewPane.edgeAction"); //$NON-NLS-1$
        utils.initButton(moduleButton, "MapViewPane.moduleAction"); //$NON-NLS-1$
        utils.initButton(centerButton, "MapViewPane.centerAction"); //$NON-NLS-1$
        utils.initButton(normalViewButton, "MapViewPane.normalViewAction"); //$NON-NLS-1$
        utils.initButton(trafficViewButton, "MapViewPane.trafficViewAction"); //$NON-NLS-1$
        toolGroup.add(selectButton);
        toolGroup.add(edgeButton);
        toolGroup.add(moduleButton);
        toolGroup.add(centerButton);

        viewGroup.add(normalViewButton);
        viewGroup.add(trafficViewButton);

        createContent();
        selectButton.doClick();
        normalViewButton.doClick();
    }

    /**
     * Set the selector mode.<br>
     * It restores the modality to selection.
     */
    public void selectSelector() {
        toolGroup.setSelected(selectButton.getModel(), true);
    }

    /**
     * Set the list of mapModules.<br>
     * It loads the module button list.
     *
     * @param mapModules te module list
     */
    public void setModule(final List<MapModule> mapModules) {
        for (final MapModule mapModule : mapModules) {
            moduleSelector.add(mapModule);
        }
        selectButton.doClick();
    }
}
