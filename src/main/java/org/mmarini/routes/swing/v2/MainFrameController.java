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

/**
 * Controller for the main frame.
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
	private final Simulator<Traffics> simulator;
	private final RouteMap routeMap;
	private final OptimizePane optimizePane;
	private final ControllerFunctions controller;

	/**
	 * Creates the main frame controller.
	 *
	 * @param mainFrame    the main frame
	 * @param fileChooser  the file chooser
	 * @param simulator    the simulator
	 * @param optimizePane the optimize panel
	 * @param routeMap     the route map panel
	 * @param controller   the main controller
	 */
	public MainFrameController(final MainFrame mainFrame, final JFileChooser fileChooser,
			final Simulator<Traffics> simulator, final OptimizePane optimizePane, final RouteMap routeMap,
			final ControllerFunctions controller) {
		this.routeMap = routeMap;
		this.mainFrame = mainFrame;
		this.fileChooser = fileChooser;
		this.mapProfilePane = new MapProfilePane();
		this.simulator = simulator;
		this.controller = controller;
		this.optimizePane = optimizePane;
	}

	/**
	 * Builds the subscriptions for actions.
	 *
	 * @param random the random generator
	 * @return the controller
	 */
	public MainFrameController build(final Random random) {

		mainFrame.getOpenMapFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				loadProcess().ifPresent(tup -> {
					mainFrame.setSaveActionEnabled(true);
					mainFrame.setTitle(tup.get2().getName());
					controller.mapChanged(tup.get1());
				});
			});
		});

		mainFrame.getSaveMapAsFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final int choice = fileChooser.showSaveDialog(mainFrame);
					if (choice == JFileChooser.APPROVE_OPTION) {
						handleSaveMap(traffics);
					}
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			});
		}, controller::showError);

		mainFrame.getSaveMapFlow().subscribe(ev -> {
			routeMap.getTraffics().ifPresentOrElse(traffics -> {
				handleSaveMap(traffics);
			}, () -> {
				logger.error("Missing traffics", new Error());
			});
		}, controller::showError);

		mainFrame.getNewMapFlow().subscribe(ev -> {
			mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
			controller.mapChanged(Traffics.create());
		}, controller::showError);

		mainFrame.getNewRandomFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				mapProfilePane.setDifficultyOnly(false);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
						Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final MapProfile profile = mapProfilePane.getProfile();
					logger.info("Selected {}", profile); //$NON-NLS-1$
					mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
					controller.mapChanged(Traffics.random(profile, new Random()));
				}
			});
		}, controller::showError);

		mainFrame.getExitFlow().subscribe(ev -> {
			mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
		}, controller::showError);

		mainFrame.getOptimizeFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
							Messages.getString("Controller.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
					if (opt == JOptionPane.OK_OPTION) {
						final double speedLimit = optimizePane.getSpeedLimit();
						controller.mapChanged(traffics.optimizeSpeed(speedLimit));
					}
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			});
		}, controller::showError);

		mainFrame.getFrequenceFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final FrequencePane frequencePane = new FrequencePane();
					final double frequence = traffics.getMap().getFrequence();
					frequencePane.setFrequence(frequence);
					final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencePane,
							Messages.getString("Controller.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
					if (opt == JOptionPane.OK_OPTION) {
						controller.mapChanged(traffics.setFrequence(frequence));
					}
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			});
		}, controller::showError);

		mainFrame.getSpeedFlow().subscribe(speed -> {
			simulator.setSpeed(speed);
		}, controller::showError);

		mainFrame.getStopFlow().subscribe(ev -> {
			if (mainFrame.isStopped()) {
				simulator.stop();
			} else {
				simulator.start();
			}
		}, controller::showError);

		mainFrame.getRoutesFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final WeightsTable table = new WeightsTable();
					table.setWeights(traffics.getMap().getWeights());
					final JScrollPane pane = new JScrollPane(table);
					final int opt = JOptionPane.showConfirmDialog(mainFrame, pane,
							Messages.getString("Controller.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
					if (opt == JOptionPane.OK_OPTION) {
						controller.mapChanged(traffics.setWeights(table.getWeights()));
					}
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			});
		}, controller::showError);

		mainFrame.getRandomizeFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					mapProfilePane.setDifficultyOnly(true);
					final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
							Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
					if (opt == JOptionPane.OK_OPTION) {
						final MapProfile profile = mapProfilePane.getProfile();
						controller.mapChanged(traffics.randomize(profile.getMinWeight(), random)
								.setFrequence(profile.getFrequence()));
					}
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			});
		}, controller::showError);

		mainFrame.getVehicleInfoFlow().subscribe(ev -> {
			controller.withSimulationStop(() -> {
				routeMap.getTraffics().ifPresentOrElse(traffics -> {
					final TrafficsTable table = new TrafficsTable(traffics);
					final JScrollPane pane = new JScrollPane(table);
					pane.setBorder(BorderFactory
							.createTitledBorder(Messages.getString("MainFrameController.trafficsPane.description")));
					JOptionPane.showMessageDialog(mainFrame, pane,
							Messages.getString("MainFrameController.trafficsPane.title"), //$NON-NLS-1$
							JOptionPane.INFORMATION_MESSAGE);
				}, () -> {
					logger.error("Missing traffics", new Error());
				});
			});
		}, controller::showError);

		return this;
	}

	/**
	 * Handles of save action.
	 *
	 * @param traffics the traffics to save
	 * @return the controller
	 */
	private MainFrameController handleSaveMap(final Traffics traffics) {
		final File file = fileChooser.getSelectedFile();
		if (file.exists() && !file.canWrite()) {
			controller.showError(Messages.getString("Controller.writeError.message"), file); //$NON-NLS-1$
		} else {
			try {
				logger.info("Saving {} ...", file); //$NON-NLS-1$
				new GeoMapSerializer(traffics.getMap()).writeFile(file);
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
	 */
	private Optional<Tuple2<Traffics, File>> loadProcess() {
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
					final Traffics traffics = Traffics.create(map);
					return Optional.of(Tuple.of(traffics, file));
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
