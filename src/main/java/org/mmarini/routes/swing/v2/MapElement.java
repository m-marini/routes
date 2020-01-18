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
public interface MapElement {

	public static class EdgeElement extends Empty {
		private final Optional<MapEdge> edge;

		/**
		 * @param edge
		 */
		public EdgeElement(final MapEdge edge) {
			this.edge = Optional.of(edge);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final EdgeElement other = (EdgeElement) obj;
			if (edge == null) {
				if (other.edge != null) {
					return false;
				}
			} else if (!edge.equals(other.edge)) {
				return false;
			}
			return true;
		}

		@Override
		public Optional<MapEdge> getEdge() {
			return edge;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
			return result;
		}

		@Override
		public boolean isEdge() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
	}

	public static class Empty implements MapElement {
		static final MapElement EMPTY_ELEMENT = new Empty();

		protected Empty() {
			super();
		}

		@Override
		public Optional<MapEdge> getEdge() {
			return Optional.empty();
		}

		@Override
		public Optional<MapNode> getNode() {
			return Optional.empty();
		}

		@Override
		public Optional<SiteNode> getSite() {
			return Optional.empty();
		}

		@Override
		public boolean isEdge() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public boolean isNode() {
			return false;
		}

		@Override
		public boolean isSite() {
			return false;
		}
	}

	public static class NodeElement extends Empty {
		private final Optional<MapNode> node;

		/**
		 * @param node
		 */
		public NodeElement(final MapNode node) {
			this.node = Optional.of(node);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final NodeElement other = (NodeElement) obj;
			if (node == null) {
				if (other.node != null) {
					return false;
				}
			} else if (!node.equals(other.node)) {
				return false;
			}
			return true;
		}

		@Override
		public Optional<MapNode> getNode() {
			return node;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			return result;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean isNode() {
			return true;
		}

	}

	public static class SiteElement extends Empty {
		private final Optional<SiteNode> site;

		/**
		 * @param node
		 */
		public SiteElement(final SiteNode site) {
			this.site = Optional.of(site);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final SiteElement other = (SiteElement) obj;
			if (site == null) {
				if (other.site != null) {
					return false;
				}
			} else if (!site.equals(other.site)) {
				return false;
			}
			return true;
		}

		@Override
		public Optional<SiteNode> getSite() {
			return site;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((site == null) ? 0 : site.hashCode());
			return result;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean isSite() {
			return true;
		}
	}

	/**
	 * Returns the element selection for an edge
	 *
	 * @param edge the edge
	 */
	public static MapElement create(final MapEdge edge) {
		return new EdgeElement(edge);
	}

	/**
	 * Returns the element selection for a node
	 *
	 * @param node the node
	 */
	public static MapElement create(final MapNode node) {
		return new NodeElement(node);
	}

	/**
	 * Returns the element selection for a site
	 *
	 * @param site the site
	 */
	public static MapElement create(final SiteNode site) {
		return new SiteElement(site);
	}

	/**
	 * Returns the element selection for an edge
	 *
	 * @param edge the edge
	 */
	public static MapElement empty() {
		return Empty.EMPTY_ELEMENT;
	}

	public Optional<MapEdge> getEdge();

	/**
	 * Returns the selected node
	 */
	public Optional<MapNode> getNode();

	/**
	 * Returns the selected site
	 */
	public Optional<SiteNode> getSite();

	/**
	 * Returns true if the element is a edge
	 */
	public boolean isEdge();

	/**
	 * Returns true id there is no element
	 */
	public boolean isEmpty();

	/**
	 * Returns true if the element is a node
	 */
	public boolean isNode();

	/**
	 * Returns true if the element is a site
	 */
	public boolean isSite();
}
