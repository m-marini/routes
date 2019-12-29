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

import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 *
 */
public class ExplorerPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	private final DefaultListModel<MapEdge> edgeList;
	private final JList<MapNode> nodeJList;
	private final JList<MapEdge> edgeJList;
	private final Observable<Optional<MapEdge>> edgeSelectionObs;
	private final Observable<Optional<MapNode>> nodeSelectionObs;

	/**
	 *
	 */
	public ExplorerPane() {
		edgeList = new DefaultListModel<>();
		nodeJList = new JList<>();
		edgeJList = new JList<>(edgeList);
		edgeSelectionObs = SwingObservable.listSelection(edgeJList).map(ev -> getMapEdge());
		nodeSelectionObs = SwingObservable.listSelection(nodeJList).map(ev -> getMapNode());
		init().createContent();
	}

	/**
	 * Creates the content
	 *
	 * @return
	 */
	private ExplorerPane createContent() {
		addTab(Messages.getString("ExplorerPane.nodeTabe.title"), new JScrollPane(nodeJList)); //$NON-NLS-1$
		addTab(Messages.getString("ExplorerPane.edgeTab.title"), new JScrollPane(edgeJList)); //$NON-NLS-1$
		return this;
	}

	/**
	 * @return the edgeSelectionObs
	 */
	public Observable<Optional<MapEdge>> getEdgeSelectionObs() {
		return edgeSelectionObs;
	}

	/**
	 * Returns the selected map edge
	 */
	public Optional<MapEdge> getMapEdge() {
		return Optional.ofNullable(edgeJList.getSelectedValue());
	}

	/**
	 * Returns the selected map node
	 */
	public Optional<MapNode> getMapNode() {
		return Optional.ofNullable(nodeJList.getSelectedValue());
	}

	/**
	 * @return the nodeSelectionObs
	 */
	public Observable<Optional<MapNode>> getNodeSelectionObs() {
		return nodeSelectionObs;
	}

	/**
	 * Initializes the panel
	 *
	 * @return
	 */
	private ExplorerPane init() {
		final String title = Messages.getString("ExplorerPane.title"); //$NON-NLS-1$
		setBorder(BorderFactory.createTitledBorder(title));
		nodeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		edgeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return this;
	}
}
