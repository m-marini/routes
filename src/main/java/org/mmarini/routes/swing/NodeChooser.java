/*
 * NodeChooser.java
 *
 * $Id: NodeChooser.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 07/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.mmarini.routes.model.MapNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: NodeChooser.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class NodeChooser extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JList nodeList;

	/**
	     * 
	     */
	public NodeChooser() {
		nodeList = new JList();
		nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		createContent();
	}

	/**
	     * 
	     */
	public void clearSelection() {
		final int idx = nodeList.getSelectedIndex();
		nodeList.setSelectedIndex(idx);
	}

	/**
	     * 
	     */
	private void createContent() {
		setLayout(new BorderLayout());
		add(new JScrollPane(nodeList), BorderLayout.CENTER);
	}

	/**
	 * @return
	 */
	public MapNode getSelectedNode() {
		final MapNodeEntry entry = (MapNodeEntry) nodeList.getSelectedValue();
		if (entry != null) {
			return entry.getNode();
		}
		return null;
	}

	/**
	 * @param nodeList2
	 */
	public void setNodeList(final DefaultListModel nodeList2) {
		nodeList.setModel(nodeList2);
	}
}
