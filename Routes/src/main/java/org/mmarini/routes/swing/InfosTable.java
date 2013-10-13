/*
 * InfosTable.java
 *
 * $Id: InfosTable.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 01/feb/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.mmarini.routes.model.SiteNode;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: InfosTable.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class InfosTable extends JTable {

	private static final double CELL_COLOR_SATURATION = 0.3;

	private static final long serialVersionUID = 1L;

	private RouteMediator mediator;

	private SiteTableCellRenderer siteTableCellRenderer;

	/**
	 * @param model
	 */
	public InfosTable(TableModel model) {
		super(model);
		siteTableCellRenderer = new SiteTableCellRenderer();

		setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			/**
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
			 *      java.lang.Object, boolean, boolean, int, int)
			 */
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				RouteInfoModel model = (RouteInfoModel) getModel();
				double v = ((Number) value).doubleValue();
				setText(NumberFormat.getNumberInstance().format(value));
				double max = model.getMaximumFlux();
				double min = model.getMinimumFlux();
				if (column == model.getColumnCount() - 1 || column == row + 1) {
					setBackground(InfosTable.this.getBackground());
				} else {
					double val;
					if (max == min)
						val = 1;
					else
						val = (v - min) / (max - min);
					setBackground(SwingUtils.getInstance().computeColor(val,
							CELL_COLOR_SATURATION));
				}
				setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1,
						Color.GRAY));
				setHorizontalAlignment(SwingConstants.RIGHT);
				return this;
			}

		});
		setDefaultRenderer(SiteNode.class, siteTableCellRenderer);
		setRowSelectionAllowed(false);
		JTableHeader header = getTableHeader();
		header.setDefaultRenderer(new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			/**
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
			 *      java.lang.Object, boolean, boolean, int, int)
			 */
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				RouteInfoModel model = (RouteInfoModel) getModel();
				JLabel label = new JLabel();
				Color bg = label.getBackground();
				if (column == 0) {
					setText(Messages.getString("InfosTable.flux.label")); //$NON-NLS-1$
					setBackground(bg);
					setBorder(BorderFactory.createRaisedBevelBorder());
				} else if (column == model.getColumnCount() - 1) {
					setText(Messages.getString("InfosTable.total.label")); //$NON-NLS-1$
					setBorder(BorderFactory.createRaisedBevelBorder());
					setBackground(bg);
				} else {
					SiteNode node = model.getNode(column - 1);
					setText(mediator.retrieveNodeName(node));
					setBackground(mediator.getNodeColor(node));
					setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1,
							Color.GRAY));
				}
				setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}

		});
		header.setReorderingAllowed(false);
		header.setResizingAllowed(true);
	}

	/**
	 * @param mediator
	 *            the mediator to set
	 */
	public void setMediator(RouteMediator mediator) {
		this.mediator = mediator;
		siteTableCellRenderer.setMediator(mediator);
	}

}
