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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mmarini.routes.model.Constants;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.GeoMapDeserializer;
import org.mmarini.routes.model.v2.GeoMapSerializer;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.MapProfile;
import org.mmarini.routes.model.v2.Simulator;
import org.mmarini.routes.model.v2.Traffics;
import org.mmarini.routes.model.v2.Tuple2;
import org.mmarini.routes.swing.v2.UIStatus.MapMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 *
 */
public class Controller implements Constants {
	private static final int FPS = 25;
	private static final double SCALE_FACTOR = Math.pow(10, 1.0 / 6);
	private static final String INITIAL_MAP = "/test.yml"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	/**
	 * Returns the default status
	 */
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
	private final OptimizePane optimizePane;
	private final RoutePane routesPane;
//	private final List<String> gridLegendPattern;
	private final List<String> pointLegendPattern;
	private final List<String> edgeLegendPattern;
	private final BehaviorSubject<UIStatus> uiStatusSubj;
	private final Observable<UIStatus> uiStatusObs;
	private final FrequencePane frequencePane;

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
		this.optimizePane = new OptimizePane();
		this.frequencePane = new FrequencePane();
		this.routesPane = new RoutePane();
//		this.gridLegendPattern = SwingUtils.loadPatterns("ScrollMap.gridLegendPattern");
		this.pointLegendPattern = SwingUtils.loadPatterns("ScrollMap.pointLegendPattern"); //$NON-NLS-1$
		this.edgeLegendPattern = SwingUtils.loadPatterns("ScrollMap.edgeLegendPattern"); //$NON-NLS-1$
		final Traffics status1 = loadDefault();
		final UIStatus initStatus = UIStatus.create().setTraffics(status1);
		this.uiStatusSubj = BehaviorSubject.createDefault(initStatus);
		this.uiStatusObs = uiStatusSubj;

		this.fileChooser.setFileFilter(
				new FileNameExtensionFilter(Messages.getString("Controller.filetype.title"), "yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		bindAll();

		mapChanged(initStatus);
		simulator.setTraffics(status1);
		startSimulator();
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	/**
	 * Returns the single status with new edge
	 *
	 * @param st         the initial status
	 * @param startPoint start point of edge
	 * @param endPoint   end point of edge
	 */
	private Single<UIStatus> addEdge(final UIStatus st, final Point2D startPoint, final Point2D endPoint) {
		logger.debug("addEdge {}, {}", startPoint, endPoint); //$NON-NLS-1$
		return simulator.stop().map(st1 -> {
			return st.createEdge(startPoint, endPoint);
		});
	}

	/**
	 *
	 */
	private Controller bindAll() {
		return bindOnStatus().bindOnMouse().bindOnMouseWheel().bindOnExplorerPane().bindOnMapViewPane().bindOnEdgePane()
				.bindOnNodePane().bindOnKey().bindOnMainframe();
	}

	/**
	 *
	 * @param withPointObs
	 * @return
	 */
	private Controller bindOnDragEdge(final Observable<Tuple2<UIStatus, Tuple2<Point2D, MouseEvent>>> withPointObs) {
		withPointObs.filter(t -> {
			final UIStatus st = t.getElem1();
			return st.getMode().equals(MapMode.DRAG_EDGE);
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final MouseEvent ev = t.getElem2().getElem2();
			final Point2D endPoint = st.snapToNode(t.getElem2().getElem1());
			final Point2D startPoint = st.getDragEdge().get().getElem1();
			switch (ev.getID()) {
			case MouseEvent.MOUSE_ENTERED:
			case MouseEvent.MOUSE_EXITED:
			case MouseEvent.MOUSE_DRAGGED:
			case MouseEvent.MOUSE_MOVED: {
				final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(startPoint, endPoint));
				routeMap.setDragEdge(dragEdge);
				uiStatusSubj.onNext(st.setDragEdge(dragEdge));
			}
				break;
			case MouseEvent.MOUSE_PRESSED: {
				logger.debug("bindOnDragEdge addEdge status {} {}", st, st.getTraffics().getTraffics().size()); //$NON-NLS-1$
				addEdge(st, startPoint, endPoint).subscribe(status -> {
					logger.debug("bindOnDragEdge addEdge {}", st); //$NON-NLS-1$
					final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(endPoint, endPoint));
					final UIStatus newStatus = status.setDragEdge(dragEdge);
					logger.debug("bindOnDragEdge addEdge new status {} {}", newStatus, //$NON-NLS-1$
							newStatus.getTraffics().getTraffics().size());
					mapChanged(newStatus);
					uiStatusSubj.onNext(newStatus);
					simulator.setTraffics(newStatus.getTraffics());
					startSimulator();
				}, this::showError);
			}
				break;
			}
		}, this::showError);
		return this;
	}

