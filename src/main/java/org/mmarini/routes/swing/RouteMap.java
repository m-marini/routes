/*
 * RouteMap.java
 *
 * $Id: RouteMap.java,v 1.20 2010/10/19 20:32:59 marco Exp $
 *
 * 28/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapElement;
import org.mmarini.routes.model.MapElementVisitor;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.Module;
import org.mmarini.routes.model.SiteNode;
import org.mmarini.routes.model.Veicle;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: RouteMap.java,v 1.20 2010/10/19 20:32:59 marco Exp $
 *
 */
public class RouteMap extends JComponent {

	class CursorPainter implements MapElementVisitor {

		/**
		 * @see org.mmarini.routes.model.MapElementVisitor#visit(org.mmarini.routes.model.MapEdge)
		 */
		@Override
		public void visit(final MapEdge edge) {
			painter.paintCursorEdge(edge);
			painter.paintCursorEdgeEnds(edge);
		}

		/**
		 * @see org.mmarini.routes.model.MapElementVisitor#visit(org.mmarini.routes.model.MapNode)
		 */
		@Override
		public void visit(final MapNode node) {
			painter.paintNodeCursor(node.getLocation());
		}

		/**
		 * @see org.mmarini.routes.model.MapElementVisitor#visit(org.mmarini.routes.model.SiteNode)
		 */
		@Override
		public void visit(final SiteNode node) {
		}
	}

	class Mode {

		/**
		 * @param ev
		 */
		public void handleMouseMoved(final MouseEvent ev) {
		}

		/**
		 * @param e
		 */
		public void handleMousePressed(final MouseEvent e) {
			computeMapLocation(point, e.getPoint());
			final MapElement element = mediator.findElement(point, (CURSOR_SELECTION_PRECISION / scale));
			setSelectedElement(element);
		}

		/**
		 * @param gr
		 */
		public void paintMode() {
		}

	}

	private static final String DELETE_ACTION = "AAAAA";

	private static final double TRAFFIC_COLOR_SATURATION = 0.9;

	private static final long serialVersionUID = 1L;

	private static final Color EDGE_DRAGING_COLOR = Color.GRAY;

	private static final int CURSOR_SELECTION_PRECISION = 10;

	private static final int MAP_BORDER = 60;

	private boolean mouseInside;

	public Module module;

	private RouteMediator mediator;

	private final Rectangle2D mapBound;

	private final Point2D point;

	private double scale;

	private final AffineTransform transform;

	private double gridSize;

	private final AffineTransform inverse;

	private final MapElementEvent mapElementEvent;

	private MapElement selectedElement;

	private List<MapElementListener> listeners;

	private final CursorPainter cursorPainter;

	private final long blinkingOn;

	private final long blinkingTime;

	private final Point2D begin;

	private final Point2D end;

	private boolean trafficView;

	private final MapElementVisitor eventFirer;

	private final Painter painter;

	private Mode currentMode;

	private final Mode selectingMode;

	private final Mode startEdgeMode;

	private final Mode endEdgeMode;

	private final Mode moduleLocationMode;

	private final Mode moduleRotationMode;

	private final Mode centerMode;

