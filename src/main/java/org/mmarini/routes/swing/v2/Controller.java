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

import static org.mmarini.routes.swing.v2.RxUtils.withMouseObs;
import static org.mmarini.routes.swing.v2.RxUtils.withPointObs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
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

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 *
 */
public class Controller {
	private static final double DEFAULT_SCALE = 1;
	private static final double BORDER_SCALE = 1;
	private static final double SCALE_FACTOR = Math.pow(10, 1.0 / 6);
	private static final int MAP_INSETS = 60;
	private static final double MIN_GRID_SIZE_METERS = 1;
	private static final int MIN_GRID_SIZE_PIXELS = 10;
	private static final String INITIAL_MAP = "/test.yml"; // $NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	/**
	 * Returns true if location is in range of an edge
	 *
	 * @param node  edge
	 * @param point point
	 */
	private static boolean isInRange(final MapEdge edge, final Point2D point) {
		final double distance = edge.getDistance(point);
		return distance <= RouteMap.EDGE_WIDTH / 2;
	}

	/**
	 * Returns true if location is in range of node
	 *
	 * @param node  node
	 * @param point location
	 */
	private static boolean isInRange(final MapNode node, final Point2D point) {
		final double distance = node.getLocation().distance(point);
		return distance <= RouteMap.NODE_SIZE / 2;
	}

	/**
	 * Returns true if location is in range of site
	 *
	 * @param node  site
	 * @param point point
	 */
	private static boolean isInRange(final SiteNode node, final Point2D point) {
		final double distance = node.getLocation().distance(point);
		return distance <= RouteMap.SITE_SIZE / 2;
	}

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

	private static Point toPoint(final Point2D point) {
		return new Point((int) Math.round(point.getX()), (int) Math.round(point.getY()));
	}

	private final JFileChooser fileChooser;
	private final MapProfilePane mapProfilePane;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final MapNodePane nodePane;
	private final EdgePane edgePane;
	private final MapElementPane mapElementPane;
	private final MainFrame mainFrame;
	private final Simulator simulator;
	private final ScrollMap scrollMap;
	private final MapViewPane mapViewPane;
	private final List<String> gridLegendPattern;
	private final List<String> pointLegendPattern;
	private final List<String> edgeLegendPattern;
	private final PublishSubject<Integer> edtObs;

	/** The scale of route map (pixels/m) */
	private double scale;
	private Optional<SimulationStatus> currentStatus;
	private Optional<SimulationStatus> status;

	/**
	 *
	 */
	public Controller() {
		this.simulator = new Simulator();
		this.routeMap = new RouteMap();
		this.mapProfilePane = new MapProfilePane();
		this.fileChooser = new JFileChooser();
		this.explorerPane = new ExplorerPane();
		this.edgePane = new EdgePane();
		this.nodePane = new MapNodePane();
		this.mapElementPane = new MapElementPane(nodePane, edgePane);
		this.scrollMap = new ScrollMap(routeMap);
		this.mapViewPane = new MapViewPane(scrollMap);
		this.mainFrame = new MainFrame(mapViewPane, explorerPane, mapElementPane);
		this.gridLegendPattern = loadPatterns("ScrollMap.gridLegendPattern");
		this.pointLegendPattern = loadPatterns("ScrollMap.pointLegendPattern");
		this.edgeLegendPattern = loadPatterns("ScrollMap.edgeLegendPattern");
		this.currentStatus = Optional.empty();
		this.status = Optional.empty();
		edtObs = PublishSubject.create();
		this.setScale(DEFAULT_SCALE);
		init();
		edtObs.subscribe(this::handleEdt);
		edtObs.onNext(1);
		simulator.start();
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
		simulator.getOutput().subscribe(s -> status = Optional.of(s));
		return bindForMapElementPane().bindForScrollMap().bindForExplorerPane().bindForRouteMap();
	}

	/**
	 *
	 */
	private Controller bindForExplorerPane() {
		createExplorerSelectNodeObs().subscribe(node -> {
			logger.debug("onNext explorer node selected {}", node);
			explorerPane.setSelectedNode(node);
		});

		createExplorerSelectSiteObs().subscribe(node -> {
			logger.debug("onNext explorer site selected {}", node);
			explorerPane.setSelectedSite(node);
		});

		createExplorerSelectEdgeObs().subscribe(edge -> {
			logger.debug("onNext explorer site selected {}", edge);
			explorerPane.setSelectedEdge(edge);
		});

		createNoneSelectionObs().subscribe(pt -> explorerPane.clearSelection());
		return this;
	}

