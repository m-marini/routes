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
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import org.mmarini.routes.model.v2.MapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * Various functionalities used in the user interface.
 */
public class SwingUtils {
	public static class GridBagConstraintsBuilder {
		private final GridBagConstraints constraints;

		/**
		 * @param constraints
		 */
		protected GridBagConstraintsBuilder(final GridBagConstraints constraints) {
			super();
			this.constraints = constraints;
		}

		public GridBagConstraintsBuilder above() {
			constraints.fill = GridBagConstraints.ABOVE_BASELINE;
			return this;
		}

		public GridBagConstraintsBuilder aboveLeading() {
			constraints.fill = GridBagConstraints.ABOVE_BASELINE_LEADING;
			return this;
		}

		public GridBagConstraintsBuilder aboveTrailing() {
			constraints.fill = GridBagConstraints.ABOVE_BASELINE_TRAILING;
			return this;
		}

		public GridBagConstraintsBuilder anchor(final int anchor) {
			constraints.anchor = anchor;
			return this;
		}

		public GridBagConstraintsBuilder baseline() {
			constraints.fill = GridBagConstraints.BASELINE;
			return this;
		}

		public GridBagConstraintsBuilder below() {
			constraints.fill = GridBagConstraints.BELOW_BASELINE;
			return this;
		}

		public GridBagConstraintsBuilder belowLeading() {
			constraints.fill = GridBagConstraints.BELOW_BASELINE_LEADING;
			return this;
		}

		public GridBagConstraintsBuilder belowTrailing() {
			constraints.fill = GridBagConstraints.BELOW_BASELINE_TRAILING;
			return this;
		}

		public GridBagConstraintsBuilder both() {
			constraints.fill = GridBagConstraints.BOTH;
			return this;
		}

		/**
		 *
		 * @return
		 */
		public GridBagConstraints build() {
			return constraints;
		}

		public GridBagConstraintsBuilder center() {
			constraints.anchor = GridBagConstraints.CENTER;
			return this;
		}

		public GridBagConstraintsBuilder east() {
			constraints.anchor = GridBagConstraints.EAST;
			return this;
		}

		public GridBagConstraintsBuilder fill(final int fill) {
			constraints.fill = fill;
			return this;
		}

		public GridBagConstraintsBuilder firstLineEnd() {
			constraints.anchor = GridBagConstraints.FIRST_LINE_END;
			return this;
		}

		public GridBagConstraintsBuilder firstLineStart() {
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			return this;
		}

		public GridBagConstraintsBuilder grid(final int gridx, final int gridy, final int gridwidth,
				final int gridheight) {
			constraints.gridx = gridx;
			constraints.gridy = gridy;
			constraints.gridwidth = gridwidth;
			constraints.gridheight = gridheight;
			return this;
		}

		public GridBagConstraintsBuilder height(final int gridheight) {
			constraints.gridheight = gridheight;
			return this;
		}

		public GridBagConstraintsBuilder horizontal() {
			constraints.fill = GridBagConstraints.HORIZONTAL;
			return this;
		}

		public GridBagConstraintsBuilder inset(final int size) {
			constraints.insets = new Insets(size, size, size, size);
			return this;
		}

		public GridBagConstraintsBuilder inset(final int vertical, final int horizontal) {
			constraints.insets = new Insets(vertical, horizontal, vertical, horizontal);
			return this;
		}

		public GridBagConstraintsBuilder inset(final int top, final int horizontal, final int bottom) {
			constraints.insets = new Insets(top, horizontal, bottom, horizontal);
			return this;
		}

		public GridBagConstraintsBuilder inset(final int top, final int left, final int bottom, final int right) {
			constraints.insets = new Insets(top, left, bottom, right);
			return this;
		}

		public GridBagConstraintsBuilder insets(final Insets insets) {
			constraints.insets = insets;
			return this;
		}

		public GridBagConstraintsBuilder ipad(final int ipadx, final int ipady) {
			constraints.ipadx = ipadx;
			constraints.ipady = ipady;
			return this;
		}

		public GridBagConstraintsBuilder ipadx(final int ipadx) {
			constraints.ipadx = ipadx;
			return this;
		}

