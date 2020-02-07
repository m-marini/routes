/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;

/**
 *
 */
public class RoutePane extends JSplitPane {
	class MapNodeTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		/**
		 *
		 */
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			final MapNode node = (MapNode) value;
			setText(node.getShortName());
			final Color bg = isSelected ? selectedColorMap.getOrDefault(node, table.getSelectionBackground())
					: colorMap.getOrDefault(node, table.getBackground());
			setBackground(bg);
			setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
			setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
			return this;
		}

	}

	class RouteTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2634066472823732066L;

		public RouteTableModel() {
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			if (columnIndex == 1) {
				return Double.class;
			} else {
				return MapNode.class;
			}
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
		public String getColumnName(final int column) {
			return COLUMN_NAMES[column];
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return destinations.size();
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(final int row, final int col) {
			final MapNode to = destinations.get(row);
			final MapNode from = departureList.getSelectedValue();
			switch (col) {
			case 0:
				return to;
			default:
				return weights.getOrDefault(new Tuple2<>(from, to), 0.0);
			}
		}

		/**
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex == 1;
		}

		/**
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int,
		 *      int)
		 */
		@Override
		public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
			final Double weight = (Double) value;
			if (weight >= 0.0 && weight <= 1.0) {
				final MapNode from = departureList.getSelectedValue();
				final MapNode to = destinations.get(rowIndex);
				final Tuple2<MapNode, MapNode> key = new Tuple2<>(from, to);
				final Map<Tuple2<MapNode, MapNode>, Double> newWeights = weights.entrySet().parallelStream().map(e -> {
					final Tuple2<MapNode, MapNode> k = e.getKey();
					final Double v = k.equals(key) ? weight : weights.get(key);
					return new Tuple2<>(k, v);
				}).collect(Collectors.toMap(Tuple2::getElem1, Tuple2::getElem2));
				weights = newWeights;
				logger.debug("setValueAt {} = {}", key, weights.get(key)); //$NON-NLS-1$
			}
		}
	}

	private static final String[] COLUMN_NAMES = { Messages.getString("RoutePane.destination.title"), //$NON-NLS-1$
			Messages.getString("RoutePane.weight.title") }; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(RoutePane.class);

	private static final long serialVersionUID = 1L;
	private final JList<MapNode> departureList;
	private final JTable routeTable;
	private final TableCellRenderer siteTableCellRenderer;
	private final RouteTableModel routeTableModel;
	private final SiteListCellRenderer departureListCellRenderer;
	private final DefaultListModel<MapNode> departureListModel;
	private Map<Tuple2<MapNode, MapNode>, Double> weights;
	private List<MapNode> destinations;
	private Map<MapNode, Color> colorMap;
	private Map<MapNode, Color> selectedColorMap;

	/**
	 *
	 */
	public RoutePane() {
		this.routeTableModel = new RouteTableModel();
		this.departureListModel = new DefaultListModel<>();
		this.departureList = new JList<>(departureListModel);
		this.routeTable = new JTable(routeTableModel);
		this.departureListCellRenderer = new SiteListCellRenderer();
		this.siteTableCellRenderer = new MapNodeTableCellRenderer();
		this.colorMap = Map.of();
		this.destinations = List.of();

		routeTable.setDefaultRenderer(MapNode.class, siteTableCellRenderer);
		departureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		departureList.setCellRenderer(departureListCellRenderer);
		SwingObservable.listSelection(departureList).subscribe(ev -> {
			handleFromSelection();
		});
		createContent();
	}

	/**
	 * @return
	 */
	private RoutePane createContent() {
		final JScrollPane departureScrollPane = new JScrollPane(departureList);
		departureScrollPane
				.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoutePane.departure.title"))); //$NON-NLS-1$

		final JScrollPane routeScrollPane = new JScrollPane(routeTable);
		routeScrollPane.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoutePane.route.title"))); //$NON-NLS-1$

		setResizeWeight(0.5);
		setDividerLocation(200);
		setLeftComponent(departureScrollPane);
		setRightComponent(routeScrollPane);
		return this;
	}

	/**
	 *
	 * @return
	 */
	public Map<Tuple2<MapNode, MapNode>, Double> getWeights() {
		return weights;
	}

	/**
	 *
	 * @return
	 */
	private RoutePane handleFromSelection() {
		final Optional<MapNode> from = Optional.ofNullable(departureList.getSelectedValue());
		final List<MapNode> toNodes = from.map(fr -> {
			final List<MapNode> list = weights.keySet().parallelStream().filter(e -> {
				return e.getElem1().equals(fr);
			}).map(e -> {
				return e.getElem2();
			}).sorted().collect(Collectors.toList());
			return list;
		}).orElse(List.of());
		destinations = toNodes;
		routeTableModel.fireTableDataChanged();
		return this;
	}

	/**
	 *
	 * @return
	 */
	private RoutePane load() {
		final List<MapNode> dep = weights.entrySet().parallelStream().map(e -> {
			return e.getKey().getElem1();
		}).sorted().collect(Collectors.toSet()).parallelStream().sorted().collect(Collectors.toList());
		this.colorMap = SwingUtils.buildColorMap(dep, 0.3);
		this.selectedColorMap = SwingUtils.buildColorMap(dep, 1);
		departureListCellRenderer.setColorMap(colorMap);
		departureListCellRenderer.setSelectionColorMap(selectedColorMap);
		departureListModel.removeAllElements();
		departureListModel.addAll(dep);
		return this;
	}

	/**
	 *
	 * @param weights
	 * @return
	 */
	public RoutePane setWeights(final Map<Tuple2<MapNode, MapNode>, Double> weights) {
		logger.debug("setWeights {}", weights); //$NON-NLS-1$
		this.weights = weights;
		return load();
	}
}
