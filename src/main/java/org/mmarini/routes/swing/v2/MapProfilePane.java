//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.swing.v2;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mmarini.routes.model.v2.MapProfile;

/**
 * Input panel to set the map profile for random map generation
 */
public class MapProfilePane extends Box {
	public static final double LARGE_SIZE = 15000.;
	public static final double NORMAL_SIZE = 10000.;
	public static final double SMALL_SIZE = 5000.;
	public static final double DIFFICULT_MIN_WEIGHT = 0.2;
	public static final double NORMAL_MIN_WEIGHT = 1. / 3.;
	public static final double EASY_MIN_WEIGHT = 1.;
	public static final double DIFFICUL_FREQUENCE = 2.;
	public static final double NORMAL_FREQUENCE = 1.;
	public static final double EASY_FREQUENCE = 0.4;

	private static final long serialVersionUID = 1L;

	private final SpinnerNumberModel siteCount;
	private final DefaultComboBoxModel<String> mapSizeModel;
	private final DefaultComboBoxModel<String> difficultyModel;
	private final double[] mapSize;
	private final double[] minWeight;
	private final double[] frequence;
	private final JComboBox<String> mapSizeField;
	private final Component siteCountField;

	/**
	 *
	 */
	public MapProfilePane() {
		super(BoxLayout.PAGE_AXIS);
		mapSizeModel = new DefaultComboBoxModel<>(
				new String[] { Messages.getString("MapProfilePane.mapSize.small.text"), //$NON-NLS-1$
						Messages.getString("MapProfilePane.mapSize.medium.text"), //$NON-NLS-1$
						Messages.getString("MapProfilePane.mapSize.large.text") }); //$NON-NLS-1$
		difficultyModel = new DefaultComboBoxModel<>(
				new String[] { Messages.getString("MapProfilePane.difficulty.easy.text"), //$NON-NLS-1$
						Messages.getString("MapProfilePane.difficulty.medium.text"), //$NON-NLS-1$
						Messages.getString("MapProfilePane.difficulty.hard.text") }); //$NON-NLS-1$
		siteCount = new SpinnerNumberModel(4, 2, 20, 1);
		mapSize = new double[] { SMALL_SIZE, NORMAL_SIZE, LARGE_SIZE };
		minWeight = new double[] { EASY_MIN_WEIGHT, NORMAL_MIN_WEIGHT, DIFFICULT_MIN_WEIGHT };
		frequence = new double[] { EASY_FREQUENCE / ((1d + EASY_MIN_WEIGHT) / 2d),
				NORMAL_FREQUENCE / ((1d + NORMAL_MIN_WEIGHT) / 2d),
				DIFFICUL_FREQUENCE / ((1d + DIFFICULT_MIN_WEIGHT) / 2d) };
		mapSizeField = new JComboBox<>(mapSizeModel);
		siteCountField = new JSpinner(siteCount);
		createContent();
	}

	/**
	 * Returns the panel with content
	 */
	private MapProfilePane createContent() {
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
		return this;
	}

	/**
	 * Returns the profile
	 */
	public MapProfile getProfile() {
		final int sizeIdx = mapSizeModel.getIndexOf(mapSizeModel.getSelectedItem());
		final int diffIdx = difficultyModel.getIndexOf(difficultyModel.getSelectedItem());
		final MapProfile result = new MapProfile(siteCount.getNumber().intValue(), mapSize[sizeIdx], mapSize[sizeIdx],
				minWeight[diffIdx], frequence[diffIdx]);
		return result;
	}

	/**
	 * Returns the panel just for difficulty level if true
	 *
	 * @param difficultOnly true for difficult level only
	 */
	public MapProfilePane setDifficultyOnly(final boolean difficultOnly) {
		siteCountField.setEnabled(!difficultOnly);
		mapSizeField.setEnabled(!difficultOnly);
		return this;
	}
}