	/**
	 * @return
	 *
	 */
	private Controller bindForMapElementPane() {
		createDetailSiteObs().subscribe(mapElementPane::setNode);
		createDetailNodeObs().subscribe(mapElementPane::setNode);
		createDetailEdgeObs().subscribe(mapElementPane::setEdge);
		return this;
	}

	/**
	 * @return
	 *
	 */
	private Controller bindForRouteMap() {
		createSelectionNodeObs().subscribe(node -> {
			logger.debug("onNext route map selected node {}", node);
			routeMap.setSelectedNode(node);
		});
		createSelectionSiteObs().subscribe(node -> {
			logger.debug("onNext route map selected site {}", node);
			routeMap.setSelectedSite(node);
		});
		createSelectionEdgeObs().subscribe(edge -> {
			logger.debug("onNext route map selected edge {}", edge);
			routeMap.setSelectedEdge(edge);
		});
		return this;
	}

	/**
	 * @return
	 *
	 */
	private Controller bindForScrollMap() {
		createMapViewPosObs().subscribe(point -> {
			logger.debug("onNext viewport at {}", point);
			scrollMap.getViewport().setViewPosition(point);
		});

		createScaleObs().subscribe(t -> {
			logger.debug("onNext scale at {}", t);
			setScale(t.getElem2());
			scrollMap.getViewport().setViewPosition(t.getElem1());
		});

		createHudObs().subscribe(scrollMap::setHud);
		return this;
	}

	/**
	 * Returns the head up display text
	 *
	 * @param patterns the text pattern
	 * @param point    cursor point
	 */

	private List<String> computeHud(final List<String> patterns, final Point2D point) {
		final Object[] parms = new Object[] { getGridSize(), point.getX(), point.getY(), scale };
		// Compute the pattern
		final List<String> texts = patterns.stream().map(pattern -> MessageFormat.format(pattern, parms))
				.collect(Collectors.toList());
		return texts;
	}

	/**
	 * Returns the location for a pivot point
	 *
	 * @param pivot    the pivot point
	 * @param newScale the new scale
	 */
	private Point computeViewportLocationWithScale(final Point pivot, final double newScale) {
		final Point p0 = scrollMap.getViewport().getViewPosition();
		final int x = Math.max(0, (int) Math.round((pivot.x - MAP_INSETS) * (newScale / scale - 1) + p0.x));
		final int y = Math.max(0, (int) Math.round((pivot.y - MAP_INSETS) * (newScale / scale - 1) + p0.y));
		final Point corner = new Point(x, y);
		return corner;
	}

	/**
	 * Returns the viewport position for a center point
	 *
	 * @param center the center point
	 */
	private Point computeViewportPosition(final Point center) {
		final Dimension size = scrollMap.getViewport().getExtentSize();
		final int x = Math.max(0, center.x - size.width / 2);
		final int y = Math.max(0, center.y - size.height / 2);
		final Point newViewPos = new Point(x, y);
		return newViewPos;
	}

	/**
	 * Returns the observable of edge detail
	 */
	private Observable<MapEdge> createDetailEdgeObs() {
		final Observable<MapEdge> result = explorerPane.getEdgeObs();
		return result;
	}

	/**
	 * Returns the observable of node detail
	 */
	private Observable<MapNode> createDetailNodeObs() {
		final Observable<MapNode> result = explorerPane.getNodeObs();
		return result;
	}

	/**
	 * Returns the location in the map of a route map screen point
	 *
	 * @param pt the screen point
	 */
//	private Point2D computeMapLocation(final Point pt) {
//		final Point2D result = getInverseTransform().transform(pt, new Point2D.Double());
//		return result;
//	}

	/**
	 * Returns the observable of site detail
	 */
	private Observable<SiteNode> createDetailSiteObs() {
		final Observable<SiteNode> result = explorerPane.getSiteObs();
		return result;
	}

	/**
	 * Returns the observable of edge selection for explorer panel
	 */
	private Observable<MapEdge> createExplorerSelectEdgeObs() {
		return createSelectionEdgeObs().filter(site -> site.isPresent()).map(Optional::get);
	}

	/**
	 * Returns the observable of node selection for explore panel
	 */
	private Observable<MapNode> createExplorerSelectNodeObs() {
		return createSelectionNodeObs().filter(site -> site.isPresent()).map(Optional::get);
	}

