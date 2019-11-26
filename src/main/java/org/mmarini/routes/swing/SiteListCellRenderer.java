/**
 *
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingConstants;

import org.mmarini.routes.model.MapNode;

/**
 * @author US00852
 *
 */
public class SiteListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 3743457808802444412L;
	private RouteMediator mediator;

	/**
	 *
	 */
	public SiteListCellRenderer() {
	}

	/**
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
	 *      java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		final String name = (String) value;
		setText(name);
		final MapNode node = mediator.findSiteNode(name);
		Color bg = mediator.getNodeColor(node);
		Color fg = list.getForeground();
		if (isSelected) {
			bg = bg.darker();
			fg = Color.WHITE;
		}
		setForeground(fg);
		setBackground(bg);
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
