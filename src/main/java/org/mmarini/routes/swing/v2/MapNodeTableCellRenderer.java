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
import java.awt.Component;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.mmarini.routes.model.v2.MapNode;

public class MapNodeTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	private Map<MapNode, Color> colorMap;

	/**
	 *
	 */
	public MapNodeTableCellRenderer() {
		this.colorMap = Map.of();
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
	}

	/**
	 *
	 */
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		final MapNode node = (MapNode) value;
		setText(node.getShortName());
		final Color bg = colorMap.getOrDefault(node, table.getBackground());
		setBackground(bg);
		setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		return this;
	}

	/**
	 * @param colorMap the colorMap to set
	 */
	public void setColorMap(final Map<MapNode, Color> colorMap) {
		this.colorMap = colorMap;
	}

}
