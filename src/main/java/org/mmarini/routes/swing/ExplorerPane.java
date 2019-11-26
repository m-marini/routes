/*
 * ExplorerPane.java
 *
 * $Id: ExplorerPane.java,v 1.9 2010/10/19 20:32:59 marco Exp $
 *
 * 06/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapElementVisitor;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.SiteNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: ExplorerPane.java,v 1.9 2010/10/19 20:32:59 marco Exp $
 *
 */
public class ExplorerPane extends JTabbedPane {

	private static final int EDGE_TAB_INDEX = 1;

	private static final int NODE_TAB_INDEX = 0;

	private static final long serialVersionUID = 1L;

	private final DefaultListModel<MapEdgeEntry> edgeList;

	private RouteMediator mediator;

	private final JList<MapNodeEntry> nodeJList;

	private final JList<MapEdgeEntry> edgeJList;

	private List<MapElementListener> listeners;

	private final MapElementEvent mapElementEvent;

	private final String title;

	private final String nodeTabTitle;

	private final String edgeTabTitle;

	private final MapElementVisitor eventFirer;

	/**
	     *
	     */
	public ExplorerPane() {
		edgeList = new DefaultListModel<>();
		nodeJList = new JList<>();
		edgeJList = new JList<>(edgeList);
		mapElementEvent = new MapElementEvent(this);
		title = Messages.getString("ExplorerPane.title"); //$NON-NLS-1$
		nodeTabTitle = Messages.getString("ExplorerPane.nodeTabe.title"); //$NON-NLS-1$
		edgeTabTitle = Messages.getString("ExplorerPane.edgeTab.title"); //$NON-NLS-1$
		eventFirer = new MapElementVisitor() {

			@Override
			public void visit(final MapEdge edge) {
			}

			@Override
			public void visit(final MapNode node) {
				fireMapNodeSelected(node);
			}

			@Override
			public void visit(final SiteNode site) {
				fireSiteSelected(site);
			}

		};
		init();
		createContent();
	}

	/**
	 *
	 * @param l
	 */
	public synchronized void addMapElementListener(final MapElementListener l) {
		List<MapElementListener> ls = listeners;
		if (ls == null) {
			ls = new ArrayList<MapElementListener>(1);
			ls.add(l);
			listeners = ls;
		} else if (!ls.contains(l)) {
			ls = new ArrayList<MapElementListener>(ls);
			ls.add(l);
			listeners = ls;
		}
	}

	/**
	     *
	     */
	private void createContent() {
		addTab(nodeTabTitle, new JScrollPane(nodeJList));
		addTab(edgeTabTitle, new JScrollPane(edgeJList));
	}

	/**
	 *
	 * @param edge
	 */
	private void fireMapEdgeSelected(final MapEdge edge) {
		mapElementEvent.setSite(null);
		mapElementEvent.setEdge(edge);
		mapElementEvent.setNode(null);
		final List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (final MapElementListener l : listeners) {
				l.edgeSelected(mapElementEvent);
			}
		}
	}

	/**
	 *
	 * @param node
	 */
	private void fireMapNodeSelected(final MapNode node) {
		mapElementEvent.setSite(null);
		mapElementEvent.setEdge(null);
		mapElementEvent.setNode(node);
		final List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (final MapElementListener l : listeners) {
				l.nodeSelected(mapElementEvent);
			}
		}
	}

	/**
	 *
	 * @param site
	 */
	private void fireSiteSelected(final SiteNode site) {
		mapElementEvent.setSite(site);
		mapElementEvent.setEdge(null);
		mapElementEvent.setNode(null);
		final List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (final MapElementListener l : listeners) {
				l.siteSelected(mapElementEvent);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public MapEdge getSelectedEdge() {
		final MapEdgeEntry entry = edgeJList.getSelectedValue();
		if (entry != null) {
			return entry.getEdge();
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public MapNode getSelectedNode() {
		final MapNodeEntry entry = nodeJList.getSelectedValue();
		if (entry != null) {
			return entry.getNode();
		}
		return null;
	}

	/**
	     *
	     */
	protected void handleEdgeSelection() {
		fireMapEdgeSelected(getSelectedEdge());
	}

	/**
	     *
	     */
	protected void handleNodeSelection() {
		final MapNode node = getSelectedNode();
		if (node != null) {
			node.apply(eventFirer);
		} else {
			fireMapNodeSelected(null);
		}
	}

	/**
	     *
	     */
	private void init() {
		setBorder(BorderFactory.createTitledBorder(title));
		nodeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent ev) {
				if (!ev.getValueIsAdjusting()) {
					handleNodeSelection();
				}
			}

		});
		edgeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		edgeJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent ev) {
				if (!ev.getValueIsAdjusting()) {
					handleEdgeSelection();
				}
			}

		});
	}

	/**
	 *
	 * @param l
	 */
	public synchronized void removeMapElementListener(final MapElementListener l) {
		List<MapElementListener> ls = listeners;
		if (ls != null && ls.contains(l)) {
			ls = new ArrayList<MapElementListener>(ls);
			ls.remove(l);
			listeners = ls;
		}
	}

	/**
	 *
	 * @param edgeList
	 */
	public void setEdgeList(final ListModel<MapEdgeEntry> edgeList) {
		edgeJList.setModel(edgeList);
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(final RouteMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 *
	 * @param nodeList
	 */
	public void setNodeList(final ListModel<MapNodeEntry> nodeList) {
		nodeJList.setModel(nodeList);
	}

	/**
	     *
	     *
	     */
	public void setSelectedElement(final MapEdge edge) {
		final int idx = mediator.getEdgeListIndex(edge);
		if (idx >= 0) {
			edgeJList.setSelectedIndex(idx);
			setSelectedIndex(EDGE_TAB_INDEX);
			return;
		}
		edgeJList.setSelectedIndex(edgeJList.getSelectedIndex());
	}

	/**
	     *
	     *
	     */
	public void setSelectedElement(final MapNode node) {
		final int idx = mediator.getNodeListIndex(node);
		if (idx >= 0) {
			nodeJList.setSelectedIndex(idx);
			setSelectedIndex(NODE_TAB_INDEX);
			return;
		}
		nodeJList.setSelectedIndex(nodeJList.getSelectedIndex());
	}

}
