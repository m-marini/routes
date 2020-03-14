/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.text.NumberFormat;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/**
 * The panel with the selector of vehicle generation frequency.
 */
public class FrequencePane extends Box {
	private static final double[] FREQUENCES = { 0.4, 0.7, 1.5, 2.2, 10. / 3. };
	private static final long serialVersionUID = 1L;

	private final BoundedRangeModel frequenceModel;

	/** Creates the panel */
	public FrequencePane() {
		super(BoxLayout.PAGE_AXIS);
		frequenceModel = new DefaultBoundedRangeModel(0, 0, 0, FREQUENCES.length - 1);
		createContent();
	}

	/**
	 * Creates the content.
	 *
	 * @return the panel
	 */
	private FrequencePane createContent() {
		final Box box = createHorizontalBox();
		box.add(new JLabel(Messages.getString("FrequencePane.frequenceLabel.text"))); //$NON-NLS-1$
		box.add(createHorizontalGlue());
		final JSlider freqSlider = new JSlider(SwingConstants.HORIZONTAL);
		freqSlider.setModel(frequenceModel);
		freqSlider.setMajorTickSpacing(1);
		freqSlider.setPaintLabels(true);
		freqSlider.setPaintTicks(true);
		freqSlider.setPaintTrack(false);
		freqSlider.setSnapToTicks(true);
		final Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		final NumberFormat format = NumberFormat.getNumberInstance();
		for (int i = 0; i < FREQUENCES.length; ++i) {
			labels.put(i, new JLabel(format.format(FREQUENCES[i])));
		}
		freqSlider.setLabelTable(labels);
		box.add(freqSlider);
		add(box);
		return this;
	}

	/** Returns the selected frequency */
	public double getFrequence() {
		return FREQUENCES[frequenceModel.getValue()];
	}

	/**
	 * Sets the selected frequency
	 *
	 * @param frequence the frequency
	 * @return the panel
	 */
	public FrequencePane setFrequence(final double frequence) {
		int idx = 0;
		double error = Double.POSITIVE_INFINITY;
		for (int i = 0; i < FREQUENCES.length; ++i) {
			final double e = Math.abs(frequence - FREQUENCES[i]);
			if (e < error) {
				idx = i;
				error = e;
			}
		}
		frequenceModel.setValue(idx);
		return this;
	}
}
