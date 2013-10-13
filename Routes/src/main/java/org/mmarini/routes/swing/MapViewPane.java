/*
 * ScrollMap.java
 *
 * $Id: MapViewPane.java,v 1.11 2010/10/19 20:32:59 marco Exp $
 *
 * 06/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.SiteNode;

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
 * @version $Id: MapViewPane.java,v 1.11 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class MapViewPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private AbstractAction zoomInAction;

	private AbstractAction zoomOutAction;

	private AbstractAction zoomDefaultAction;

	private AbstractAction fitInWindowAction;

	private AbstractAction selectAction;

	private AbstractAction edgeAction;

	private AbstractAction moduleAction;

	private AbstractAction centerAction;

	private ScrollMap scrollPane;

	private JToggleButton selectButton;

	private JToggleButton edgeButton;

	private JToggleButton moduleButton;

	private JToggleButton centerButton;

	private ButtonGroup toolGroup;

	private ModuleSelector moduleSelector;

	private AbstractAction normalViewAction;

	private AbstractAction trafficViewAction;

	private JToggleButton normalViewButton;

	private JToggleButton trafficViewButton;

	private ButtonGroup viewGroup;

	/**
	 * Create the component
	 */
	public MapViewPane() {
		scrollPane = new ScrollMap();
		selectButton = new JToggleButton();
		edgeButton = new JToggleButton();
		moduleButton = new JToggleButton();
		centerButton = new JToggleButton();

		toolGroup = new ButtonGroup();
		viewGroup = new ButtonGroup();

		normalViewButton = new JToggleButton();
		trafficViewButton = new JToggleButton();
		moduleSelector = new ModuleSelector();
		moduleSelector.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				handleModuleSelected();
			}

		});
		zoomInAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.zoomIn();
				repaint();
			}
		};
		zoomOutAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.zoomOut();
				repaint();
			}
		};
		fitInWindowAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scaleToFit();
			}
		};
		zoomDefaultAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.setScale(1);
				repaint();
			}
		};
		selectAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				scrollPane.startSelectMode();
			}
		};
		edgeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				scrollPane.startEdgeMode();
			}

		};
		moduleAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.startModuleMode(moduleSelector.getSelectedEntry()
						.getModule());
			}
		};
		centerAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.startCenterMode();
			}
		};

		normalViewAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.setTrafficView(false);
			}
		};
		trafficViewAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollPane.setTrafficView(true);
			}
		};

		init();
		setOpaque(false);
	}

	/**
	 * Add a MapElementListener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addMapElementListener(MapElementListener listener) {
		scrollPane.addMapElementListener(listener);
	}

	/**
	 * Create the content
	 */
	private void createContent() {
		setLayout(new BorderLayout());
		add(createToolbar(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Create the tool bar
	 * 
	 * @return the tool bar
	 */
	private JToolBar createToolbar() {
		JToolBar bar = new JToolBar();
		bar.add(selectButton);
		bar.add(edgeButton);
		bar.add(centerButton);
		bar.add(moduleButton);
		bar.add(moduleSelector);
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		bar.add(zoomDefaultAction);
		bar.add(zoomInAction);
		bar.add(zoomOutAction);
		bar.add(fitInWindowAction);
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		bar.add(normalViewButton);
		bar.add(trafficViewButton);
		return bar;
	}

	/**
	 * Return the current view scale
	 * 
	 * @return the current view scale (real px/virtual px)
	 */
	public double getScale() {
		return scrollPane.getScale();
	}

	/**
	 * Handle of the selection of a module. <br>
	 * It is call whenever the insert module button is pressed.
	 */
	private void handleModuleSelected() {
		ModuleEntry entry = moduleSelector.getSelectedEntry();
		moduleAction.putValue(Action.SMALL_ICON, entry.getIcon());
		moduleButton.doClick();
	}

	/**
	 * Initialize the component
	 */
	private void init() {
		SwingUtils utils = SwingUtils.getInstance();
		utils.initAction(zoomInAction, "MapViewPane.zoomInAction"); //$NON-NLS-1$
		utils.initAction(zoomOutAction, "MapViewPane.zoomOutAction"); //$NON-NLS-1$
		utils.initAction(zoomDefaultAction, "MapViewPane.zoomDefaultAction"); //$NON-NLS-1$
		utils.initAction(fitInWindowAction, "MapViewPane.fitInWindowAction"); //$NON-NLS-1$
		utils.initAction(selectAction, "MapViewPane.selectAction"); //$NON-NLS-1$
		utils.initAction(edgeAction, "MapViewPane.edgeAction"); //$NON-NLS-1$
		utils.initAction(moduleAction, "MapViewPane.moduleAction"); //$NON-NLS-1$
		utils.initAction(centerAction, "MapViewPane.centerAction"); //$NON-NLS-1$
		utils.initAction(normalViewAction, "MapViewPane.normalViewAction"); //$NON-NLS-1$
		utils.initAction(trafficViewAction, "MapViewPane.trafficViewAction"); //$NON-NLS-1$
		selectButton.setAction(selectAction);
		edgeButton.setAction(edgeAction);
		moduleButton.setAction(moduleAction);
		centerButton.setAction(centerAction);
		toolGroup.add(selectButton);
		toolGroup.add(edgeButton);
		toolGroup.add(moduleButton);
		toolGroup.add(centerButton);

		normalViewButton.setAction(normalViewAction);
		trafficViewButton.setAction(trafficViewAction);
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
		scrollPane.reset();
	}

	/**
	 * Scale the view to fit the current component size
	 */
	public void scaleToFit() {
		scrollPane.scaleToFit();
		repaint();
	}

	/**
	 * Scroll the map view and center to an edge
	 * 
	 * @param edge
	 *            the edge element to center the view to
	 */
	public void scrollTo(MapEdge edge) {
		scrollPane.scrollTo(edge);
	}

	/**
	 * Scroll the map view and center to a node
	 * 
	 * @param node
	 *            the node element to center the view to
	 */
	public void scrollTo(MapNode node) {
		scrollPane.scrollTo(node);
	}

	/**
	 * Set the selector mode.<br>
	 * It restore the modality to selection.
	 */
	public void selectSelector() {
		toolGroup.setSelected(selectButton.getModel(), true);
	}

	/**
	 * Set the mediator.<br>
	 * The mediator is used by ScrollPane subcomponent of this component.
	 * 
	 * @param mediator
	 *            the mediator
	 */
	public void setMediator(RouteMediator mediator) {
		scrollPane.setMediator(mediator);
	}

	/**
	 * Set the list of modules.<br>
	 * It loads the module button list.
	 * 
	 * @param modules
	 *            te module list
	 */
	public void setModule(List<Module> modules) {
		for (Module module : modules) {
			moduleSelector.add(module);
		}
		selectButton.doClick();
	}

	/**
	 * Sets the view scale
	 * 
	 * @param scale
	 *            the scale (real px/virtual px)
	 */
	public void setScale(double scale) {
		scrollPane.setScale(scale);
	}

	/**
	 * Set an edge as selected map element
	 * 
	 * @param edge
	 *            the edge
	 */
	public void setSelectedElement(MapEdge edge) {
		scrollPane.setSelectedElement(edge);
	}

	/**
	 * Set a node as selected map element
	 * 
	 * @param node
	 *            the node
	 */
	public void setSelectedElement(MapNode node) {
		scrollPane.setSelectedElement(node);
	}

	/**
	 * Set a site as selected map element
	 * 
	 * @param site
	 *            the site
	 */
	public void setSelectedElement(SiteNode site) {
		scrollPane.setSelectedElement(site);
	}
}
