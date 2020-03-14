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
 * The list cell renderer for site nodes
 * <p>
 * The cell is rendered with color and name of site
 * </p>
 */
public class SiteListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 3743457808802444412L;
	private Map<MapNode, Color> colorMap;
	private Map<MapNode, Color> selectionColorMap;

	/** Create the renderer */
	public SiteListCellRenderer() {
		this.colorMap = Map.of();
		this.selectionColorMap = Map.of();
	}

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
	 * Sets the color map
	 *
	 * @param colorMap the color map
	 * @return the renderer
	 */
	public SiteListCellRenderer setColorMap(final Map<MapNode, Color> colorMap) {
		this.colorMap = colorMap;
		return this;
	}

	/**
	 * Sets the color map for selected sites
	 *
	 * @param selectionColorMap the color map for selected sites
	 * @return the renderer
	 */
	public SiteListCellRenderer setSelectionColorMap(final Map<MapNode, Color> selectionColorMap) {
		this.selectionColorMap = selectionColorMap;
		return this;
	}
}
