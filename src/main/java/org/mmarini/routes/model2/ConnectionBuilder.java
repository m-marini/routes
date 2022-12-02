package org.mmarini.routes.model2;

public interface ConnectionBuilder {
    /**
     * Returns the list of connections
     */
    Topology build(TrafficEngine engine);
}
