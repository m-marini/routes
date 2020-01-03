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

import java.awt.Component;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.SiteNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 *
 */
public class ExplorerPane extends JTabbedPane {
	static class StringCellRenderer<T> extends JLabel implements ListCellRenderer<T> {
		private static final long serialVersionUID = 1L;
		private final Function<T, String> f;

		/**
		 * @param f
		 */
		public StringCellRenderer(final Function<T, String> converter) {
			super();
			this.f = converter;
		}

		@Override
		public Component getListCellRendererComponent(final JList<? extends T> list, final T item, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			setText(f.apply(item));
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(ExplorerPane.class);

	private static final long serialVersionUID = 1L;
	private static final int SITE_TAB = 0;
	private static final int NODE_TAB = 1;
	private static final int EDGE_TAB = 2;

	private final DefaultListModel<SiteNode> siteList;
	private final DefaultListModel<MapNode> nodeList;
	private final DefaultListModel<MapEdge> edgeList;
	private final JList<SiteNode> siteJList;
	private final JList<MapNode> nodeJList;
	private final JList<MapEdge> edgeJList;
	private final Observable<MapEdge> edgeObs;
	private final Observable<MapNode> nodeObs;
	private final Observable<SiteNode> siteObs;

	/**
	 *
	 */
	public ExplorerPane() {
		siteList = new DefaultListModel<>();
		nodeList = new DefaultListModel<>();
		edgeList = new DefaultListModel<>();
		siteJList = new JList<>(siteList);
		nodeJList = new JList<>(nodeList);
		edgeJList = new JList<>(edgeList);
		edgeObs = SwingObservable.listSelection(edgeJList)
				.filter(ev -> !ev.getValueIsAdjusting() && edgeJList.getSelectedIndex() >= 0)
				.map(ev -> edgeJList.getSelectedValue()).doOnNext(edge -> logger.debug("Emit edge event {}", edge));
		siteObs = SwingObservable.listSelection(siteJList)
				.filter(ev -> !ev.getValueIsAdjusting() && siteJList.getSelectedIndex() >= 0)
				.map(ev -> siteJList.getSelectedValue()).doOnNext(site -> logger.debug("Emit site event {}", site));
		nodeObs = SwingObservable.listSelection(nodeJList)
				.filter(ev -> !ev.getValueIsAdjusting() && nodeJList.getSelectedIndex() >= 0)
				.map(ev -> nodeJList.getSelectedValue()).doOnNext(node -> logger.debug("Emit node ebent {}", node));
		siteJList.setCellRenderer(new StringCellRenderer<>(a -> a.getShortName()));
		nodeJList.setCellRenderer(new StringCellRenderer<>(a -> a.getShortName()));
		edgeJList.setCellRenderer(new StringCellRenderer<>(a -> a.getShortName()));
		init().createContent();
	}

	/**
	 * Returns the explorer panel with selection cleared
	 */
	public ExplorerPane clearSelection() {
		logger.debug("clearSelection");
		final int siteIdx = siteJList.getSelectedIndex();
		if (siteIdx >= 0) {
			siteJList.removeSelectionInterval(siteIdx, siteIdx);
		}
		final int nodeIdx = nodeJList.getSelectedIndex();
		if (nodeIdx >= 0) {
			nodeJList.removeSelectionInterval(nodeIdx, nodeIdx);
		}
		final int edgeIdx = edgeJList.getSelectedIndex();
		if (nodeIdx >= 0) {
			edgeJList.removeSelectionInterval(edgeIdx, edgeIdx);
		}
		return this;
	}

	/**
	 * Creates the content
	 *
	 * @return
	 */
	private ExplorerPane createContent() {
		addTab(Messages.getString("ExplorerPane.siteTabe.title"), new JScrollPane(siteJList)); //$NON-NLS-1$
		addTab(Messages.getString("ExplorerPane.nodeTabe.title"), new JScrollPane(nodeJList)); //$NON-NLS-1$
		addTab(Messages.getString("ExplorerPane.edgeTab.title"), new JScrollPane(edgeJList)); //$NON-NLS-1$
		return this;
	}

	/**
	 * @return the edgeSelectionObs
	 */
	public Observable<MapEdge> getEdgeObs() {
		return edgeObs;
	}

	/**
	 * @return the nodeSelectionObs
	 */
	public Observable<MapNode> getNodeObs() {
		return nodeObs;
	}

	/**
	 * @return the nodeSelectionObs
	 */
	public Observable<SiteNode> getSiteObs() {
		return siteObs;
	}

	/**
	 * Initializes the panel
	 *
	 * @return
	 */
	private ExplorerPane init() {
		final String title = Messages.getString("ExplorerPane.title"); //$NON-NLS-1$
		setBorder(BorderFactory.createTitledBorder(title));
		siteJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		edgeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return this;
	}

	/**
	 * Returns the explorer panel for the given map
	 *
	 * @param map the map
	 */
	public ExplorerPane setMap(final GeoMap map) {
		logger.debug("setMap {}", map);
		final List<SiteNode> sites = map.getSites().stream().sorted().collect(Collectors.toList());
		final List<MapNode> nodes = map.getNodes().stream().sorted().collect(Collectors.toList());
		final List<MapEdge> edges = map.getEdges().stream().sorted().collect(Collectors.toList());
		siteList.removeAllElements();
		siteList.addAll(sites);
		nodeList.removeAllElements();
		nodeList.addAll(nodes);
		edgeList.removeAllElements();
		edgeList.addAll(edges);
		return this;
	}

	/**
	 * Returns the explorer panel with selected edge
	 *
	 * @param edge the selected edge
	 */
	public ExplorerPane setSelectedEdge(final MapEdge edge) {
		logger.debug("setSelectedEdge {}", edge);
		if (!edge.equals(edgeJList.getSelectedValue())) {
			clearSelection();
			edgeJList.setSelectedValue(edge, true);
		}
		setSelectedIndex(EDGE_TAB);
		return this;
	}

	/**
	 * Returns the explorer panel with selected node
	 *
	 * @param node the selected node
	 */
	public ExplorerPane setSelectedNode(final MapNode node) {
		logger.debug("setSelectedNode {}", node);
		if (!node.equals(nodeJList.getSelectedValue())) {
			clearSelection();
			nodeJList.setSelectedValue(node, true);
		}
		setSelectedIndex(NODE_TAB);
		return this;
	}

	/**
	 * Returns the explorer panel with selected site
	 *
	 * @param site the selected site
	 */
	public ExplorerPane setSelectedSite(final SiteNode site) {
		logger.debug("setSelectedSite {}", site);
		if (!site.equals(siteJList.getSelectedValue())) {
			clearSelection();
			siteJList.setSelectedValue(site, true);
		}
		setSelectedIndex(SITE_TAB);
		return this;
	}
}