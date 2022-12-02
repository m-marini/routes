package org.mmarini.routes.model2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Math.min;
import static java.lang.Math.signum;
import static org.mmarini.routes.model2.Algebra.*;
import static org.mmarini.routes.model2.Constants.DEFAULT_PRIORITY;
import static org.mmarini.routes.model2.Constants.computeSafetySpeed;

public class BypassesBuilder extends EdgeConnectionBuilder {

    private static final BypassesBuilder DEFAULT_BUILDER = new BypassesBuilder();

    private static double angleX(Point2D a, Point2D b) {
        Point2D c = sub(b, a);
        return c.getX() / length(c);
    }

    public static ConnectionBuilder create() {
        return DEFAULT_BUILDER;
    }

    static List<MapEdge> createBypasses(List<? extends MapNode> sites, double maxSpeed) {
        List<MapEdge> edges = new ArrayList<>();
        List<MapNode> unconnected = new ArrayList<>(sites);
        while (!unconnected.isEmpty()) {
            List<MapNode> ring = jarvisMarch(unconnected);
            if (ring.size() > 1) {
                for (int i = 0; i < ring.size() - 1; i++) {
                    MapNode from = ring.get(i);
                    MapNode to = ring.get(i + 1);
                    double speed = min(maxSpeed, computeSafetySpeed(from.getLocation().distance(to.getLocation())));
                    MapEdge edge = new MapEdge(from, to, speed, DEFAULT_PRIORITY);
                    edges.add(edge);
                }
                if (ring.size() > 2) {
                    MapNode from = ring.get(ring.size() - 1);
                    MapNode to = ring.get(0);
                    double speed = min(maxSpeed, computeSafetySpeed(from.getLocation().distance(to.getLocation())));
                    MapEdge edge = new MapEdge(from, to, speed, DEFAULT_PRIORITY);
                    edges.add(edge);
                }
            }
            unconnected.removeAll(ring);
        }
        return edges;
    }

    static int envelope(List<MapNode> result) {
        int n = result.size();
        return envelope(result.get(n - 3).getLocation(), result.get(n - 2).getLocation(), result.get(n - 1).getLocation());
    }

    static int envelope(Point2D a, Point2D b, Point2D c) {
        Point2D ba = sub(b, a);
        Point2D cb = sub(c, b);
        return (int) signum(vectProd(ba, cb));
    }

    private static Optional<MapNode> findMinAngleNegX(List<MapNode> nodes, MapNode seed) {
        Point2D seedPoint = seed.getLocation();
        double maxY = seedPoint.getY();
        return nodes.stream()
                .filter(Predicate.not(Predicate.isEqual(seed)))
                .filter(n -> n.getLocation().getY() <= maxY)
                .max(Comparator.comparingDouble(n ->
                        -angleX(seedPoint, n.getLocation())));
    }

    private static Optional<MapNode> findMinAngleX(List<MapNode> nodes, MapNode seed) {
        Point2D seedPoint = seed.getLocation();
        double minY = seedPoint.getY();
        return nodes.stream()
                .filter(Predicate.not(Predicate.isEqual(seed)))
                .filter(n -> n.getLocation().getY() >= minY)
                .max(Comparator.comparingDouble(n -> angleX(seedPoint, n.getLocation())));
    }

    static List<MapNode> jarvisMarch(List<MapNode> nodes) {
        List<MapNode> result = new ArrayList<>();
        List<MapNode> missing = new ArrayList<>(nodes);
        MapNode head = nodes.stream().min(Comparator.comparingDouble(n -> n.getLocation().getY())).orElseThrow();
        result.add(head);
        // Right chain
        MapNode seed = head;
        Optional<MapNode> next;
        while ((next = findMinAngleX(missing, seed)).isPresent()) {
            seed = next.get();
            result.add(seed);
            missing.remove(seed);
        }
        if (result.size() < nodes.size()) {
            // Left chain
            while ((next = findMinAngleNegX(missing, seed)).isPresent()) {
                seed = next.get();
                if (seed.equals(head)) {
                    break;
                }
                result.add(seed);
                missing.remove(seed);
            }
        }
        return result;
    }

    public BypassesBuilder() {
        super();
    }

    /**
     * Returns the edges of lattice
     */
    @Override
    protected List<MapEdge> createEdges() {
        List<MapEdge> edges = createBypasses(sites, maxSpeed);
        edges.addAll(StarBuilder.createEdges(sites, maxSpeed));
        return edges;
    }
}
