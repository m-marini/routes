/*
 * MainFrame.java
 *
 * $Id: SwingUtils.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.swing.v2;

import java.awt.Color;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/**
 * Various functionalities used in the user interface.
 */
public class SwingUtils {
	private static final double BRIGHTNESS_ZERO = 0.7;
	private static final double BRIGHTNESS_ONE = 1;
	private static final double HUE_ZERO = 0.8;
	private static final double HUE_ONE = 0;

	/**
	 * Returns the iride color depending on a control value.<br>
	 * The result color are between violet color to red color varying from zero
	 * level to one level of the control value <br>
	 *
	 *
	 * @param value      the control value
	 * @param saturation the saturation of result color
	 */
	public static Color computeColor(final double value, final double saturation) {
		final double b = interpolate(value, BRIGHTNESS_ZERO, BRIGHTNESS_ONE);
		final double h = interpolate(value, HUE_ZERO, HUE_ONE);
		final Color color = Color.getHSBColor((float) h, (float) saturation, (float) b);
		return color;
	}

	/**
	 * Returns a new JButton initialized with key properties
	 *
	 * @param key the key properties
	 */
	public static JButton createJButton(final String key) {
		return setButtonProperties(new JButton(), key);
	}

	/**
	 * Returns a new JCheckBoxMenuItem initialized with key properties
	 *
	 * @param key the key properties
	 */
	public static JCheckBoxMenuItem createJCheckBoxMenuItem(final String key) {
		return setMenuProperties(new JCheckBoxMenuItem(), key);
	}

	/**
	 * Returns a new JMenuItem initialized with key properties
	 *
	 * @param key the key properties
	 */
	public static JMenuItem createJMenuItem(final String key) {
		return setMenuProperties(new JMenuItem(), key);
	}

	/**
	 * Returns a new JRadioButtonMenuItem initialized with key properties
	 *
	 * @param key the key properties
	 */
	public static JRadioButtonMenuItem createJRadioButtonMenuItem(final String key) {
		return setMenuProperties(new JRadioButtonMenuItem(), key);
	}

	/**
	 * Returns a new JToggleButton initialized with key properties
	 *
	 * @param key the key properties
	 */
	public static JToggleButton createJToggleButton(final String key) {
		return setButtonProperties(new JToggleButton(), key);
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
	private static double interpolate(final double control, final double zeroLevel, final double oneLevel) {
		return control * (oneLevel - zeroLevel) + zeroLevel;
	}

	/**
	 * Returns the Abstract Button initialized with key properties
	 *
	 * @param key the key properties
	 */
	private static <T extends AbstractButton> T setButtonProperties(final T result, final String key) {
		final String text = Messages.getString(key + ".name"); //$NON-NLS-1$
		if (!text.startsWith("!")) { //$NON-NLS-1$
			result.setText(text);
		}
		final String tooltip = Messages.getString(key + ".tooltip"); //$NON-NLS-1$
		if (!tooltip.startsWith("!")) { //$NON-NLS-1$
			result.setToolTipText(tooltip);
		}
		final String mnemomic = Messages.getString(key + ".mnemonic"); //$NON-NLS-1$
		if (!mnemomic.startsWith("!")) { //$NON-NLS-1$
			result.setMnemonic(mnemomic.charAt(0));
		}
		final String icon = Messages.getString(key + ".smallIcon"); //$NON-NLS-1$
		if (!icon.startsWith("!")) { //$NON-NLS-1$
			final URL url = MainFrame.class.getResource(icon);
			if (url != null) {
				final ImageIcon img = new ImageIcon(url);
				result.setIcon(img);
			}
		}
		return result;
	}

	/**
	 * Returns the menu item initialized with key properties
	 *
	 * @param key the key properties
	 */
	private static <T extends JMenuItem> T setMenuProperties(final T result, final String key) {
		setButtonProperties((AbstractButton) result, key);
		final String acc = Messages.getString(key + ".accelerator"); //$NON-NLS-1$
		if (!acc.startsWith("!")) { //$NON-NLS-1$
			result.setAccelerator(KeyStroke.getKeyStroke(acc));
		}
		return result;
	}
}
