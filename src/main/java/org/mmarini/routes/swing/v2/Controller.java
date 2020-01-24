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
import org.mmarini.routes.model.v2.EdgeTraffic;
import org.mmarini.routes.model.v2.GeoMap;
import org.mmarini.routes.model.v2.MapEdge;
import org.mmarini.routes.model.v2.MapNode;
import org.mmarini.routes.model.v2.MapProfile;
import org.mmarini.routes.model.v2.SimulationStatus;
import org.mmarini.routes.model.v2.SimulationStatusDeserializer;
import org.mmarini.routes.model.v2.SimulationStatusSerializer;
import org.mmarini.routes.model.v2.Simulator;
import org.mmarini.routes.model.v2.SiteNode;
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
//	private final List<String> gridLegendPattern;
	private final List<String> pointLegendPattern;
	private final List<String> edgeLegendPattern;
	private final BehaviorSubject<UIStatus> uiStatusSubj;
	private final Observable<UIStatus> uiStatusObs;

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
//		this.gridLegendPattern = SwingUtils.loadPatterns("ScrollMap.gridLegendPattern");
		this.pointLegendPattern = SwingUtils.loadPatterns("ScrollMap.pointLegendPattern");
		this.edgeLegendPattern = SwingUtils.loadPatterns("ScrollMap.edgeLegendPattern");
		final SimulationStatus status1 = loadDefault();
		final UIStatus initStatus = UIStatus.create().setStatus(status1);
		this.uiStatusSubj = BehaviorSubject.createDefault(initStatus);
		this.uiStatusObs = uiStatusSubj;

		this.fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("Controller.filetype.title"), //$NON-NLS-1$
				"yml", "rml")); //$NON-NLS-1$ //$NON-NLS-2$

		bindAll();

		mapChanged(initStatus);
		simulator.setSimulationStatus(status1);
		simulator.start();
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
		logger.debug("addEdge {}, {}", startPoint, endPoint);
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
				logger.debug("bindOnDragEdge addEdge status {} {}", st, st.getStatus().getTraffics().size());
				addEdge(st, startPoint, endPoint).subscribe(status -> {
					logger.debug("bindOnDragEdge addEdge {}", st);
					final Optional<Tuple2<Point2D, Point2D>> dragEdge = Optional.of(new Tuple2<>(endPoint, endPoint));
					final UIStatus newStatus = status.setDragEdge(dragEdge);
					logger.debug("bindOnDragEdge addEdge new status {} {}", newStatus,
							newStatus.getStatus().getTraffics().size());
					mapChanged(newStatus);
					uiStatusSubj.onNext(newStatus);
					simulator.setSimulationStatus(newStatus.getStatus()).start();
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
			simulator.setSimulationStatus(st.getStatus()).start();
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
			simulator.setSimulationStatus(st.getStatus()).start();
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
			simulator.setSimulationStatus(st.getStatus()).start();
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
			final SiteNode site = t.getElem2();
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
					final Optional<MapNode> mapNode = routeMap.getSelectedSite().map(site -> (MapNode) site)
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
					simulator.setSimulationStatus(st.getStatus()).start();
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
						simulator.setSimulationStatus(uiStatus.getStatus());
					});
					simulator.start();
				}, this::showError);

		mainFrame.getSaveMapAsObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			simulator.stop().subscribe(s -> {
				final int choice = fileChooser.showSaveDialog(mainFrame);
				if (choice == JFileChooser.APPROVE_OPTION) {
					handleSaveMap(st);
				}
				simulator.start();
			}, this::showError);
		}, this::showError);

		mainFrame.getSaveMapObs().withLatestFrom(uiStatusObs, (ev, st) -> st).subscribe(st -> {
			simulator.stop().subscribe(s -> {
				handleSaveMap(st);
				simulator.start();
			}, this::showError);
		}, this::showError);

		mainFrame.getNewMapObs().subscribe(ev -> {
			simulator.stop().subscribe(st -> {
				final UIStatus status = UIStatus.create();
				mapChanged(status);
				mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
				uiStatusSubj.onNext(status);
				simulator.setSimulationStatus(status.getStatus());
				simulator.start();
			}, this::showError);
		}, this::showError);

		mainFrame.getNewRandomObs().subscribe(ev -> {
			simulator.stop().subscribe(st -> {
				mapProfilePane.setDifficultyOnly(false);
				final int opt = JOptionPane.showConfirmDialog(mainFrame, mapProfilePane,
						Messages.getString("Controller.mapProfilePane.title"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
				if (opt == JOptionPane.OK_OPTION) {
					final MapProfile profile = mapProfilePane.getProfile();
					logger.info("Selected {}", profile);
					final SimulationStatus status = SimulationStatus.random(profile, new Random());
					final UIStatus uiStatus = UIStatus.create().setStatus(status);
					simulator.setSimulationStatus(status);
					mapChanged(uiStatus);
					mainFrame.resetTitle().setSaveActionEnabled(false).repaint();
					uiStatusSubj.onNext(uiStatus);
				}
				simulator.start();
			}, this::showError);
		}, this::showError);

		mainFrame.getExitObs().subscribe(ev -> {
			mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
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
			elem.getSite().ifPresent(explorerPane::setSelectedNode);
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
			logger.debug("onNext scale at {}", newScale);
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
			final GeoMap map = st.getStatus().getMap();
			routeMap.setSelectedSite(map.getSite(node));
			routeMap.setSelectedNode(map.getNode(node));
			explorerPane.setSelectedNode(node);
			uiStatusSubj.onNext(st);
			simulator.setSimulationStatus(st.getStatus()).start();
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
			simulator.setSimulationStatus(st.getStatus()).start();
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
				.filter(t -> !t.getElem2().getStatus().equals(t.getElem1()))
				// Update ui status, refresh panels and send new event
				.compose(SwingObservable.observeOnEdt()).subscribe(t -> {
					final UIStatus uiStatus = t.getElem2().setStatus(t.getElem1());
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
		logger.debug("centerMapTo {} ", center);
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
			logger.debug("changeNode {} ", node);
			final SimulationStatus nextSt = uiStatus.getStatus().changeNode(node);
			final UIStatus nextUiStatus = uiStatus.setStatus(nextSt).setSelectedElement(MapElement.empty());
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
			logger.debug("changePriority {} {}", edge.getShortName(), priority);
			final MapEdge newEdge = edge.setPriority(priority);
			final UIStatus newStatus = uiStatus.setStatus(uiStatus.getStatus().changeEdge(newEdge));
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
			logger.debug("changeSpeedLimit {} {}", edge.getShortName(), speed);
			final MapEdge newEdge = edge.setSpeedLimit(speed);
			final UIStatus newStatus = uiStatus.setStatus(uiStatus.getStatus().changeEdge(newEdge));
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
	 * @return
	 */
	private List<String> computeHud(final List<String> patterns, final double gridSize, final Point2D point,
			final Optional<Tuple2<Point2D, Point2D>> dragEdge) {
		final Double length = dragEdge.map(t -> t.getElem1().distance(t.getElem2())).orElse(null);
		final List<String> texts = patterns.stream()
				.map(pattern -> String.format(pattern, gridSize, point.getX(), point.getY(), length))
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
			return computeHud(edgeLegendPattern, status.getGridSize(), point, status.getDragEdge());
		default:
			return computeHud(pointLegendPattern, status.getGridSize(), point, status.getDragEdge());
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
			logger.debug("deleteEdge {} ", edge);
			final SimulationStatus nextSt = uiStatus.getStatus().removeEdge(edge);
			return uiStatus.setStatus(nextSt).setSelectedElement(MapElement.empty());
		});
	}

	/**
	 * @param uiStatus
	 * @param edge
	 * @return
	 */
	private Single<UIStatus> deleteNode(final UIStatus uiStatus, final MapNode node) {
		return simulator.stop().map(status -> {
			logger.debug("deleteNode {} ", node);
			final SimulationStatus nextSt = uiStatus.getStatus().removeNode(node);
			return uiStatus.setStatus(nextSt).setSelectedElement(MapElement.empty());
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
				logger.info("Saving {} ...", file);
				new SimulationStatusSerializer(status.getStatus()).writeFile(file);
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
						logger.debug("loadProcess {}", file);
						final SimulationStatus newStatus = SimulationStatusDeserializer.create().parse(file);
						return Optional.of(new Tuple2<>(status.setStatus(newStatus), file));
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
		routeMap.setStatus(uiStatus.getStatus()).setGridSize(uiStatus.getGridSize())
				.setTransform(uiStatus.getTransform()).setPreferredSize(uiStatus.getScreenMapSize());
		scrollMap.repaint();
		explorerPane.setMap(uiStatus.getStatus().getMap());
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
				Messages.getString("RouteMediator.error.title"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
		return this;
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

	/**
	 *
	 * @param uiStatus
	 * @return
	 */
	private Controller trafficChanged(final UIStatus uiStatus) {
		routeMap.setStatus(uiStatus.getStatus());
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
