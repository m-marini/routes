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

/**
 * Map element with the information about the selected element on map
 */
public interface MapElement {

	/** The edge element */
	public static class EdgeElement extends Empty {
		private final Optional<MapEdge> edge;

		/**
		 * Create the edge element
		 *
		 * @param edge the edge
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

	/** The empty element */
	public static class Empty implements MapElement {
		static final MapElement EMPTY_ELEMENT = new Empty();

		/** Create the empty element */
		protected Empty() {
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
	}

	/** The node elment */
	public static class NodeElement extends Empty {
		private final Optional<MapNode> node;

		/**
		 * Creates the node element
		 *
		 * @param node the node
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
	 * Returns the element selection for an edge
	 *
	 * @param edge the edge
	 */
	public static MapElement empty() {
		return Empty.EMPTY_ELEMENT;
	}

	/** Returns the edge element */
	public Optional<MapEdge> getEdge();

	/** Returns the selected node */
	public Optional<MapNode> getNode();

	/** Returns true if the element is a edge */
	public boolean isEdge();

	/** Returns true if there is no element */
	public boolean isEmpty();

	/** Returns true if the element is a node */
	public boolean isNode();
}
