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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mmarini.Tuple2.stream;
import static org.mmarini.Tuple2.toMap;
import static org.mmarini.Utils.getValue;
import static org.mmarini.Utils.join;
import static org.mmarini.routes.model2.Routes.computeRoutes;

public class StatusImpl implements Status {

    /**
     * Returns the status
     *
     * @param maxVehicles      the maximum number of vehicles
     * @param speedLimit       the speed limit
     * @param frequency        the frequency of new vehicles for every node
     * @param time             the current time
     * @param topology         the topology
     * @param vehicles         the vehicle list
     * @param edgeTransitTimes the effective edge transit time
     * @param weights          the cumulative probability of path from site to site
     */
    public static StatusImpl createStatus(int maxVehicles, double speedLimit, double frequency, double time, Topology topology, List<Vehicle> vehicles, Map<MapEdge, Double> edgeTransitTimes, double[][] weights) {
        return new StatusImpl(maxVehicles, speedLimit, frequency, time, topology, vehicles, edgeTransitTimes, weights);
    }

    /**
     * Returns the status with default weight and edge transit times
     *
     * @param maxVehicles the maximum number of vehicles
     * @param speedLimit  the speed limit
     * @param frequency   the frequency of new vehicles for every node
     * @param time        the current time
     * @param topology    the topology
     * @param vehicles    the vehicle list
     */
    public static StatusImpl createStatus(int maxVehicles, double speedLimit, double frequency, double time, Topology topology,
                                          List<Vehicle> vehicles) {
        int n = topology.getSites().size();
        double[][] weights = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weights[i][j] = i == j ? 0 : 1;
            }
        }
        return createStatus(maxVehicles, speedLimit, frequency, time, topology, vehicles, weights);
    }

    /**
     * Returns the status with default edge transit times
     *
     * @param maxVehicles maximum number of vehicles
     * @param speedLimit  the speed limit in m/s
     * @param frequency   the frequency of new vehicles for every node in vehicles/s
     * @param time        the current time in sec.
     * @param topology    the topology
     * @param vehicles    the vehicle
     * @param weights     the path weights
     */
    public static StatusImpl createStatus(int maxVehicles, double speedLimit, double frequency, double time, Topology topology,
                                          List<Vehicle> vehicles,
                                          double[][] weights
    ) {
        Map<MapEdge, Double> edgeTransitTimes = topology.getEdges()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        MapEdge::getTransitTime
                ));
        return new StatusImpl(maxVehicles, speedLimit, frequency, time, topology,
                vehicles, edgeTransitTimes, weights);
    }

    private final int maxVehicles;
    private final double speedLimit;
    private final double frequency;
    private final double time;
    private final Topology topology;
    private final List<Vehicle> vehicles;
    private final Map<MapEdge, Double> edgeTransitTimes;
    private final double[][] weights;
    private Map<Tuple2<MapNode, MapNode>, MapEdge> edgeByPath;
    private Map<MapEdge, Integer> vehicleCountByEdge;

    /**
     * Create the status
     *
     * @param maxVehicles      the maximum number of vehicles
     * @param speedLimit       the speed limit
     * @param frequency        the frequency of new vehicles for every node
     * @param time             the current time
     * @param topology         the topology
     * @param vehicles         the vehicle list
     * @param edgeTransitTimes the effective edge transit time
     * @param weights          the weights
     */
    protected StatusImpl(int maxVehicles, double speedLimit, double frequency, double time, Topology topology, List<Vehicle> vehicles, Map<MapEdge, Double> edgeTransitTimes, double[][] weights) {
        this.maxVehicles = maxVehicles;
        this.time = time;
        this.topology = topology;
        this.frequency = frequency;
        this.speedLimit = speedLimit;
        this.vehicles = vehicles;
        this.edgeTransitTimes = edgeTransitTimes;
        this.weights = weights;
    }

    /**
     * Returns the transit time from a site to another site if a path exists
     *
     * @param from the departure site
     * @param to   the destination site
     */
    Optional<Double> computeTransitTime(SiteNode from, SiteNode to) {
        Optional<MapEdge> edge = getNextEdge(from, to);
        MapNode node = from;
        double time = 0;
        while (edge.isPresent() && !node.equals(to)) {
            time += edge.map(MapEdge::getTransitTime).orElse(0.0);
            Optional<MapNode> nextNode = edge.map(MapEdge::getEnd);
            edge = nextNode.flatMap(n -> getNextEdge(n, to));
            node = nextNode.orElse(to);
        }
        double finalTime = time;
        return edge.map(e -> finalTime);
    }

    /**
     * Returns transit time matrix from site to site
     */
    Map<Tuple2<SiteNode, SiteNode>, Double> createTransitTimeMatrix() {
        Map<Tuple2<SiteNode, SiteNode>, Double> transitTimeMatrix = join(getSites(), getSites())
                .filter(path -> !path._1.equals(path._2))
                .flatMap(path ->
                        computeTransitTime(path._1, path._2)
                                .map(t -> Tuple2.of(path, t))
                                .stream()
                ).collect(toMap());
        return join(getSites(), getSites())
                .flatMap(path ->
                        getValue(transitTimeMatrix, path)
                                .map(t -> t
                                        + getValue(transitTimeMatrix, Tuple2.of(path._2, path._1))
                                        .orElse(0.0))
                                .map(t -> Tuple2.of(path, t))
                                .stream()
                ).collect(toMap());
    }

    @Override
    public double edgeTrafficLevel(MapEdge edge) {
        return edge.getTrafficLevel(getVehicleCount(edge));
    }

    /**
     *
     */
    Map<Tuple2<MapNode, MapNode>, MapEdge> getEdgeByPath() {
        if (edgeByPath == null) {
            this.edgeByPath = computeRoutes(topology.getEdges(),
                    edgeTransitTimes);
        }
        return edgeByPath;
    }

    @Override
    public List<MapEdge> getEdges() {
        return topology.getEdges();
    }

    @Override
    public double getFrequency() {
        return this.frequency;
    }

    @Override
    public int getMaxVehicle() {
        return maxVehicles;
    }

    /**
     * Returns the edge to go from a node to another node
     *
     * @param from the start node
     * @param to   the destination node
     */
    Optional<MapEdge> getNextEdge(MapNode from, MapNode to) {
        return getValue(getEdgeByPath(), Tuple2.of(from, to));
    }

    @Override
    public List<MapNode> getNodes() {
        return topology.getNodes();
    }

    @Override
    public DoubleMatrix<SiteNode> getPathFrequencies() {
        int n = getSites().size();
        double[][] freq = new double[n][n];
        double[][] w = getWeightMatrix().getValues();
        double[] tot = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tot[i] += w[i][j];
            }
        }
        for (int i = 0; i < n; i++) {
            freq[i][i] = 2 * w[i][i] / tot[i] * frequency;
            for (int j = i + 1; j < n; j++) {
                freq[i][j] = freq[j][i] = ((w[i][j] / tot[i]) + (w[j][i] / tot[j])) * frequency;
            }
        }
        return new DoubleMatrix<>(getSites(), freq);
    }

    @Override
    public List<SiteNode> getSites() {
        return topology.getSites();
    }

    @Override
    public double getSpeedLimit() {
        return speedLimit;
    }

    @Override
    public double getTime() {
        return time;
    }

    @Override
    public Topology getTopology() {
        return topology;
    }

    @Override
    public List<TrafficInfo> getTrafficInfo() {
        Map<Tuple2<SiteNode, SiteNode>, Double> transitTime = createTransitTimeMatrix();
        return getSites().stream()
                .map(site -> {
                    int noVehicles = (int) vehicles.stream()
                            .map(Vehicle::getDestination)
                            .filter(site::equals)
                            .count();
                    List<Double> delayTimes = vehicles.stream()
                            .filter(v -> site.equals(v.getDestination()))
                            .flatMap(v -> getValue(transitTime, Tuple2.of(v.getDeparture(), v.getDestination()))
                                    .map(tt -> time - v.getCreationTime() - tt)
                                    .stream())
                            .filter(t -> t > 0)
                            .collect(Collectors.toList());
                    int delayCount = delayTimes.size();
                    double totalDelayTime = delayTimes.stream().mapToDouble(v -> v).sum();
                    return new TrafficInfo(site, noVehicles, delayCount, totalDelayTime);
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the numbr of vehicle in a given edge
     *
     * @param edge the edge
     */
    int getVehicleCount(MapEdge edge) {
        Optional<MapEdge> edgeOpt = Optional.ofNullable(edge);
        return edgeOpt.flatMap(getValue(getVehicleCountByEdge()))
                .orElse(0);
    }

    public Map<MapEdge, Integer> getVehicleCountByEdge() {
        if (vehicleCountByEdge == null) {
            Map<MapEdge, List<Tuple2<MapEdge, Vehicle>>> map = vehicles.stream()
                    .flatMap(v -> v.getCurrentEdge().map(edge -> Tuple2.of(edge, v)).stream())
                    .collect(Collectors.groupingBy(Tuple2::getV1));
            vehicleCountByEdge = stream(map)
                    .map(t -> t.setV2(t.getV2().size()))
                    .collect(toMap());
        }
        return vehicleCountByEdge;
    }

    @Override
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    @Override
    public DoubleMatrix<SiteNode> getWeightMatrix() {
        return new DoubleMatrix<>(getSites(), this.weights);
    }
}
