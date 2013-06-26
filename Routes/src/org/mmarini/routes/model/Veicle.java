/*
 * Veicle.java
 *
 * $Id: Veicle.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 *
 * 29/dic/08
 *
 * Copyright notice
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;

import org.mmarini.routes.xml.Dumpable;
import org.mmarini.routes.xml.Dumper;
import org.w3c.dom.Element;

/**
 * <p>
 * Si trova il punto di check (macchina sucessiva o nodo di stop) poi si calcola
 * la velocità per raggiungere il punto alla velocità desiderata. Poi si
 * calcola la velocità massima per raggiungere il punto in base ai limiti di
 * velocità dei singoli tratti (distanza / tempo in base alla velocità
 * limite). La velocità finale del veicolo è la minore di quelle calcolate.
 * </p>
 * <p>
 * Se nel percorso c'è un nodo di stop bisogna calcolare la velocità per
 * fermarsi alla nodo.
 * </p>
 * 
 * @author marco.marini@mmarini.org
 * @version $Id: Veicle.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class Veicle implements Constants, Dumpable {

	private MapNode destination;

	private Queue<Itinerary> itinerary;

	private MapEdge currentEdge;

	private double distance;

	private double transitTime;

	private double travelingTime;
	private double expectedTravelingTime;
	private int priority;

	private AbstractSimulationFunctions functions;

	/**
         * 
         */
	public Veicle() {
		functions = AbstractSimulationFunctions.createInstance();
		itinerary = new LinkedList<Itinerary>();
	}

	/**
	 * @param t
	 * @param l
	 * @return
	 */
	private double calculateSecurityDistance(double t, double l) {
		return functions.computeVeicleMovement(t, l);
	}

	/**
	 * @param context
	 */
	private void dispatch(SimContext context) {
		MapEdge edge = currentEdge;
		distance = edge.getDistance();
		MapEdge next = context.findNextEdge(edge.getEnd(), destination);
		if (next == null) {
			edge.remove(this);
			context.removeVeicle(this);
		} else {
			next.push(this);
		}
	}

	/**
	 * @see org.mmarini.routes.xml.Dumpable#dump(org.w3c.dom.Element)
	 */
	public void dump(Element root) {
		Dumper dumper = Dumper.getInstance();
		dumper.dumpReference(root, "currentEdge", currentEdge);
		dumper.dumpReference(root, "destination", destination);
		dumper.dumpValue(root, "distance", distance);
		dumper.dumpValue(root, "priority", priority);
		dumper.dumpValue(root, "transitTime", transitTime);
		dumper.dumpReference(root, "itinerary", itinerary);
	}

	/**
	 * @return
	 */
	private Veicle findNextVeicle() {
		if (currentEdge == null)
			return null;
		return currentEdge.findNextVeicle(this);
	}

	/**
	 * 
	 * @return
	 */
	public double getDelay() {
		return travelingTime - expectedTravelingTime;
	}

	/**
	 * @return the destination
	 */
	public MapNode getDestination() {
		return destination;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the transitTime
	 */
	public double getTransitTime() {
		return transitTime;
	}

	/**
	 * @return
	 */
	public Point2D getVector() {
		return currentEdge.getVector();
	}

	/**
	 * @param context
	 */
	private void handleEdgeExit(SimContext context) {
		MapEdge edge = currentEdge;
		MapNode end = edge.getEnd();
		MapNode dest = destination;
		if (dest.equals(end)) {
			Itinerary next = popDestination();
			if (next == null) {
				edge.remove(this);
				context.removeVeicle(this);
			} else {
				SiteNode nextDest = next.getDestination();
				setDestination(nextDest);
				setExpectedTravelingTime(next.getExpectedTime());
				travelingTime = 0.;
				dispatch(context);
			}
		} else {
			dispatch(context);
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isDeleyed() {
		return travelingTime > expectedTravelingTime;
	}

	/**
	 * @return
	 */
	public boolean isRunning() {
		return currentEdge != null;
	}

	/**
	 * <p>
	 * La distanza tra un'auto e la sucessiva è determinata da<br>
	 * s = Ke v^2 + Kr v + Vl <br>
	 * dove Ke è la costante di frenata, Kr costante di reazione e Vl la
	 * lunghezza del veicolo.<br>
	 * Se v è espressa in Km/h allora e s in metri allora Ke=0.01,Kr=0.3 e Vl=5
	 * <br>
	 * Il numero di veicoli presenti nel tratto è dato allora da<br>
	 * n = Sl / s = Sl / (Ke v^2 + Kr v + Vl)<br>
	 * Quindi la velocità nel tratto deve soddisfare l'equazione:<br>
	 * Ke v^2 + Kr v + Vl - Sl / n = 0<br>
	 * La cui soluzione è:<br>
	 * v = [- Kr + sqrt(Kr^2-4 Ke (Vl - Sl / n)] / (2 Ke)
	 * </p>
	 * <p>
	 * La distanza percorsa sarà:<br>
	 * d=(sqrt((Kr+t)^2-4 Ke (Vl - l))-Kr-t)/(2 Ke) t
	 * </p>
	 * 
	 * @param context
	 */
	public void move(SimContext context) {
		if (currentEdge != null) {
			double time = context.getTime();
			Veicle nextVeicle = findNextVeicle();
			double dist = this.distance;
			double d = time * currentEdge.getSpeedLimit();
			if (nextVeicle != null) {
				/*
				 * Calculate the position relative to next veicle
				 */
				double distNext = nextVeicle.distance - dist;
				double eSec = currentEdge.getSecurity();
				if (d + eSec > distNext) {
					/*
					 * Brake
					 */
					double secDist = calculateSecurityDistance(time, distNext);
					d = secDist;
				}
				if (d > distNext) {
					d = distNext;
				}
			}
			dist += d;
			transitTime += time;
			travelingTime += time;
			double edgeDistance = currentEdge.getDistance();
			if (dist >= edgeDistance) {
				handleEdgeExit(context);
			} else {
				distance = dist;
			}
		}
	}

	/**
	 * @param context
	 */
	public void moveToEdge(MapEdge edge) {
		if (currentEdge != null)
			currentEdge.remove(this);
		setPriority(edge.getPriority());
		currentEdge = edge;
		distance = 0;
		transitTime = 0;
	}

	/**
	 * @return
	 */
	private Itinerary popDestination() {
		return itinerary.poll();
	}

	/**
	 * @param it
	 */
	public void pushDestination(Itinerary it) {
		itinerary.offer(it);
	}

	/**
         * 
         * 
         */
	public void removeFromEdge() {
		if (currentEdge != null)
			currentEdge.remove(this);
	}

	/**
	 * @param oldNode
	 * @param newNode
	 */
	public void replaceNode(MapNode oldNode, MapNode newNode) {
		if (destination.equals(oldNode)) {
			setDestination(newNode);
		}
	}

	/**
	 * 
	 * @param location
	 */
	public void retrieveLocation(Point2D location) {
		currentEdge.computeLocation(location, distance);
	}

	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(MapNode destination) {
		this.destination = destination;
	}

	/**
	 * @param expectedTravelingTime
	 *            the expectedTravelingTime to set
	 */
	public void setExpectedTravelingTime(double expectedTravelingTime) {
		this.expectedTravelingTime = expectedTravelingTime;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