	/**
	 *
	 * @return
	 */
	private Controller bindOnEdgePane() {
		edgePane.getDeleteObs().withLatestFrom(uiStatusObs, (edge, st) -> {
			return new Tuple2<>(st, edge);
		}).flatMap(t -> {
			return deleteEdge(t.getElem1(), t.getElem2()).toObservable();
		}).subscribe(st -> {
			mapChanged(st);
			explorerPane.clearSelection();
			routeMap.clearSelection();
			uiStatusSubj.onNext(st);
			simulator.setTraffics(st.getTraffics());
			startSimulator();
		}, this::showError);

		edgePane.getPriorityObs().withLatestFrom(uiStatusObs, (p, st) -> {
			// add last ui status
			return new Tuple2<>(st, p);
		}).filter(t -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getPriority() != t.getElem2();
			}).orElse(false);
		}).flatMap(t -> {
			// change priority
			final UIStatus status = t.getElem1();
			final MapEdge edge = edgePane.getEdge().get();
			final int priority = t.getElem2();
			return changePriority(status, edge, priority).toObservable();
		}).subscribe(t -> {
			// update
			final UIStatus st = t.getElem1();
			final MapEdge edge = t.getElem2();
			mapChanged(st);
			routeMap.setSelectedEdge(Optional.of(edge)).repaint();
			explorerPane.setSelectedEdge(edge);
			uiStatusSubj.onNext(st);
			simulator.setTraffics(st.getTraffics());
			startSimulator();
		}, this::showError);

		edgePane.getSpeedLimitObs().withLatestFrom(uiStatusObs, (speed, st) -> {
			// add last ui status
			return new Tuple2<>(st, speed);
		}).filter(t -> {
			// filter changes
			return edgePane.getEdge().map(ed -> {
				return ed.getSpeedLimit() != t.getElem2();
			}).orElse(false);
		}).flatMap(t -> {
			// change priority
			final UIStatus status = t.getElem1();
			final MapEdge edge = edgePane.getEdge().get();
			final double speedLimit = t.getElem2() * KMH_TO_MPS;
			return changeSpeedLimit(status, edge, speedLimit).toObservable();
		}).subscribe(t -> {
			// update
			final UIStatus st = t.getElem1();
			final MapEdge edge = t.getElem2();
			mapChanged(st);
			routeMap.setSelectedEdge(Optional.of(edge)).repaint();
			explorerPane.setSelectedEdge(edge);
			uiStatusSubj.onNext(st);
			simulator.setTraffics(st.getTraffics());
			startSimulator();
		}, this::showError);
		return this;
	}

	/**
	 *
	 * @return
	 */
	private Controller bindOnExplorerPane() {
		explorerPane.getSiteObs().withLatestFrom(uiStatusObs, (site, st) -> {
			return new Tuple2<>(st, site);
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final MapNode site = t.getElem2();
			final UIStatus newStatus = st.setSelectedElement(MapElement.create(site));
			mapElementPane.setNode(site);
			routeMap.setSelectedSite(Optional.of(site));
			centerMapTo(newStatus, site.getLocation());
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		explorerPane.getNodeObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return new Tuple2<>(st, node);
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final MapNode node = t.getElem2();
			final UIStatus newStatus = st.setSelectedElement(MapElement.create(node));
			mapElementPane.setNode(node);
			routeMap.setSelectedNode(Optional.of(node));
			centerMapTo(newStatus, node.getLocation());
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		explorerPane.getEdgeObs().withLatestFrom(uiStatusObs, (edge, st) -> {
			return new Tuple2<>(st, edge);
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final MapEdge edge = t.getElem2();
			final UIStatus newStatus = st.setSelectedElement(MapElement.create(edge));
			mapElementPane.setEdge(edge);
			routeMap.setSelectedEdge(Optional.of(edge));
			centerMapTo(newStatus, edge.getBeginLocation());
			uiStatusSubj.onNext(newStatus);
		}, this::showError);
		return this;
	}

	/**
	 * Returns the controller with bind for map change
	 */
	private Controller bindOnKey() {
		// observable of delete keys
		routeMap.getKeyboardObs()
				// Filter for delete keys pressed
				.filter(ev -> {
					return ev.getID() == KeyEvent.KEY_PRESSED
							&& (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE || ev.getKeyCode() == KeyEvent.VK_DELETE);
				})
				// Combine with ui status
				.withLatestFrom(uiStatusObs, (ev, st) -> st)
				// Flat map with delete process
				.flatMap(st -> {
					final Optional<MapNode> mapNode = routeMap.getSelectedSite().map(site -> site)
							.or(() -> routeMap.getSelectedNode());
					final Optional<Observable<UIStatus>> deleteNodeProcess = mapNode
							.map(node -> deleteNode(st, node).toObservable());
					final Optional<Observable<UIStatus>> deleteProcess = deleteNodeProcess
							.or(() -> routeMap.getSelectedEdge().map(edge -> deleteEdge(st, edge).toObservable()));
					final Observable<UIStatus> result = deleteProcess.orElseGet(() -> Observable.just(st));
					return result;
				}).subscribe(st -> {
					mapChanged(st);
					uiStatusSubj.onNext(st);
					simulator.setTraffics(st.getTraffics());
					startSimulator();
				}, this::showError);

		return this;
	}

	/**
	 *
	 * @return
	 */
	private Controller bindOnMainframe() {
		mainFrame.getOpenMapObs().withLatestFrom(uiStatusObs, (ev, st) -> st)
				.flatMap(uiStatus -> loadProcess(uiStatus).toObservable())
				//
				.subscribe(t -> {
					t.ifPresent(tup -> {
						mainFrame.setSaveActionEnabled(true);
						mainFrame.setTitle(tup.getElem2().getName());
						final UIStatus uiStatus = tup.getElem1();
						mapChanged(uiStatus);
						uiStatusSubj.onNext(uiStatus);
						simulator.setTraffics(uiStatus.getTraffics());
					});
					startSimulator();
				}, this::showError);

		mainFrame.getSaveMapAsObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			simulator.stop().subscribe(s -> {
				final int choice = fileChooser.showSaveDialog(mainFrame);
				if (choice == JFileChooser.APPROVE_OPTION) {
					handleSaveMap(st);
				}
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getSaveMapObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			simulator.stop().subscribe(s -> {
				handleSaveMap(st);
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getNewMapObs().subscribe(ev -> {
			simulator.stop().subscribe(st -> {
				final UIStatus status = UIStatus.create();
				mapChanged(status);
				mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
				uiStatusSubj.onNext(status);
				simulator.setTraffics(status.getTraffics());
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getNewRandomObs().subscribe(ev -> {
			simulator.stop().subscribe(st -> {
				mapProfilePane.setDifficultyOnly(false);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
						Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final MapProfile profile = mapProfilePane.getProfile();
					logger.info("Selected {}", profile); //$NON-NLS-1$
					final Traffics status = Traffics.random(profile, new Random());
					final UIStatus uiStatus = UIStatus.create().setTraffics(status);
					simulator.setTraffics(status);
					mapChanged(uiStatus);
					mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
					uiStatusSubj.onNext(uiStatus);
				}
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getExitObs().subscribe(ev -> {
			mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
		}, this::showError);

		mainFrame.getOptimizeObs().withLatestFrom(uiStatusObs, (ev, status) -> status).subscribe(status -> {
			simulator.stop().subscribe(st -> {
				optimizePane.setSpeedLimit(status.getSpeedLimit());
				final int opt = JOptionPane.showConfirmDialog(mainFrame, optimizePane,
						Messages.getString("Controller.optimizerPane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final double speedLimit = optimizePane.getSpeedLimit();
					if (optimizePane.isOptimizeSpeed()) {
						final UIStatus newStatus = status.setSpeedLimit(speedLimit).optimizeSpeed();
						simulator.setTraffics(newStatus.getTraffics());
						mapChanged(newStatus);
						uiStatusSubj.onNext(newStatus);
					}
				}
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getFrequenceObs().withLatestFrom(uiStatusObs, (ev, status) -> status).subscribe(status -> {
			simulator.stop().subscribe(st -> {
				optimizePane.setSpeedLimit(status.getSpeedLimit());
				final double frequence = status.getTraffics().getMap().getFrequence();
				frequencePane.setFrequence(frequence);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, frequencePane,
						Messages.getString("Controller.frequencePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final UIStatus newStatus = status.setFrequence(frequencePane.getFrequence());
					simulator.setTraffics(newStatus.getTraffics());
					mapChanged(newStatus);
					uiStatusSubj.onNext(newStatus);
				}
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getSpeedObs().subscribe(speed -> {
			simulator.stop().subscribe(st -> {
				simulator.setSimulationSpeed(speed);
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getStopObs().subscribe(ev -> {
			if (mainFrame.isStopped()) {
				simulator.stop();
			} else {
				simulator.start();
			}
		}, this::showError);

		mainFrame.getRoutesObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			simulator.stop().subscribe(tx -> {
				routesPane.setWeights(st.getTraffics().getMap().getWeights());
				final int opt = JOptionPane.showConfirmDialog(mainFrame, routesPane,
						Messages.getString("Controller.routePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final UIStatus newStatus = st.setWeights(routesPane.getWeights());
					simulator.setTraffics(newStatus.getTraffics());
					uiStatusSubj.onNext(newStatus);
				}
				startSimulator();
			}, this::showError);
		}, this::showError);

		mainFrame.getRandomizeObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			simulator.stop().subscribe(tx -> {
				mapProfilePane.setDifficultyOnly(true);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
						Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final MapProfile profile = mapProfilePane.getProfile();
					final UIStatus newStatus = st.randomize(profile.getMinWeight())
							.setFrequence(profile.getFrequence());
					simulator.setTraffics(newStatus.getTraffics());
					uiStatusSubj.onNext(newStatus);
				}
				startSimulator();
			}, this::showError);
		}, this::showError);

		return this;
	}

	/**
	 * @return
	 */
	private Controller bindOnMapViewPane() {
		mapViewPane.getEdgeModeObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			routeMap.setDragEdge(Optional.empty()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			final UIStatus newStatus = st.setMode(MapMode.START_EDGE).setDragEdge(Optional.empty());
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		mapViewPane.getSelectModeObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			routeMap.setDragEdge(Optional.empty()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			final UIStatus newStatus = st.setMode(MapMode.SELECTION).setDragEdge(Optional.empty());
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		mapViewPane.getZoomDefaultObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			final UIStatus newStatus = scaleTo(st, UIStatus.DEFAULT_SCALE, pivot);
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		mapViewPane.getZoomInObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			final UIStatus newStatus = scaleTo(st, st.getScale() * SCALE_FACTOR, pivot);
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		mapViewPane.getZoomOutObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			final Rectangle rect = scrollMap.getViewport().getViewRect();
			final Point pivot = toPoint(new Point2D.Double(rect.getCenterX(), rect.getCenterY()));
			final UIStatus newStatus = scaleTo(st, st.getScale() / SCALE_FACTOR, pivot);
			uiStatusSubj.onNext(newStatus);
		}, this::showError);

		mapViewPane.getFitInWindowObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			final Rectangle2D mapRect = st.getMapBound();
			final Dimension screenSize = scrollMap.getViewport().getExtentSize();
			final double sx = (screenSize.getWidth() - UIStatus.MAP_INSETS * 2) / mapRect.getWidth();
			final double sy = (screenSize.getHeight() - UIStatus.MAP_INSETS * 2) / mapRect.getHeight();
			final double newScale1 = Math.min(sx, sy);
			final double scaleStep = Math.floor(Math.log(newScale1) / Math.log(SCALE_FACTOR));
			final double newScale = Math.pow(SCALE_FACTOR, scaleStep);
			final UIStatus newStatus = scaleTo(st, newScale, new Point());
			uiStatusSubj.onNext(newStatus);
		}, this::showError);
		return this;
	}

	/**
	 *
	 * @return
	 */
	private Controller bindOnMouse() {
		final PublishSubject<Tuple2<UIStatus, Tuple2<Point2D, MouseEvent>>> withPointObs1 = PublishSubject.create();
		routeMap.getMouseObs().withLatestFrom(uiStatusObs, (ev, status) -> {
			final Point2D pt = status.toMapPoint(ev.getPoint());
			return new Tuple2<>(status, new Tuple2<>(pt, ev));
		}).subscribe(withPointObs1);
		final Observable<Tuple2<UIStatus, Tuple2<Point2D, MouseEvent>>> withPointObs = withPointObs1;
		return bindOnMouseForHud(withPointObs).bindOnMouseSelection(withPointObs).bindOnStartEdge(withPointObs)
				.bindOnDragEdge(withPointObs);
	}

	/**
	 * @param withPointObs
	 */
	private Controller bindOnMouseForHud(final Observable<Tuple2<UIStatus, Tuple2<Point2D, MouseEvent>>> withPointObs) {
		withPointObs.subscribe(t -> {
			updateHud(t.getElem1(), t.getElem2().getElem1());
		}, this::showError);
		return this;
	}

	/**
	 * @param withPointObs
	 * @return
	 */
	private Controller bindOnMouseSelection(
			final Observable<Tuple2<UIStatus, Tuple2<Point2D, MouseEvent>>> withPointObs) {
		withPointObs.filter(t -> {
			final UIStatus st = t.getElem1();
			final MouseEvent ev = t.getElem2().getElem2();
			return st.getMode().equals(MapMode.SELECTION) && ev.getID() == MouseEvent.MOUSE_PRESSED;
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final Point2D point = t.getElem2().getElem1();
			final MapElement elem = st.findElementAt(point);
			elem.getNode().ifPresent(explorerPane::setSelectedNode);
			elem.getEdge().ifPresent(explorerPane::setSelectedEdge);
			if (elem.isEmpty()) {
				explorerPane.clearSelection();
				centerMapTo(st, t.getElem2().getElem1());
			}
			if (!st.getSelectedElement().equals(elem)) {
				uiStatusSubj.onNext(st.setSelectedElement(elem));
			}
		}, this::showError);
		return this;
	}

	/**
	 *
	 * @return
	 */
	private Controller bindOnMouseWheel() {
		routeMap.getMouseWheelObs().withLatestFrom(uiStatusObs, (ev, status) -> {
			return new Tuple2<>(status, ev);
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final MouseWheelEvent ev = t.getElem2();
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
	 * @return
	 */
	private Controller bindOnNodePane() {
		// Change node type
		nodePane.getChangeObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return new Tuple2<>(st, node);
		}).flatMap(t -> {
			return changeNode(t).toObservable();
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final MapNode node = t.getElem2();
			mapChanged(st);
			final GeoMap map = st.getTraffics().getMap();
			routeMap.setSelectedSite(map.getSite(node));
			routeMap.setSelectedNode(map.getNode(node));
			explorerPane.setSelectedNode(node);
			uiStatusSubj.onNext(st);
			simulator.setTraffics(st.getTraffics());
			startSimulator();
		}, this::showError);

		// delete node type
		nodePane.getDeleteObs().withLatestFrom(uiStatusObs, (node, st) -> {
			return new Tuple2<>(st, node);
		}).flatMap(t -> {
			return deleteNode(t.getElem1(), t.getElem2()).toObservable();
		}).subscribe(st -> {
			mapChanged(st);
			explorerPane.clearSelection();
			routeMap.clearSelection();
			uiStatusSubj.onNext(st);
			simulator.setTraffics(st.getTraffics());
			startSimulator();
		}, this::showError);
		return this;
	}

	/**
	 *
	 * @param withPointObs
	 * @return
	 */
	private Controller bindOnStartEdge(final Observable<Tuple2<UIStatus, Tuple2<Point2D, MouseEvent>>> withPointObs) {
		withPointObs.filter(t -> {
			final UIStatus st = t.getElem1();
			final MouseEvent ev = t.getElem2().getElem2();
			return st.getMode().equals(MapMode.START_EDGE) && ev.getID() == MouseEvent.MOUSE_PRESSED;
		}).subscribe(t -> {
			final UIStatus st = t.getElem1();
			final Point2D startPoint = st.snapToNode(t.getElem2().getElem1());
			final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(startPoint, startPoint));

			routeMap.setSelectedEdge(Optional.empty()).setSelectedNode(Optional.empty())
					.setSelectedSite(Optional.empty()).setDragEdge(dragEdge);
			scrollMap.repaint();
			uiStatusSubj.onNext(st.setDragEdge(dragEdge).setMode(MapMode.DRAG_EDGE));
		}, this::showError);
		return this;
	}

	/**
	 * Returns the controller with refresh status event binding
	 */
	private Controller bindOnStatus() {
		Observable.interval(1000 / FPS, TimeUnit.MILLISECONDS)
				// Add last simulator status
				.withLatestFrom(simulator.getOutput(), (t, status) -> status)
				// Add last ui status
				.withLatestFrom(uiStatusObs, (simStat, uiStat) -> new Tuple2<>(simStat, uiStat))
				// discard no change events
				.filter(t -> !t.getElem2().getTraffics().equals(t.getElem1()))
				// Update ui status, refresh panels and send new event
				.compose(SwingObservable.observeOnEdt()).subscribe(t -> {
					final UIStatus uiStatus = t.getElem2().setTraffics(t.getElem1());
					trafficChanged(uiStatus);
					uiStatusSubj.onNext(uiStatus);
				}, this::showError);
		return this;
	}

	/**
	 * Returns the controller with centered map
	 *
	 * @param status the ui status
	 * @param center the center
	 */
	private Controller centerMapTo(final UIStatus status, final Point2D center) {
		logger.debug("centerMapTo {} ", center); //$NON-NLS-1$
		final Point2D pt = status.toScreenPoint(center);
		final Point vp = computeViewportPosition(pt);
		scrollMap.getViewport().setViewPosition(vp);
		return this;
	}

	/**
	 *
	 * @param tuple
	 * @return
	 */
	private Single<Tuple2<UIStatus, MapNode>> changeNode(final Tuple2<UIStatus, MapNode> tuple) {
		final UIStatus uiStatus = tuple.getElem1();
		final MapNode node = tuple.getElem2();
		return simulator.stop().map(status -> {
			logger.debug("changeNode {} ", node); //$NON-NLS-1$
			final Traffics nextSt = uiStatus.getTraffics().changeNode(node, (a, b) -> 1);
			final UIStatus nextUiStatus = uiStatus.setTraffics(nextSt).setSelectedElement(MapElement.empty());
			return new Tuple2<>(nextUiStatus, node);
		});
	}

	/**
	 *
	 * @param uiStatus
	 * @param edge
	 * @param priority
	 * @return
	 */
	private Single<Tuple2<UIStatus, MapEdge>> changePriority(final UIStatus uiStatus, final MapEdge edge,
			final int priority) {
		return simulator.stop().map(s -> {
			logger.debug("changePriority {} {}", edge.getShortName(), priority); //$NON-NLS-1$
			final MapEdge newEdge = edge.setPriority(priority);
			final UIStatus newStatus = uiStatus.setTraffics(uiStatus.getTraffics().change(newEdge));
			return new Tuple2<>(newStatus, newEdge);
		});
	}

	/**
	 *
	 * @param uiStatus
	 * @param edge
	 * @param speed
	 * @return
	 */
	private Single<Tuple2<UIStatus, MapEdge>> changeSpeedLimit(final UIStatus uiStatus, final MapEdge edge,
			final double speed) {
		return simulator.stop().map(s -> {
			logger.debug("changeSpeedLimit {} {}", edge.getShortName(), speed); //$NON-NLS-1$
			final MapEdge newEdge = edge.setSpeedLimit(speed);
			final UIStatus newStatus = uiStatus.setTraffics(uiStatus.getTraffics().change(newEdge));
			return new Tuple2<>(newStatus, newEdge);
		});
	}

	/**
	 * Returns the head up display text
	 *
	 * @param patterns
	 * @param gridSize
	 * @param point
	 * @param dragEdge
	 * @param maxSpeed
	 */
	private List<String> computeHud(final List<String> patterns, final double gridSize, final Point2D point,
			final Optional<Tuple2<Point2D, Point2D>> dragEdge, final double maxSpeed) {
		final Optional<Double> lengthOpt = dragEdge.map(t -> t.getElem1().distance(t.getElem2()));
		final Double length = dragEdge.map(t -> t.getElem1().distance(t.getElem2())).orElse(null);
		final Double speed = lengthOpt.map(l -> {
			return Math.min(MapEdge.computeSpeedLimit(l), maxSpeed) * MPS_TO_KMH;
		}).orElse(null);
		final List<String> texts = patterns.stream()
				.map(pattern -> String.format(pattern, gridSize, point.getX(), point.getY(), length, speed))
				.collect(Collectors.toList());
		return texts;
	}

	/**
	 * Returns the head up display text
	 *
	 * @param status
	 * @param point
	 * @return
	 */
	private List<String> computeHud(final UIStatus status, final Point2D point) {
		switch (status.getMode()) {
		case START_EDGE:
		case DRAG_EDGE:
			return computeHud(edgeLegendPattern, status.getGridSize(), point, status.getDragEdge(),
					status.getSpeedLimit());
		default:
			return computeHud(pointLegendPattern, status.getGridSize(), point, status.getDragEdge(),
					status.getSpeedLimit());
		}
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

	/**
	 * @param uiStatus
	 * @param edge
	 * @return
	 */
	private Single<UIStatus> deleteEdge(final UIStatus uiStatus, final MapEdge edge) {
		return simulator.stop().map(status -> {
			logger.debug("deleteEdge {} ", edge); //$NON-NLS-1$
			final Traffics nextSt = uiStatus.getTraffics().removeEdge(edge);
			return uiStatus.setTraffics(nextSt).setSelectedElement(MapElement.empty());
		});
	}

	/**
	 * @param uiStatus
	 * @param edge
	 * @return
	 */
	private Single<UIStatus> deleteNode(final UIStatus uiStatus, final MapNode node) {
		return simulator.stop().map(status -> {
			logger.debug("deleteNode {} ", node); //$NON-NLS-1$
			final Traffics nextSt = uiStatus.getTraffics().removeNode(node);
			return uiStatus.setTraffics(nextSt).setSelectedElement(MapElement.empty());
		});
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
	 * Returns the controller after status saved
	 *
	 * @param status the status
	 */
	private Controller handleSaveMap(final UIStatus status) {
		final File file = fileChooser.getSelectedFile();
		if (file.exists() && !file.canWrite()) {
			showError(Messages.getString("Controller.writeError.message"), new Object[] { file }); //$NON-NLS-1$
		} else {
			try {
				logger.info("Saving {} ...", file); //$NON-NLS-1$
				new GeoMapSerializer(status.getTraffics().getMap()).writeFile(file);
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
	 *
	 * @param status
	 * @return
	 */
	private Single<Optional<Tuple2<UIStatus, File>>> loadProcess(final UIStatus status) {
		return simulator.stop().map(s -> {
			final int choice = fileChooser.showOpenDialog(mainFrame);
			if (choice == JFileChooser.APPROVE_OPTION) {
				final File file = fileChooser.getSelectedFile();
				if (!file.canRead()) {
					showError(MessageFormat.format(Messages.getString("Controller.readError.message"), file)); //$NON-NLS-1$
					return Optional.empty();
				} else {
					try {
						logger.debug("loadProcess {}", file); //$NON-NLS-1$
						final GeoMap map = GeoMapDeserializer.create().parse(file);
						final Traffics newStatus = Traffics.create(map);
						return Optional.of(new Tuple2<>(status.setTraffics(newStatus), file));
					} catch (final Throwable ex) {
						showError(ex);
						return Optional.empty();
					}
				}
			} else {
				return Optional.empty();
			}
		});
	}

	/**
	 *
	 * @param uiStatus
	 * @return
	 */
	private Controller mapChanged(final UIStatus uiStatus) {
		routeMap.setStatus(uiStatus.getTraffics()).setGridSize(uiStatus.getGridSize()).clearSelection()
				.setTransform(uiStatus.getTransform()).setPreferredSize(uiStatus.getScreenMapSize());
		scrollMap.repaint();
		explorerPane.setMap(uiStatus.getTraffics().getMap());
		mapElementPane.clearSelection();
		return this;
	}

	/**
	 *
	 * @param status
	 * @param scale
	 * @param pivot
	 * @return
	 */
	private UIStatus scaleTo(final UIStatus status, final double scale, final Point pivot) {
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

	/**
	 * Returns the controller with error message from pattern
	 *
	 * @param pattern   the pattern
	 * @param arguments the argument
	 */
	private Controller showError(final String pattern, final Object... arguments) {
		JOptionPane.showMessageDialog(mainFrame, MessageFormat.format(pattern, arguments),
				Messages.getString("Controller.error.title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		return this;
	}

	/**
	 * Returns the controller with error message from exception
	 *
	 * @param e the exception
	 */
	private Controller showError(final Throwable e) {
		logger.error(e.getMessage(), e);
		return showError("{0}", new Object[] { e.getMessage(), e.getMessage() }); //$NON-NLS-1$
	}

	/**
	 *
	 * @return
	 */
	private Controller startSimulator() {
		if (!mainFrame.isStopped()) {
			simulator.start();
		}
		return this;
	}

	/**
	 * @return
	 */
	Traffics testMap() {
		final MapNode s0 = MapNode.create(15, 15);
		final MapNode s1 = MapNode.create(1000, 1000);
		final Set<MapNode> sites = Set.of(s0, s1);
		final Set<MapEdge> edges = Set.of(MapEdge.create(s0, s1), MapEdge.create(s1, s0));
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(sites, (a, b) -> 1);
		final GeoMap map = GeoMap.create(edges, weights);
		final Traffics status = Traffics.create(map);
		return status;
	}

	/**
	 *
	 * @param uiStatus
	 * @return
	 */
	private Controller trafficChanged(final UIStatus uiStatus) {
		routeMap.setStatus(uiStatus.getTraffics());
		scrollMap.repaint();
		return this;
	}

	/**
	 * @param st
	 * @param mapPt
	 * @return
	 */
	private Controller updateHud(final UIStatus st, final Point2D mapPt) {
		scrollMap.setHud(computeHud(st, mapPt));
		return this;
	}
}
