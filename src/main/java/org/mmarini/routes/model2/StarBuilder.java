package org.mmarini.routes.model2;

import java.awt.geom.Point2D;
import java.util.*;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.mmarini.routes.model2.Constants.DEFAULT_PRIORITY;
import static org.mmarini.routes.model2.Constants.computeSafetySpeed;

public class StarBuilder extends EdgeConnectionBuilder {

    private static final StarBuilder DEFAULT_BUILDER = new StarBuilder();

    static Optional<Point2D> centerOf(Collection<? extends MapNode> nodes) {
        OptionalDouble xCenter = nodes.stream()
                .map(MapNode::getLocation)
                .mapToDouble(Point2D::getX)
                .average();
        OptionalDouble yCenter = nodes.stream()
                .map(MapNode::getLocation)
                .mapToDouble(Point2D::getY)
                .average();
        return xCenter.stream()
                .boxed()
                .flatMap(x ->
                        yCenter.stream()
                                .mapToObj(y -> (Point2D) new Point2D.Double(round(x), round(y))))
                .findAny();
    }

    public static ConnectionBuilder create() {
        return DEFAULT_BUILDER;
    }

    static List<MapEdge> createEdges(List<? extends MapNode> sites, double maxSpeed) {
        return centerOf(sites).map(center -> {
            List<MapEdge> edges = new ArrayList<>();
            MapNode centerNode = CrossNode.createNode(center.getX(), center.getY());
            for (MapNode site : sites) {
                double speed = min(maxSpeed, computeSafetySpeed(site.getLocation().distance(center)));
                MapEdge edge = new MapEdge(centerNode, site, speed, DEFAULT_PRIORITY);
                edges.add(edge);
            }
            return edges;
        }).orElse(new ArrayList<>());
    }

    public StarBuilder() {
        super();
    }

    /**
     * Returns the edges of lattice
     */
    @Override
    protected List<MapEdge> createEdges() {
        return createEdges(sites, maxSpeed);
    }
}
