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

package org.mmarini.routes.model2;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * The vehicle from a departure node to a destination node
 */
public class Vehicle {

    /**
     * Returns a new vehicle from a departure node to a destination node
     *
     * @param departure   the departure
     * @param destination the destination
     * @param time        the creation time
     */
    public static Vehicle create(SiteNode departure, SiteNode destination, double time) {
        return new Vehicle(UUID.randomUUID(), departure, destination,
                time, null, 0, false, 0);
    }

    private final UUID id;
    private final SiteNode departure;
    private final SiteNode destination;
    private final double creationTime;
    private MapEdge currentEdge;
    private double distance;
    private boolean returning;
    private double edgeEntryTime;

    /**
     * Creates a vehicle
     *
     * @param id            the identifier
     * @param departure     the departure node
     * @param destination   the destination node
     * @param creationTime  the creation time
     * @param currentEdge   the current edge
     * @param distance      the distance from the beginning of edge
     * @param returning     true if it is moving from destination to departure
     * @param edgeEntryTime the entry time of vehicle into the edge
     */
    public Vehicle(UUID id,
                   SiteNode departure,
                   SiteNode destination,
                   double creationTime,
                   MapEdge currentEdge,
                   double distance,
                   boolean returning,
                   double edgeEntryTime) {
        this.id = id;
        this.departure = departure;
        this.destination = destination;
        this.creationTime = creationTime;
        this.currentEdge = currentEdge;
        this.distance = distance;
        this.returning = returning;
        this.edgeEntryTime = edgeEntryTime;
    }

    /**
     * Returns a copy of this vehicle
     */
    public Vehicle copy() {
        return new Vehicle(id, departure, destination, creationTime, currentEdge, distance, returning, edgeEntryTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return id.equals(vehicle.id);
    }

    public double getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the current destination
     * destination if not returning else departure
     */
    SiteNode getCurrentDestination() {
        return returning ? departure : destination;
    }

    /**
     * Returns the current edge
     */
    public Optional<MapEdge> getCurrentEdge() {
        return Optional.ofNullable(currentEdge);
    }

    /**
     * Sets the current edge
     *
     * @param currentEdge the edge
     */
    public Vehicle setCurrentEdge(MapEdge currentEdge) {
        this.currentEdge = currentEdge;
        return this;
    }

    /**
     * Returns the departure node
     */
    public SiteNode getDeparture() {
        return departure;
    }

    /**
     * Returns the destination node
     */
    public SiteNode getDestination() {
        return destination;
    }

    /**
     * Returns the distance from beginning of edge
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Sets the distance from the beginning of edge
     *
     * @param distance the distance
     */
    public Vehicle setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    /**
     * Returns the edge entry time
     */
    public double getEdgeEntryTime() {
        return edgeEntryTime;
    }

    /**
     * Sets the edge entry time
     *
     * @param edgeEntryTime the edge entry time
     */
    public Vehicle setEdgeEntryTime(double edgeEntryTime) {
        this.edgeEntryTime = edgeEntryTime;
        return this;
    }

    /**
     * Returns the vehicle identifier
     */
    public UUID getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns true if the vehicle is moving from destination to departure
     */
    public boolean isReturning() {
        return returning;
    }

    /**
     * Sets if the vehicle is moving from destination to departure
     *
     * @param returning true if the vehicle is moving from destination to departure
     */
    public Vehicle setReturning(boolean returning) {
        this.returning = returning;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vehicle.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .toString();
    }
}
