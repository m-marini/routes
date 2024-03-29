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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.ceil;
import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Tuple2.toMap;
import static org.mmarini.Utils.*;
import static org.mmarini.routes.model2.Constants.*;
import static org.mmarini.routes.model2.Routes.computeRoutes;
import static org.mmarini.routes.model2.SiteNode.createSite;
import static org.mmarini.routes.model2.StatusImpl.createStatus;
import static org.mmarini.routes.model2.Topology.createTopology;

public class TrafficEngineImpl implements TrafficEngine {
    private static final Logger logger = LoggerFactory.getLogger(TrafficEngineImpl.class);

    /**
     * Returns a new transit time by topology change
     *
     * @param oldTransitTime the old transit times
     * @param newTopology    the new topology
     * @param oldTopology    the old topology
     * @param vehiclesByEdge the vehicle by edge in new topology
     */
    static TransitTimes computeNewTransitTime(TransitTimes oldTransitTime, Topology newTopology, Topology oldTopology, Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge) {
        Map<MapEdge, MapEdge> oldEdgeByNewEdge = newTopology.createEdgeMap(oldTopology);
        return TransitTimes.create(newTopology.getEdges().stream(), edge -> {
            // Get the list of vehicle in the new edge
            Optional<LinkedList<Vehicle>> value1 = getValue(vehiclesByEdge, edge);
            return value1
                    // filter if there is at least a vehicle
                    .filter(Predicate.not(LinkedList::isEmpty))
                    .flatMap(list -> getValue(oldEdgeByNewEdge, edge))
                    // map to old transit time
                    //.map(e -> oldTransitTime.getValue(e))
                    .map(oldTransitTime::getValue)
                    .orElseGet(edge::getTransitTime);
        });
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
                                    Tuple2.of(vehicles1.get(i), vehicles1.get(i + 1))
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
        return vehicles.parallelStream()
                .filter(v -> v.getCurrentEdge().isPresent())
                .collect(Collectors.groupingBy(
                        v -> v.getCurrentEdge().orElseThrow()))
                .entrySet().stream()
                .peek(entry -> entry.getValue()
                        .sort(Comparator.comparingDouble(
                                Vehicle::getDistance)))
                .map(mapValue(LinkedList::new))
                .collect(entriesToMap());
    }

    /**
     * Returns the initial status
     *
     * @param maxVehicles maximum number of vehicles
     * @param topology    the topology
     * @param time        the current time
     * @param vehicles    the vehicle
     * @param speedLimit  the speed limit
     * @param frequency   the frequency of new vehicles for every node
     * @param weights     the path weights
     */
    public static TrafficEngineImpl createEngine(int maxVehicles, Topology topology, double time,
                                                 List<Vehicle> vehicles,
                                                 double speedLimit,
                                                 double frequency,
                                                 double[][] weights
    ) {
        Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = computeVehicleByEdges(vehicles);
        Map<Vehicle, Vehicle> nextVehicles = computeNextVehicleMap(vehiclesByEdge);
        TransitTimes edgeTransitTimes = TransitTimes.create(topology.getEdges());

        return new TrafficEngineImpl(maxVehicles, time, topology, new ArrayList<>(vehicles),
                speedLimit, frequency, toCdf(weights), vehiclesByEdge, nextVehicles,
                edgeTransitTimes, null);
    }

