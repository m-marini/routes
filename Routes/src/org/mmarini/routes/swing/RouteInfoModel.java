/**
 * 
 */
package org.mmarini.routes.swing;

import javax.swing.table.AbstractTableModel;

import org.mmarini.routes.model.RouteInfos;
import org.mmarini.routes.model.SiteNode;

/**
 * @author Marco
 * 
 */
public class RouteInfoModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private RouteInfos infos;

	private double maximumFlux;

	private double minimumFlux;

	/**
         * 
         */
	public RouteInfoModel() {
	}

	/**
	 * 
	 * @param row
	 * @return
	 */
	private double computeColSum(int row) {
		double sum = 0.;
		int n = infos.getNodesCount();
		for (int i = 0; i < n; ++i) {
			sum += infos.getFrequence(row, i);
		}
		return sum;
	}

	/**
         * 
         * 
         */
	private void computeMinMax() {
		int n = infos.getNodesCount();
		double max = 0;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; ++i) {
			for (int j = i + 1; j < n; ++j) {
				double value = infos.getFrequence(i, j);
				max = Math.max(max, value);
				min = Math.min(min, value);
			}
		}
		setMinimumFlux(min);
		setMaximumFlux(max);
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0)
			return SiteNode.class;
		return Double.class;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return infos.getNodesCount() + 2;
	}

	/**
	 * @return
	 */
	public double getMaximumFlux() {
		return maximumFlux;
	}

	/**
	 * @return
	 */
	public double getMinimumFlux() {
		return minimumFlux;
	}

	/**
	 * @param index
	 * @return
	 */
	public SiteNode getNode(int index) {
		return infos.getNode(index);
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return infos.getNodesCount();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return infos.getNode(row);
		}
		if (col == infos.getNodesCount() + 1) {
			return computeColSum(row);
		}
		return infos.getFrequence(row, col - 1);
	}

	/**
	 * @param infos
	 *            the infos to set
	 */
	public void setInfos(RouteInfos infos) {
		this.infos = infos;
		computeMinMax();
		fireTableStructureChanged();
	}

	/**
	 * @param maximumFlux
	 *            the maximumFlux to set
	 */
	private void setMaximumFlux(double maximumFlux) {
		this.maximumFlux = maximumFlux;
	}

	/**
	 * @param minimumFlux
	 *            the minimumFlux to set
	 */
	private void setMinimumFlux(double minimumFlux) {
		this.minimumFlux = minimumFlux;
	}
}
