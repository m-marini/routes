/**
 *
 */
package org.mmarini.routes.swing;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mmarini.routes.model.MapProfile;

/**
 * Pannello di selezione del profilo di generazione delle mappe casuali
 *
 * @author Marco
 *
 */
public class MapProfilePane extends Box {
	private static final double LARGE_SIZE = 15000.;
	private static final double NORMAL_SIZE = 10000.;
	private static final double SMALL_SIZE = 5000.;
	private static final double DIFFICULT_MIN_WEIGHT = 0.2;
	private static final double NORMAL_MIN_WEIGHT = 1. / 3.;
	private static final double EASY_MIN_WEIGHT = 1.;
	private static final double DIFFICUL_FREQUENCE = 2.;
	private static final double NORMAL_FREQUENCE = 1.;
	private static final double EASY_FREQUENCE = 0.4;
	private static final long serialVersionUID = 1L;

	private final SpinnerNumberModel siteCount;
	private final DefaultComboBoxModel<String> mapSizeModel;
	private final DefaultComboBoxModel<String> difficultyModel;
	private final double[] mapSize;
	private final double[] minWeight;
	private final double[] frequence;
	private JComboBox<String> mapSizeField;
	private Component siteCountField;

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
		createContent();
	}

	/**
	     *
	     *
	     */
	private void createContent() {
		Box box = createHorizontalBox();
		box.add(new JLabel(Messages.getString("MapProfilePane.sizeLabel.text"))); //$NON-NLS-1$
		box.add(createHorizontalGlue());
		mapSizeField = new JComboBox<>(mapSizeModel);
		box.add(mapSizeField);
		add(box);

		add(createVerticalStrut(4));

		box = createHorizontalBox();
		box.add(new JLabel(Messages.getString("MapProfilePane.siteCountLabel.text"))); //$NON-NLS-1$
		box.add(createHorizontalGlue());
		siteCountField = new JSpinner(siteCount);
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
	 * @param profile
	 */
	public void retrieveProfile(final MapProfile profile) {
		profile.setSiteCount(siteCount.getNumber().intValue());
		final int sizeIdx = mapSizeModel.getIndexOf(mapSizeModel.getSelectedItem());
		final int diffIdx = difficultyModel.getIndexOf(difficultyModel.getSelectedItem());
		profile.setWidth(mapSize[sizeIdx]);
		profile.setHeight(mapSize[sizeIdx]);
		profile.setFrequence(frequence[diffIdx]);
		profile.setMinWeight(minWeight[diffIdx]);
	}

	/**
	 * Set the panel just for difficulty level
	 *
	 * @param difficultOnly
	 */
	public void setDifficultyOnly(final boolean difficultOnly) {
		siteCountField.setEnabled(!difficultOnly);
		mapSizeField.setEnabled(!difficultOnly);
	}
}
