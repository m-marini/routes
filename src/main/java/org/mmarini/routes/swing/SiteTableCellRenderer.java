/**
 *
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.mmarini.routes.model.MapNode;

/**
 * @author US00852
 *
 */
public class SiteTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 3743457808802444412L;
	private RouteMediator mediator;

	/**
	 *
	 */
	public SiteTableCellRenderer() {
	}

	/**
	 *
	 */
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		setText(mediator.retrieveNodeName((MapNode) value));
		setBackground(mediator.getNodeColor((MapNode) value));
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
		setHorizontalAlignment(SwingConstants.CENTER);
		return this;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(final RouteMediator mediator) {
		this.mediator = mediator;
	}

}