	/**
	     *
	     */
	public RouteMap() {
		painter = new Painter();
		mapBound = new Rectangle2D.Double();
		point = new Point2D.Double();
		begin = new Point2D.Double();
		end = new Point2D.Double();
		transform = new AffineTransform();
		inverse = new AffineTransform();
		mapElementEvent = new MapElementEvent(this);
		cursorPainter = new CursorPainter();
		scale = 1;
		blinkingOn = 500;
		blinkingTime = 500;
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed DELETE"), DELETE_ACTION);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed BACK_SPACE"), DELETE_ACTION);
		getActionMap().put(DELETE_ACTION, new AbstractAction() {

			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				delete();
			}
		});

		selectingMode = new Mode();
		startEdgeMode = new Mode() {

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#handleMousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void handleMousePressed(final MouseEvent e) {
				computeMapLocation(begin, e.getPoint());
				mediator.snapToNode(begin, (CURSOR_SELECTION_PRECISION / scale));
				end.setLocation(begin);
				setCurrentMode(endEdgeMode);
				repaint();
			}

		};
		endEdgeMode = new Mode() {

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#handleMouseMoved(java.awt.event.MouseEvent)
			 */
			@Override
			public void handleMouseMoved(final MouseEvent ev) {
				computeMapLocation(end, ev.getPoint());
				mediator.snapToNode(end, (CURSOR_SELECTION_PRECISION / scale));
				repaint();
			}

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#handleMousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void handleMousePressed(final MouseEvent e) {
				handleEndEdge(e);
			}

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#paintMode(java.awt.Graphics2D)
			 */
			@Override
			public void paintMode() {
				if (isMouseInside()) {
					painter.paintEdge(begin, end, EDGE_DRAGING_COLOR);
				}
			}

		};
		moduleLocationMode = new Mode() {
			/**
			 * @param ev
			 */
			@Override
			public void handleMouseMoved(final MouseEvent ev) {
				repaint();
			}

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#handleMousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void handleMousePressed(final MouseEvent e) {
				computeMapLocation(begin, e.getPoint());
				setCurrentMode(moduleRotationMode);
				repaint();
			}

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#paintMode()
			 */
			@Override
			public void paintMode() {
				final Point mousePosition = getMousePosition();
				if (mousePosition != null) {
					computeMapLocation(point, mousePosition);
					paintModule(point, 0., 0.);
				}
			}

		};
		moduleRotationMode = new Mode() {
			/**
			 * @param ev
			 */
			@Override
			public void handleMouseMoved(final MouseEvent ev) {
				repaint();
			}

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#handleMousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void handleMousePressed(final MouseEvent e) {
				computeMapLocation(point, e.getPoint());
				mediator.addModule(module, begin, point.getX() - begin.getX(), point.getY() - begin.getY());
				startSelectMode();
				repaint();
			}

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#paintMode()
			 */
			@Override
			public void paintMode() {
				final Point mousePosition = getMousePosition();
				if (mousePosition != null) {
					computeMapLocation(point, mousePosition);
					paintModule(begin, point.getX() - begin.getX(), point.getY() - begin.getY());
				} else {
					paintModule(begin, 0., 0.);
				}
			}

		};
		eventFirer = new MapElementVisitor() {

			@Override
			public void visit(final MapEdge edge) {
				fireMapEdgeSelected(edge);
			}

			@Override
			public void visit(final MapNode node) {
				fireMapNodeSelected(node);
			}

			@Override
			public void visit(final SiteNode node) {
				fireSiteSelected(node);
			}

		};
		centerMode = new Mode() {

			/**
			 * @see org.mmarini.routes.swing.RouteMap.Mode#handleMousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void handleMousePressed(final MouseEvent e) {
				computeMapLocation(point, e.getPoint());
				mediator.centerMap(point);
				startSelectMode();
			}

		};

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				handleMouseEntered();
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				handleMouseExited();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				currentMode.handleMousePressed(e);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(final MouseEvent ev) {
				currentMode.handleMouseMoved(ev);
			}
		});

		setCurrentMode(selectingMode);
		setBackground(Color.WHITE);
	}

	/**
	 *
	 * @param l
	 */
	public synchronized void addMapElementListener(final MapElementListener l) {
		List<MapElementListener> ls = listeners;
		if (ls == null) {
			ls = new ArrayList<MapElementListener>(1);
			ls.add(l);
			listeners = ls;
		} else if (!ls.contains(l)) {
			ls = new ArrayList<MapElementListener>(ls);
			ls.add(l);
			listeners = ls;
		}
	}

	/**
	     *
	     */
	private void computeGridSize() {
		final double size = 10 / scale;
		gridSize = 1;
		while (size > gridSize) {
			gridSize *= 10;
		}
	}

	/**
	     *
	     */
	private void computeMapBound() {
		mediator.computeMapBound(mapBound);
		computeGridSize();
	}

	/**
	 *
	 * @param result
	 * @param point
	 */
	public void computeMapLocation(final Point2D result, final Point point) {
		inverse.transform(point, result);
		result.setLocation(Math.round(result.getX()), Math.round(result.getY()));
	}

	/**
	     *
	     */
	private void computePreferredSize() {
		final int width = (int) Math.round(mapBound.getWidth() * scale) + MAP_BORDER * 2;
		final int height = (int) Math.round(mapBound.getHeight() * scale) + MAP_BORDER * 2;
		setPreferredSize(new Dimension(width, height));
		revalidate();
	}

	/**
	     *
	     */
	private void computeTransform() {
		transform.setToTranslation(MAP_BORDER, MAP_BORDER);
		transform.scale(scale, scale);
		transform.translate(-mapBound.getMinX(), -mapBound.getMinY());
		inverse.setTransform(transform);
		try {
			inverse.invert();
		} catch (final NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param result
	 * @param point
	 */
	public void computeViewLocation(final Point result, final Point2D point) {
		transform.transform(point, result);
	}

	/**
	 * Delete current selected element
	 */
	protected void delete() {
		if (selectedElement instanceof MapEdge) {
			mediator.remove((MapEdge) selectedElement);
		} else {
			mediator.remove((MapNode) selectedElement);
		}
	}

	/**
	 *
	 * @param edge
	 */
	private void fireMapEdgeSelected(final MapEdge edge) {
		mapElementEvent.setNode(null);
		mapElementEvent.setSite(null);
		mapElementEvent.setEdge(edge);
		final List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (final MapElementListener l : listeners) {
				l.edgeSelected(mapElementEvent);
			}
		}
	}

	/**
	     *
	     *
	     */
	private void fireMapNodeSelected(final MapNode node) {
		mapElementEvent.setNode(node);
		mapElementEvent.setSite(null);
		mapElementEvent.setEdge(null);
		final List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (final MapElementListener l : listeners) {
				l.nodeSelected(mapElementEvent);
			}
		}
	}

	/**
	 *
	 * @param site
	 */
	private void fireSiteSelected(final SiteNode site) {
		mapElementEvent.setNode(null);
		mapElementEvent.setSite(site);
		mapElementEvent.setEdge(null);
		final List<MapElementListener> ls = listeners;
		if (ls != null) {
			for (final MapElementListener l : listeners) {
				l.siteSelected(mapElementEvent);
			}
		}
	}

	/**
	 * @return
	 */
	public double getEdgeLength() {
		return begin.distance(end);
	}

	/**
	 * @return the gridSize
	 */
	public double getGridSize() {
		return gridSize;
	}

	/**
	 * @return the inverse
	 */
	public AffineTransform getInverse() {
		return inverse;
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	private Color getNodeColor(final MapNode node) {
		return mediator.getNodeColor(node);
	}

	/**
	 * Return the current view scale
	 *
	 * @return the current view scale (real px/virtual px)
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @return the selectedElement
	 */
	public MapElement getSelectedElement() {
		return selectedElement;
	}

	/**
	 * @return the transform
	 */
	public AffineTransform getTransform() {
		return transform;
	}

	/**
	 * @param e
	 */
	private void handleEndEdge(final MouseEvent e) {
		computeMapLocation(end, e.getPoint());
		mediator.snapToNode(end, (CURSOR_SELECTION_PRECISION / scale));
		mediator.createEdge(begin, end);
		begin.setLocation(end);
		repaint();
	}

	/**
	     *
	     */
	private void handleMouseEntered() {
		setMouseInside(true);
		repaint();
	}

	/**
	     *
	     */
	private void handleMouseExited() {
		setMouseInside(false);
		repaint();
	}

	/**
	 * @return the mouseInside
	 */
	private boolean isMouseInside() {
		return mouseInside;
	}

	/**
	 * @return the selectingEnd
	 */
	public boolean isSelectingEnd() {
		return currentMode.equals(endEdgeMode);
	}

	/**
	 *
	 * @return
	 */
	private boolean isShowingCursor() {
		final long t = System.currentTimeMillis() % blinkingTime;
		return t <= blinkingOn;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		final Dimension size = getSize();
		Color bg;
		if (trafficView) {
			bg = Color.BLACK;
		} else {
			bg = getBackground();
		}
		g.setColor(bg);
		g.fillRect(0, 0, size.width, size.height);
		if (mediator != null) {
			final Graphics2D gr = (Graphics2D) g.create();
			gr.transform(transform);
			painter.setGraphics(gr);
			painter.setBorderPainted(scale >= 1f);
			painter.setReversed(trafficView);
			painter.paintGrid(mapBound, gridSize);
			paintEdges();
			paintSites();
			currentMode.paintMode();
			paintCursor();
			if (!trafficView) {
				paintVeicles();
			}
		}
	}

	/**
	     *
	     */
	private void paintCursor() {
		final MapElement element = getSelectedElement();
		if (element != null && isShowingCursor()) {
			element.apply(cursorPainter);
		}
	}

	/**
	     *
	     */
	private void paintEdges() {
		if (trafficView) {
			for (final MapEdge edge : mediator.getMapEdges()) {
				if (!(edge.equals(selectedElement) && isShowingCursor())) {
					double trafficLevel = edge.getTrafficLevel();
					trafficLevel = Math.sqrt(trafficLevel);
					final Color color = SwingUtils.getInstance().computeColor(trafficLevel, TRAFFIC_COLOR_SATURATION);
					painter.paintEdge(edge, color);
				}
			}
		} else {
			for (final MapEdge edge : mediator.getMapEdges()) {
				if (!(edge.equals(selectedElement) && isShowingCursor())) {
					painter.paintEdge(edge);
				}
			}
		}
	}

	/**
	 *
	 * @param location
	 * @param vecx
	 * @param vecy
	 */
	private void paintModule(final Point2D location, final double vecx, final double vecy) {
		if (module != null) {
			painter.paintModule(module, location, vecx, vecy);
		}
	}

	/**
	     *
	     */
	private void paintSites() {
		for (final SiteNode node : mediator.getSiteNodes()) {
			if (node.equals(selectedElement) && isShowingCursor()) {
				painter.paintSiteCursor(node.getLocation());
			} else {
				painter.paintSite(node.getLocation(), getNodeColor(node));
			}

		}
	}

	/**
	     *
	     */
	private void paintVeicles() {
		for (final Veicle veicle : mediator.getVeicles()) {
			if (veicle.isRunning()) {
				veicle.retrieveLocation(point);
				final Color color = getNodeColor(veicle.getDestination());
				painter.paintVeicle(point, veicle.getVector(), color);
			}
		}
	}

	/**
	 *
	 * @param l
	 */
	public synchronized void removeMapElementListener(final MapElementListener l) {
		List<MapElementListener> ls = listeners;
		if (ls != null && ls.contains(l)) {
			ls = new ArrayList<MapElementListener>(ls);
			ls.remove(l);
			listeners = ls;
		}
	}

	/**
	     *
	     */
	public void reset() {
		computeMapBound();
		computeTransform();
		computePreferredSize();
	}

	/**
	 * @param size
	 */
	public void scaleToFit(final Dimension size) {
		computeMapBound();
		double scale = Math.min((size.width - MAP_BORDER * 2) / mapBound.getWidth(),
				(size.height - MAP_BORDER * 2) / mapBound.getHeight());
		scale = Math.max(scale, 1e-6);
		setScale(scale);
	}

	/**
	 * @param currentMode the currentMode to set
	 */
	private void setCurrentMode(final Mode currentMode) {
		this.currentMode = currentMode;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(final RouteMediator handler) {
		this.mediator = handler;
	}

	/**
	 * @param module the module to set
	 */
	private void setModule(final Module module) {
		this.module = module;
	}

	/**
	 * @param mouseInside the mouseInside to set
	 */
	private void setMouseInside(final boolean mouseInside) {
		this.mouseInside = mouseInside;
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(final double scale) {
		this.scale = scale;
		reset();
	}

	/**
	 * @param selectedElement the selectedElement to set
	 */
	public void setSelectedElement(final MapElement selectedElement) {
		final MapElement oldSelectedElement = this.selectedElement;
		this.selectedElement = selectedElement;
		if (selectedElement == null && oldSelectedElement != null) {
			fireMapNodeSelected(null);
			repaint();
		} else if ((selectedElement != null && !selectedElement.equals(oldSelectedElement))) {
			selectedElement.apply(eventFirer);
			repaint();
		}
	}

	/**
	 * @param trafficView
	 */
	public void setTrafficView(final boolean trafficView) {
		this.trafficView = trafficView;
		repaint();
	}

	/**
	     *
	     */
	public void startCenterMode() {
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		setCurrentMode(centerMode);
		repaint();
	}

	/**
	     *
	     */
	public void startEdgeMode() {
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		setCurrentMode(startEdgeMode);
	}

	/**
	 *
	 * @param module
	 */
	public void startModuleMode(final Module module) {
		setModule(module);
		setCurrentMode(moduleLocationMode);
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}

	/**
	     *
	     */
	public void startSelectMode() {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setCurrentMode(selectingMode);
		repaint();
	}
}
