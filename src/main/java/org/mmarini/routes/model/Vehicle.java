/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>
 * Si trova il punto di check (macchina sucessiva o nodo di stop) poi si calcola
 * la velocità per raggiungere il punto alla velocità desiderata. Poi si calcola
 * la velocità massima per raggiungere il punto in base ai limiti di velocità
 * dei singoli tratti (distanza / tempo in base alla velocità limite). La
 * velocità finale del veicolo è la minore di quelle calcolate.
 * </p>
 * <p>
 * Se nel percorso c'è un nodo di stop bisogna calcolare la velocità per
 * fermarsi alla nodo.
 * </p>
 *
 * @author marco.marini@mmarini.org
 * @version $Id: Veicle.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 */
public class Vehicle implements Constants {
    private final Queue<Itinerary> itinerary;
    private final AbstractSimulationFunctions functions;
    private MapNode destination;
    private MapEdge currentEdge;
    private double distance;
    private double transitTime;
    private double travelingTime;
    private double expectedTravelingTime;
    private int priority;

    /**
     *
     */
    public Vehicle() {
        functions = AbstractSimulationFunctions.createInstance();
        itinerary = new LinkedList<Itinerary>();
    }

    /**
     * @param t
     * @param l
     * @return
     */
    private double calculateSecurityDistance(final double t, final double l) {
        return functions.computeVeicleMovement(t, l);
    }

    /**
     * @param context
     */
    private void dispatch(final SimContext context) {
        final MapEdge edge = currentEdge;
        distance = edge.getDistance();
        final MapEdge next = context.findNextEdge(edge.getEnd(), destination);
        if (next == null) {
            edge.remove(this);
            context.removeVeicle(this);
        } else {
            next.push(this);
        }
    }

    /**
     * @return
     */
    private Vehicle findNextVeicle() {
        if (currentEdge == null) {
            return null;
        }
        return currentEdge.findNextVeicle(this);
    }

    /**
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
     * @param destination the destination to set
     */
    public void setDestination(final MapNode destination) {
        this.destination = destination;
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
     * @param priority the priority to set
     */
    public void setPriority(final int priority) {
        this.priority = priority;
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
    private void handleEdgeExit(final SimContext context) {
        final MapEdge edge = currentEdge;
        final MapNode end = edge.getEnd();
        final MapNode dest = destination;
        if (dest.equals(end)) {
            final Itinerary next = popDestination();
            if (next == null) {
                edge.remove(this);
                context.removeVeicle(this);
            } else {
                final SiteNode nextDest = next.getDestination();
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
     * dove Ke è la costante di frenata, Kr costante di reazione e Vl la lunghezza
     * del veicolo.<br>
     * Se v è espressa in Km/h allora e s in metri allora Ke=0.01,Kr=0.3 e Vl=5 <br>
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
    public void move(final SimContext context) {
        if (currentEdge != null) {
            final double time = context.getTime();
            final Vehicle nextVeicle = findNextVeicle();
            double dist = this.distance;
            double d = time * currentEdge.getSpeedLimit();
            if (nextVeicle != null) {
                /*
                 * Calculate the position relative to next veicle
                 */
                final double distNext = nextVeicle.distance - dist;
                final double eSec = currentEdge.getSecurity();
                if (d + eSec > distNext) {
                    /*
                     * Brake
                     */
                    final double secDist = calculateSecurityDistance(time, distNext);
                    d = secDist;
                }
                if (d > distNext) {
                    d = distNext;
                }
            }
            dist += d;
            transitTime += time;
            travelingTime += time;
            final double edgeDistance = currentEdge.getDistance();
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
    public void moveToEdge(final MapEdge edge) {
        if (currentEdge != null) {
            currentEdge.remove(this);
        }
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
    public void pushDestination(final Itinerary it) {
        itinerary.offer(it);
    }

    /**
     *
     */
    public void removeFromEdge() {
        if (currentEdge != null) {
            currentEdge.remove(this);
        }
    }

    /**
     * @param oldNode
     * @param newNode
     */
    public void replaceNode(final MapNode oldNode, final MapNode newNode) {
        if (destination.equals(oldNode)) {
            setDestination(newNode);
        }
    }

    /**
     * @param location
     */
    public void retrieveLocation(final Point2D location) {
        currentEdge.computeLocation(location, distance);
    }

    /**
     * @param expectedTravelingTime the expectedTravelingTime to set
     */
    public void setExpectedTravelingTime(final double expectedTravelingTime) {
        this.expectedTravelingTime = expectedTravelingTime;
    }
}
