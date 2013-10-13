/**
 * 
 */
package org.mmarini.routes.swing;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.mmarini.routes.model.SiteNode;
import org.mmarini.routes.xml.Path;

/**
 * @author US00852
 * 
 */
public class RouteTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -2634066472823732066L;
	private static final String[] COLUMN_NAMES = { "Destination", "Weight" };
	private List<Path> routes;

	/**
	 * 
	 */
	public RouteTableModel() {
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 1)
			return Double.class;
		else
			return SiteNode.class;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (routes == null)
			return 0;
		return routes.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		Path p = routes.get(row);
		switch (col) {
		case 0:
			return p.getDestination();
		default:
			return p.getWeight();
		}
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	/**
	 * 
	 * @param paths
	 */
	public void setPaths(List<Path> paths) {
		routes = paths;
		fireTableDataChanged();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 *      int, int)
	 */
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		routes.get(rowIndex).setWeight((Double) value);
	}
}
