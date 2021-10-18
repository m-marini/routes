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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.mmarini.Utils.*;
import static org.mmarini.routes.model2.Constants.VEHICLE_LENGTH;
import static org.mmarini.routes.model2.Constants.brakingMovement;

public class StatusImpl implements Status {

    /**
     * Returns the initial status
     *
     * @param topology  the topology
     * @param time      the current time
     * @param vehicles  the vehicle
     * @param frequency the frequency of new vehicles for every node
     */
    public static StatusImpl create(Topology topology, double time,
                                    List<Vehicle> vehicles, double frequency) {
        int n = topology.getSites().size();
        double[][] weights = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weights[i][j] = i == j ? 0 : 1;
            }
        }
        return create(topology, time, vehicles, frequency, weights);
    }

    /**
     * Returns the initial status
     *
     * @param topology  the topology
     * @param time      the current time
     * @param vehicles  the vehicle
     * @param frequency the frequency of new vehicles for every node
     * @param weights   the path weights
     */
    public static StatusImpl create(Topology topology, double time,
                                    List<Vehicle> vehicles, double frequency,
                                    double[][] weights
    ) {
        @SuppressWarnings("OptionalGetWithoutIsPresent") Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge = vehicles.stream()
                .filter(v -> v.getCurrentEdge().isPresent())
                .collect(Collectors.groupingBy(
                        v -> v.getCurrentEdge().get()))
                .entrySet().stream()
                .peek(entry -> entry.getValue()
                        .sort(Comparator.comparingDouble(
                                Vehicle::getDistance)))
                .map(entry -> entry(entry.getKey(), new LinkedList<>(entry.getValue())))
                .collect(toMap());
        Map<Vehicle, Vehicle> nextVehicles = vehiclesByEdge.entrySet().stream()
                .flatMap(entry -> {
                    List<Vehicle> vehicles1 = entry.getValue();
                    return IntStream.range(0, vehicles1.size() - 1).mapToObj(i ->
                            entry(vehicles1.get(i), vehicles1.get(i + 1))
                    );
                })
                .collect(toMap());
        Map<MapEdge, Double> edgeTransitTimes = topology.getEdges().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        MapEdge::getTransitTime
                ));
        int n = weights.length;
        double[][] pathCdf = new double[n][];
        for (int i = 0; i < n; i++) {
            pathCdf[i] = cumulative(weights[i]);
        }

        return new StatusImpl(time, topology, new ArrayList<>(vehicles),
                frequency, pathCdf, nextVehicles, edgeTransitTimes, vehiclesByEdge, null)
                .createPath();
    }
    private final double time;
    private final Topology topology;
    private final List<Vehicle> vehicles;
    private final double frequency;
    private final double[][] pathCdf;
    private final Map<Vehicle, Vehicle> nextVehicles;
    private final Map<MapEdge, Double> edgeTransitTimes;
    private final Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge;
    private Map<Path, MapEdge> edgeByPath;
    /**
     * @param time             the current time
     * @param topology         the topology
     * @param vehicles         the vehicle list
     * @param frequency        the frequency of new vehicles for every node
     * @param pathCdf          the cumulative probability of path from site to site
     * @param nextVehicles     the map between a vehicle and the next
     * @param edgeTransitTimes the effective edge transit time
     * @param vehiclesByEdge   the vehicles by edge
     * @param edgeByPath       the edge by path
     */
    protected StatusImpl(double time,
                         Topology topology,
                         List<Vehicle> vehicles,
                         double frequency,
                         double[][] pathCdf,
                         Map<Vehicle, Vehicle> nextVehicles,
                         Map<MapEdge, Double> edgeTransitTimes,
                         Map<MapEdge, LinkedList<Vehicle>> vehiclesByEdge,
                         Map<Path, MapEdge> edgeByPath) {
        this.frequency = frequency;
        this.pathCdf = pathCdf;
        assert vehicles != null;
        assert nextVehicles != null;
        assert edgeTransitTimes != null;
        assert edgeTransitTimes.size() == topology.getEdges().size();
        this.time = time;
        this.topology = topology;
        this.vehiclesByEdge = vehiclesByEdge;
        this.vehicles = vehicles;
        this.nextVehicles = nextVehicles;
        this.edgeTransitTimes = edgeTransitTimes;
        this.edgeByPath = edgeByPath;
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
            double elapsedDt = getFirstExitingVehicle(remainingDt).map(mv -> {
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
        Map<MapEdge, LinkedList<Vehicle>> newVehiclesByEdge = vehiclesByEdge.entrySet().stream()
                .map(entry -> {
                    LinkedList<Vehicle> newList = new LinkedList<>(entry.getValue());
                    return entry(entry.getKey(), newList);
                })
                .collect(toMap());
        return new StatusImpl(time, topology, newVehicles,
                frequency, pathCdf, new HashMap<>(nextVehicles),
                edgeTransitTimes,
                newVehiclesByEdge,
                edgeByPath);
    }

    /**
     * Returns the status with the current best path from node to node
     */
    private StatusImpl createPath() {
        this.edgeByPath = Path.create(topology.getNodes(),
                topology.getEdges(),
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
        return IntStream.range(0, topology.getSites().size())
                // Generates the number of vehicle for each departure node
                .mapToObj(i -> new int[]{
                        i,
                        nextPoison(random, frequency * dt)})
                // Filter the departure node without new vehicles
                .filter(ary -> ary[1] > 0)
                .flatMap(ary -> {
                    // Generates the vehicles
                    int departure = ary[0];
                    SiteNode depSite = sites.get(departure);
                    return IntStream.range(0, ary[1])
                            .mapToObj(i -> Vehicle.create(depSite,
                                    sites.get(nextCdf(random, pathCdf[departure])),
                                    time)
                            );
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
            Optional.ofNullable(vehiclesByEdge.get(edge)).ifPresentOrElse(vehicles -> {
                if (!vehicles.isEmpty()) {
                    nextVehicles.put(vehicle, vehicles.getFirst());
                }
                // Adds to the edge vehicle list
                vehicles.addFirst(vehicle);
            }, () -> {
                // Adds to the edge vehicle list
                vehiclesByEdge.put(edge, new LinkedList<>(List.of(vehicle)));
            });
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
            LinkedList<Vehicle> edgeVehicles = vehiclesByEdge.get(edge);
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
    Optional<Double> getEdgeTransitTime(MapEdge edge) {
        return Optional.ofNullable(edgeTransitTimes.get(edge));
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
                        .filter(edge -> v.getDistance() < edge.getLength())
                        .isPresent());
        return computeVehicleMovements(lastVehicles, dt)
                .reduce((a, b) ->
                        a.getDt() <= b.getDt() ? a : b);
    }

    public double getFrequency() {
        return frequency;
    }

    /**
     * Returns the list of last vehicles
     */
    Stream<Vehicle> getLastVehicles() {
        return vehiclesByEdge.values().stream()
                .filter(list -> !list.isEmpty())
                .map(LinkedList::getLast);
    }

    /**
     * Returns the edge to go from a node to another node
     *
     * @param from the start node
     * @param to   the destination node
     */
    Optional<MapEdge> getNextEdge(MapNode from, MapNode to) {
        return Optional.ofNullable(edgeByPath.get(new Path(from, to)));
    }

    /**
     * Returns the next vehicle of a vehicle
     *
     * @param vehicle the vehicle
     */
    public Optional<Vehicle> getNextVehicle(Vehicle vehicle) {
        return Optional.ofNullable(nextVehicles.get(vehicle));
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

    /**
     * @param pathCdf the cumulative path distribution
     */
    StatusImpl setPathCdf(double[][] pathCdf) {
        return new StatusImpl(time, topology, vehicles, frequency, pathCdf, nextVehicles, edgeTransitTimes, vehiclesByEdge, edgeByPath);
    }

    @Override
    public List<SiteNode> getSites() {
        return topology.getSites();
    }

    public double getTime() {
        return time;
    }

    /**
     * Returns the status with time changed
     *
     * @param time the time
     */
    StatusImpl setTime(double time) {
        return new StatusImpl(time, topology, vehicles, frequency, pathCdf, nextVehicles, edgeTransitTimes, vehiclesByEdge, edgeByPath);
    }

    public Topology getTopology() {
        return topology;
    }

    /**
     * Returns the list of vehicles for an edge
     *
     * @param edge the edge
     */
    LinkedList<Vehicle> getVehicles(MapEdge edge) {
        return Optional.ofNullable(vehiclesByEdge.get(edge)).orElseGet(LinkedList::new);
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
                                .filter(edge ->
                                        vehicle.getDistance() < edge.getLength())
                                .isEmpty())
                .collect(Collectors.toList());
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
        return Optional.ofNullable(vehiclesByEdge.get(enteringEdge))
                .filter(list -> !list.isEmpty())
                .map(LinkedList::getFirst)
                .filter(v -> v.getDistance() <= VEHICLE_LENGTH)
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
                .filter(list -> !list.isEmpty())
                // gets the last vehicle
                .map(LinkedList::getLast)
                // filter the vehicle in exit proximity
                .filter(v ->
                        v.getCurrentEdge()
                                .filter(edge ->
                                        v.getDistance() >= edge.getLength() - edge.getSafetyDistance())
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
                                .filter(edge -> v.getDistance() < edge.getLength())
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
    public Status next(Random random, double dt) {
        // Computes the path
        StatusImpl next = copy();
        next.applyTimeInterval(random, dt);
        return next;
    }

    /**
     * @param edge       the edge
     * @param travelTime the travel time
     */
    StatusImpl setEdgeTravelTimes(MapEdge edge, double travelTime) {
        edgeTransitTimes.put(edge, travelTime);
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
