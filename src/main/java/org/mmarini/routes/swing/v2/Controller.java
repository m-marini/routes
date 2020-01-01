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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mmarini.routes.model.v2.EdgeTraffic;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.MapProfile;
import org.mmarini.routes.model.v2.SimulationStatus;
import org.mmarini.routes.model.v2.SimulationStatusDeserializer;
import org.mmarini.routes.model.v2.Simulator;
import org.mmarini.routes.model.v2.SiteNode;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Observable;

/**
 *
 */
public class Controller {
	private static final int BORDER_SCALE = 10;
	private static final double SCALE_FACTOR = Math.sqrt(2);
	private static final int MAP_BORDER = 60;
	private static final double DEFAULT_SCALE = 10;
	private static final double MIN_GRID_SIZE_METERS = 1;
	private static final int MIN_GRID_SIZE_PIXELS = 10;
	private static final String INITIAL_MAP = "/test.yml"; // $NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	/**
	 * Returns the default status
	 */
	private static SimulationStatus loadDefault() {
		final URL url = Controller.class.getResource(INITIAL_MAP);
		if (url != null) {
			try {
				return SimulationStatusDeserializer.create().parse(url);
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return SimulationStatus.create();
	}

	/**
	 * Returns the list of patterns by key.n
	 *
	 * @param key the key
	 */
	private static List<String> loadPatterns(final String key) {
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

	private final JFileChooser fileChooser;
	private final MapProfilePane mapProfilePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final MapElementPane mapElementPane;
	private final MainFrame mainFrame;
	private final Simulator simulator;
	private final ScrollMap scrollMap;
	private final MapViewPane mapViewPane;
	private final List<String> gridLegendPattern;
	private final List<String> pointLegendPattern;
	private final List<String> edgeLegendPattern;
	/** The scale of route map (pixels/m) */
	private double scale;
	private Optional<SimulationStatus> currentStatus;

	/**
	 *
	 */
	public Controller() {
		this.simulator = new Simulator();
		this.routeMap = new RouteMap();
		this.mapProfilePane = new MapProfilePane();
		this.fileChooser = new JFileChooser();
		this.explorerPane = new ExplorerPane();
		this.mapElementPane = new MapElementPane();
		this.scrollMap = new ScrollMap(routeMap);
		this.mapViewPane = new MapViewPane(scrollMap);
		this.mainFrame = new MainFrame(mapViewPane, explorerPane, mapElementPane);
		this.gridLegendPattern = loadPatterns("ScrollMap.gridLegendPattern");
		this.pointLegendPattern = loadPatterns("ScrollMap.pointLegendPattern");
		this.edgeLegendPattern = loadPatterns("ScrollMap.edgeLegendPattern");
		this.currentStatus = Optional.empty();
		this.setScale(DEFAULT_SCALE);
		init();
	}

	/**
	 *
	 */
	private Controller bindAll() {
		mainFrame.getNewRandomObs().subscribe(this::handleNewRandomMap);
		mainFrame.getSaveMapAsObs().subscribe(this::handleSaveAsMap);
		mainFrame.getOpenMapObs().subscribe(this::handleOpenMap);
		mainFrame.getSaveMapObs().subscribe(this::handleSaveMap);
		mainFrame.getNewMapObs().subscribe(this::handleNewMap);
		mainFrame.getExitObs().subscribe(ev -> {
			mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
		});

		createCenterMapObs().subscribe(point -> {
			scrollMap.getViewport().setViewPosition(point);
			mainFrame.repaint();
		});

		createScaleObs().subscribe(t -> {
			logger.debug("{}", t);
			setScale(t.getElem2());
			scrollMap.getViewport().setViewPosition(t.getElem1());
			routeMap.repaint();
//			mainFrame.repaint();
		});

		scrollMap.getChangeObs().subscribe(ev -> scrollMap.repaint());
		routeMap.getMouseObs().subscribe(ev -> {
			Optional.ofNullable(ev.getPoint()).ifPresent(pt -> {
				final Point2D mapPt = computeMapLocation(pt);
				scrollMap.setHud(computeHud(pointLegendPattern, mapPt));
			});
		});

		simulator.getOutput().subscribe(this::handleNewStatus);

		return this;
	}

	/**
	 * Returns the head up display text
	 *
	 * @param patterns the text pattern
	 * @param point
	 */
	private List<String> computeHud(final List<String> patterns, final Point2D point) {
		final Object[] parms = new Object[] { routeMap.getGridSize(), point.getX(), point.getY(), 0 };
		/*
		 * Compute the pattern
		 */
		final List<String> texts = patterns.stream().map(pattern -> MessageFormat.format(pattern, parms))
				.collect(Collectors.toList());
		return texts;
	}

	/**
	 * Returns the location in the map of a route map screen point
	 *
	 * @param pt the screen point
	 */
	private Point2D computeMapLocation(final Point pt) {
		final Point2D result = getInverseTransform().transform(pt, new Point2D.Double());
		return result;
	}

	/**
	 * Returns the observable of viewport location for center map action
	 */
	private Observable<Point> createCenterMapObs() {
		return routeMap.getMouseObs().filter(ev -> ev.getID() == MouseEvent.MOUSE_CLICKED).map(ev -> {
			final Point mouseAt = ev.getPoint();
			final Dimension size = scrollMap.getViewport().getExtentSize();
			final int x = Math.max(0, mouseAt.x - size.width / 2);
			final int y = Math.max(0, mouseAt.y - size.height / 2);
			final Point newViewPos = new Point(x, y);
			return newViewPos;
		});
	}

	/**
	 * Returns the observable of viewport location and scale for zoom action
	 */
	private Observable<Tuple2<Point, Double>> createScaleObs() {
		return routeMap.getMouseWheelObs().map(ev -> {
			final Point point = ev.getPoint();
			final double newScale = this.scale * Math.pow(SCALE_FACTOR, -ev.getWheelRotation());
			final Point p0 = scrollMap.getViewport().getViewPosition();
			final int x = Math.max(0, (int) Math.round((point.x - MAP_BORDER) * (newScale / scale - 1) + p0.x));
			final int y = Math.max(0, (int) Math.round((point.y - MAP_BORDER) * (newScale / scale - 1) + p0.y));
			final Point corner = new Point(x, y);
			return new Tuple2<>(corner, newScale);
		});
	}

	/**
	 * Returns the explorerPane
	 */
	public ExplorerPane getExplorerPane() {
		return explorerPane;
	}

	/**
	 * Returns the grid size in meters
	 */
	private double getGridSize() {
		// size meters to have a grid of at least 10 pixels in the screen
		final double size = MIN_GRID_SIZE_PIXELS / scale;
		// Minimum grid of size 1 m
		double gridSize = MIN_GRID_SIZE_METERS;
		while (size > gridSize) {
			gridSize *= 10;
		}
		return gridSize;
	}

	/**
	 * Returns the transformation from screen coordinates to map coordinates
	 */
	private AffineTransform getInverseTransform() {
		try {
			return getTransform().createInverse();
		} catch (final NoninvertibleTransformException e) {
			e.printStackTrace();
			return new AffineTransform();
		}
	}

	/**
	 * Returns the map bound
	 *
	 * @param map the map
	 */
	private Rectangle2D getMapBound() {
		final Rectangle2D result = currentStatus.flatMap(st -> {
			final GeoMap map = st.getMap();
			final Set<MapNode> all = new HashSet<>(map.getSites());
			all.addAll(map.getNodes());
			final OptionalDouble x0 = all.parallelStream().mapToDouble(n -> n.getX()).min();
			final OptionalDouble x1 = all.parallelStream().mapToDouble(n -> n.getX()).max();
			final OptionalDouble y0 = all.parallelStream().mapToDouble(n -> n.getY()).min();
			final OptionalDouble y1 = all.parallelStream().mapToDouble(n -> n.getY()).max();

			final Optional<Rectangle2D> result1 = (x0.isPresent() && x1.isPresent() && y0.isPresent() && y1.isPresent())
					? Optional.of(new Rectangle2D.Double(x0.getAsDouble(), y0.getAsDouble(),
							x1.getAsDouble() - x0.getAsDouble(), y1.getAsDouble() - y0.getAsDouble()))
					: Optional.empty();
			;
			return result1;
		}).orElseGet(() -> new Rectangle2D.Double());
		return result;
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
	 * Returns the map size
	 *
	 * @param map the map
	 */
	private Dimension getScreenMapSize() {
		final Rectangle2D bound = getMapBound();
		final int width = (int) Math.round(bound.getWidth() * scale) + MAP_BORDER * 2;
		final int height = (int) Math.round(bound.getHeight() * scale) + MAP_BORDER * 2;
		final Dimension result = new Dimension(width, height);
		return result;
	}

	/**
	 * Returns the transformation from map coordinates to screen coordinates
	 */
	private AffineTransform getTransform() {
		return getTransform(scale);
	}

	/**
	 * Returns the transformation from map coordinates to screen coordinate
	 *
	 * @param scale the scale
	 */
	private AffineTransform getTransform(final double scale) {
		final Rectangle2D mapBound = getMapBound();
		final AffineTransform result = AffineTransform.getTranslateInstance(MAP_BORDER, MAP_BORDER);
		result.scale(scale, scale);
		result.translate(-mapBound.getMinX(), -mapBound.getMinY());
		return result;
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
	 *
	 * @param status
	 * @return
	 */
	private Controller handleNewStatus(final SimulationStatus status) {
		currentStatus = Optional.of(status);
		final Dimension preferredSize = getScreenMapSize();
		routeMap.setTransform(getTransform()).setStatus(status).setPreferredSize(preferredSize);
		scrollMap.setHud(computeHud(pointLegendPattern, new Point2D.Double()));
		mainFrame.repaint();
		return this;
	}

	/**
	 * Returns the controller with open map as ... event handled
	 *
	 * @param ev the event
	 */
	private Controller handleOpenMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
		simulator.stop();
		final int choice = fileChooser.showOpenDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			if (!file.canRead()) {
				showError(Messages.getString("Controller.readError.message"), new Object[] { file }); //$NON-NLS-1$
			} else {
				try {
					logger.info("Opening {} ...", file);
					final SimulationStatus status = SimulationStatusDeserializer.create().parse(file);
					simulator.setSimulationStatus(status);
					mainFrame.setSaveActionEnabled(true);
					mainFrame.setTitle(file.getName());
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
					showError(e.getMessage());
				} catch (final Throwable e) {
					logger.error(e.getMessage(), e);
					showError(e);
				}
//				mapViewPane.reset();
			}
		}
		simulator.start();
		return this;
	}

	/**
	 * Returns the controller with save map as ... event handled
	 *
	 * @param ev the event
	 */
	private Controller handleSaveAsMap(final ActionEvent ev) {
		logger.info(String.valueOf(ev));
		simulator.stop();
		final int choice = fileChooser.showSaveDialog(mainFrame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			handleSaveMap(ev);
		}
		simulator.start();
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
				logger.info("Saving {} ...", file);
//				new SimulationStatusSerializer(null).writeFile(file);
				mainFrame.setSaveActionEnabled(true);
				mainFrame.setTitle(file.getPath());
			} catch (final Throwable e) {
				logger.error(e.getMessage(), e);
				showError(e);
			}
		}
		return this;
	}

	/**
	 * @return
	 *
	 */
	private Controller init() {
		this.fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("Controller.filetype.title"), //$NON-NLS-1$
				"yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$
		bindAll();

		final SimulationStatus status = loadDefault();
		simulator.setSimulationStatus(status);

		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		return this;
	}

	/**
	 *
	 * @param scale
	 * @return
	 */
	private Controller setScale(final double scale) {
		this.scale = scale;
		routeMap.setTransform(getTransform()).setGridSize(getGridSize()).setBorderPainted(scale >= BORDER_SCALE)
				.setPreferredSize(getScreenMapSize());
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

	/**
	 * @return
	 */
	SimulationStatus testMap() {
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
		return status;
	}
}
