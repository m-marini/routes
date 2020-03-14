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
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mmarini.routes.model.v2.Constants;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.GeoMapDeserializer;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapModule;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.Simulator;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.TrafficsBuilder;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.swing.v2.UIStatus.MapMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * The main controller of simulation application
 * <p>
 * The controller keeps the instance of ui components and bind the interactions
 * of user with the components
 * </p>
 */
public class Controller implements Constants, ControllerFunctions {
	private static final long SIMULATOR_INTERVAL = 5;
	private static final int FPS = 25;
	public static final double SCALE_FACTOR = Math.pow(10, 1.0 / 6);
	private static final String INITIAL_MAP = "/test.yml"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	/** Returns the default traffics status */
	private static Traffics loadDefault() {
		final URL url = Controller.class.getResource(INITIAL_MAP);
		if (url != null) {
			try {
				final GeoMap map = GeoMapDeserializer.create().parse(url);
				return Traffics.create(map);
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return Traffics.create();
	}

	private final JFileChooser fileChooser;
	private final RouteMap routeMap;
	private final ExplorerPane explorerPane;
	private final MapNodePane nodePane;
	private final EdgePane edgePane;
	private final MapElementPane mapElementPane;
	private final MainFrame mainFrame;
	private final Simulator<Traffics> simulator;
	private final ScrollMap scrollMap;
	private final MapViewPane mapViewPane;
	private final ModuleSelector moduleSelector;
	private final BehaviorSubject<UIStatus> uiStatusSubj;
	private final Observable<UIStatus> uiStatusObs;
	private Random random;

	/** Creates the controller */
	public Controller() {
		this.random = new Random();
		this.simulator = Simulator.<Traffics>create((tr, t) -> {
			return TrafficsBuilder.create(tr, t).build(random);
		}, tr -> {
			return tr.getTime();
		}).setInterval(SIMULATOR_INTERVAL, TimeUnit.MILLISECONDS);
		this.routeMap = new RouteMap();
		this.fileChooser = new JFileChooser();
		this.explorerPane = new ExplorerPane();
		this.edgePane = new EdgePane();
		this.nodePane = new MapNodePane();
		this.mapElementPane = new MapElementPane(nodePane, edgePane);
		this.scrollMap = new ScrollMap(routeMap);
		final List<MapModule> modules = loadModules();
		this.moduleSelector = new ModuleSelector(modules);
		this.mapViewPane = new MapViewPane(scrollMap, moduleSelector.getDropDownButton());
		this.mainFrame = new MainFrame(mapViewPane, explorerPane, mapElementPane);
		final Traffics status1 = loadDefault();
		final UIStatus initStatus = UIStatus.create().setTraffics(status1);
		this.uiStatusSubj = BehaviorSubject.createDefault(initStatus);
		this.uiStatusObs = uiStatusSubj;

		this.fileChooser.setFileFilter(
				new FileNameExtensionFilter(Messages.getString("Controller.filetype.title"), "yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		bindAll();
		if (!moduleSelector.getItems().isEmpty()) {
			mapViewPane.setModuleIcon(moduleSelector.getItems().get(0).getIcon());
			mapViewPane.setModule(modules.get(0));
		}
		mapChanged(initStatus);
		simulator.setEvent(status1);
		startSimulator();
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	/**
	 * Binds all user action to the components
	 *
	 * @return the controller
	 */
	private Controller bindAll() {
		new MouseController(scrollMap, routeMap, mapElementPane, explorerPane, uiStatusSubj, uiStatusObs, this).build();
		new MainFrameController(mainFrame, fileChooser, uiStatusObs, simulator, this).build(random);
		new MapViewPaneController(mapViewPane, scrollMap, routeMap, uiStatusSubj, uiStatusObs, this).build();
		new EdgePaneController(edgePane, routeMap, explorerPane, uiStatusObs, this).build();
		new MapNodePaneController(nodePane, routeMap, explorerPane, uiStatusObs, this).build();
		new ExplorerPaneController(explorerPane, routeMap, mapElementPane, uiStatusSubj, uiStatusObs, this).build();
		new KeyController(routeMap, uiStatusObs, this).build();
		return bindOnStatus().bindOnMouseWheel().bindOnModuleSelector();
	}

	/**
	 * Binds the user actions from module selector
	 *
	 * @return the controller
	 */
	private Controller bindOnModuleSelector() {
		moduleSelector.getModuleObs().withLatestFrom(uiStatusObs, (t, s) -> {
			return Tuple.of(s, t.get1(), t.get2());
		}).subscribe(t -> {
			request(tr -> {
				final UIStatus st = t.get1();
				final JMenuItem menuItem = (JMenuItem) t.get2().getSource();
				final MapModule module = t.get3();
				logger.debug("bindOnModuleSelector module={}", module);
				mapViewPane.setModuleIcon(menuItem.getIcon()).setModule(module).selectModuleMode();
				final UIStatus newSt = st.setMode(MapMode.DRAG_MODULE);
				return newSt;
			});
		}, this::showError);
		return this;
	}

	/**
	 * Binds the user actions from mousew heel
	 *
	 * @return the controller
	 */
	private Controller bindOnMouseWheel() {
		routeMap.getMouseWheelObs().withLatestFrom(uiStatusObs, (ev, status) -> {
			return Tuple.of(status, ev);
		}).subscribe(t -> {
			final UIStatus st = t.get1();
			final MouseWheelEvent ev = t.get2();
			final double newScale = st.getScale() * Math.pow(SCALE_FACTOR, -ev.getWheelRotation());
			logger.debug("onNext scale at {}", newScale); //$NON-NLS-1$
			final Point pivot = ev.getPoint();
			final UIStatus newStatus = scaleTo(st, newScale, pivot);
			updateHud(newStatus, newStatus.toMapPoint(pivot));
			uiStatusSubj.onNext(newStatus);
		}, this::showError);
		return this;
	}

	/**
	 * Binds the events from repainting clock
	 *
	 * @return the controller
	 */
	private Controller bindOnStatus() {
		Observable.interval(1000 / FPS, TimeUnit.MILLISECONDS)
				// Add last simulator status
				.withLatestFrom(simulator.getEvents().toObservable(), (t, status) -> status)
				// Add last ui status
				.withLatestFrom(uiStatusObs, (simStat, uiStat) -> Tuple.of(simStat, uiStat))
				// discard no change events
				.filter(t -> !t.get2().getTraffics().equals(t.get1()))
				// Update ui status, refresh panels and send new event
				.compose(SwingObservable.observeOnEdt()).subscribe(t -> {
					final UIStatus uiStatus = t.get2().setTraffics(t.get1());
					trafficChanged(uiStatus);
					uiStatusSubj.onNext(uiStatus);
				}, this::showError);
		return this;
	}

	@Override
	public Controller centerMapTo(final UIStatus status, final Point2D center) {
		logger.debug("centerMapTo {} ", center); //$NON-NLS-1$
		final Point2D pt = status.toScreenPoint(center);
		final Point vp = computeViewportPosition(pt);
		scrollMap.getViewport().setViewPosition(vp);
		return this;
	}

	/**
	 * Returns the viewport position for a center point
	 *
	 * @param center the graph center point
	 */
	private Point computeViewportPosition(final Point2D center) {
		final Dimension size = scrollMap.getViewport().getExtentSize();
		final double x = Math.max(0, center.getX() - size.width / 2);
		final double y = Math.max(0, center.getY() - size.height / 2);
		final Point result = new Point((int) Math.round(x), (int) Math.round(y));
		return result;
	}

	@Override
	public UIStatus deleteEdge(final UIStatus uiStatus, final MapEdge edge) {
		logger.debug("deleteEdge {} ", edge); //$NON-NLS-1$
		final Traffics nextSt = uiStatus.getTraffics().removeEdge(edge);
		return uiStatus.setTraffics(nextSt);
	}

	@Override
	public UIStatus deleteNode(final UIStatus uiStatus, final MapNode node) {
		logger.debug("deleteNode {} ", node); //$NON-NLS-1$
		final Traffics nextSt = uiStatus.getTraffics().removeNode(node);
		return uiStatus.setTraffics(nextSt);
	}

	/** Returns the explorerPane */
	public ExplorerPane getExplorerPane() {
		return explorerPane;
	}

	/** Returns the mapElementPane */
	public MapElementPane getMapElementPane() {
		return mapElementPane;
	}

	/** Returns the routeMap */
	public RouteMap getRouteMap() {
		return routeMap;
	}

	/** Returns the list of modules by loading a file */
	private List<MapModule> loadModules() {
		final File path = new File("modules");
		if (path.isDirectory()) {
			final List<MapModule> modules = Arrays.stream(path.listFiles()).filter(file -> {
				return file.isFile() && file.canRead() && file.getName().endsWith(".yml");
			}).sorted((a, b) -> {
				return a.getName().compareTo(b.getName());
			}).map(file -> {
				GeoMap map;
				try {
					logger.debug("Loading {} ...", file);
					map = GeoMapDeserializer.create().parse(file);
					final MapModule module = MapModule.create(map);
					return module;
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
					return MapModule.create();
				}
			}).filter(m -> {
				return !m.getEdges().isEmpty();
			}).collect(Collectors.toList());
			return modules;
		} else {
			return List.of();
		}
	}

	@Override
	public Controller mapChanged(final UIStatus uiStatus) {
		routeMap.setTraffics(uiStatus.getTraffics()).setGridSize(uiStatus.getGridSize()).clearSelection()
				.setTransform(uiStatus.getTransform()).setPreferredSize(uiStatus.getScreenMapSize());
		scrollMap.repaint();
		explorerPane.setMap(uiStatus.getTraffics().getMap());
		mapElementPane.clearSelection();
		return this;
	}

	@Override
	public Controller request(final Function<Traffics, UIStatus> changeStatus) {
		simulator.request(traffics -> {
			final UIStatus status = changeStatus.apply(traffics);
			uiStatusSubj.onNext(status);
			return status.getTraffics();
		});
		return this;
	}

	@Override
	public UIStatus scaleTo(final UIStatus status, final double scale, final Point pivot) {
		final UIStatus newStatus = status.setScale(scale);
		final Point vp = scrollMap.getViewport().getViewPosition();
		final Point newVp = status.computeViewporPositionWithScale(vp, pivot, scale);
		scrollMap.getViewport().setViewPosition(newVp);
		routeMap.setTransform(newStatus.getTransform()).setGridSize(newStatus.getGridSize())
				.setPreferredSize(newStatus.getScreenMapSize());
		scrollMap.repaint();
		updateHud(newStatus, newStatus.toMapPoint(pivot));
		return newStatus;
	}

	@Override
	public Controller showError(final String pattern, final Object... arguments) {
		JOptionPane.showMessageDialog(mainFrame, MessageFormat.format(pattern, arguments),
				Messages.getString("Controller.error.title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		return this;
	}

	@Override
	public Controller showError(final Throwable e) {
		logger.error(e.getMessage(), e);
		return showError("{0}", new Object[] { e.getMessage(), e.getMessage() }); //$NON-NLS-1$
	}

	/** Returns the controller with the simulator started */
	private Controller startSimulator() {
		if (!mainFrame.isStopped()) {
			simulator.start();
		}
		return this;
	}

	/**
	 * Upgrades the component to repaint the new traffic
	 *
	 * @param uiStatus the ui status
	 * @return the controller
	 */
	private Controller trafficChanged(final UIStatus uiStatus) {
		routeMap.setTraffics(uiStatus.getTraffics());// .requestFocus();
		scrollMap.repaint();
		return this;
	}

	@Override
	public Controller updateHud(final UIStatus status, final Point2D point) {
		switch (status.getMode()) {
		case START_EDGE:
		case DRAG_EDGE:
			scrollMap.setEdgeHud(status.getGridSize(), point, status.getDragEdge(), status.getSpeedLimit());
			break;
		default:
			scrollMap.setPointHud(status.getGridSize(), point);
		}
		return this;
	}

}
