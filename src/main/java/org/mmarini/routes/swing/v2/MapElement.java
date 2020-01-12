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

import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.SiteNode;

/**
 * Keeps the information about the selected element on map
 */
public class MapElement {

	/**
	 * Returns the element selection for an edge
	 *
	 * @param edge the edge
	 */
	public static MapElement create(final MapEdge edge) {
		return new MapElement(Optional.empty(), Optional.empty(), Optional.of(edge));
	}

	/**
	 * Returns the element selection for a node
	 *
	 * @param node the node
	 */
	public static MapElement create(final MapNode node) {
		return new MapElement(Optional.empty(), Optional.of(node), Optional.empty());
	}

	/**
	 * Returns the element selection for a site
	 *
	 * @param site the site
	 */
	public static MapElement create(final SiteNode site) {
		return new MapElement(Optional.of(site), Optional.empty(), Optional.empty());
	}

	private final Optional<SiteNode> site;
	private final Optional<MapNode> node;
	private final Optional<MapEdge> edge;

	/**
	 * @param site
	 * @param node
	 * @param edge
	 */
	protected MapElement(final Optional<SiteNode> site, final Optional<MapNode> node, final Optional<MapEdge> edge) {
		super();
		this.site = site;
		this.node = node;
		this.edge = edge;
	}

	/**
	 * Returns the selected edge
	 */
	public Optional<MapEdge> getEdge() {
		return edge;
	}

	/**
	 * Returns the selected node
	 */
	public Optional<MapNode> getNode() {
		return node;
	}

	/**
	 * Returns the selected site
	 */
	public Optional<SiteNode> getSite() {
		return site;
	}

	/**
	 * Returns true if the element is a edge
	 */
	public boolean isEdge() {
		return edge.isPresent();
	}

	/**
	 * Returns true if the element is a node
	 */
	public boolean isNode() {
		return node.isPresent();
	}

	/**
	 * Returns true if the element is a site
	 */
	public boolean isSite() {
		return site.isPresent();
	}

}
