/*
 * InfosTable.java
 *
 * $Id: TrafficInfoTable.java,v 1.3 2010/10/19 20:32:59 marco Exp $
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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: TrafficInfoTable.java,v 1.3 2010/10/19 20:32:59 marco Exp $
 *
 */
public class TrafficInfoTable extends JTable {

	private static final long serialVersionUID = 1L;

	private RouteMediator mediator;

	/**
	 * @param model
	 */
	public TrafficInfoTable(final TableModel model) {
		super(model);
		setRowSelectionAllowed(false);
		final JTableHeader header = getTableHeader();
		header.setReorderingAllowed(false);
		header.setResizingAllowed(true);
		setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			/**
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
			 *      java.lang.Object, boolean, boolean, int, int)
			 */
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
					final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				NumberFormat formatter = NumberFormat.getNumberInstance();
				switch (column) {
				case 3:
					formatter = NumberFormat.getPercentInstance();
					break;
				default:
					break;
				}
				setText(formatter.format(value));
				setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
				setBackground(TrafficInfoTable.this.getBackground());
				setHorizontalAlignment(SwingConstants.RIGHT);
				return this;
			}

		});
		setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			/**
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
			 *      java.lang.Object, boolean, boolean, int, int)
			 */
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
					final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final NumberFormat formatter = NumberFormat.getIntegerInstance();
				setText(formatter.format(value));
				setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
				setBackground(TrafficInfoTable.this.getBackground());
				setHorizontalAlignment(SwingConstants.RIGHT);
				return this;
			}

		});
		setDefaultRenderer(String.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			/**
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
			 *      java.lang.Object, boolean, boolean, int, int)
			 */
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
					final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				setText(String.valueOf(value));
				final TrafficInfoModel model = (TrafficInfoModel) table.getModel();
				final int modelRow = table.convertRowIndexToModel(row);
				setBackground(mediator.getNodeColor(model.getNode(modelRow)));
				setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
				setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}

		});
		setAutoCreateRowSorter(true);
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(final RouteMediator mediator) {
		this.mediator = mediator;
	}

}
