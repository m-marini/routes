package org.mmarini.routes.model2;

import java.util.List;

public abstract class EdgeConnectionBuilder implements ConnectionBuilder {
    protected List<SiteNode> sites;
    protected double maxSpeed;

    @Override
    public Topology build(TrafficEngine engine) {
        sites = engine.getTopology().getSites();
        maxSpeed = engine.getSpeedLimit();
        return Topology.createTopology(createEdges());
    }

    protected abstract List<MapEdge> createEdges();
}
