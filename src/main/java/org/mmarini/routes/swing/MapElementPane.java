/*
 * MapElementPane.java
 *
 * $Id: MapElementPane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 05/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.CardLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.SiteNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: MapElementPane.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 */
public class MapElementPane extends JPanel {

	private static final String EMPTY_CARD = "Empty"; //$NON-NLS-1$

	private static final String NODE_CARD = "Node"; //$NON-NLS-1$

	private static final String EDGE_CARD = "Edge"; //$NON-NLS-1$

	private static final String SITE_CARD = "Site"; //$NON-NLS-1$

	private static final long serialVersionUID = 1L;

	private final CardLayout cardLayout;

	private final SiteNodePane siteNodePane;

	private final MapNodePane mapNodePane;

	private final EdgePane edgePane;

	/**
	     *
	     */
	public MapElementPane() {
		cardLayout = new CardLayout();
		mapNodePane = new MapNodePane();
		siteNodePane = new SiteNodePane();
		edgePane = new EdgePane();
		createContent();
	}

	/**
	     *
	     */
	private void createContent() {
		setLayout(cardLayout);
		final Box empty = Box.createVerticalBox();
		empty.setBorder(BorderFactory.createTitledBorder(Messages.getString("MapElementPane.emptyPane.title"))); //$NON-NLS-1$
		add(empty, EMPTY_CARD);
		add(mapNodePane, NODE_CARD);
		add(siteNodePane, SITE_CARD);
		add(edgePane, EDGE_CARD);
	}

	/**
	 * @param mediator
	 */
	public void setMediator(final RouteMediator mediator) {
		mapNodePane.setMediator(mediator);
		siteNodePane.setMediator(mediator);
		edgePane.setMediator(mediator);
	}

	/**
	 * @see org.mmarini.routes.model.MapElementVisitor#visit(org.mmarini.routes.model.MapEdge)
	 */
	public void setSelectedElement(final MapEdge edge) {
		if (edge != null) {
			edgePane.setEdge(edge);
			cardLayout.show(this, EDGE_CARD);
		} else {
			cardLayout.show(this, EMPTY_CARD);
		}
	}

	/**
	 * @see org.mmarini.routes.model.MapElementVisitor#visit(org.mmarini.routes.model.MapNode)
	 */
	public void setSelectedElement(final MapNode node) {
		if (node != null) {
			mapNodePane.setNode(node);
			cardLayout.show(this, NODE_CARD);
		} else {
			cardLayout.show(this, EMPTY_CARD);
		}
	}

	/**
	 * @see org.mmarini.routes.model.MapElementVisitor#visit(org.mmarini.routes.model.SiteNode)
	 */
	public void setSelectedElement(final SiteNode node) {
		if (node != null) {
			siteNodePane.setNode(node);
			cardLayout.show(this, SITE_CARD);
		} else {
			cardLayout.show(this, EMPTY_CARD);
		}
	}
}
