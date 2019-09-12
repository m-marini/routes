/**
 * 
 */
package org.mmarini.routes.swing;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.mmarini.routes.model.SiteNode;
import org.mmarini.routes.model.TrafficInfo;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: TrafficInfoModel.java,v 1.3 2010/10/19 20:32:59 marco Exp $
 */
public class TrafficInfoModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static String[] columnLabelName = { "destination", "veicleCount", "delayedCount", "delayedCountPerc",
			"delayedTime" };
	private List<TrafficInfo> infos;
	private final Class<?>[] columnClass = { String.class, Integer.class, Integer.class, Double.class, Double.class };
	private RouteMediator mediator;

	/**
	     * 
	     */
	public TrafficInfoModel() {
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return columnClass[columnIndex];
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columnLabelName.length;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(final int column) {
		return Messages.getString("TrafficInfoModel." + columnLabelName[column] + ".label"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public SiteNode getNode(final int index) {
		return infos.get(index).getDestination();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return infos.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(final int row, final int col) {
		final TrafficInfo record = infos.get(row);
		switch (col) {
		case 0:
			return mediator.retrieveNodeName(record.getDestination());
		case 1:
			return record.getVeicleCount();
		case 2:
			return record.getDelayCount();
		case 3:
			return (double) record.getDelayCount() / record.getVeicleCount();
		case 4:
			return record.getAverageDelayTime();
		default:
			return "?";
		}
	}

	/**
	 * @param infos the info to set
	 */
	public void setInfos(final List<TrafficInfo> infos) {
		this.infos = infos;
		fireTableStructureChanged();
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(final RouteMediator mediator) {
		this.mediator = mediator;
	}
}
