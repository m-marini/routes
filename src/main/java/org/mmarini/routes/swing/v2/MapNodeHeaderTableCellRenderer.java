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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MapNodeHeaderTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	private final Color bg;
	private List<Color> colorMap;

	/**
	 *
	 */
	public MapNodeHeaderTableCellRenderer() {
		this.bg = getBackground();
		this.colorMap = List.of();
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
	}

	/**
	 *
	 */
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		setText(String.valueOf(value));
		final Color bg = column > 0 && column <= colorMap.size() ? colorMap.get(column - 1) : this.bg;
		setBackground(bg);
		return this;
	}

	/**
	 * @param colorMap the colorMap to set
	 */
	public void setColorMap(final List<Color> colorMap) {
		this.colorMap = colorMap;
	}

}
