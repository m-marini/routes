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

import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.GeoMapDeserializer;
import org.mmarini.routes.model.v2.GeoMapSerializer;
import org.mmarini.routes.model.v2.MapProfile;
import org.mmarini.routes.model.v2.Simulator;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;

/**
 * The controller for the main frame
 * <p>
 * The controller manages all the user interactions from the main frame to the
 * main controller and other components
 * </p>
 */
public class MainFrameController {
	private static final Logger logger = LoggerFactory.getLogger(MainFrameController.class);

	private final MainFrame mainFrame;
	private final JFileChooser fileChooser;
	private final MapProfilePane mapProfilePane;
	private final Observable<UIStatus> uiStatusObs;
	private final Simulator<Traffics> simulator;
	private final ControllerFunctions controller;

	/**
	 * Creates the main frame controller
	 *
	 * @param mainFrame   the main frame
	 * @param fileChooser the file chooser
	 * @param uiStatusObs the observable of ui status
	 * @param simulator   the simulator
	 * @param controller  the main controller
	 */
	public MainFrameController(final MainFrame mainFrame, final JFileChooser fileChooser,
			final Observable<UIStatus> uiStatusObs, final Simulator<Traffics> simulator,
			final ControllerFunctions controller) {
		this.mainFrame = mainFrame;
		this.fileChooser = fileChooser;
		this.mapProfilePane = new MapProfilePane();
		this.uiStatusObs = uiStatusObs;
		this.simulator = simulator;
		this.controller = controller;
	}

