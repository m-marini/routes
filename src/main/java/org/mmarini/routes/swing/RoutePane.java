/**
 *
 */
package org.mmarini.routes.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mmarini.routes.model.Path;
import org.mmarini.routes.model.SiteNode;

/**
 * Pannello di selezione del profilo di generazione delle mappe casuali
 *
 * @author Marco
 *
 */
public class RoutePane extends Box {
	class PathComparator implements Comparator<Path> {
		private RouteMediator mediator;

		/**
		 *
		 */
		@Override
		public int compare(final Path o1, final Path o2) {
			return mediator.createNodeName(o1.getDestination()).compareTo(mediator.createNodeName(o2.getDestination()));
		}

		/**
		 * @param mediator the mediator to set
		 */
		public void setMediator(final RouteMediator mediator) {
			this.mediator = mediator;
		}

	}

	private static final long serialVersionUID = 1L;
	private final DefaultListModel<String> departureListModel;
	private final RouteTableModel routeTableModel;
	private final Map<String, List<Path>> routeMap;
	private final JList<String> departureList;
	private final JTable routeTable;
	private RouteMediator mediator;
	private final PathComparator pathComparator;
	private final SiteTableCellRenderer siteTableCellRenderer;

	private final SiteListCellRenderer cellRenderer;

	/**
	     *
	     */
	public RoutePane() {
		super(BoxLayout.LINE_AXIS);
		departureListModel = new DefaultListModel<>();
		routeTableModel = new RouteTableModel();
		routeMap = new HashMap<String, List<Path>>();
		departureList = new JList<>(departureListModel);
		routeTable = new JTable(routeTableModel);
		pathComparator = new PathComparator();
		siteTableCellRenderer = new SiteTableCellRenderer();
		cellRenderer = new SiteListCellRenderer();

		routeTable.setDefaultRenderer(SiteNode.class, siteTableCellRenderer);
		departureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		departureList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					departureSelected();
				}
			}

		});
		departureList.setCellRenderer(cellRenderer);
		createContent();
	}

	/**
	 *
	 * @param path
	 */
	private void add(final Path path) {
		final String name = mediator.createNodeName(path.getDeparture());
		List<Path> paths = routeMap.get(name);
		if (paths == null) {
			paths = new ArrayList<Path>();
			routeMap.put(name, paths);
		}
		paths.add(path);
	}

	/**
	 *
	 * @return
	 */
	public void copyPathsTo() {
		final List<Path> list = new ArrayList<Path>();
		for (final List<Path> l : routeMap.values()) {
			list.addAll(l);
		}
		mediator.loadPaths(list);
	}

	/**
	     *
	     *
	     */
	private void createContent() {
		final JScrollPane departureScrollPane = new JScrollPane(departureList);
		departureScrollPane.setBorder(BorderFactory.createTitledBorder("Departure title"));

		final JScrollPane routeScrollPane = new JScrollPane(routeTable);
		routeScrollPane.setBorder(BorderFactory.createTitledBorder("Route title"));

		add(departureScrollPane);
		add(routeScrollPane);
	}

	/**
	 *
	 */
	private void departureSelected() {
		final String name = departureList.getSelectedValue();
		final List<Path> paths = routeMap.get(name);
		routeTableModel.setPaths(paths);
	}

	/**
	 *
	 */
	public void loadPath() {
		routeMap.clear();
		for (final Path p : mediator.getPaths()) {
			add(p.clone());
		}
		for (final List<Path> list : routeMap.values()) {
			Collections.sort(list, pathComparator);
		}
		final List<String> list = new ArrayList<String>(routeMap.keySet());
		Collections.sort(list);
		departureListModel.clear();
		for (final String p : list) {
			departureListModel.addElement(p);
		}
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(final RouteMediator mediator) {
		this.mediator = mediator;
		siteTableCellRenderer.setMediator(mediator);
		pathComparator.setMediator(mediator);
		cellRenderer.setMediator(mediator);
	}
}