    /**
     * Returns the initial status
     *
     * @param maxVehicles maximum number of vehicles
     * @param topology    the topology
     * @param time        the current time
     * @param vehicles    the vehicle
     * @param speedLimit  the speed limit
     * @param frequency   the frequency of new vehicles for every node
     */
    public static TrafficEngineImpl createEngine(int maxVehicles, Topology topology, double time,
                                                 List<Vehicle> vehicles, double speedLimit, double frequency) {
        int n = topology.getSites().size();
        double[][] weights = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weights[i][j] = i == j ? 0 : 1;
            }
        }
        return createEngine(maxVehicles, topology, time, new ArrayList<>(vehicles), speedLimit, frequency, weights);
    }

    /**
     * Returns a random status base on a given map profile
     *
     * @param maxVehicles maximum number of vehicles
     * @param profile     the map profile
     * @param speedLimits the speed limit
     */
    public static TrafficEngineImpl createRandom(int maxVehicles, Random random, MapProfile profile, double speedLimits) {
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
                                round((2 * (loc[0] - x0) / dx - 1) * w),
                                round((2 * (loc[1] - y0) / dy - 1) * h)))
                .collect(Collectors.toList());
        List<MapNode> nodes = sites.stream().map(x -> (MapNode) x).collect(Collectors.toList());
        double[][] pathsCdf = toCdf(createRandomWeights(n, profile.getMinWeight(), random));
        Topology topology = createTopology(nodes, List.of());
        return new TrafficEngineImpl(maxVehicles, 0,
                topology,
                new ArrayList<>(),
                speedLimits,
                profile.getFrequency(),
                pathsCdf,
                new HashMap<>(),
                new HashMap<>(),
                TransitTimes.create(topology.getEdges()),
                null);
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

    static Stream<Vehicle> replaceEdge(Vehicle vehicle, Map<MapEdge, MapEdge> newEdgeMap) {
        return vehicle.getCurrentEdge().
                map(oldEdge ->
                        // replace the edge or empty if not exits
                        getValue(newEdgeMap, oldEdge)
                                .map(vehicle::setCurrentEdge)
                                .stream()
                ).orElseGet(() -> Stream.of(vehicle));
    }

    /**
     * Returns the stream ov the new vehicle with changed edge
     *
     * @param newEdgeMap the new edge by old edge
     */
    static Function<Vehicle, Stream<Vehicle>> replaceEdgeFun(Map<MapEdge, MapEdge> newEdgeMap) {
        return vehicle -> replaceEdge(vehicle, newEdgeMap);
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

    private final int maxVehicles;
    private final Topology topology;
    private final List<Vehicle> vehicles;
    private final double frequency;
    private final double speedLimit;
    private final double[][] pathCdf;
    private final Map<Vehicle, Vehicle> nextVehicles;
    private final TransitTimes transitTimeByEdge;
    private final Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge;
    private double time;
    private Map<Tuple2<MapNode, MapNode>, MapEdge> edgeByPath;

    /**
     * @param maxVehicles       maximum number of vehicles
     * @param time              the current time
     * @param topology          the topology
     * @param vehicles          the vehicle list
     * @param speedLimit        the speed limit
     * @param frequency         the frequency of new vehicles for every node
     * @param pathCdf           the cumulative probability of path from site to site
     * @param vehiclesByEdge    the vehicles by edge
     * @param nextVehicles      the map between a vehicle and the next
     * @param transitTimeByEdge the effective edge transit time
     * @param edgeByPath        the edge by path
     */
    protected TrafficEngineImpl(int maxVehicles, double time,
                                Topology topology,
                                List<Vehicle> vehicles,
                                double speedLimit,
                                double frequency,
                                double[][] pathCdf,
                                Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge, Map<Vehicle, Vehicle> nextVehicles,
                                TransitTimes transitTimeByEdge,
                                Map<Tuple2<MapNode, MapNode>, MapEdge> edgeByPath) {
        this.topology = requireNonNull(topology);
        this.vehicles = requireNonNull(vehicles);
        this.nextVehicles = requireNonNull(nextVehicles);
        this.transitTimeByEdge = requireNonNull(transitTimeByEdge);
        this.vehiclesByEdge = requireNonNull(vehiclesByEdge);
        this.pathCdf = requireNonNull(pathCdf);
        assert pathCdf.length == topology.getSites().size();
        this.maxVehicles = maxVehicles;
        this.frequency = frequency;
        this.speedLimit = speedLimit;
        this.time = time;
        this.edgeByPath = edgeByPath;
    }

    @Override
    public TrafficEngineImpl addEdge(MapEdge edge) {
        Topology newTopology = topology.addEdge(edge);
        TransitTimes newEdgeTransitTime = computeNewTransitTime(
                transitTimeByEdge, newTopology, topology, vehiclesByEdge);
        return new TrafficEngineImpl(maxVehicles, time, newTopology, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles,
                newEdgeTransitTime, null);
    }

    @Override
    public TrafficEngineImpl addModule(MapModule mapModule, Point2D location, Point2D direction, double epsilon) {
        Topology topology = this.topology.addModule(mapModule, location, direction, epsilon);
        // TODO copy the edge transit time and add new edge modules
        TransitTimes edgeTransitTimes = TransitTimes.create(topology.getEdges());
        return new TrafficEngineImpl(maxVehicles, time, topology, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles, edgeTransitTimes, null);
    }

    /**
     * Returns the time interval to the changed status
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    double applyTimeInterval(Random random, double dt) {
        if (dt > 0) {
            // Finds the first exiting vehicle and gets the time interval for that vehicles
            double realDt = findFirstExitingVehicle(dt)
                    .map(VehicleMovement::getDt)
                    .orElse(dt);
            // snap the time interval
            realDt = ceil(realDt / TIME_STEP) * TIME_STEP;
            // moves all the vehicles for the time interval
            moveVehicles(realDt);
            // Generates new vehicles
            vehicles.addAll(createVehicles(random, realDt));
            // Dispatches the waiting vehicles for the travel edge
            handleWaitingVehicles();
            // Updates the simulation time
            time += realDt;
            return realDt;
        }
        return dt;
    }

    @Override
    public StatusImpl buildStatus() {
        List<Vehicle> vehicles = this.vehicles.stream()
                .map(Vehicle::copy)
                .collect(Collectors.toList());
        return createStatus(maxVehicles, speedLimit, frequency, time,
                topology, vehicles, transitTimeByEdge,
                getWeightMatrix().getValues());
    }

    @Override
    public TrafficEngineImpl changeEdge(MapEdge oldEdge, MapEdge newEdge) {
        Topology newTopology = topology.removeEdge(oldEdge).addEdge(newEdge);
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
        TransitTimes edgeTransitTimes = computeNewTransitTime(this.transitTimeByEdge, newTopology, topology, vehiclesByEdge);
        return new TrafficEngineImpl(maxVehicles, time, newTopology, vehicles,
                speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles,
                edgeTransitTimes, null);
    }

    @Override
    public TrafficEngineImpl changeNode(MapNode node) {
        Topology newTop = topology.changeNode(node);
        // Creates edge mapping between new and old
        Map<MapEdge, MapEdge> newEdgeMap = topology.createEdgeMap(newTop);

        // remove vehicles with departure or destination equal to old node
        Stream<Vehicle> vehicleStream = node instanceof SiteNode
                ? vehicles.stream().filter(v -> !v.isSiteInPath(node))
                : vehicles.stream();

        // Substitutes the vehicle data
        List<Vehicle> newVehicles = vehicleStream
                // duplicate vehicles
                .map(Vehicle::copy)
                .flatMap(replaceEdgeFun(newEdgeMap))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = computeVehicleByEdges(newVehicles);
        Map<Vehicle, Vehicle> newNextVehicles = computeNextVehicleMap(newVehiclesByEdge);
        TransitTimes newEdgeTransitTime = computeNewTransitTime(transitTimeByEdge, newTop, topology, newVehiclesByEdge);
        // Create the new weights
        Map<SiteNode, SiteNode> oldSiteMap = newTop.createSiteMap(this.topology);
        double[][] weights = getWeightMatrix().map(newTop.getSites(), site -> getValue(oldSiteMap, site)).getValues();
        return new TrafficEngineImpl(maxVehicles, time, newTop, newVehicles, speedLimit, frequency,
                toCdf(weights), newVehiclesByEdge, newNextVehicles, newEdgeTransitTime,
                null);
    }

    /**
     * Returns the vehicle movement for the time interval
     * if it is running in an edge.
     * The movement is bound to the speed limit of edge,
     * the safety distance to the next vehicle if any
     * and the edge length.
     * The time is the effective time of movement <= dt
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
            double ds = findNextVehicle(vehicle).map(next -> {
                // Gets the distance to next vehicle
                final double distToNext = next.getDistance() - dist;
                // Compute the safety distance
                final double edgeSafetyDistance = edge.getSafetyDistance();
                if (maxMovement + edgeSafetyDistance > distToNext) {
                    /*
                      Vehicle is moving too close the next vehicle
                      Computes the brake movement for the vehicle
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
     * Returns the status with the current best path from node to node
     */
    private void createPath() {
        updateTransitTime();
        this.edgeByPath = computeRoutes(topology.getEdges(), transitTimeByEdge);
    }

    /**
     * Returns the status with new vehicles
     *
     * @param random the random generator
     * @param dt     the time interval
     */
    List<Vehicle> createVehicles(Random random, double dt) {
        List<SiteNode> sites = topology.getSites();
        return vehicles.size() > maxVehicles
                ? List.of()
                : IntStream.range(0, sites.size())
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
                LinkedList<Vehicle> vehicles = findVehicles(edge);
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
            LinkedList<Vehicle> edgeVehicles = findVehicles(edge);
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
    double findEdgeTransitTime(MapEdge edge) {
        return transitTimeByEdge.getValue(edge);
    }

    /**
     * Returns the last vehicle movement with lower time interval within a given time interval
     *
     * @param dt the time interval
     */
    Optional<VehicleMovement> findFirstExitingVehicle(double dt) {
        Stream<Vehicle> lastVehicles = getLastVehicles()
                // Filter the vehicles that is in the current edge (position less than the length of edge
                .filter(v -> v.getCurrentEdge()
                        .map(MapEdge::getLength)
                        .filter(length -> v.getDistance() < length)
                        .isPresent());
        // Moves all the last vehicles
        return computeVehicleMovements(lastVehicles, dt)
                .reduce((a, b) ->
                        a.getDt() <= b.getDt() ? a : b);
    }

    /**
     * Returns the edge to go from a node to another node
     *
     * @param from the start node
     * @param to   the destination node
     */
    Optional<MapEdge> findNextEdge(MapNode from, MapNode to) {
        return getValue(getEdgeByPath(), Tuple2.of(from, to));
    }

    /**
     * Returns the next vehicle of a vehicle
     *
     * @param vehicle the vehicle
     */
    public Optional<Vehicle> findNextVehicle(Vehicle vehicle) {
        return getValue(nextVehicles, vehicle);
    }

    /**
     * Returns the list of vehicles for an edge
     *
     * @param edge the edge
     */
    LinkedList<Vehicle> findVehicles(MapEdge edge) {
        return vehiclesByEdge.getOrDefault(edge, new LinkedList<>());
    }

    /**
     * Returns all the vehicles
     */
    List<Vehicle> findVehicles() {
        return vehicles;
    }

    @Override
    public TrafficEngine generateConnections(ConnectionBuilder builder) {
        Topology newTop = builder.build(this);
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = new HashMap<>();
        TransitTimes newTransitTimeByEdge = computeNewTransitTime(this.transitTimeByEdge, newTop, topology, newVehiclesByEdge);
        return new TrafficEngineImpl(maxVehicles, time,
                newTop, new ArrayList<>(),
                speedLimit, frequency,
                pathCdf,
                newVehiclesByEdge,
                new HashMap<>(),
                newTransitTimeByEdge, null);
    }

    private Map<Tuple2<MapNode, MapNode>, MapEdge> getEdgeByPath() {
        if (edgeByPath == null) {
            createPath();
        }
        return edgeByPath;
    }

    List<MapEdge> getEdges() {
        return topology.getEdges();
    }

    double getFrequency() {
        return frequency;
    }

    @Override
    public TrafficEngineImpl setFrequency(double frequency) {
        return new TrafficEngineImpl(maxVehicles, time, topology, vehicles, speedLimit, frequency, pathCdf, vehiclesByEdge, nextVehicles, transitTimeByEdge, edgeByPath);
    }

    /**
     * Returns the list of last vehicles
     */
    Stream<Vehicle> getLastVehicles() {
        return vehiclesByEdge.values().stream()
                .filter(Predicate.not(List::isEmpty))
                .map(LinkedList::getLast);
    }

    List<MapNode> getNodes() {
        return topology.getNodes();
    }

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

    List<SiteNode> getSites() {
        return topology.getSites();
    }

    @Override
    public double getSpeedLimit() {
        return speedLimit;
    }

    @Override
    public TrafficEngineImpl setSpeedLimit(double speedLimit) {
        return new TrafficEngineImpl(maxVehicles, time, topology, vehicles, speedLimit, frequency,
                pathCdf, vehiclesByEdge, nextVehicles, transitTimeByEdge, edgeByPath);
    }

    @Override
    public Topology getTopology() {
        return topology;
    }

    @Override
    public TransitTimes getTransitTimeByEdge() {
        return transitTimeByEdge;
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

    public DoubleMatrix<SiteNode> getWeightMatrix() {
        return new DoubleMatrix<>(getSites(), toWeight(pathCdf));
    }

    void handleVehicleAtSite(Vehicle vehicle) {
        // vehicle on the site
        // Extracts next edge
        Optional<MapEdge> nextEdgeOpt = vehicle.isReturning()
                ? findNextEdge(vehicle.getDestination(), vehicle.getDeparture())
                : findNextEdge(vehicle.getDeparture(), vehicle.getDestination());
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

    void handleWaitingVehicleOnEdge(Vehicle vehicle, MapEdge edge) {
        if (edge.getEnd().equals(vehicle.getCurrentDestination())) {
            // Vehicle at destination
            exitVehicleFromEdge(vehicle);
            if (vehicle.isReturning()) {
                // Vehicle completed the trip
                vehicles.remove(vehicle);
            } else {
                // Vehicle at destination
                vehicle.setReturning(true).setCurrentEdge(null);
                handleVehicleAtSite(vehicle);
            }
        } else {
            // vehicle on the edge
            // Extracts next edge
            findNextEdge(edge.getEnd(), vehicle.getCurrentDestination())
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

    /**
     * Handles the waiting vehicle moving them to the next edge if possible
     */
    void handleWaitingVehicles() {
        // Partition the waiting vehicles at edge or at site
        Map<Boolean, List<Vehicle>> map = getWaitingVehicles().stream()
                .collect(Collectors.groupingBy(v -> v.getCurrentEdge().isPresent()));
        // Sort vehicles vehicle on edge by age and handle
        for (Vehicle vehicle : getValue(map, true)
                .map(vehicles -> {
                    vehicles.sort(Comparator.comparingDouble(Vehicle::getStartWaitingTime));
                    return vehicles;
                })
                .orElse(List.of())) {
            vehicle.getCurrentEdge().ifPresent(edge ->
                    handleWaitingVehicleOnEdge(vehicle, edge)
            );
        }
        // vehicle on the site
        // Extracts next edge
        for (Vehicle vehicle : getValue(map, false)
                .map(vehicles -> {
                    vehicles.sort(Comparator.comparingDouble(Vehicle::getEdgeEntryTime));
                    return vehicles;
                })
                .orElse(List.of())) {
            handleVehicleAtSite(vehicle);
        }
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
        boolean edgeAvailable = isEdgeAvailable(enteringEdge);
        boolean incomesFree = isIncomesFree(exitingEdge);
        return edgeAvailable &&
                incomesFree;
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
                .filter(distance ->
                        distance <= VEHICLE_LENGTH)
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
                .map(this::findVehicles)
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
        // Creates the stream of vehicles not at end of traveling edge
        Stream<Vehicle> stream = findVehicles().stream()
                .filter(v ->
                        v.getCurrentEdge()
                                .map(MapEdge::getLength)
                                .filter(length -> v.getDistance() < length)
                                .isPresent()
                );
        // Computes the movement of vehicle in edges
        // and for each update the vehicle and edge status
        computeVehicleMovements(stream, dt)
                .forEach(vm -> {
                    Vehicle vehicle = vm.getVehicle();
                    double distance = vehicle.getDistance();
                    double distance1 = distance + vm.getDs();
                    vehicle.setDistance(distance1);
                    // Update the instant of
                    vehicle.getCurrentEdge()
                            .map(MapEdge::getLength)
                            .filter(length -> distance < length && distance1 >= length)
                            .ifPresent(l -> {
                                // Updates the instants of start of waiting status
                                // They are used to sort the dispatching of waiting vehicles
                                vehicle.setStartWaitingTime(time + vm.getDt());
                            });
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
    public Tuple2<TrafficEngine, Double> next(Random random, double dt) {
        // Computes the new status after elapsed time interval
        double resultDt = applyTimeInterval(random, dt);
        return Tuple2.of(this, resultDt);
    }

    public TrafficEngineImpl optimize() {
        Topology newTopology = this.topology.optimize(speedLimit);
        Map<MapEdge, MapEdge> newEdgeMap = this.topology.createEdgeMap(newTopology);
        List<Vehicle> vehicles = this.vehicles.stream()
                // Change the edge
                .map(v -> v.getCurrentEdge()
                        .flatMap(getValue(newEdgeMap))
                        .map(v::setCurrentEdge)
                        .orElse(v))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = computeVehicleByEdges(vehicles);
        Map<Vehicle, Vehicle> nextVehicles = computeNextVehicleMap(vehiclesByEdge);
        TransitTimes edgeTransitTimes = computeNewTransitTime(this.transitTimeByEdge, newTopology, this.topology, vehiclesByEdge);

        return new TrafficEngineImpl(maxVehicles, time, newTopology, vehicles, speedLimit, frequency, pathCdf,
                vehiclesByEdge, nextVehicles, edgeTransitTimes, null);
    }

    @Override
    public TrafficEngineImpl optimizeNodes() {
        return optimize();
    }

    @Override
    public TrafficEngineImpl optimizeSpeed(double speedLimit) {
        return setSpeedLimit(speedLimit).optimize();
    }

    @Override
    public TrafficEngineImpl randomizeWeights(Random random, double minWeight) {
        double[][] weights = createRandomWeights(topology.getSites().size(), minWeight, random);
        return new TrafficEngineImpl(maxVehicles, time, topology, vehicles, speedLimit, frequency, toCdf(weights),
                vehiclesByEdge, nextVehicles, transitTimeByEdge, edgeByPath);
    }

    @Override
    public TrafficEngineImpl removeEdge(MapEdge edge) {
        Topology newTopology = this.topology.removeEdge(edge);
        List<Vehicle> newVehicles = vehicles.stream()
                .filter(vehicle -> !vehicle.isTransitingEdge(edge))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehicleByEdge = computeVehicleByEdges(newVehicles);
        Map<Vehicle, Vehicle> newNextVehicle = computeNextVehicleMap(newVehicleByEdge);
        TransitTimes newEdgeTransitTime = computeNewTransitTime(transitTimeByEdge, newTopology, this.topology, newVehicleByEdge);
        return new TrafficEngineImpl(maxVehicles, time, newTopology, newVehicles, speedLimit, frequency, pathCdf,
                newVehicleByEdge, newNextVehicle, newEdgeTransitTime, null);
    }

    @Override
    public TrafficEngineImpl removeNode(MapNode node) {
        Topology newTopology = this.topology.removeNode(node);

        List<Vehicle> newVehicles = vehicles.stream()
                .filter(vehicleNotInRemovedNode(node))
                .collect(Collectors.toList());
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = computeVehicleByEdges(newVehicles);
        Map<Vehicle, Vehicle> newNextVehicles = computeNextVehicleMap(newVehiclesByEdge);
        TransitTimes newEdgeTransitTimes = computeNewTransitTime(transitTimeByEdge, newTopology, topology, newVehiclesByEdge);
        Map<SiteNode, SiteNode> olsSiteMap = newTopology.createSiteMap(this.topology);
        double[][] weights = getWeightMatrix().map(newTopology.getSites(), site -> getValue(olsSiteMap, site)).getValues();
        return new TrafficEngineImpl(maxVehicles, time, newTopology, newVehicles, speedLimit, frequency, toCdf(weights),
                newVehiclesByEdge, newNextVehicles, newEdgeTransitTimes, null);
    }

    /**
     * Returns this traffic engine with changed transit time
     *
     * @param edge       the edge
     * @param travelTime the travel time
     */
    TrafficEngineImpl setEdgeTravelTimes(MapEdge edge, double travelTime) {
        transitTimeByEdge.setValue(edge, travelTime);
        return this;
    }

    @Override
    public TrafficEngineImpl setOffset(Point2D offset) {
        List<MapNode> oldNodes = getNodes();
        List<MapNode> newNodes = oldNodes.stream()
                .map(node -> node.setLocation(gridPoint(
                        node.getLocation().getX() - offset.getX(),
                        node.getLocation().getY() - offset.getY()
                )))
                .collect(Collectors.toList());
        Map<MapNode, MapNode> nodeMap = IntStream.range(0, oldNodes.size())
                .collect(
                        HashMap::new,
                        (map, i) -> map.put(oldNodes.get(i), newNodes.get(i)),
                        HashMap::putAll
                );
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
                .collect(HashMap::new,
                        (map, i) -> map.put(oldEdges.get(i), newEdges.get(i)),
                        HashMap::putAll
                );
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
        TransitTimes newEdgeTransitTimes = transitTimeByEdge.copy().mapKeys(edgeMap::get);

        return new TrafficEngineImpl(maxVehicles, time, topology, vehicles, speedLimit, frequency,
                pathCdf, vehiclesByEdge, nextVehicles, newEdgeTransitTimes, edgeByPath);
    }

    @Override
    public TrafficEngineImpl setWeights(double[][] weights) {
        assert weights.length == getSites().size();
        return new TrafficEngineImpl(maxVehicles, time, topology, vehicles, speedLimit, frequency,
                toCdf(weights), vehiclesByEdge, nextVehicles, transitTimeByEdge,
                edgeByPath);
    }

    @Override
    public TrafficEngineImpl updateRoutes(Map<Tuple2<MapNode, MapNode>, MapEdge> routes) {
        // Validate
        List<MapNode> nodes = topology.getNodes();
        List<MapEdge> edges = topology.getEdges();
        for (Map.Entry<Tuple2<MapNode, MapNode>, MapEdge> entry : routes.entrySet()) {
            MapNode from = entry.getKey()._1;
            MapNode to = entry.getKey()._2;
            MapEdge edge = entry.getValue();
            if (!(nodes.contains(from) && nodes.contains(to) && edges.contains(edge))) {
                logger.error("Wrong routes");
                return this;
            }
        }
        this.edgeByPath = routes;
        return this;
    }

    /**
     * Returns this traffic engine with updated transit time
     */
    TrafficEngineImpl updateTransitTime() {
        transitTimeByEdge.update(time, getLastVehicles());
        return this;
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