	/**
	 * Builds the subscriptions for actions
	 *
	 * @param random the random generator
	 * @return the controller
	 */
	public MainFrameController build(final Random random) {

		mainFrame.getOpenMapObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				final Optional<Tuple2<UIStatus, File>> t = loadProcess(st);
				final UIStatus s = t.map(tup -> {
					mainFrame.setSaveActionEnabled(true);
					mainFrame.setTitle(tup.get2().getName());
					final UIStatus uiStatus = tup.get1();
					controller.mapChanged(uiStatus);
					return uiStatus;
				}).orElse(st);
				return s;
			});
		});

		mainFrame.getSaveMapAsObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				final int choice = fileChooser.showSaveDialog(mainFrame);
				if (choice == JFileChooser.APPROVE_OPTION) {
					handleSaveMap(st);
				}
				return st;
			});
		}, controller::showError);

		mainFrame.getSaveMapObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				handleSaveMap(st);
				return st;
			});
		}, controller::showError);

		mainFrame.getNewMapObs().subscribe(ev -> {
			controller.request(st -> {
				final UIStatus status = UIStatus.create();
				controller.mapChanged(status);
				mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
				return status;
			});
		}, controller::showError);

		mainFrame.getNewRandomObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				mapProfilePane.setDifficultyOnly(false);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
						Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final MapProfile profile = mapProfilePane.getProfile();
					logger.info("Selected {}", profile); //$NON-NLS-1$
					final Traffics status = Traffics.random(profile, new Random());
					final UIStatus uiStatus = UIStatus.create().setTraffics(status);
					controller.mapChanged(uiStatus);
					mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
					return uiStatus;
				} else {
					return st;
				}
			});
		}, controller::showError);

		mainFrame.getExitObs().subscribe(ev -> {
			mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
		}, controller::showError);

		mainFrame.getOptimizeObs().withLatestFrom(uiStatusObs, (ev, status) -> status).subscribe(status -> {
			controller.request(st -> {
				final OptimizePane optimizePane = new OptimizePane();
				optimizePane.setSpeedLimit(status.getSpeedLimit());
				final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
						Messages.getString("Controller.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final double speedLimit = optimizePane.getSpeedLimit();
					if (optimizePane.isOptimizeSpeed()) {
						final UIStatus newStatus = status.setSpeedLimit(speedLimit).optimizeSpeed();
						controller.mapChanged(newStatus);
						return newStatus;
					}
				}
				return status;
			});
		}, controller::showError);

		mainFrame.getFrequenceObs().withLatestFrom(uiStatusObs, (ev, status) -> status).subscribe(status -> {
			controller.request(st -> {
				final FrequencePane frequencePane = new FrequencePane();
				final double frequence = status.getTraffics().getMap().getFrequence();
				frequencePane.setFrequence(frequence);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencePane,
						Messages.getString("Controller.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final UIStatus newStatus = status.setFrequence(frequencePane.getFrequence());
					controller.mapChanged(newStatus);
					return newStatus;
				}
				return status;
			});
		}, controller::showError);

		mainFrame.getSpeedObs().withLatestFrom(uiStatusObs, (speed, status) -> {
			return Tuple.of(status, speed);
		}).subscribe(t -> {
			simulator.setSpeed(t.get2());
		}, controller::showError);

		mainFrame.getStopObs().subscribe(ev -> {
			if (mainFrame.isStopped()) {
				simulator.stop();
			} else {
				simulator.start();
			}
		}, controller::showError);

		mainFrame.getRoutesObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				final WeightsTable table = new WeightsTable();
				table.setWeights(st.getTraffics().getMap().getWeights());
				final JScrollPane pane = new JScrollPane(table);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, pane,
						Messages.getString("Controller.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final UIStatus newStatus = st.setWeights(table.getWeights());
					return newStatus;
				}
				return st;
			});
		}, controller::showError);

		mainFrame.getRandomizeObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				mapProfilePane.setDifficultyOnly(true);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
						Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final MapProfile profile = mapProfilePane.getProfile();
					final UIStatus newStatus = st.randomize(profile.getMinWeight(), random)
							.setFrequence(profile.getFrequence());
					return newStatus;
				}
				return st;
			});
		}, controller::showError);

		mainFrame.getVehicleInfoObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			controller.request(st1 -> {
				final Traffics traffics = st.getTraffics();
				final TrafficsTable table = new TrafficsTable(traffics);
				final JScrollPane pane = new JScrollPane(table);
				pane.setBorder(BorderFactory
						.createTitledBorder(Messages.getString("MainFrameController.trafficsPane.description")));
				JOptionPane.showMessageDialog(mainFrame, pane,
						Messages.getString("MainFrameController.trafficsPane.title"), //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
				return st;
			});
		}, controller::showError);

		return this;
	}

	/**
	 * Handles of save action
	 *
	 * @param status the status
	 * @return the controller
	 */
	private MainFrameController handleSaveMap(final UIStatus status) {
		final File file = fileChooser.getSelectedFile();
		if (file.exists() && !file.canWrite()) {
			controller.showError(Messages.getString("Controller.writeError.message"), file); //$NON-NLS-1$
		} else {
			try {
				logger.info("Saving {} ...", file); //$NON-NLS-1$
				new GeoMapSerializer(status.getTraffics().getMap()).writeFile(file);
				mainFrame.setSaveActionEnabled(true);
				mainFrame.setTitle(file.getPath());
			} catch (final Throwable e) {
				logger.error(e.getMessage(), e);
				try {
					controller.showError(e);
				} catch (final Throwable e1) {
				}
			}
		}
		return this;
	}

	/**
	 * Returns the new ui status and selected file of the load process.
	 *
	 * @param status the initial uistatus
	 */
	private Optional<Tuple2<UIStatus, File>> loadProcess(final UIStatus status) {
		final int choice = fileChooser.showOpenDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			if (!file.canRead()) {
				controller.showError(MessageFormat.format(Messages.getString("Controller.readError.message"), file)); //$NON-NLS-1$
				return Optional.empty();
			} else {
				try {
					logger.debug("loadProcess {}", file); //$NON-NLS-1$
					final GeoMap map = GeoMapDeserializer.create().parse(file);
					final Traffics newStatus = Traffics.create(map);
					return Optional.of(Tuple.of(status.setTraffics(newStatus), file));
				} catch (final Throwable ex) {
					controller.showError(ex);
					return Optional.empty();
				}
			}
		} else {
			return Optional.empty();
		}
	}
}