		public GridBagConstraintsBuilder ipady(final int ipady) {
			constraints.ipady = ipady;
			return this;
		}

		public GridBagConstraintsBuilder lastInColumn() {
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			return this;
		}

		public GridBagConstraintsBuilder lastInRow() {
			constraints.gridheight = GridBagConstraints.REMAINDER;
			return this;
		}

		public GridBagConstraintsBuilder lastLineEnd() {
			constraints.anchor = GridBagConstraints.LAST_LINE_END;
			return this;
		}

		public GridBagConstraintsBuilder lastLineStart() {
			constraints.anchor = GridBagConstraints.LAST_LINE_START;
			return this;
		}

		public GridBagConstraintsBuilder leading() {
			constraints.fill = GridBagConstraints.BASELINE_LEADING;
			return this;
		}

		public GridBagConstraintsBuilder lineEnd() {
			constraints.anchor = GridBagConstraints.LINE_END;
			return this;
		}

		public GridBagConstraintsBuilder lineStart() {
			constraints.anchor = GridBagConstraints.LINE_START;
			return this;
		}

		public GridBagConstraintsBuilder nextLastInColumn() {
			constraints.gridwidth = GridBagConstraints.RELATIVE;
			return this;
		}

		public GridBagConstraintsBuilder nextLastInRow() {
			constraints.gridheight = GridBagConstraints.RELATIVE;
			return this;
		}

		public GridBagConstraintsBuilder nextx() {
			constraints.gridx = GridBagConstraints.RELATIVE;
			return this;
		}

		public GridBagConstraintsBuilder nexty() {
			constraints.gridy = GridBagConstraints.RELATIVE;
			return this;
		}

		public GridBagConstraintsBuilder none() {
			constraints.fill = GridBagConstraints.NONE;
			return this;
		}

		public GridBagConstraintsBuilder north() {
			constraints.anchor = GridBagConstraints.NORTH;
			return this;
		}

		public GridBagConstraintsBuilder northEast() {
			constraints.anchor = GridBagConstraints.NORTHEAST;
			return this;
		}

		public GridBagConstraintsBuilder northWest() {
			constraints.anchor = GridBagConstraints.NORTHWEST;
			return this;
		}

		public GridBagConstraintsBuilder pageEnd() {
			constraints.anchor = GridBagConstraints.PAGE_END;
			return this;
		}

		public GridBagConstraintsBuilder pageStart() {
			constraints.anchor = GridBagConstraints.PAGE_START;
			return this;
		}

		public GridBagConstraintsBuilder south() {
			constraints.anchor = GridBagConstraints.SOUTH;
			return this;
		}

		public GridBagConstraintsBuilder southEast() {
			constraints.anchor = GridBagConstraints.SOUTHEAST;
			return this;
		}

		public GridBagConstraintsBuilder southWest() {
			constraints.anchor = GridBagConstraints.SOUTHWEST;
			return this;
		}

		public GridBagConstraintsBuilder trailing() {
			constraints.fill = GridBagConstraints.BASELINE_TRAILING;
			return this;
		}

		public GridBagConstraintsBuilder vertical() {
			constraints.fill = GridBagConstraints.VERTICAL;
			return this;
		}

		public GridBagConstraintsBuilder weight(final double weightx, final double weighty) {
			constraints.weightx = weightx;
			constraints.weighty = weighty;
			return this;
		}

		public GridBagConstraintsBuilder weightx(final double weightx) {
			constraints.weightx = weightx;
			return this;
		}

		public GridBagConstraintsBuilder weighty(final double weighty) {
			constraints.weighty = weighty;
			return this;
		}

		public GridBagConstraintsBuilder west() {
			constraints.anchor = GridBagConstraints.WEST;
			return this;
		}

		public GridBagConstraintsBuilder width(final int gridwidth) {
			constraints.gridwidth = gridwidth;
			return this;
		}

		public GridBagConstraintsBuilder x(final int x) {
			constraints.gridx = x;
			return this;
		}

		public GridBagConstraintsBuilder y(final int y) {
			constraints.gridy = y;
			return this;
		}
	}

	/**
	 *
	 */
	public static class WithGridBagConstraints<T extends Container> {
		private final T container;

