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

import static java.lang.String.format;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.TrafficStats;
import org.mmarini.routes.model.v2.Traffics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TrafficsTable extends JTable {
	private class TrafficsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return columnIndex == 0 ? MapNode.class : String.class;
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
				final Optional<String> timeStr = stats.getTime(from, to).stream().mapToObj(t -> {
					return format("%.0f", t);
				}).findAny();
				final Optional<String> minTimeStr = stats.getMinTime(from, to).stream().mapToObj(t -> {
					return format("%.0f", t);
				}).findAny();
				return format("%s / %s", timeStr.orElse("-"), minTimeStr.orElse("-"));
			}
		}
	}

	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(TrafficsTable.class);

	private final MapNodeTableCellRenderer cellRenderer;
	private final MapNodeHeaderTableCellRenderer headerRenderer;
	private final List<MapNode> nodes;
	private final TrafficStats stats;

	/**
	 * @param traffics
	 */
	public TrafficsTable(final Traffics traffics) {
		logger.debug("TrafficsTable");
		this.cellRenderer = new MapNodeTableCellRenderer();
		this.headerRenderer = new MapNodeHeaderTableCellRenderer();
		this.nodes = traffics.getMap().getSites().stream().sorted().collect(Collectors.toList());
		final Map<MapNode, Color> colorMap = SwingUtils.buildColorMap(nodes);
		cellRenderer.setColorMap(colorMap);
		headerRenderer.setColorMap(nodes.stream().map(colorMap::get).collect(Collectors.toList()));
		stats = TrafficStats.create().setEdgeStats(traffics.getTraffics());
		setModel(new TrafficsTableModel());
		setDefaultRenderer(MapNode.class, cellRenderer);
		getTableHeader().setDefaultRenderer(headerRenderer);
	}
}
