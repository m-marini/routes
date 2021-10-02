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

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class SiteListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 3743457808802444412L;

    /**
     *
     */
    public SiteListCellRenderer() {
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                                                  final boolean isSelected, final boolean cellHasFocus) {
        final MapNodeEntry entry = (MapNodeEntry) value;
        setText(entry.getName());
        Color bg = entry.getColor();
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
}