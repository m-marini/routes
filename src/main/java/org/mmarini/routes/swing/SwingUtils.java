/*
 * MainFrame.java
 *
 * $Id: SwingUtils.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.mmarini.routes.swing.v2.MainFrame;

/**
 * Variuous functionalities used in the user interface.
 *
 * @author marco.marini@mmarini.org
 * @version $Id: SwingUtils.java,v 1.6 2010/10/19 20:32:59 marco Exp $
 *
 */
public class SwingUtils {
	private static SwingUtils instance = new SwingUtils();

	private static final double BRIGHTNESS_ZERO = 0.8;

	private static final double BRIGHTNESS_ONE = 1;

	private static final double HUE_ZERO = 0;

	private static final double HUE_ONE = 0.8;

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
	 * Returns the singleton instance of the utilites
	 *
	 * @return the instance
	 */
	public static SwingUtils getInstance() {
		return instance;
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

	/**
	 * Create the utilitties
	 */
	protected SwingUtils() {
	}

	/**
	 * Compute the iride color depending on a control value.<br>
	 * The result color are between violet color to red color varying from zero
	 * level to one level of the control value <br>
	 *
	 *
	 * @param value      the control value
	 * @param saturation the saturation of result color
	 * @return the iride color
	 */
	public Color computeColor(final double value, final double saturation) {
		final double b = interpolate(value, BRIGHTNESS_ZERO, BRIGHTNESS_ONE);
		final double h = interpolate(value, HUE_ZERO, HUE_ONE);
		final Color color = Color.getHSBColor((float) h, (float) saturation, (float) b);
		return color;
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
			action.putValue(Action.MNEMONIC_KEY, Integer.valueOf(msg.charAt(0)));
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
