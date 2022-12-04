package org.mmarini.routes.model2;

import org.mmarini.Utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.mmarini.routes.model2.Constants.DEFAULT_PRIORITY;
import static org.mmarini.routes.model2.Constants.computeSafetySpeed;

public class StarBuilder extends EdgeConnectionBuilder {

    private static final StarBuilder DEFAULT_BUILDER = new StarBuilder();

    static Point2D centerOf(List<? extends MapNode> nodes, double[] weights) {
        double tot = Arrays.stream(weights).sum();
        double xCenter = Utils.zipWithIndex(nodes)
                .mapToDouble(t -> t._2.getLocation().getX() * weights[t._1]
                )
                .sum() / tot;
        double yCenter = Utils.zipWithIndex(nodes)
                .mapToDouble(t -> t._2.getLocation().getY() * weights[t._1]
                )
                .sum() / tot;
        return new Point2D.Double(round(xCenter), round(yCenter));
    }

    public static ConnectionBuilder create() {
        return DEFAULT_BUILDER;
    }

    static List<MapEdge> createEdges(List<? extends MapNode> sites, double[] weights, double maxSpeed) {
        Point2D center = centerOf(sites, weights);
        List<MapEdge> edges = new ArrayList<>();
        MapNode centerNode = CrossNode.createNode(center.getX(), center.getY());
        for (MapNode site : sites) {
            double speed = min(maxSpeed, computeSafetySpeed(site.getLocation().distance(center)));
            MapEdge edge = new MapEdge(centerNode, site, speed, DEFAULT_PRIORITY);
            edges.add(edge);
        }
        return edges;
    }

    static double[] createWeights(TrafficEngine engine) {
        List<SiteNode> nodes = engine.getTopology().getSites();
        DoubleMatrix<SiteNode> pf = engine.getPathFrequencies();
        int n = nodes.size();
        double[] weights = new double[n];
        for (int i = 0; i < nodes.size(); i++) {
            SiteNode from = nodes.get(i);
            double w = 0;
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    w += pf.getValue(from, nodes.get(j)).orElse(0);
                }
            }
            weights[i] = w;
        }
        return weights;
    }

    private double[] weights;

    public StarBuilder() {
        super();
    }

    @Override
    public Topology build(TrafficEngine engine) {
        weights = createWeights(engine);
        return super.build(engine);
    }

    /**
     * Returns the edges of lattice
     */
    @Override
    protected List<MapEdge> createEdges() {
        return createEdges(sites, weights, maxSpeed);
    }
}
