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

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The weights table woth the weights from a departure node to a destination
 * node
 */
public class WeightsTable extends JTable {
	private class WeightsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return columnIndex == 0 ? MapNode.class : Double.class;
		}

		@Override
		public int getColumnCount() {
			return nodes.size() + 1;
		}

		@Override
		public String getColumnName(final int column) {
			return column == 0 ? "" : nodes.get(column - 1).getShortName();
		}

		@Override
		public int getRowCount() {
			return nodes.size();
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			final MapNode from = nodes.get(rowIndex);
			if (columnIndex == 0) {
				return from;
			} else {
				final MapNode to = nodes.get(columnIndex - 1);
				return weights.getOrDefault(Tuple.of(from, to), 0.0);
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex > 0 && columnIndex - 1 != rowIndex;
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			if (columnIndex > 0 && columnIndex - 1 != rowIndex) {
				final MapNode from = nodes.get(rowIndex);
				final MapNode to = nodes.get(columnIndex - 1);
				final Tuple2<MapNode, MapNode> key = Tuple.of(from, to);
				final Map<Tuple2<MapNode, MapNode>, Double> newWeights = weights.entrySet().parallelStream().map(e -> {
					final Tuple2<MapNode, MapNode> k = e.getKey();
					return Tuple.of(k, k.equals(key) ? (Double) aValue : e.getValue());
				}).collect(Collectors.toMap(Tuple2::get1, Tuple2::get2));
				weights = newWeights;
			}
		}

	}

	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(WeightsTable.class);

	private final MapNodeTableCellRenderer cellRenderer;
	private final MapNodeHeaderTableCellRenderer headerRenderer;
	private Map<Tuple2<MapNode, MapNode>, Double> weights;
	private List<MapNode> nodes;

	/** Creates the table */
	public WeightsTable() {
		logger.debug("WeightTable");
		this.weights = Map.of();
		this.cellRenderer = new MapNodeTableCellRenderer();
		this.headerRenderer = new MapNodeHeaderTableCellRenderer();
		this.nodes = List.of();
		setModel(new WeightsTableModel());
		setDefaultRenderer(MapNode.class, cellRenderer);
		getTableHeader().setDefaultRenderer(headerRenderer);
	}

	/** Return the weights */
	public Map<Tuple2<MapNode, MapNode>, Double> getWeights() {
		return weights;
	}

	/**
	 * Sets the weights
	 *
	 * @param weights the weights
	 * @return the table
	 */
	public WeightsTable setWeights(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		this.weights = weights;
		nodes = weights.keySet().stream().flatMap(k -> {
			return Stream.of(k.get1(), k.get2());
		}).collect(Collectors.toSet()).stream().sorted().collect(Collectors.toList());
		final Map<MapNode, Color> colorMap = SwingUtils.buildColorMap(nodes);
		cellRenderer.setColorMap(colorMap);
		final List<Color> cm = nodes.stream().map(colorMap::get).collect(Collectors.toList());
		headerRenderer.setColorMap(cm);
		((WeightsTableModel) getModel()).fireTableStructureChanged();
		return this;
	}
}
