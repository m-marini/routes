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

package org.mmarini.routes.model.v2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mmarini.routes.model.Constants;

/**
 *
 */
public class GeoMap implements Constants {
	private final static GeoMap EMPTY = new GeoMap(Collections.emptySet(), Collections.emptySet(),
			Collections.emptySet());

	/**
	 *
	 * @return
	 */
	public static GeoMap create() {
		return EMPTY;
	}

	private final Set<SiteNode> sites;
	private final Set<MapNode> nodes;
	private final Set<MapEdge> edges;

	/**
	 *
	 * @param sites
	 * @param nodes
	 * @param edges
	 */
	protected GeoMap(final Set<SiteNode> sites, final Set<MapNode> nodes, final Set<MapEdge> edges) {
		super();
		this.sites = sites;
		this.nodes = nodes;
		this.edges = edges;
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public GeoMap add(final MapEdge edge) {
		if (edges.contains(edge)) {
			return this;
		} else {
			final Set<MapEdge> newEdges = new HashSet<>(edges);
			newEdges.add(edge);
			return setEdges(newEdges);
		}
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public GeoMap add(final MapNode node) {
		if (nodes.contains(node)) {
			return this;
		} else {
			final Set<MapNode> newNodes = new HashSet<>(nodes);
			newNodes.add(node);
			return setNodes(newNodes);
		}
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public GeoMap add(final SiteNode node) {
		if (sites.contains(node)) {
			return this;
		} else {
			final Set<SiteNode> newSites = new HashSet<>(sites);
			newSites.add(node);
			return setSites(newSites);
		}
	}

	/**
	 *
	 * @return
	 */
	public Set<MapEdge> getEdges() {
		return edges;
	}

	/**
	 *
	 * @return
	 */
	public Set<MapNode> getNodes() {
		return nodes;
	}

	/**
	 *
	 * @return
	 */
	public Set<SiteNode> getSites() {
		return sites;
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public GeoMap remove(final MapEdge edge) {
		if (edges.contains(edge)) {
			final Set<MapEdge> newEdges = new HashSet<>(edges);
			newEdges.remove(edge);
			return setEdges(newEdges);
		} else {
			return this;
		}
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public GeoMap remove(final MapNode node) {
		if (nodes.contains(node)) {
			final Set<MapNode> newNodes = new HashSet<>(nodes);
			newNodes.remove(node);
			return setNodes(newNodes);
		} else {
			return this;
		}
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public GeoMap remove(final SiteNode node) {
		if (sites.contains(node)) {
			final Set<SiteNode> newSites = new HashSet<>(sites);
			newSites.remove(node);
			return setSites(newSites);
		} else {
			return this;
		}
	}

	/**
	 *
	 * @param edges
	 * @return
	 */
	public GeoMap setEdges(final Set<MapEdge> edges) {
		return new GeoMap(sites, nodes, edges);
	}

	/**
	 *
	 * @param nodes
	 * @return
	 */
	public GeoMap setNodes(final Set<MapNode> nodes) {
		return new GeoMap(sites, nodes, edges);
	}

	/**
	 *
	 * @param sites
	 * @return
	 */
	public GeoMap setSites(final Set<SiteNode> sites) {
		return new GeoMap(sites, nodes, edges);
	}
}
