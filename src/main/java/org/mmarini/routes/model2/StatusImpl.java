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

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.*;
import static org.mmarini.routes.model2.Constants.*;
import static org.mmarini.routes.model2.Routes.computeRoutes;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.Topology.createTopology;

public class StatusImpl implements Status {
    /**
     * Computes the transit time from previous traffic information
     *
     * @param edges          the edges
     * @param vehiclesByEdge the vehicles by edge
     * @param oldTransitTime the previous traffic information
     * @param oldEdgeMap     the previous edge by new edge
     */
    static Map<MapEdge, Double> computeNewTransitTime(
            List<MapEdge> edges,
            Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge,
            Map<MapEdge, Double> oldTransitTime,
            Map<MapEdge, MapEdge> oldEdgeMap) {
        return edges.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        edge ->
                                // Get the list of vehicle in the new edge
                                getValue(vehiclesByEdge, edge)
                                        // filter if there is at least a vehicle
                                        .filter(Predicate.not(LinkedList::isEmpty))
                                        // map to the old edge
                                        .flatMap(list -> getValue(oldEdgeMap, edge))
                                        // map to old transit time
                                        .flatMap(getValue(oldTransitTime))
                                        // get default value if not exits
                                        .orElseGet(edge::getTransitTime)
                ));
    }

    /**
     * Returns the next vehicle map
     *
     * @param vehiclesByEdge the vehicle by edge
     */
    static Map<Vehicle, Vehicle> computeNextVehicleMap(Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge) {
        return vehiclesByEdge.entrySet().stream()
                .flatMap(entry -> {
                    List<Vehicle> vehicles1 = entry.getValue();
                    return IntStream.range(0, vehicles1.size() - 1)
                            .mapToObj(i ->
                                    entry(vehicles1.get(i), vehicles1.get(i + 1))
                            );
                })
                .collect(toMap());
    }

    /**
     * Returns the vehicles by edge
     *
     * @param vehicles the vehicles
     */
    private static Map<MapEdge, LinkedList<Vehicle>> computeVehicleByEdges(List<Vehicle> vehicles) {
        @SuppressWarnings("OptionalGetWithoutIsPresent") Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = vehicles.stream()
                .filter(v -> v.getCurrentEdge().isPresent())
                .collect(Collectors.groupingBy(
                        v -> v.getCurrentEdge().get()))
                .entrySet().stream()
                .peek(entry -> entry.getValue()
                        .sort(Comparator.comparingDouble(
                                Vehicle::getDistance)))
                .map(mapValue(LinkedList::new))
                .collect(toMap());
        return vehiclesByEdge;
    }

    /**
     * Returns a random status base on a given map profile
     *
     * @param profile     the map profile
     * @param speedLimits the speed limit
     */
    public static StatusImpl createRandom(Random random, MapProfile profile, double speedLimits) {
        int n = profile.getSiteCount();
        double[][] pts = new double[n][2];
        for (int i = 0; i < n; i++) {
            boolean unique = true;
            double x;
            double y;
            do {
                x = random.nextDouble();
                y = random.nextDouble();
                // check for previous points
                for (int j = 0; j < i && unique; j++) {
                    unique = x != pts[j][0] && y != pts[j][1];
                }
            } while (!unique);
            pts[i][0] = x;
            pts[i][1] = y;
        }
        double x0 = Arrays.stream(pts).mapToDouble(x -> x[0]).min().orElse(0);
        double x1 = Arrays.stream(pts).mapToDouble(x -> x[0]).max().orElse(0);
        double y0 = Arrays.stream(pts).mapToDouble(x -> x[1]).min().orElse(0);
        double y1 = Arrays.stream(pts).mapToDouble(x -> x[1]).max().orElse(0);
        double dx = x0 != x1 ? x1 - x0 : 1;
        double dy = y0 != y1 ? y1 - y0 : 1;
        double w = profile.getWidth();
        double h = profile.getHeight();

        List<SiteNode> sites = Arrays.stream(pts)
                .map(loc ->
                        createSite(
                                (2 * (loc[0] - x0) / dx - 1) * w,
                                (2 * (loc[1] - y0) / dy - 1) * h))
                .collect(Collectors.toList());
        List<MapNode> nodes = sites.stream().map(x -> (MapNode) x).collect(Collectors.toList());
        double[][] pathsCdf = toCdf(createRandomWeights(n, profile.getMinWeight(), random));
        return new StatusImpl(0,
                createTopology(nodes, List.of()),
                List.of(),
                speedLimits,
                profile.getFrequency(),
                pathsCdf,
                Map.of(), Map.of(),
                Map.of(),
                null).createPath();
    }

    /**
     * Returns a random weight matrix
     *
     * @param noSites   number of sites
     * @param minWeight min weight value
     * @param random    the random generator
     */
    static double[][] createRandomWeights(int noSites, double minWeight, Random random) {
        double[][] weights = new double[noSites][noSites];
        for (int i = 0; i < noSites; i++) {
            for (int j = 0; j < noSites; j++) {
                weights[i][j] = i != j ? random.nextDouble() * (1 - minWeight) + minWeight : 0;
            }
        }
        return weights;
    }

    /**
     * Returns the initial status
     *
     * @param topology   the topology
     * @param time       the current time
     * @param vehicles   the vehicle
     * @param speedLimit the speed limit
     * @param frequency  the frequency of new vehicles for every node
     * @param weights    the path weights
     */
    public static StatusImpl createStatus(Topology topology, double time,
                                          List<Vehicle> vehicles,
                                          double speedLimit,
                                          double frequency,
                                          double[][] weights
    ) {
        Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = computeVehicleByEdges(vehicles);
        Map<Vehicle, Vehicle> nextVehicles = computeNextVehicleMap(vehiclesByEdge);
        Map<MapEdge, Double> edgeTransitTimes = topology.getEdges().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        MapEdge::getTransitTime
                ));

        return new StatusImpl(time, topology, new ArrayList<>(vehicles),
                speedLimit, frequency, toCdf(weights), vehiclesByEdge, nextVehicles,
                edgeTransitTimes, null)
                .createPath();
    }

    /**
     * Returns the initial status
     *
     * @param topology   the topology
     * @param time       the current time
     * @param vehicles   the vehicle
     * @param speedLimit the speed limit
     * @param frequency  the frequency of new vehicles for every node
     */
    public static StatusImpl createStatus(Topology topology, double time,
                                          List<Vehicle> vehicles, double speedLimit, double frequency) {
        int n = topology.getSites().size();
        double[][] weights = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weights[i][j] = i == j ? 0 : 1;
            }
        }
        return createStatus(topology, time, vehicles, speedLimit, frequency, weights);
    }

    /**
     * Returns the stream ov the new vehicle with changed edge
     *
     * @param edgeMap the edge mapping
     */
    static Function<Vehicle, Stream<Vehicle>> replaceEdge(Map<MapEdge, MapEdge> edgeMap) {
        return vehicle -> vehicle.getCurrentEdge().
                map(edge ->
                        // replace the edge or empty if not exits
                        getValue(edgeMap, edge)
                                .map(vehicle::setCurrentEdge)
                                .stream()
                ).orElseGet(() -> Stream.of(vehicle));
    }

    /**
     * Returns the cumulative values from weights
     *
     * @param weights the weights
     */
    static double[][] toCdf(double[][] weights) {
        int n = weights.length;
        double[][] cdf = new double[n][];
        for (int i = 0; i < n; i++) {
            cdf[i] = cumulative(weights[i]);
        }
        return cdf;
    }

    /**
     * Returns the weights from cumulative values
     *
     * @param cdf the cumulative values
     */
    static double[][] toWeight(double[][] cdf) {
        int n = cdf.length;
        double[][] weights = new double[n][];
        for (int i = 0; i < n; i++) {
            weights[i] = preferences(cdf[i]);
        }
        return weights;
    }

    /**
     * Returns the predicate of vehicle not in removed site path
     *
     * @param site the site
     */
    static Predicate<Vehicle> vehicleNotInRemovedNode(MapNode site) {
        return v -> !(v.isRelatedToNode(site));
    }

    private final double time;
    private final Topology topology;
    private final List<Vehicle> vehicles;
    private final double frequency;
    private final double speedLimit;
    private final double[][] pathCdf;
    private final Map<Vehicle, Vehicle> nextVehicles;
    private final Map<MapEdge, Double> edgeTransitTimes;
    private final Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge;
    private Map<Tuple2<MapNode, MapNode>, MapEdge> edgeByPath;

    /**
     * @param time             the current time
     * @param topology         the topology
     * @param vehicles         the vehicle list
     * @param speedLimit       the speed limit
     * @param frequency        the frequency of new vehicles for every node
     * @param pathCdf          the cumulative probability of path from site to site
     * @param vehiclesByEdge   the vehicles by edge
     * @param nextVehicles     the map between a vehicle and the next
     * @param edgeTransitTimes the effective edge transit time
     * @param edgeByPath       the edge by path
     */
    protected StatusImpl(double time,
                         Topology topology,
                         List<Vehicle> vehicles,
                         double speedLimit,
                         double frequency,
                         double[][] pathCdf,
                         Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge, Map<Vehicle, Vehicle> nextVehicles,
                         Map<MapEdge, Double> edgeTransitTimes,
                         Map<Tuple2<MapNode, MapNode>, MapEdge> edgeByPath) {

        this.topology = requireNonNull(topology);
        this.vehicles = requireNonNull(vehicles);
        this.nextVehicles = requireNonNull(nextVehicles);
        this.edgeTransitTimes = requireNonNull(edgeTransitTimes);
        this.vehiclesByEdge = requireNonNull(vehiclesByEdge);
        this.pathCdf = requireNonNull(pathCdf);
        this.frequency = frequency;
        this.speedLimit = speedLimit;
        this.time = time;
        this.edgeByPath = edgeByPath;
        assert edgeTransitTimes.size() == topology.getEdges().size();
    }

    @Override
    public StatusImpl addEdge(MapEdge edge) {
        Topology t = topology.addEdge(edge);
        Map<MapEdge, Double> newEdgeTransitTime = computeNewTransitTime(
                t.getEdges(), vehiclesByEdge, edgeTransitTimes,
                t.createEdgeMap(topology)
        );
        return new StatusImpl(time, t, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles,
                newEdgeTransitTime, null)
                .createPath();
    }

    @Override
    public StatusImpl addModule(Module module, Point2D location, Point2D direction, double epsilon) {
        Topology topology = this.topology.addModule(module, location, direction, epsilon);
        Map<MapEdge, Double> edgeTransitTimes = topology.getEdges().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getEdgeTransitTime
                ));
        return new StatusImpl(time, topology, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles, edgeTransitTimes, null).createPath();
    }

    /**
     * Returns the status after a time interval
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    void applyTimeInterval(Random random, double dt) {
        createPath();
        for (double remainingDt = dt; remainingDt > 0; ) {
            handleWaitingVehicles();
            double elapsedDt = getFirstExitingVehicle(remainingDt)
                    .map(mv -> {
                        moveVehicles(mv.getDt());
                        return mv.getDt();
                    }).orElse(
                            // No vehicles to move
                            remainingDt
                    );
            setTime(time + elapsedDt);
            remainingDt -= elapsedDt;
        }
        vehicles.addAll(createVehicles(random, dt));
    }

    @Override
    public StatusImpl changeEdge(MapEdge oldEdge, MapEdge newEdge) {
        Topology t = topology.removeEdge(oldEdge).addEdge(newEdge);
        // move all vehicle to the new edge
        double length = newEdge.getLength();
        List<Vehicle> vehicles = this.vehicles.stream()
                .filter(v ->
                        !(v.isTransitingEdge(oldEdge) && v.getDistance() > length)
                ).map(v ->
                        v.isTransitingEdge(oldEdge)
                                ? v.copy().setCurrentEdge(newEdge)
                                : v
                ).collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = computeVehicleByEdges(vehicles);
        Map<Vehicle, Vehicle> nextVehicles = computeNextVehicleMap(vehiclesByEdge);
        Map<MapEdge, Double> edgeTransitTimes = computeNewTransitTime(t.getEdges(),
                vehiclesByEdge, this.edgeTransitTimes, t.createEdgeMap(topology));
        return new StatusImpl(time, t, vehicles,
                speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles,
                edgeTransitTimes, null).createPath();
    }

    @Override
    public StatusImpl changeNode(MapNode node) {
        Topology newTop = topology.changeNode(node);
        // Creates edge mapping between new and old
        Map<MapEdge, MapEdge> oldEdgeMap = newTop.createEdgeMap(topology);
        Map<MapEdge, MapEdge> newEdgeMap = topology.createEdgeMap(newTop);

        // remove vehicles with departure or destination equal to old node
        Stream<Vehicle> vehicleStream = node instanceof SiteNode
                ? vehicles.stream().filter(v -> !v.isSiteInPath(node))
                : vehicles.stream();

        // Substitutes the vehicle data
        List<Vehicle> newVehicles = vehicleStream
                // duplicate vehicles
                .map(Vehicle::copy)
                .flatMap(replaceEdge(newEdgeMap))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = computeVehicleByEdges(newVehicles);
        Map<Vehicle, Vehicle> newNextVehicles = computeNextVehicleMap(newVehiclesByEdge);
        Map<MapEdge, Double> newEdgeTransitTime = computeNewTransitTime(
                newTop.getEdges(),
                newVehiclesByEdge,
                edgeTransitTimes,
                oldEdgeMap
        );
        // Create the new weights
        Map<SiteNode, SiteNode> olsSiteMap = newTop.createSiteMap(this.topology);
        double[][] weights = getWeightMatrix().map(newTop.getSites(), site -> getValue(olsSiteMap, site)).getValues();
        return new StatusImpl(time, newTop, newVehicles, speedLimit, frequency,
                toCdf(weights), newVehiclesByEdge, newNextVehicles, newEdgeTransitTime,
                null)
                .createPath();
    }

    /**
     * Returns the vehicle movement for the time interval
     * if it is running in an edge.
     * The movement is bound to the speed limit of edge,
     * the safety distance to the next vehicle if any
     * and the edge length.
     *
     * @param vehicle the vehicle
     * @param dt      the time interval
     */
    VehicleMovement computeVehicleMovement(Vehicle vehicle, double dt) {
        return vehicle.getCurrentEdge().map(edge -> {
            // Compute the maximum movement of the vehicle
            final double dist = vehicle.getDistance();
            final double maxMovement = dt * edge.getSpeedLimit();
                /*
                Checks for next vehicle distance.
                Computes the distance to the next vehicle
                 */
            double ds = getNextVehicle(vehicle).map(next -> {
                final double distToNext = next.getDistance() - dist;
                final double edgeSafetyDistance = edge.getSafetyDistance();
                if (maxMovement + edgeSafetyDistance > distToNext) {
                    /*
                      Vehicle is moving too close the next vehicle
                     */
                    return brakingMovement(distToNext, dt);
                } else {
                    /*
                    Returns the minimum movement between the distance of next vehicle
                    and the maximum distance
                     */
                    return maxMovement;
                }
            }).orElse(maxMovement);
            // Check for edge exit
            if (dist + ds >= edge.getLength()) {
                // Vehicle is exiting the edge
                double distanceToEnd = edge.getLength() - vehicle.getDistance();
                double timeToEnd = distanceToEnd * dt / ds;
                return new VehicleMovement(vehicle, distanceToEnd, timeToEnd, true);
            } else {
                // Vehicle is not exiting the edge
                return new VehicleMovement(vehicle, ds, dt, false);
            }
        }).orElseGet(() ->
                // No edge
                new VehicleMovement(vehicle, 0, dt, false));
    }

    /**
     * Returns the stream of movements of last vehicles in edges in a given interval
     *
     * @param vehicles the stream of vehicles
     * @param dt       the time interval
     */
    Stream<VehicleMovement> computeVehicleMovements(Stream<Vehicle> vehicles, double dt) {
        return vehicles
                .map(vehicle -> computeVehicleMovement(vehicle, dt));
    }

    /**
     * Returns a copy of status only for the traffic changes
     * It clones the vehicle, the vehicleByEdge map
     */
    StatusImpl copy() {
        List<Vehicle> newVehicles = vehicles.stream()
                .map(Vehicle::copy)
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = vehiclesByEdge.entrySet()
                .stream()
                .map(mapValue(LinkedList::new))
                .collect(toMap());
        return new StatusImpl(time, topology, newVehicles,
                speedLimit, frequency, pathCdf, newVehiclesByEdge, new HashMap<>(nextVehicles),
                edgeTransitTimes,
                edgeByPath);
    }

    /**
     * Returns the status with the current best path from node to node
     */
    private StatusImpl createPath() {
        this.edgeByPath = computeRoutes(topology.getEdges(),
                edgeTransitTimes);
        return this;
    }

    /**
     * Returns the status with new vehicles
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    List<Vehicle> createVehicles(Random random, double dt) {
        List<SiteNode> sites = topology.getSites();

        return IntStream.range(0, sites.size())
                // Generates the number of vehicle for each departure node
                .mapToObj(i -> new int[]{
                        i,
                        nextPoison(random, frequency * dt)})
                // Filter the departure node without new vehicles
                .filter(ary -> ary[1] > 0)
                .flatMap(ary -> {
                    // Generates n vehicles
                    int departure = ary[0];
                    int n = ary[1];
                    SiteNode depSite = sites.get(departure);
                    return IntStream.range(0, n)
                            .map(i -> nextCdf(random, pathCdf[departure]))
                            .mapToObj(sites::get)
                            .map(destination -> Vehicle.createVehicle(depSite, destination, time));
                })
                .collect(Collectors.toList());
    }

    /**
     * Registers the entry of a vehicle into its current edge.
     * Sets the next vehicle if present.
     * Adds the vehicle to the edge vehicle list
     * Sets the entry time for the vehicle
     *
     * @param vehicle the vehicle
     */
    void enterVehicleToEdge(Vehicle vehicle) {
        vehicle.getCurrentEdge().ifPresent(edge -> {
            // Sets the entry time
            vehicle.setEdgeEntryTime(time);
            // Set the next vehicle
            if (vehiclesByEdge.containsKey(edge)) {
                LinkedList<Vehicle> vehicles = getVehicles(edge);
                if (!vehicles.isEmpty()) {
                    nextVehicles.put(vehicle, vehicles.getFirst());
                }
                // Adds to the edge vehicle list
                vehicles.addFirst(vehicle);
            } else {
                vehiclesByEdge.put(edge, new LinkedList<>(List.of(vehicle)));
            }
        });
    }

    /**
     * Registers the exit of a vehicle from its current edge
     *
     * @param vehicle the vehicle
     */
    void exitVehicleFromEdge(Vehicle vehicle) {
        vehicle.getCurrentEdge().ifPresent(edge -> {
            // Updates edge transit time depending on it is the only vehicle
            // Removes from vehicle list of edge
            LinkedList<Vehicle> edgeVehicles = getVehicles(edge);
            if (!edgeVehicles.isEmpty() && edgeVehicles.getLast().equals(vehicle)) {
                // Removes from vehicle edge list
                edgeVehicles.removeLast();
                int n = edgeVehicles.size();
                double transitTime = n > 0
                        ? time - vehicle.getEdgeEntryTime()
                        : edge.getTransitTime();
                setEdgeTravelTimes(edge, transitTime);
                // removes the previous next prev if present
                if (!edgeVehicles.isEmpty()) {
                    nextVehicles.remove(edgeVehicles.getLast());
                }
            }
        });
    }

    /**
     * Returns the index of the edge
     *
     * @param edge the edge
     */
    double getEdgeTransitTime(MapEdge edge) {
        return edgeTransitTimes.getOrDefault(edge, edge.getTransitTime());
    }

    @Override
    public List<MapEdge> getEdges() {
        return topology.getEdges();
    }

    /**
     * Returns the last vehicle movement with lower time interval within a given time interval
     *
     * @param dt the time interval
     */
    Optional<VehicleMovement> getFirstExitingVehicle(double dt) {
        Stream<Vehicle> lastVehicles = getLastVehicles()
                .filter(v -> v.getCurrentEdge()
                        .map(MapEdge::getLength)
                        .filter(length -> v.getDistance() < length)
                        .isPresent());
        return computeVehicleMovements(lastVehicles, dt)
                .reduce((a, b) ->
                        a.getDt() <= b.getDt() ? a : b);
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    @Override
    public StatusImpl setFrequency(double frequency) {
        return new StatusImpl(time, topology, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles, edgeTransitTimes, edgeByPath);
    }

    /**
     * Returns the list of last vehicles
     */
    Stream<Vehicle> getLastVehicles() {
        return vehiclesByEdge.values().stream()
                .filter(Predicate.not(List::isEmpty))
                .map(LinkedList::getLast);
    }

    /**
     * Returns the edge to go from a node to another node
     *
     * @param from the start node
     * @param to   the destination node
     */
    Optional<MapEdge> getNextEdge(MapNode from, MapNode to) {
        return getValue(edgeByPath, Tuple2.of(from, to));
    }

    /**
     * Returns the next vehicle of a vehicle
     *
     * @param vehicle the vehicle
     */
    public Optional<Vehicle> getNextVehicle(Vehicle vehicle) {
        return getValue(nextVehicles, vehicle);
    }

    @Override
    public List<MapNode> getNodes() {
        return topology.getNodes();
    }

    /**
     * Returns the path cumulative distribution values
     */
    public double[][] getPathCdf() {
        return pathCdf;
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
    public StatusImpl setSpeedLimit(double speedLimit) {
        return new StatusImpl(time, topology, vehicles, speedLimit, frequency,
                pathCdf, vehiclesByEdge, nextVehicles, edgeTransitTimes, edgeByPath);
    }

    @Override
    public double getTime() {
        return time;
    }

    /**
     * Returns the status with time changed
     *
     * @param time the time
     */
    StatusImpl setTime(double time) {
        return new StatusImpl(time, topology, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles, edgeTransitTimes, edgeByPath);
    }

    @Override
    public List<TrafficInfo> getTrafficInfo() {
        return getSites().stream()
                .map(site ->
                        new TrafficInfo(site, 0, 0, 0))
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of vehicles for an edge
     *
     * @param edge the edge
     */
    LinkedList<Vehicle> getVehicles(MapEdge edge) {
        return vehiclesByEdge.getOrDefault(edge, new LinkedList<>());
    }

    @Override
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Returns the vehicles without edge or at the end of edge
     */
    List<Vehicle> getWaitingVehicles() {
        return vehicles.stream().filter(vehicle ->
                        vehicle.getCurrentEdge()
                                .map(MapEdge::getLength)
                                .filter(length -> vehicle.getDistance() < length)
                                .isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public DoubleMatrix<SiteNode> getWeightMatrix() {
        return new DoubleMatrix<>(getSites(), toWeight(pathCdf));
    }

    void handleVehicleAtSite(Vehicle vehicle) {
        // vehicle on the site
        // Extracts next edge
        Optional<MapEdge> nextEdgeOpt = vehicle.isReturning()
                ? getNextEdge(vehicle.getDestination(), vehicle.getDeparture())
                : getNextEdge(vehicle.getDeparture(), vehicle.getDestination());
        nextEdgeOpt.ifPresentOrElse(nextEdge -> {
            // Next edge exist
            if (isEdgeAvailable(nextEdge)) {
                // cross to next edge is free
                exitVehicleFromEdge(vehicle);
                vehicle.setCurrentEdge(nextEdge).setDistance(0);
                enterVehicleToEdge(vehicle);
            }
        }, () -> {
            // Next edge does not exist
            // remove vehicle
            exitVehicleFromEdge(vehicle);
            vehicles.remove(vehicle);
        });
    }

    /**
     * Handles the waiting vehicle moving them to the next edge if possible
     */
    void handleWaitingVehicles() {
        getWaitingVehicles().forEach(vehicle ->
                vehicle.getCurrentEdge().ifPresentOrElse(edge -> {
                            if (edge.getEnd().equals(vehicle.getCurrentDestination())) {
                                exitVehicleFromEdge(vehicle);
                                // Vehicle at destination
                                if (vehicle.isReturning()) {
                                    // Vehicle completed the trip
                                    vehicles.remove(vehicle);
                                } else {
                                    // Vehicle at destination
                                    vehicle.setReturning(true);
                                    handleVehicleAtSite(vehicle);
                                }
                            } else {
                                // vehicle on the edge
                                // Extracts next edge
                                getNextEdge(edge.getEnd(), vehicle.getCurrentDestination())
                                        .ifPresentOrElse(nextEdge -> {
                                            // Next edge exists
                                            if (isCrossFree(edge, nextEdge)) {
                                                // cross to next edge is free
                                                exitVehicleFromEdge(vehicle);
                                                vehicle.setCurrentEdge(nextEdge).setDistance(0);
                                                enterVehicleToEdge(vehicle);
                                            }
                                        }, () -> {
                                            // Next edge does not exist
                                            // remove vehicle
                                            exitVehicleFromEdge(vehicle);
                                            vehicles.remove(vehicle);
                                        });
                            }
                        }
                        , () ->
                                // vehicle on the site
                                // Extracts next edge
                                handleVehicleAtSite(vehicle)
                )
        );
    }

    /**
     * Returns true if entering edge is available.
     * It checks if the entering edge has space for the vehicle
     * and if there is no incoming vehicles from edges with higher priority then the exiting edge
     *
     * @param exitingEdge  the exiting edge
     * @param enteringEdge the entering edge
     */
    boolean isCrossFree(MapEdge exitingEdge, MapEdge enteringEdge) {
        return isEdgeAvailable(enteringEdge) &&
                isIncomesFree(exitingEdge);

    }

    /**
     * Returns true if edge is available.
     * It checks if the entering edge has space for the vehicle.
     *
     * @param enteringEdge the edge
     */
    boolean isEdgeAvailable(MapEdge enteringEdge) {
        return getValue(vehiclesByEdge, enteringEdge)
                .filter(Predicate.not(List::isEmpty))
                .map(LinkedList::getFirst)
                .map(Vehicle::getDistance)
                .filter(distance -> distance <= VEHICLE_LENGTH)
                .isEmpty();
    }

    /**
     * Returns true if higher priority incomes are free.
     * It checks if there is no incoming vehicles from edges with higher priority then the exiting edge
     *
     * @param incomingEdge the edge
     */
    boolean isIncomesFree(MapEdge incomingEdge) {
        return topology.getIncomeEdges(incomingEdge)
                .stream()
                // Gets the vehicles
                .map(this::getVehicles)
                // drop the empty list
                .filter(Predicate.not(List::isEmpty))
                // gets the last vehicle
                .map(LinkedList::getLast)
                // filter the vehicle in exit proximity
                .filter(v -> v.getCurrentEdge()
                        // Compute the distance for busy cross
                        .map(edge -> edge.getLength() - edge.getSafetyDistance())
                        // filter for vehicle beyond the busy distance
                        .filter(busyDistance -> v.getDistance() >= busyDistance)
                        .isPresent())
                // get any vehicle
                .findAny()
                // return true not exit
                .isEmpty();
    }

    /**
     * Moves all the vehicles for the given time interval
     *
     * @param dt the time interval
     */
    void moveVehicles(double dt) {
        Stream<Vehicle> stream = getVehicles().stream()
                .filter(v ->
                        v.getCurrentEdge()
                                .map(MapEdge::getLength)
                                .filter(length -> v.getDistance() < length)
                                .isPresent()
                );
        computeVehicleMovements(stream, dt)
                .forEach(vm -> {
                    Vehicle vehicle = vm.getVehicle();
                    vehicle.setDistance(vehicle.getDistance() + vm.getDs());
                });
    }

    /**
     * Returns the next status after a time interval.
     * It computes the next edge for every path, the creates a copy of status
     * and changes the new status.
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    @Override
    public StatusImpl next(Random random, double dt) {
        // Computes the path
        StatusImpl next = copy();
        next.applyTimeInterval(random, dt);
        return next;
    }

    @Override
    public StatusImpl optimize() {
        Topology topology = this.topology.optimize(speedLimit);
        Map<MapEdge, MapEdge> edgeMap = topology.createEdgeMap(topology);
        List<Vehicle> vehicles = this.vehicles.stream()
                .map(v -> v.getCurrentEdge()
                        .flatMap(getValue(edgeMap))
                        .map(v::setCurrentEdge)
                        .orElse(v))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = computeVehicleByEdges(vehicles);
        Map<Vehicle, Vehicle> nextVehicles = computeNextVehicleMap(vehiclesByEdge);
        Map<MapEdge, Double> edgeTransitTimes = computeNewTransitTime(topology.getEdges(),
                vehiclesByEdge, this.edgeTransitTimes, edgeMap);

        return new StatusImpl(time, topology, vehicles, speedLimit, frequency, pathCdf,
                vehiclesByEdge, nextVehicles, edgeTransitTimes, null)
                .createPath();
    }

    @Override
    public StatusImpl randomizeWeights(Random random, double minWeight) {
        double[][] weights = createRandomWeights(topology.getSites().size(), minWeight, random);
        return new StatusImpl(time, topology, vehicles, speedLimit, frequency, toCdf(weights),
                vehiclesByEdge, nextVehicles, edgeTransitTimes, null).createPath();
    }

    @Override
    public StatusImpl removeEdge(MapEdge edge) {
        Topology topology = this.topology.removeEdge(edge);
        List<Vehicle> newVehicles = vehicles.stream()
                .filter(vehicle -> !vehicle.isTransitingEdge(edge))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehicleByEdge = computeVehicleByEdges(newVehicles);
        Map<Vehicle, Vehicle> newNextVehicle = computeNextVehicleMap(newVehicleByEdge);
        Map<MapEdge, Double> newEdgeTransitTime = computeNewTransitTime(topology.getEdges(),
                newVehicleByEdge, edgeTransitTimes, topology.createEdgeMap(this.topology));
        return new StatusImpl(time, topology, newVehicles, speedLimit, frequency, pathCdf,
                newVehicleByEdge, newNextVehicle, newEdgeTransitTime, null)
                .createPath();
    }

    @Override
    public StatusImpl removeNode(MapNode node) {
        Topology topology = this.topology.removeNode(node);

        List<Vehicle> newVehicles = vehicles.stream()
                .filter(vehicleNotInRemovedNode(node))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = computeVehicleByEdges(newVehicles);
        Map<Vehicle, Vehicle> newNextVehicles = computeNextVehicleMap(newVehiclesByEdge);
        Map<MapEdge, Double> newEdgeTransitTimes = computeNewTransitTime(topology.getEdges(),
                newVehiclesByEdge,
                edgeTransitTimes,
                topology.createEdgeMap(this.topology));
        Map<SiteNode, SiteNode> olsSiteMap = topology.createSiteMap(this.topology);
        double[][] weights = getWeightMatrix().map(topology.getSites(), site -> getValue(olsSiteMap, site)).getValues();
        return new StatusImpl(time, topology, newVehicles, speedLimit, frequency, toCdf(weights),
                newVehiclesByEdge, newNextVehicles, newEdgeTransitTimes, null)
                .createPath();
    }

    /**
     * @param edge       the edge
     * @param travelTime the travel time
     */
    StatusImpl setEdgeTravelTimes(MapEdge edge, double travelTime) {
        edgeTransitTimes.put(edge, travelTime);
        return this;
    }

    @Override
    public StatusImpl setOffset(Point2D offset) {
        List<MapNode> oldNodes = getNodes();
        List<MapNode> newNodes = oldNodes.stream()
                .map(node -> (MapNode) node.setLocation(gridPoint(
                        node.getLocation().getX() - offset.getX(),
                        node.getLocation().getY() - offset.getY()
                )))
                .collect(Collectors.toList());
        Map<MapNode, MapNode> nodeMap = IntStream.range(0, oldNodes.size())
                .boxed()
                .collect(Collectors.toMap(
                        oldNodes::get,
                        newNodes::get
                ));
        List<MapEdge> oldEdges = getEdges();
        List<MapEdge> newEdges = oldEdges.stream().flatMap(
                edge ->
                        getValue(nodeMap, edge.getBegin())
                                .flatMap(begin ->
                                        getValue(nodeMap, edge.getEnd()).map(end ->
                                                edge.setBegin(begin).setEnd(end)
                                        ))
                                .stream()
        ).collect(Collectors.toList());
        Map<MapEdge, MapEdge> edgeMap = IntStream.range(0, oldEdges.size())
                .boxed()
                .collect(Collectors.toMap(
                        oldEdges::get,
                        newEdges::get
                ));
        Topology topology = createTopology(newNodes, newEdges);

        // Creates new vehicles
        List<Vehicle> vehicles = this.vehicles.stream()
                .flatMap(v -> getValue(nodeMap, v.getDeparture())
                        .flatMap(departure -> getValue(nodeMap, v.getDestination())
                                .flatMap(destination ->
                                        v.getCurrentEdge()
                                                .map(edge -> getValue(edgeMap, edge)
                                                        .map(newEdge ->
                                                                v.setDeparture((SiteNode) departure)
                                                                        .setDestination((SiteNode) destination)
                                                                        .setCurrentEdge(newEdge)))
                                                .orElseGet(() -> Optional.of(v))
                                ))
                        .stream())
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = computeVehicleByEdges(vehicles);
        Map<Vehicle, Vehicle> nextVehicles = computeNextVehicleMap(vehiclesByEdge);
        Map<MapEdge, Double> edgeTransitTimes = this.edgeTransitTimes.entrySet().stream()
                .flatMap(entry -> getValue(edgeMap, entry.getKey())
                        .map(newEdge -> entry(newEdge, entry.getValue()))
                        .stream())
                .collect(toMap());

        return new StatusImpl(time, topology, vehicles, speedLimit, frequency,
                pathCdf, vehiclesByEdge, nextVehicles, edgeTransitTimes, null)
                .createPath();
    }

    @Override
    public StatusImpl setWeights(double[][] weights) {
        assert weights.length == getSites().size();
        return new StatusImpl(time, topology, vehicles, speedLimit, frequency,
                toCdf(weights), vehiclesByEdge, nextVehicles, edgeTransitTimes,
                edgeByPath);
    }

    /**
     * The movement of a vehicle in a time interval with the distance traveled, the time elapsed
     * and the indicator of end of edge reached
     */
    public static class VehicleMovement {
        private final Vehicle vehicle;
        private final double ds;
        private final double dt;
        private final boolean atEdgeEnd;

        public VehicleMovement(Vehicle vehicle, double ds, double dt, boolean atEdgeEnd) {
            this.vehicle = vehicle;
            this.ds = ds;
            this.dt = dt;
            this.atEdgeEnd = atEdgeEnd;
        }

        public double getDs() {
            return ds;
        }

        public double getDt() {
            return dt;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public boolean isAtEdgeEnd() {
            return atEdgeEnd;
        }

        @Override
        public String toString() {
            return "VehicleMovement{" +
                    "vehicle=" + vehicle +
                    ", ds=" + ds +
                    ", dt=" + dt +
                    ", atEdgeEnd=" + atEdgeEnd +
                    '}';
        }
    }
}
