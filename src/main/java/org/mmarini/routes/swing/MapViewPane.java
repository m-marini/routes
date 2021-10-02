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
import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.SiteNode;

import javax.swing.*;
import java.awt.*;
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
    private final ScrollMap scrollMap;
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

    /**
     * Create the component
     *
     * @param scrollMap the scroll Map
     */
    public MapViewPane(final ScrollMap scrollMap) {
        this.scrollMap = scrollMap;
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

        createFlows();
        init();
        setOpaque(false);
    }

    public void clearSelection() {
        scrollMap.clearSelection();
    }

    /**
     * Create the content
     */
    private void createContent() {
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.NORTH);
        add(scrollMap, BorderLayout.CENTER);
    }

    /**
     *
     */
    private void createFlows() {
        SwingObservable.actions(normalViewButton).doOnNext(ev ->
                MapViewPane.this.scrollMap.setTrafficView(false)).subscribe();
        SwingObservable.actions(trafficViewButton).doOnNext(ev ->
                MapViewPane.this.scrollMap.setTrafficView(true)).subscribe();
        SwingObservable.actions(selectButton).doOnNext(ev ->
                MapViewPane.this.scrollMap.startSelectMode()).subscribe();
        SwingObservable.actions(edgeButton).doOnNext(ev ->
                MapViewPane.this.scrollMap.startEdgeMode()).subscribe();
        SwingObservable.actions(moduleButton).doOnNext(ev ->
                MapViewPane.this.scrollMap.startModuleMode(moduleSelector.getSelectedEntry().getModule())).subscribe();
        SwingObservable.actions(centerButton).doOnNext(ev ->
                MapViewPane.this.scrollMap.startCenterMode()).subscribe();
        SwingObservable.actions(zoomInButton).doOnNext(ev -> {
            MapViewPane.this.scrollMap.zoomIn();
            repaint();
        }).subscribe();
        SwingObservable.actions(zoomOutButton).doOnNext(ev -> {
            MapViewPane.this.scrollMap.zoomOut();
            repaint();
        }).subscribe();
        SwingObservable.actions(fitInWindowButton).doOnNext(ev -> scaleToFit()).subscribe();
        SwingObservable.actions(zoomDefaultButton).doOnNext(ev -> {
            MapViewPane.this.scrollMap.setScale(1);
            repaint();
        }).subscribe();
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

    /**
     * Handle of the selection of a module. <br>
     * It is call whenever the insert module button is pressed.
     */
    private void handleModuleSelected() {
        final ModuleEntry entry = moduleSelector.getSelectedEntry();
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
     * Reset the status of the view.<br>
     * This method reset the map view. It must be call when a map is changed.
     */
    public void reset() {
        scrollMap.reset();
    }

    /**
     * Scale the view to fit the current component size
     */
    public void scaleToFit() {
        scrollMap.scaleToFit();
        repaint();
    }

    /**
     * Scroll the map view and center to an edge
     *
     * @param edge the edge element to center the view to
     */
    public void scrollTo(final MapEdge edge) {
        scrollMap.scrollTo(edge);
    }

    /**
     * Scroll the map view and center to a node
     *
     * @param node the node element to center the view to
     */
    public void scrollTo(final MapNode node) {
        scrollMap.scrollTo(node);
    }

    /**
     * Set the selector mode.<br>
     * It restores the modality to selection.
     */
    public void selectSelector() {
        toolGroup.setSelected(selectButton.getModel(), true);
    }

    /**
     * Set the list of modules.<br>
     * It loads the module button list.
     *
     * @param modules te module list
     */
    public void setModule(final List<Module> modules) {
        for (final Module module : modules) {
            moduleSelector.add(module);
        }
        selectButton.doClick();
    }

    /**
     * Set an edge as selected map element
     *
     * @param edge the edge
     */
    public void setSelectedEdge(final MapEdge edge) {
        scrollMap.setSelectedElement(edge);
    }

    /**
     * Set a node as selected map element
     *
     * @param node the node
     */
    public void setSelectedNode(final MapNode node) {
        scrollMap.setSelectedElement(node);
    }

    /**
     * Set a site as selected map element
     *
     * @param site the site
     */
    public void setSelectedSite(final SiteNode site) {
        scrollMap.setSelectedElement(site);
    }
}
