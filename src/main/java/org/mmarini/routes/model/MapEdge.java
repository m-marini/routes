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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Al tratto di strada associamo la velocità corrente dei veicoli. La velocità
 * corrente è determinanata dal primo veicolo entrato nel tratto di strada. Se
 * il veicolo è in prossimità dell'uscita la velocità è limitata dalla velocità
 * del tratto sucessivo. Il veicolo è in prossimità se alla velocità corrente il
 * veicolo per il dato intervallo il veicolo esce dal tratto stradale. Se il
 * veicolo non è in prossimità la velocità è il limite del tratto stradale.
 * <p>
 * Durante la simulazione vengono dapprima calcolate le velocità correnti di
 * ogni tratto stradale, poi vengono calcolate le posizioni dei veicoli per ogni
 * tratto stradale.
 * </p>
 * <p>
 * Il numero massimo di veicoli presenti nel tratto è dato da<br>
 * n = Sl / Vl<br>
 * dove Vl = 5 è la lunghezza del veicolo.<br>
 * </p>
 *
 * @author marco.marini@mmarini.org
 * @version $Id: MapEdge.java,v 1.14 2010/10/19 20:33:00 marco Exp $
 */
public class MapEdge implements MapElement, Constants {
    private MapNode begin;
    private MapNode end;
    private double speedLimit;
    private double distance;
    private double security;
    private int priority;
    private final List<Vehicle> veicleList;
    private final AbstractSimulationFunctions functions;
    private final Point2D vector;
    private double transitTime;
    private Vehicle nextVeicle;
    private final Comparator<Vehicle> priorityComparator;

