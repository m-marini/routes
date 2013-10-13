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

	private DefaultListModel edgeList;

	private RouteMediator mediator;

	private JList nodeJList;

	private JList edgeJList;

	private List<MapElementListener> listeners;

	private MapElementEvent mapElementEvent;

	private String title;

	private String nodeTabTitle;

	private String edgeTabTitle;

	private MapElementVisitor eventFirer;

	/**
         * 
         */
	public ExplorerPane() {
		edgeList = new DefaultListModel();
		nodeJList = new JList();
		edgeJList = new JList(edgeList);
		mapElementEvent = new MapElementEvent(this);
		title = Messages.getString("ExplorerPane.title"); //$NON-NLS-1$
		nodeTabTitle = Messages.getString("ExplorerPane.nodeTabe.title"); //$NON-NLS-1$
		edgeTabTitle = Messages.getString("ExplorerPane.edgeTab.title"); //$NON-NLS-1$
		eventFirer = new MapElementVisitor() {

			@Override
			public void visit(MapEdge edge) {
			}

			@Override
			public void visit(MapNode node) {
				fireMapNodeSelected(node);
			}

			@Override
			public void visit(SiteNode site) {
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
	public synchronized void addMapElementListener(MapElementListener l) {
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
	private void fireMapEdgeSelected(MapEdge edge) {
		mapElementEvent.setSite(null);
		mapElementEvent.setEdge(edge);
		mapElementEvent.setNode(null);
		List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (MapElementListener l : listeners) {
				l.edgeSelected(mapElementEvent);
			}
		}
	}

	/**
	 * 
	 * @param node
	 */
	private void fireMapNodeSelected(MapNode node) {
		mapElementEvent.setSite(null);
		mapElementEvent.setEdge(null);
		mapElementEvent.setNode(node);
		List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (MapElementListener l : listeners) {
				l.nodeSelected(mapElementEvent);
			}
		}
	}

	/**
	 * 
	 * @param site
	 */
	private void fireSiteSelected(SiteNode site) {
		mapElementEvent.setSite(site);
		mapElementEvent.setEdge(null);
		mapElementEvent.setNode(null);
		List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (MapElementListener l : listeners) {
				l.siteSelected(mapElementEvent);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public MapEdge getSelectedEdge() {
		MapEdgeEntry entry = (MapEdgeEntry) edgeJList.getSelectedValue();
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
		MapNodeEntry entry = (MapNodeEntry) nodeJList.getSelectedValue();
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
		MapNode node = getSelectedNode();
		if (node != null)
			node.apply(eventFirer);
		else
			fireMapNodeSelected(null);
	}

	/**
         * 
         */
	private void init() {
		setBorder(BorderFactory.createTitledBorder(title));
		nodeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeJList.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent ev) {
						if (!ev.getValueIsAdjusting()) {
							handleNodeSelection();
						}
					}

				});
		edgeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		edgeJList.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent ev) {
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
	public synchronized void removeMapElementListener(MapElementListener l) {
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
	public void setEdgeList(ListModel edgeList) {
		edgeJList.setModel(edgeList);
	}

	/**
	 * @param mediator
	 *            the mediator to set
	 */
	public void setMediator(RouteMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * 
	 * @param nodeList
	 */
	public void setNodeList(ListModel nodeList) {
		nodeJList.setModel(nodeList);
	}

	/**
         * 
         * 
         */
	public void setSelectedElement(MapEdge edge) {
		int idx = mediator.getEdgeListIndex(edge);
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
	public void setSelectedElement(MapNode node) {
		int idx = mediator.getNodeListIndex(node);
		if (idx >= 0) {
			nodeJList.setSelectedIndex(idx);
			setSelectedIndex(NODE_TAB_INDEX);
			return;
		}
		nodeJList.setSelectedIndex(nodeJList.getSelectedIndex());
	}

}
