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

import org.mmarini.Tuple2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.mmarini.Tuple2.stream;

public class TransitTimes {
    /**
     * Returns the default transit time by edge
     *
     * @param edges the edges
     */
    public static TransitTimes create(List<MapEdge> edges) {
        return create(edges.stream(), MapEdge::getTransitTime);
    }

    /**
     * Returns the transit time by edge
     *
     * @param edges  the edge stream
     * @param mapper the transit time mapper
     */
    public static TransitTimes create(Stream<MapEdge> edges, ToDoubleFunction<MapEdge> mapper) {
        return new TransitTimes(edges.collect(Collectors.toMap(
                Function.identity(),
                mapper::applyAsDouble
        )));
    }

    private final Map<MapEdge, Double> transitTimeByEdge;

    /**
     * Creates a Map of double by edge
     *
     * @param transitTimeByEdge the transit time map
     */
    protected TransitTimes(Map<MapEdge, Double> transitTimeByEdge) {
        this.transitTimeByEdge = requireNonNull(transitTimeByEdge);
    }

    /**
     * Returns a copy o f transit time
     */
    public TransitTimes copy() {
        return new TransitTimes(new HashMap<>(transitTimeByEdge));
    }

    /**
     * Returns the transit time of an edge
     *
     * @param edge the edge
     */
    public double getValue(MapEdge edge) {
        requireNonNull(edge);
        Double t = transitTimeByEdge.get(edge);
        return t != null ? t : edge.getTransitTime();
    }

    /**
     * Returns this transit time by applying the key mapper
     *
     * @param mapper the key mapper
     */
    public TransitTimes mapKeys(UnaryOperator<MapEdge> mapper) {
        requireNonNull(mapper);
        Map<MapEdge, Double> newMap = stream(transitTimeByEdge)
                .collect(Collectors.toMap(
                        t -> mapper.apply(t._1),
                        Tuple2::getV2));
        transitTimeByEdge.clear();
        transitTimeByEdge.putAll(newMap);
        return this;
    }

    /**
     * Returns this transit time by changing an edge transit time
     *
     * @param edge        the edge
     * @param transitTime the transit time
     */
    public TransitTimes setValue(MapEdge edge, double transitTime) {
        requireNonNull(edge);
        transitTimeByEdge.put(edge, transitTime);
        return this;
    }

    /**
     * Returns this transit time with the value updated by vehicle states and time
     *
     * @param time     the simulation time
     * @param vehicles the vehicle status
     */
    public TransitTimes update(double time, Stream<Vehicle> vehicles) {
        requireNonNull(vehicles);
        // map to vehicle edge and vehicle in edge time
        vehicles.flatMap(v -> v.getCurrentEdge()
                        .map(edge ->
                                Tuple2.of(edge, time - v.getEdgeEntryTime()))
                        .stream())
                // filter on vehicle in edge time > edge transit time
                .filter(tuple -> tuple._2 > getValue(tuple._1))
                // apply the new transit time values
                .forEach(tuple ->
                        transitTimeByEdge.put(tuple._1, tuple._2)
                );
        return this;
    }
}
