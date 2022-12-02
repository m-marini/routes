package org.mmarini.routes.model2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static org.mmarini.routes.model2.Constants.DEFAULT_PRIORITY;
import static org.mmarini.routes.model2.Constants.computeSafetySpeed;

public class ByDistanceBuilder extends EdgeConnectionBuilder {

    private static final ByDistanceBuilder DEFAULT_BUILDER = new ByDistanceBuilder();

    public static ConnectionBuilder create() {
        return DEFAULT_BUILDER;
    }

    static List<MapEdge> createEdges(List<? extends MapNode> sites, double maxSpeed) {
        List<MapEdge> allEdges = IntStream.range(0, sites.size())
                .boxed()
                .flatMap(fromIdx -> {
                    MapNode from = sites.get(fromIdx);
                    return IntStream.range(fromIdx + 1, sites.size())
                            .mapToObj(toIdx -> {
                                MapNode to = sites.get(toIdx);
                                double speed = min(maxSpeed, computeSafetySpeed(from.getLocation().distance(to.getLocation())));
                                return new MapEdge(
                                        from, to, speed, DEFAULT_PRIORITY);
                            });
                })
                .sorted(Comparator.comparingDouble(MapEdge::getLength))
                .collect(Collectors.toList());
        List<MapEdge> edges = new ArrayList<>();
        MapEdge seed = allEdges.get(0);
        edges.add(seed);
        Set<MapNode> connected = new HashSet<>();
        connected.add(seed.getBegin());
        connected.add(seed.getEnd());

        while (connected.size() != sites.size()) {
            MapEdge candidate = allEdges.stream()
                    // Filters for edge with one end connected and other end not connected
                    .filter(edge ->
                            (connected.contains(edge.getBegin()) && !connected.contains(edge.getEnd()))
                                    || (!connected.contains(edge.getBegin()) && connected.contains(edge.getEnd()))
                    )
                    .findFirst()
                    .orElseThrow();
            edges.add(candidate);
            connected.add(candidate.getBegin());
            connected.add(candidate.getEnd());
        }
        return edges;
    }

    public ByDistanceBuilder() {
        super();
    }

    @Override
    protected List<MapEdge> createEdges() {
        return createEdges(sites, maxSpeed);
    }
}