	/**
	 * Returns the observable of site selection for explorer panel
	 */
	private Observable<SiteNode> createExplorerSelectSiteObs() {
		return createSelectionSiteObs().filter(site -> site.isPresent()).map(Optional::get);
	}

	/**
	 * Returns the observable of head up display
	 */
	private Observable<List<String>> createHudObs() {
		return withMouseObs(routeMap.getMouseObs()).move().withPoint().transform(() -> getInverseTransform())
				.observable().map(mapPt -> computeHud(pointLegendPattern, mapPt));
	}

	/**
	 * Returns the observable of viewport location for center map action.
	 * <p>
	 * The viewport location is changed when the mouse click on an empty point in
	 * the map or when an element is selected in the explorer panel
	 * </p>
	 */
	private Observable<Point> createMapViewPosObs() {
		final Observable<Point2D> onMouseClick = withMouseObs(routeMap.getMouseObs()).click().withPoint()
				.transform(() -> getInverseTransform()).withFilter(pt -> findAnyNodeAt(pt).isEmpty()).observable();

		final Observable<Point2D> onSiteSelection = explorerPane.getSiteObs().map(SiteNode::getLocation);
		final Observable<Point2D> onNodeSelection = explorerPane.getNodeObs().map(MapNode::getLocation);
		final Observable<Point2D> onEdgeSelection = explorerPane.getEdgeObs().map(MapEdge::getBeginLocation);

		final Observable<Point> result = withPointObs(
				onSiteSelection.mergeWith(onNodeSelection).mergeWith(onEdgeSelection).mergeWith(onMouseClick))
						.transform(() -> getTransform()).toPoint().map(this::computeViewportPosition);
		return result;
	}

	/**
	 * Returns the observable of none selection
	 */
	private Observable<Point2D> createNoneSelectionObs() {
		final Observable<Point2D> result = withMouseObs(routeMap.getMouseObs()).click().withPoint()
				.transform(() -> getInverseTransform())
				.withFilter(pt -> findNodeAt(pt).isEmpty() && findSiteAt(pt).isEmpty() && findEdgeAt(pt).isEmpty())
				.observable();
		return result;
	}

