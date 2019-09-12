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
	private double computeColSum(final int row) {
		double sum = 0.;
		final int n = infos.getNodesCount();
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
		final int n = infos.getNodesCount();
		double max = 0;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; ++i) {
			for (int j = i + 1; j < n; ++j) {
				final double value = infos.getFrequence(i, j);
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
	public Class<?> getColumnClass(final int columnIndex) {
		if (columnIndex == 0) {
			return SiteNode.class;
		}
		return Double.class;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
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
	public SiteNode getNode(final int index) {
		return infos.getNode(index);
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return infos.getNodesCount();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(final int row, final int col) {
		if (col == 0) {
			return infos.getNode(row);
		}
		if (col == infos.getNodesCount() + 1) {
			return computeColSum(row);
		}
		return infos.getFrequence(row, col - 1);
	}

	/**
	 * @param infos the infos to set
	 */
	public void setInfos(final RouteInfos infos) {
		this.infos = infos;
		computeMinMax();
		fireTableStructureChanged();
	}

	/**
	 * @param maximumFlux the maximumFlux to set
	 */
	private void setMaximumFlux(final double maximumFlux) {
		this.maximumFlux = maximumFlux;
	}

	/**
	 * @param minimumFlux the minimumFlux to set
	 */
	private void setMinimumFlux(final double minimumFlux) {
		this.minimumFlux = minimumFlux;
	}
}