		/**
		 * @param container
		 */
		public WithGridBagConstraints(final T container) {
			super();
			this.container = container;
			container.setLayout(new GridBagLayout());
		}

		public WithGridBagConstraints<T> add(final Component component, final GridBagConstraints constraints) {
			((GridBagLayout) container.getLayout()).setConstraints(component, constraints);
			container.add(component);
			return this;
		}

		public T getContainer() {
			return container;
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(SwingUtils.class);

	private static final double BRIGHTNESS_ZERO = 0.7;

	private static final double BRIGHTNESS_ONE = 1;

	private static final double HUE_ZERO = 0.8;

	private static final double HUE_ONE = 0;

	private static final double NODE_SATURATION = 1;

	/**
	 *
	 * @param field
	 * @return
	 */
	public static Observable<ActionEvent> action(final JTextField field) {
		return Observable.<ActionEvent>create(emitter -> {
			logger.debug("register listener on {}", field);
			field.addActionListener(ev -> emitter.onNext(ev));
		});
	}

	/**
	 * Returns the color map for sites
	 *
	 * @param sites the sites
	 */
	public static Map<MapNode, Color> buildColorMap(final Collection<MapNode> sites) {
		return buildColorMap(sites, NODE_SATURATION);
	}

	/**
	 * Returns the color map for sites
	 *
	 * @param sites      the sites
	 * @param saturation saturation
	 */
	public static Map<MapNode, Color> buildColorMap(final Collection<MapNode> sites, final double saturation) {
		final int n = sites.size();
		if (n == 0) {
			return Map.of();
		} else if (n == 1) {
			return Map.of(sites.stream().findAny().get(), computeColor(0, saturation));
		} else {
			final List<MapNode> sorted = sites.stream().sorted().collect(Collectors.toList());
			final Map<MapNode, Color> result = IntStream.range(0, n).mapToObj(i -> i)
					.collect(Collectors.toMap(sorted::get, i -> {
						return computeColor((double) (n - i - 1) / (n - 1), saturation);
					}));
			return result;
		}
	}

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

	public static GridBagConstraintsBuilder createFieldConstraints(final int x, final int y, final int w, final int h) {
		return createGridConstraints(x, y, w, h).west().horizontal().inset(2);
	}

	/**
	 *
	 * @return
	 */
	public static GridBagConstraintsBuilder createGridConstraints() {
		return new GridBagConstraintsBuilder(new GridBagConstraints());
	}

	public static GridBagConstraintsBuilder createGridConstraints(final int x, final int y, final int w, final int h) {
		return new GridBagConstraintsBuilder(new GridBagConstraints()).grid(x, y, w, h);
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

	public static GridBagConstraintsBuilder createLabelConstraints(final int x, final int y, final int w, final int h) {
		return createGridConstraints(x, y, w, h).east().inset(2);
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
	 * Returns the list of patterns by key.n
	 *
	 * @param key the key
	 */
	public static List<String> loadPatterns(final String key) {
		final List<String> list = new ArrayList<String>(0);
		int i = 0;
		for (;;) {
			final String text = Messages.getString(key + "." + i);
			if (text.startsWith("!")) {
				break;
			}
			list.add(text);
			++i;
		}
		return list;
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
	 *
	 * @param field
	 * @return
	 */
	public static <T> Observable<T> value(final JFormattedTextField field) {
		final Observable<JFormattedTextField> focusObs = SwingObservable.focus(field)
				.filter(ev -> ev.getID() == FocusEvent.FOCUS_LOST).map(ev -> field);
		final Observable<JFormattedTextField> actionObs = action(field).map(ev -> field);
		@SuppressWarnings("unchecked")
		final Observable<T> result = focusObs.mergeWith(actionObs).filter(c -> c.isEditValid()).map(c -> {
			c.commitEdit();
			return (T) c.getValue();
		});
		return result;
	}

	/**
	 *
	 * @param <T>
	 * @param container
	 * @return
	 */
	public static <T extends Container> WithGridBagConstraints<T> withGridBagConstraints(final T container) {
		return new WithGridBagConstraints<>(container);
	}
}
