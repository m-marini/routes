/**
 * 
 */
package org.mmarini.routes.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mmarini.routes.model.Module;

/**
 * @author Marco
 * 
 */
public class ModuleSelector extends JButton {

	private static final long serialVersionUID = 263680633302339361L;

	private Action dropAction;

	private JPopupMenu popupMenu;

	private ActionListener selectionListener;

	private ModuleEntry selectedEntry;

	private List<ModuleEntry> entries;

	private List<JMenuItem> items;

	private List<ListSelectionListener> listeners;

	/**
         * 
         */
	public ModuleSelector() {
		popupMenu = new JPopupMenu();
		entries = new ArrayList<ModuleEntry>(0);
		items = new ArrayList<JMenuItem>(0);
		selectionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleItemSelection(e.getSource());
			}

		};
		dropAction = new AbstractAction() {

			/**
                 * 
                 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				dropDown();
			}

		};
		SwingUtils.getInstance().initAction(dropAction,
				"ModuleSelector.dropAction"); //$NON-NLS-1$
		setAction(dropAction);
	}

	/**
	 * 
	 * @param module
	 */
	public void add(Module module) {
		ModuleEntry entry = new ModuleEntry();
		entry.setModule(module);
		entries.add(entry);
		JMenuItem item = new JMenuItem();
		item.setIcon(entry.getIcon());
		item.addActionListener(selectionListener);
		items.add(item);
		popupMenu.add(item);
		if (selectedEntry == null) {
			item.doClick();
		}
	}

	/**
	 * 
	 * @param l
	 */
	public synchronized void addListSelectionListener(ListSelectionListener l) {
		List<ListSelectionListener> ll = listeners;
		if (ll == null) {
			ll = new ArrayList<ListSelectionListener>(0);
			ll.add(l);
		} else if (!ll.contains(l)) {
			ll = new ArrayList<ListSelectionListener>(ll);
			ll.add(l);
		}
		listeners = ll;
	}

	/**
         * 
         * 
         */
	protected void dropDown() {
		Dimension size = getSize();
		popupMenu.show(this, 0, size.height);
	}

	/**
	 * 
	 * @param index
	 */
	private void fireValueChanged() {
		List<ListSelectionListener> ll = listeners;
		if (ll != null) {
			ListSelectionEvent ev = new ListSelectionEvent(this, 0, 0, false);
			for (ListSelectionListener l : ll) {
				l.valueChanged(ev);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public ModuleEntry getSelectedEntry() {
		return selectedEntry;
	}

	/**
	 * 
	 * @param source
	 */
	protected void handleItemSelection(Object source) {
		int idx = items.indexOf(source);
		setSelectedEntry(entries.get(idx));
	}

	/**
	 * 
	 * @param l
	 */
	public synchronized void removeListSelectionListener(ListSelectionListener l) {
		List<ListSelectionListener> ll = listeners;
		if (ll != null && ll.contains(l)) {
			ll = new ArrayList<ListSelectionListener>(ll);
			ll.remove(l);
			listeners = ll;
		}
	}

	/**
	 * @param selectedEntry
	 *            the selectedEntry to set
	 */
	private void setSelectedEntry(ModuleEntry selectedEntry) {
		this.selectedEntry = selectedEntry;
		fireValueChanged();
	}
}
