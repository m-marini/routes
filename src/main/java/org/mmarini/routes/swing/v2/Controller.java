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

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mmarini.routes.model.v2.EdgeTraffic;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapProfile;
import org.mmarini.routes.model.v2.SimulationStatus;
import org.mmarini.routes.model.v2.Simulator;
import org.mmarini.routes.model.v2.SiteNode;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;

/**
 *
 */
public class Controller {
	public static final String INITIAL_MAP = "/test.yml"; // $NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private final JFileChooser fileChooser;
	private final MapProfilePane mapProfilePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final MapElementPane mapElementPane;
	private final MainFrame mainFrame;
	private final Simulator simulator;

	/**
	 *
	 */
	public Controller(final MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		mapProfilePane = new MapProfilePane();
		fileChooser = new JFileChooser();
		routeMap = new RouteMap();
		explorerPane = new ExplorerPane();
		mapElementPane = new MapElementPane();
		simulator = new Simulator();

		fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("Controller.filetype.title"), //$NON-NLS-1$
				"yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$
		bindAll();
	}

	/**
	 *
	 */
	private Controller bindAll() {
		return bindMainframe();
	}

	/**
	 * Returns the controller with bound main frame
	 */
	private Controller bindMainframe() {
		mainFrame.getNewRandomObs().subscribe(this::handleNewRandomMap);
		mainFrame.getSaveMapAsObs().subscribe(this::handleSaveAsMap);
		mainFrame.getOpenMapObs().subscribe(this::handleOpenMap);
		mainFrame.getSaveMapObs().subscribe(this::handleSaveMap);
		mainFrame.getNewMapObs().subscribe(this::handleNewMap);
		simulator.getOutput().compose(SwingObservable.observeOnEdt()).subscribe(routeMap::setStatus);

		final SiteNode s0 = SiteNode.create(15, 15);
		final SiteNode s1 = SiteNode.create(1000, 1000);
		final Set<SiteNode> sites = Set.of(s0, s1);
		final Set<MapEdge> edges = Set.of(MapEdge.create(s0, s1), MapEdge.create(s1, s0));
		final GeoMap map = GeoMap.create().setSites(sites).setEdges(edges);
		final Set<EdgeTraffic> traffics = edges.stream().map(EdgeTraffic::create).collect(Collectors.toSet());
		final Map<Tuple2<SiteNode, SiteNode>, Double> weights = Map.of(new Tuple2<>(s0, s1), Double.valueOf(1),
				new Tuple2<>(s1, s0), Double.valueOf(1));
		final SimulationStatus status = SimulationStatus.create().setGeoMap(map).setTraffics(traffics)
				.setWeights(weights);
		;
		simulator.setSimulationStatus(status).start();

		return this;
	}

	/**
	 * Returns the explorerPane
	 */
	public ExplorerPane getExplorerPane() {
		return explorerPane;
	}

	/**
	 * Returns the mapElementPane
	 */
	public MapElementPane getMapElementPane() {
		return mapElementPane;
	}

	/**
	 * Returns the routeMap
	 */
	public RouteMap getRouteMap() {
		return routeMap;
	}

	/**
	 * Returns the controller with new random map event handled
	 *
	 * @param ev the event
	 */
	private Controller handleNewMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
//			handler.clearMap();
//			refresh();
//			mapViewPane.selectSelector();
//			mapViewPane.reset();
//			mapViewPane.setSelectedElement((MapNode) null);
//			mapElementPane.setSelectedElement((MapNode) null);
		mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
		return this;
	}

	/**
	 * Returns the controller with new random map event handled
	 *
	 * @param ev the event
	 */
	private Controller handleNewRandomMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
//		stopSimulation();
//		mapProfilePane.setDifficultyOnly(false);
		final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
				Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
		if (opt == JOptionPane.OK_OPTION) {
			final MapProfile profile = mapProfilePane.getProfile();
			logger.info("Selected {}", profile);
//			handler.createRandomMap(profile);
//			refresh();
//			mapViewPane.selectSelector();
//			mapViewPane.reset();
//			mapViewPane.setSelectedElement((MapNode) null);
//			mapElementPane.setSelectedElement((MapNode) null);
			mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
		}
//		startSimulation();
		return this;
	}

	/**
	 * Returns the controller with open map as ... event handled
	 *
	 * @param ev the event
	 */
	private Controller handleOpenMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
		// stopSimulation();
		final int choice = fileChooser.showOpenDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			if (!file.canRead()) {
				showError(Messages.getString("Controller.readError.message"), new Object[] { file }); //$NON-NLS-1$
			} else {
				try {
					logger.info("Opening {} ...", file);
//					handler.load(file);
					mainFrame.setSaveActionEnabled(true);
					mainFrame.setTitle(file.getName());
//				} catch (final SAXParseException e) {
//					logger.error(e.getMessage(), e);
//					showError(Messages.getString("RouteMediator.parseError.message"), new Object[] { e.getMessage(), //$NON-NLS-1$
//							e.getLineNumber(), e.getColumnNumber() });
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
					showError(e.getMessage());
				} catch (final Throwable e) {
					logger.error(e.getMessage(), e);
					showError(e);
				}
//				refresh();
//				mapViewPane.reset();
			}
		}
//		startSimulation();
		return this;
	}

	/**
	 * Returns the controller with save map as ... event handled
	 *
	 * @param ev the event
	 */
	private Controller handleSaveAsMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
//			stopSimulation();
		final int choice = fileChooser.showSaveDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			handleSaveMap(ev);
		}
		// startSimulation();
		return this;
	}

	/**
	 * Returns the controller with save map event handled
	 *
	 * @param ev the event
	 */
	private Controller handleSaveMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
		final File file = fileChooser.getSelectedFile();
		if (file.exists() && !file.canWrite()) {
			showError(Messages.getString("Controller.writeError.message"), new Object[] { file }); //$NON-NLS-1$
		} else {
			try {
//				handler.save(file);
				mainFrame.setSaveActionEnabled(true);
				logger.info("Saving {} ...", file);
				mainFrame.setTitle(file.getPath());
			} catch (final Throwable e) {
				logger.error(e.getMessage(), e);
				showError(e);
			}
		}
		return this;
	}

	/**
	 * Returns the controller with error message
	 *
	 * @param message the message
	 */
	private Controller showError(final String message) {
		JOptionPane.showMessageDialog(mainFrame, message, Messages.getString("RouteMediator.error.title"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
		return this;
	}

	/**
	 * Returns the controller with error message from pattern
	 *
	 * @param pattern   the pattern
	 * @param arguments the argument
	 */
	private Controller showError(final String pattern, final Object[] arguments) {
		return showError(MessageFormat.format(pattern, arguments));
	}

	/**
	 * Returns the controller with error message from exception
	 *
	 * @param e the exception
	 */
	private Controller showError(final Throwable e) {
		logger.error(e.getMessage(), e);
		return showError("{0}", new Object[] { e.getMessage(), //$NON-NLS-1$
				e.getMessage() });
	}
}
