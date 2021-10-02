/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.routes.swing;

import org.mmarini.routes.model.Module;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco
 */
public class ModuleSelector extends JButton {

    private static final long serialVersionUID = 263680633302339361L;
    private final JPopupMenu popupMenu;
    private final ActionListener selectionListener;
    private final List<ModuleView> entries;
    private final List<JMenuItem> items;
    private ModuleView selectedEntry;
    private List<ListSelectionListener> listeners;

    /**
     *
     */
    public ModuleSelector() {
        popupMenu = new JPopupMenu();
        entries = new ArrayList<>(0);
        items = new ArrayList<>(0);
        selectionListener = e -> handleItemSelection(e.getSource());
        Action dropAction = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                dropDown();
            }

        };
        SwingUtils.getInstance().initAction(dropAction, "ModuleSelector.dropAction"); //$NON-NLS-1$
        setAction(dropAction);
    }

    /**
     * @param module
     */
    public void add(final Module module) {
        final ModuleView entry = new ModuleView();
        entry.setModule(module);
        entries.add(entry);
        final JMenuItem item = new JMenuItem();
        item.setIcon(entry.getIcon());
        item.addActionListener(selectionListener);
        items.add(item);
        popupMenu.add(item);
        if (selectedEntry == null) {
            item.doClick();
        }
    }

    /**
     * @param l
     */
    public synchronized void addListSelectionListener(final ListSelectionListener l) {
        List<ListSelectionListener> ll = listeners;
        if (ll == null) {
            ll = new ArrayList<>(0);
            ll.add(l);
        } else if (!ll.contains(l)) {
            ll = new ArrayList<>(ll);
            ll.add(l);
        }
        listeners = ll;
    }

    /**
     *
     */
    protected void dropDown() {
        final Dimension size = getSize();
        popupMenu.show(this, 0, size.height);
    }

    /**
     *
     */
    private void fireValueChanged() {
        final List<ListSelectionListener> ll = listeners;
        if (ll != null) {
            final ListSelectionEvent ev = new ListSelectionEvent(this, 0, 0, false);
            for (final ListSelectionListener l : ll) {
                l.valueChanged(ev);
            }
        }
    }

    /**
     * @return
     */
    public ModuleView getSelectedEntry() {
        return selectedEntry;
    }

    /**
     * @param selectedEntry the selectedEntry to set
     */
    private void setSelectedEntry(final ModuleView selectedEntry) {
        this.selectedEntry = selectedEntry;
        fireValueChanged();
    }

    /**
     * @param source
     */
    protected void handleItemSelection(final Object source) {
        final int idx = items.indexOf(source);
        setSelectedEntry(entries.get(idx));
    }
}
