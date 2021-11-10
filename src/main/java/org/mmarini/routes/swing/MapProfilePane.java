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

import org.mmarini.routes.model2.MapProfile;

import javax.swing.*;

/**
 * Manages the panel of parameters for random map generation profile
 */
public class MapProfilePane extends Box {
    private static final double LARGE_SIZE = 15000.;
    private static final double NORMAL_SIZE = 10000.;
    private static final double SMALL_SIZE = 5000.;
    private static final double[] MAP_SIZES = {SMALL_SIZE, NORMAL_SIZE, LARGE_SIZE};
    private static final double DIFFICULT_MIN_WEIGHT = 0.2;
    private static final double NORMAL_MIN_WEIGHT = 1. / 3.;
    private static final double EASY_MIN_WEIGHT = 1.;
    private static final double[] MIN_WEIGHTS = {EASY_MIN_WEIGHT, NORMAL_MIN_WEIGHT, DIFFICULT_MIN_WEIGHT};
    private static final double DIFFICULT_FREQUENCY = 2.;
    private static final double NORMAL_FREQUENCY = 1.;
    private static final double EASY_FREQUENCY = 0.4;
    private static final double[] FREQUENCIES = {EASY_FREQUENCY / ((1d + EASY_MIN_WEIGHT) / 2d),
            NORMAL_FREQUENCY / ((1d + NORMAL_MIN_WEIGHT) / 2d),
            DIFFICULT_FREQUENCY / ((1d + DIFFICULT_MIN_WEIGHT) / 2d)};
    private static final long serialVersionUID = 1L;

    private final SpinnerNumberModel siteCount;
    private final DefaultComboBoxModel<String> mapSizeModel;
    private final DefaultComboBoxModel<String> difficultyModel;
    private final JComboBox<String> mapSizeField;
    private final JSpinner siteCountField;

    /**
     *
     */
    public MapProfilePane() {
        super(BoxLayout.PAGE_AXIS);
        mapSizeModel = new DefaultComboBoxModel<>(
                new String[]{Messages.getString("MapProfilePane.mapSize.small.text"), //$NON-NLS-1$
                        Messages.getString("MapProfilePane.mapSize.medium.text"), //$NON-NLS-1$
                        Messages.getString("MapProfilePane.mapSize.large.text")}); //$NON-NLS-1$
        difficultyModel = new DefaultComboBoxModel<>(
                new String[]{Messages.getString("MapProfilePane.difficulty.easy.text"), //$NON-NLS-1$
                        Messages.getString("MapProfilePane.difficulty.medium.text"), //$NON-NLS-1$
                        Messages.getString("MapProfilePane.difficulty.hard.text")}); //$NON-NLS-1$
        siteCount = new SpinnerNumberModel(4, 2, 20, 1);
        mapSizeField = new JComboBox<>(mapSizeModel);
        siteCountField = new JSpinner(siteCount);
        createContent();
    }

    /**
     *
     */
    private void createContent() {
        Box box = createHorizontalBox();
        box.add(new JLabel(Messages.getString("MapProfilePane.sizeLabel.text"))); //$NON-NLS-1$
        box.add(createHorizontalGlue());
        box.add(mapSizeField);
        add(box);

        add(createVerticalStrut(4));
        box = createHorizontalBox();
        box.add(new JLabel(Messages.getString("MapProfilePane.siteCountLabel.text"))); //$NON-NLS-1$
        box.add(createHorizontalGlue());
        box.add(siteCountField);
        add(box);

        add(createVerticalStrut(4));

        box = createHorizontalBox();
        box.add(new JLabel(Messages.getString("MapProfilePane.difficultyLabel.text"))); //$NON-NLS-1$
        box.add(createHorizontalGlue());
        box.add(new JComboBox<>(difficultyModel));
        add(box);
    }

    /**
     *
     */
    public MapProfile getProfile() {
        final int sizeIdx = mapSizeModel.getIndexOf(mapSizeModel.getSelectedItem());
        final int diffIdx = difficultyModel.getIndexOf(difficultyModel.getSelectedItem());
        return new MapProfile(siteCount.getNumber().intValue(),
                MAP_SIZES[sizeIdx],
                MAP_SIZES[sizeIdx],
                MIN_WEIGHTS[diffIdx],
                FREQUENCIES[diffIdx]);
    }

    /**
     * Set the panel just for difficulty level
     *
     * @param difficultOnly true if difficult only
     */
    public void setDifficultyOnly(final boolean difficultOnly) {
        siteCountField.setEnabled(!difficultOnly);
        mapSizeField.setEnabled(!difficultOnly);
    }
}
