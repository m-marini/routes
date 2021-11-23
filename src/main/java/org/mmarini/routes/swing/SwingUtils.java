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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Various functionalities used in the user interface.
 *
 * @author marco.marini@mmarini.org
 */
public class SwingUtils {
    private static final double BRIGHTNESS_ZERO = 0.8;
    private static final double BRIGHTNESS_ONE = 1;
    private static final double HUE_ZERO = 0.75;
    private static final double HUE_ONE = 0;
    private static final SwingUtils instance = new SwingUtils();

    /**
     * Returns the singleton instance of the utilities
     *
     * @return the instance
     */
    public static SwingUtils getInstance() {
        return instance;
    }

    /**
     * @param key the key
     */
    public static String[] loadPatterns(final String key) {
        final List<String> list = new ArrayList<>(0);
        int i = 0;
        for (; ; ) {
            final String text = Messages.getString(key + "." + i);
            if (text.startsWith("!")) {
                break;
            }
            list.add(text);
            ++i;
        }
        return list.toArray(new String[0]);
    }

    /**
     * Create the utilities
     */
    protected SwingUtils() {
    }

    /**
     * Compute the irides color depending on a control value.<br>
     * The result color are between violet color to red color varying from zero
     * level to one level of the control value <br>
     *
     * @param value      the control value
     * @param saturation the saturation of result color
     * @return the irides color
     */
    public Color computeColor(final double value, final double saturation) {
        double v = max(0d, min(value, 1d));
        final double b = interpolate(v, BRIGHTNESS_ZERO, BRIGHTNESS_ONE);
        final double h = interpolate(v, HUE_ZERO, HUE_ONE);
        return Color.getHSBColor((float) h, (float) saturation, (float) b);
    }

    /**
     * Initialize an action loading the values from message resources file.<br>
     * The loaded value are:
     * <ul>
     * <li>name</li>
     * <li>tooltip</li>
     * <li>accelerator</li>
     * <li>mnemonic</li>
     * <li>smallIcon</li>
     * <li>largeIcon</li>
     * </ul>
     *
     * @param action the action to be initialized
     * @param key    the key identifier in the message file
     */
    public void initAction(final Action action, final String key) {
        String msg = Messages.getString(key + ".name"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            action.putValue(Action.NAME, msg);
        }
        msg = Messages.getString(key + ".tooltip"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            action.putValue(Action.SHORT_DESCRIPTION, msg);
        }
        msg = Messages.getString(key + ".accelerator"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(msg));
        }
        msg = Messages.getString(key + ".mnemonic"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            action.putValue(Action.MNEMONIC_KEY, (int) msg.charAt(0));
        }
        msg = Messages.getString(key + ".smallIcon"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            final URL url = getClass().getResource(msg);
            if (url != null) {
                final ImageIcon img = new ImageIcon(url);
                action.putValue(Action.SMALL_ICON, img);
            }
        }
        msg = Messages.getString(key + ".largeIcon"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            final URL url = getClass().getResource(msg);
            if (url != null) {
                final ImageIcon img = new ImageIcon(url);
                action.putValue(Action.LARGE_ICON_KEY, img);
            }
        }
    }

    /**
     * @param button the button
     * @param key    the key
     * @param <T>    the type of button
     */
    public <T extends AbstractButton> T initButton(final T button, final String key) {
        final String name = Messages.getString(key + ".name"); //$NON-NLS-1$
        if (!name.startsWith("!")) { //$NON-NLS-1$
            button.setName(name);
        }
        final String tooltip = Messages.getString(key + ".tooltip"); //$NON-NLS-1$
        if (!tooltip.startsWith("!")) { //$NON-NLS-1$
            button.setToolTipText(tooltip);
        }
        final String msg = Messages.getString(key + ".smallIcon"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            final URL url = getClass().getResource(msg);
            if (url != null) {
                final ImageIcon img = new ImageIcon(url);
                button.setIcon(img);
            }
        }
        return button;
    }

    /**
     * Initialize an action loading the values from message resources file.<br>
     * The loaded value are:
     * <ul>
     * <li>name</li>
     * <li>tooltip</li>
     * <li>accelerator</li>
     * <li>mnemonic</li>
     * <li>smallIcon</li>
     * <li>largeIcon</li>
     * </ul>
     *
     * @param item the action to be initialized
     * @param key  the key identifier in the message file
     */
    public <T extends JMenuItem> T initMenuItem(final T item, final String key) {
        final String name = Messages.getString(key + ".name"); //$NON-NLS-1$
        if (!name.startsWith("!")) { //$NON-NLS-1$
            item.setText(name);
        }
        final String tooltip = Messages.getString(key + ".tooltip"); //$NON-NLS-1$
        if (!tooltip.startsWith("!")) { //$NON-NLS-1$
            item.setToolTipText(tooltip);
        }
        final String mnemonic = Messages.getString(key + ".mnemonic"); //$NON-NLS-1$
        if (!mnemonic.startsWith("!")) { //$NON-NLS-1$
            item.setMnemonic(Integer.valueOf(mnemonic.charAt(0)));
        }
        final String accelerator = Messages.getString(key + ".accelerator"); //$NON-NLS-1$
        if (!accelerator.startsWith("!")) { //$NON-NLS-1$
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        final String msg = Messages.getString(key + ".smallIcon"); //$NON-NLS-1$
        if (!msg.startsWith("!")) { //$NON-NLS-1$
            final URL url = getClass().getResource(msg);
            if (url != null) {
                final ImageIcon img = new ImageIcon(url);
                item.setIcon(img);
            }
        }
        return item;
    }

    /**
     * Interpolates linearly the result from two levels depending on a control
     * value.
     *
     * @param control   the control value
     * @param zeroLevel the start value (associated to the 0 level of control value)
     * @param oneLevel  the end value (associated to the 1 level of control value)
     * @return the linear interpolated value
     */
    private double interpolate(final double control, final double zeroLevel, final double oneLevel) {
        return control * (oneLevel - zeroLevel) + zeroLevel;
    }
}
