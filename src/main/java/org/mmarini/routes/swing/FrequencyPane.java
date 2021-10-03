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
import java.text.NumberFormat;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Manages the panel of frequency parameters for vehicle creation
 *
 * @author Marco
 */
public class FrequencyPane extends Box {
    private static final double[] FREQUENCES = {0.4, 0.7, 1.5, 2.2, 10. / 3.};
    private static final long serialVersionUID = 1L;
    private final BoundedRangeModel frequenceModel;

    /**
     *
     */
    public FrequencyPane() {
        super(BoxLayout.PAGE_AXIS);
        frequenceModel = new DefaultBoundedRangeModel(0, 0, 0, FREQUENCES.length - 1);
        createContent();
    }

    /**
     *
     */
    private void createContent() {
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
        final Dictionary<Integer, JComponent> labels = new Hashtable<>();
        final NumberFormat format = NumberFormat.getNumberInstance();
        for (int i = 0; i < FREQUENCES.length; ++i) {
            labels.put(i, new JLabel(format.format(FREQUENCES[i])));
        }
        freqSlider.setLabelTable(labels);
        box.add(freqSlider);
        add(box);
    }

    /**
     *
     */
    public double getFrequence() {
        return FREQUENCES[frequenceModel.getValue()];
    }

    /**
     * @param frequency the frequency
     */
    public void setFrequency(final double frequency) {
        int idx = 0;
        double error = Double.POSITIVE_INFINITY;
        for (int i = 0; i < FREQUENCES.length; ++i) {
            final double e = Math.abs(frequency - FREQUENCES[i]);
            if (e < error) {
                idx = i;
                error = e;
            }
        }
        frequenceModel.setValue(idx);
    }
}