	/**
	 * Returns the observable of viewport location and scale for zoom action
	 * <p>
	 * The scale is changed when the mouse wheel is rolled or when the zoom in zoom
	 * out button are pressed.
	 * </p>
	 */
	private Observable<Tuple2<Point, Double>> createScaleObs() {
		final Observable<Tuple2<Point, Double>> defaultZoomObs = mapViewPane.getZoomDefaultObs().map(ev -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			return new Tuple2<>(pivot, DEFAULT_SCALE);
		});
		final Observable<Tuple2<Point, Double>> zoomInObs = mapViewPane.getZoomInObs().map(ev -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			return new Tuple2<>(pivot, this.scale * SCALE_FACTOR);
		});
		final Observable<Tuple2<Point, Double>> zoomOutObs = mapViewPane.getZoomOutObs().map(ev -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			return new Tuple2<>(pivot, this.scale / SCALE_FACTOR);
		});

		final Observable<Tuple2<Point, Double>> fitInWindowObs = mapViewPane.getFitInWindowObs().map(ev -> {
			final Rectangle2D mapRect = getMapBound();
			final Dimension screenSize = scrollMap.getViewport().getExtentSize();
			final double sx = (screenSize.getWidth() - MAP_INSETS * 2) / mapRect.getWidth();
			final double sy = (screenSize.getHeight() - MAP_INSETS * 2) / mapRect.getHeight();
			final double newScale1 = Math.min(sx, sy);
			final double scaleStep = Math.floor(Math.log(newScale1) / Math.log(SCALE_FACTOR));
			final double newScale = Math.pow(SCALE_FACTOR, scaleStep);
			return new Tuple2<>(new Point(), newScale);
		});
		final Observable<Tuple2<Point, Double>> wheelScaleObs = routeMap.getMouseWheelObs()
				.map(ev -> new Tuple2<>(ev.getPoint(), this.scale * Math.pow(SCALE_FACTOR, -ev.getWheelRotation())));

		final Observable<Tuple2<Point, Double>> result = fitInWindowObs.mergeWith(zoomOutObs).mergeWith(zoomInObs)
				.mergeWith(wheelScaleObs).mergeWith(defaultZoomObs).map(tuple -> {
					final double newScale = tuple.getElem2();
					return new Tuple2<>(computeViewportLocationWithScale(tuple.getElem1(), newScale), newScale);
				}).mergeWith(fitInWindowObs);
		return result;
	}

	/**
	 * Returns the observable of edge selection from mouse click
	 */
	private Observable<Optional<MapEdge>> createSelectionEdgeObs() {
		final Observable<Optional<MapEdge>> result = withMouseObs(routeMap.getMouseObs()).click().withPoint()
				.transform(() -> getInverseTransform()).observable().doOnNext(ev -> logger.debug("Find edge at {}", ev))
				.map(this::findEdgeAt);
		return result;
	}

	/**
	 * Returns the observable of node selection from map
	 */
	private Observable<Optional<MapNode>> createSelectionNodeObs() {
		final Observable<Optional<MapNode>> result = withMouseObs(routeMap.getMouseObs()).click().withPoint()
				.transform(() -> getInverseTransform()).observable().doOnNext(ev -> logger.debug("Find node at {}", ev))
				.map(this::findNodeAt);
		return result;
	}

	/**
	 * Returns the observable of site selection from map
	 */
	private Observable<Optional<SiteNode>> createSelectionSiteObs() {
		final Observable<Optional<SiteNode>> result = withMouseObs(routeMap.getMouseObs()).click().withPoint()
				.transform(() -> getInverseTransform()).observable().doOnNext(ev -> logger.debug("Find site at {}", ev))
				.map(this::findSiteAt);
		return result;
	}

	/**
	 * Returns any node at location
	 *
	 * @param pt the location
	 */
	private Optional<MapNode> findAnyNodeAt(final Point2D pt) {
		final Optional<MapNode> node = findNodeAt(pt).or(() -> findSiteAt(pt));
		return node;
	}

	/**
	 * Returns the edge at location
	 *
	 * @param pt the location
	 */
	private Optional<MapEdge> findEdgeAt(final Point2D pt) {
		final Optional<MapEdge> result = currentStatus.map(SimulationStatus::getMap).flatMap(map -> {
			final Optional<MapEdge> node = map.getEdges().stream().filter(s -> isInRange(s, pt)).findAny();
			return node;
		});
		return result;
	}

	/**
	 * Returns the node at location
	 *
	 * @param pt the location
	 */
	private Optional<MapNode> findNodeAt(final Point2D pt) {
		final Optional<MapNode> result = currentStatus.map(SimulationStatus::getMap).flatMap(map -> {
			final Optional<MapNode> node = map.getNodes().stream().filter(s -> isInRange(s, pt)).findAny();
			return node;
		});
		return result;

	}

	/**
	 * Returns the node at location
	 *
	 * @param pt the location
	 */
	private Optional<SiteNode> findSiteAt(final Point2D pt) {
		final Optional<SiteNode> result = currentStatus.map(SimulationStatus::getMap).flatMap(map -> {
			final Optional<SiteNode> node = map.getSites().stream().filter(s -> isInRange(s, pt)).findAny();
			return node;
		});
		return result;
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
			logger.error(e.getMessage(), e);
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
		final int width = (int) Math.round(bound.getWidth() * scale) + MAP_INSETS * 2;
		final int height = (int) Math.round(bound.getHeight() * scale) + MAP_INSETS * 2;
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
		final AffineTransform result = AffineTransform.getTranslateInstance(MAP_INSETS, MAP_INSETS);
		result.scale(scale, scale);
		result.translate(-mapBound.getMinX(), -mapBound.getMinY());
		return result;
	}

	private Controller handleEdt(final int x) {
		final Optional<SimulationStatus> s = status;
		if (!s.equals(currentStatus)) {
			currentStatus = s;
			currentStatus.ifPresent(status -> {
				final Dimension preferredSize = getScreenMapSize();
				routeMap.setTransform(getTransform()).setStatus(status).setPreferredSize(preferredSize);
//				scrollMap.setHud(computeHud(pointLegendPattern, new Point2D.Double()));
				explorerPane.setMap(status.getMap());
			});
		}
		Observable.just(x + 1).compose(SwingObservable.observeOnEdt()).subscribe(y -> {
			edtObs.onNext(y);
		});
		return this;
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
				.setBorderPainted(scale >= BORDER_SCALE).setPreferredSize(getScreenMapSize());
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