    /**
     *
     */
    public MapEdge() {
        veicleList = new ArrayList<Vehicle>(0);
        functions = AbstractSimulationFunctions.createInstance();
        vector = new Point2D.Double();
        priorityComparator = new Comparator<Vehicle>() {

            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final Vehicle v1, final Vehicle v2) {
                return v2.getPriority() - v1.getPriority();
            }

        };
    }

    /**
     * @param veicle
     */
    private void add(final Vehicle veicle) {
        if (veicleList.isEmpty()) {
            transitTime = 0;
        }
        veicleList.add(veicle);
    }

    /**
     * @see org.mmarini.routes.model.MapElement#apply(org.mmarini.routes.model.MapElementVisitor)
     */
    @Override
    public void apply(final MapElementVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return
     */
    private double computeCurrentSpeed() {
        double speed = speedLimit;
        final double dist = distance;
        if (!veicleList.isEmpty()) {
            if (isBusy()) {
                speed = 0;
            } else {
                /*
                 * Compute the speed depending on first veicle in edge
                 */
                final double d = getLastVeicle().getDistance();
                double speed1 = functions.computeSpeed(d);
                if (speed1 < speed) {
                    speed = speed1;
                }

                /*
                 * Compute the speed depending on the total number of veicle
                 */
                final int n = veicleList.size();
                speed1 = functions.computeSpeedByVeicles(dist, n);
                if (speed1 < speed) {
                    speed = speed1;
                }
            }
        }
        return speed;
    }

    /**
     * @return
     */
    public double computeExpectedSpeed() {
        double speed = computeCurrentSpeed();
        final boolean empty = veicleList.isEmpty();
        if (!empty) {
            final double speed1 = distance / transitTime;
            speed = Math.min(speed, speed1);
        }
        return speed;
    }

    /**
     * @return
     */
    public double computeExpectedTransitTime() {
        double time = distance / computeCurrentSpeed();
        if (!veicleList.isEmpty() && transitTime > time) {
            time = transitTime;
        }
        return time;
    }

    /**
     * @param point
     * @return
     */
    private double computeKa(final Point2D point) {
        final Point2D begin = getBeginLocation();
        final double x0 = begin.getX();
        final double y0 = begin.getY();
        final Point2D ev = vector;
        final double xe = ev.getX();
        final double ye = ev.getY();
        final double xp = point.getX() - x0;
        final double yp = point.getY() - y0;
        final double ep = xe * xp + ye * yp;
        final double ka = ep / distance;
        return ka;
    }

    /**
     * @param location
     * @param distance
     */
    public void computeLocation(final Point2D location, final double distance) {
        final Point2D end = getEndLocation();
        final Point2D begin = getBeginLocation();
        final double k = distance / end.distance(begin);
        final double x0 = begin.getX();
        final double y0 = begin.getY();
        location.setLocation(k * (end.getX() - x0) + x0, k * (end.getY() - y0) + y0);
    }

    /**
     * Computes the transite time.
     * <p>
     * The best transtit time is the distance / speedLimit
     * </p>
     *
     * @return the transit time
     */
    public double computeTransitTime() {
        return distance / speedLimit;
    }

    /**
     * @return
     */
    public MapEdge createClone() {
        final MapEdge edge = new MapEdge();
        edge.setSpeedLimit(speedLimit);
        edge.setPriority(priority);
        return edge;
    }

    /**
     * @param context
     */
    public void dequeue(final SimContext context) {
        final Vehicle veicle = nextVeicle;
        if (veicle != null && !isBusy()) {
            final int priority = begin.computeIncomeMaxPriority();
            if (veicle.getPriority() >= priority) {
                veicle.moveToEdge(this);
                add(veicle);
            }
        }
        nextVeicle = null;
    }

    /**
     * @param result
     * @param point
     */
    private void findCloser(final Point2D result, final Point2D point) {
        final double ka = computeKa(point);
        if (ka <= 0) {
            result.setLocation(getBeginLocation());
        } else if (ka >= distance) {
            result.setLocation(getEndLocation());
        } else {
            computeLocation(result, ka);
        }
    }

    /**
     * @return
     */
    private double findNextTransitTime() {
        double time = 0;
        for (final Vehicle veicle : veicleList) {
            time = Math.max(time, veicle.getTransitTime());
        }
        return time;
    }

    /**
     * @param veicle
     * @return
     */
    public Vehicle findNextVeicle(final Vehicle veicle) {
        final int i = veicleList.indexOf(veicle);
        if (i >= 1) {
            return veicleList.get(i - 1);
        }
        return null;
    }

    /**
     * @return the begin
     */
    public MapNode getBegin() {
        return begin;
    }

    /**
     * @param begin the begin to set
     */
    public void setBegin(final MapNode begin) {
        this.begin = begin;
        updateInfo();
    }

    /**
     * @return
     */
    public Point2D getBeginLocation() {
        return begin.getLocation();
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    private void setDistance(final double distance) {
        this.distance = distance;
        validateSpeedLimits();
    }

    /**
     * @param point
     * @return
     */
    public double getDistanceSq(final Point2D point) {
        final Point2D tmp = new Point2D.Double();
        findCloser(tmp, point);
        return point.distanceSq(tmp);
    }

    /**
     * @return the end
     */
    public MapNode getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(final MapNode end) {
        final MapNode old = this.end;
        this.end = end;
        if (end != null) {
            end.addIncome(this);
        }
        if (old != null) {
            old.removeIncome(this);
        }
        updateInfo();
    }

    /**
     * @return
     */
    public Point2D getEndLocation() {
        return end.getLocation();
    }

    /**
     * @return
     */
    private Vehicle getFirstVeicle() {
        if (veicleList.isEmpty()) {
            return null;
        }
        return veicleList.get(0);
    }

    /**
     * @return
     */
    private Vehicle getLastVeicle() {
        if (veicleList.isEmpty()) {
            return null;
        }
        final int n = veicleList.size();
        return veicleList.get(n - 1);
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
        for (final Vehicle veicle : veicleList) {
            veicle.setPriority(priority);
        }
    }

    /**
     * @return the security
     */
    public double getSecurity() {
        return security;
    }

    /**
     * @return the speedLimit
     */
    public double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * @param speedLimit the speedLimit to set
     */
    public void setSpeedLimit(final double speedLimit) {
        this.speedLimit = speedLimit;
        validateSpeedLimits();
    }

    /**
     * Return the traffic level.<br>
     * The traffic level is a value between 0 to 1 that indicates how stucked is the
     * edge.<br>
     * A 0 level means the edge has no trafic and the veicles can run at speed limit
     * of the edge. <br>
     * A 1 level means the veicle is strucked in the traffic and they cannot run.
     *
     * @return the traffic level
     */
    public double getTrafficLevel() {
        return functions.computeTrafficLevel(distance, veicleList.size(), speedLimit);
    }

    /**
     * @return the edgeVector
     */
    public Point2D getVector() {
        return vector;
    }

    /**
     * @return
     */
    public boolean isBusy() {
        final Vehicle last = getLastVeicle();
        if (last == null) {
            return false;
        }
        return last.getDistance() <= VEICLE_LENGTH;
    }

    /**
     * @return
     */
    public boolean isVeicleExiting() {
        final Vehicle veicle = getFirstVeicle();
        return veicle != null && veicle.getDistance() + security >= distance;
    }

    /**
     * @param veicle
     */
    public void push(final Vehicle veicle) {
        final Vehicle nextVeicle = this.nextVeicle;
        if (nextVeicle == null || priorityComparator.compare(veicle, nextVeicle) < 0) {
            this.nextVeicle = veicle;
        }
    }

    /**
     * @param veicle
     */
    public void remove(final Vehicle veicle) {
        double tt = transitTime;
        if (tt > veicle.getTransitTime()) {
            tt = findNextTransitTime();
            transitTime = tt;
        }
        veicleList.remove(veicle);
    }

    /**
     * @param oldNode
     * @param newNode
     */
    public void replaceNode(final MapNode oldNode, final MapNode newNode) {
        if (begin.equals(oldNode)) {
            setBegin(newNode);
        }
        if (end.equals(oldNode)) {
            setEnd(newNode);
        }
    }

    /**
     * @param context
     */
    public void reset(final SimContext context) {
        for (final Vehicle veicle : veicleList) {
            context.removeVeicle(veicle);
        }
        veicleList.clear();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(begin);
        builder.append("->");
        builder.append(end);
        builder.append(",p=");
        builder.append(priority);
        builder.append(",l=");
        builder.append(distance);
        builder.append(",ls=");
        builder.append(security);
        return builder.toString();
    }

    /**
     *
     */
    private void updateInfo() {
        if (begin != null && end != null) {
            final Point2D el = end.getLocation();
            final Point2D bl = begin.getLocation();
            setDistance(bl.distance(el));
            final double dx = el.getX() - bl.getX();
            final double dy = el.getY() - bl.getY();
            vector.setLocation(dx, dy);
        }
    }

    /**
     * @param time
     */
    public void updateTransitTime(final double time) {
        if (!veicleList.isEmpty()) {
            transitTime += time;
        }
    }

    /**
     *
     */
    private void validateSpeedLimits() {
        if (begin != null && end != null) {
            final double maxSpeedLimit = functions.computeSpeed(distance);
            if (speedLimit > maxSpeedLimit) {
                speedLimit = maxSpeedLimit;
            }
            security = functions.computeDistanceBySpeed(speedLimit);
        }
    }
}
