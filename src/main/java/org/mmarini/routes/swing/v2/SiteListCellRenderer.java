/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.mmarini.routes.model.v2.MapNode;

/**
 * @author US00852
 *
 */
public class SiteListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 3743457808802444412L;
	private Map<MapNode, Color> colorMap;
	private Map<MapNode, Color> selectionColorMap;

	/**
	 *
	 */
	public SiteListCellRenderer() {
		this.colorMap = Map.of();
		this.selectionColorMap = Map.of();
	}

	/**
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
	 *      java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		final MapNode node = (MapNode) value;
		final String name = node.getShortName();
		setText(name);
		if (isSelected) {
			final Color bg = selectionColorMap.getOrDefault(node, list.getSelectionBackground());
			setBackground(bg);
			setForeground(list.getSelectionForeground());
		} else {
			final Color bg = colorMap.getOrDefault(node, list.getBackground());
			setBackground(bg);
			setForeground(list.getForeground());
		}
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
		return this;
	}

	/**
	 *
	 * @param colorMap
	 * @return
	 */
	public SiteListCellRenderer setColorMap(final Map<MapNode, Color> colorMap) {
		this.colorMap = colorMap;
		return this;
	}

	/**
	 * @param selectionColorMap the selectionColorMap to set
	 * @return
	 */
	public SiteListCellRenderer setSelectionColorMap(final Map<MapNode, Color> selectionColorMap) {
		this.selectionColorMap = selectionColorMap;
		return this;
	}
}
