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

package org.mmarini.routes.model2.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;
import org.mmarini.routes.model2.CrossNode;
import org.mmarini.routes.model2.SiteNode;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mmarini.Utils.join;
import static org.mmarini.Utils.toList;
import static org.mmarini.yaml.schema.Validator.*;

public class CrossValidators {

    private static final Validator UNIQUE_CROSS_NODES = locator -> root -> {
        JsonNode node = locator.getNode(root);
        Map<String, ? extends SiteNode> siteByName = Parsers.sites(node);
        List<String> names = toList(node.fieldNames());
        Optional<Tuple2<String, String>> dup = unorderedStream(names)
                .filter(t -> {
                    Point2D li = siteByName.get(t._1).getLocation();
                    Point2D lj = siteByName.get(t._2).getLocation();
                    return li.equals(lj);
                })
                .findFirst();
        assertFor(dup.isEmpty(),
                () -> dup.map(Tuple2::getV2).map(locator::path).orElseThrow(),
                () -> "must not have the same location of %s",
                () -> new Object[]{dup.map(Tuple2::getV1).map(locator::path).orElseThrow()}
        );
    };

    private static final Validator UNIQUE_SITES = locator -> root -> {
        JsonNode node = locator.getNode(root);
        Map<String, ? extends CrossNode> nodeByName = Parsers.nodes(node);
        List<String> names = toList(node.fieldNames());
        Optional<Tuple2<String, String>> dup = unorderedStream(names)
                .filter(t -> {
                    Point2D li = nodeByName.get(t._1).getLocation();
                    Point2D lj = nodeByName.get(t._2).getLocation();
                    return li.equals(lj);
                })
                .findFirst();
        assertFor(dup.isEmpty(),
                () -> dup.map(Tuple2::getV2).map(locator::path).orElseThrow(),
                () -> "must not have the same location of %s",
                () -> new Object[]{dup.map(Tuple2::getV1).map(locator::path).orElseThrow()}
        );
    };

    private static final Validator UNIQUE_NODES_KEYS = locator -> root -> {
        Locator sitesLocator = locator.path("sites");
        Locator nodesLocator = locator.path("nodes");
        List<String> siteNames = toList(sitesLocator.getNode(root).fieldNames());
        List<String> nodeNames = toList(nodesLocator.getNode(root).fieldNames());
        siteNames.retainAll(nodeNames);
        assertFor(siteNames.isEmpty(),
                () -> nodesLocator.path(siteNames.get(0)),
                () -> "must not have the same key of %s",
                () -> new Object[]{sitesLocator.path(siteNames.get(0))}
        );
    };

    private static final Validator UNIQUE_NODES = locator -> root -> {
        Locator sitesLocator = locator.path("sites");
        Locator nodesLocator = locator.path("nodes");
        List<String> siteNames = toList(sitesLocator.getNode(root).fieldNames());
        List<String> nodeNames = toList(nodesLocator.getNode(root).fieldNames());
        Map<String, ? extends SiteNode> siteByName = Parsers.sites(sitesLocator.getNode(root));
        Map<String, ? extends CrossNode> nodeByName = Parsers.nodes(nodesLocator.getNode(root));

        Optional<Tuple2<String, String>> dup = join(siteNames, nodeNames)
                .filter(t -> {
                    Point2D li = siteByName.get(t._1).getLocation();
                    Point2D lj = nodeByName.get(t._2).getLocation();
                    return li.equals(lj);
                })
                .findFirst();

        assertFor(dup.isEmpty(),
                () -> dup.map(t -> nodesLocator.path(t._2)).orElseThrow(),
                () -> "must not have the same location of %s",
                () -> new Object[]{dup.map(t -> sitesLocator.path(t._1)).orElseThrow()}
        );
    };

    private static final Validator EDGE_NODES = locator -> root -> {
        Locator start = locator.path("start");
        Locator end = locator.path("end");
        String value = start.getNode(root).asText();
        assertFor(!value.equals(end
                        .getNode(root).asText()),
                end,
                "must not be equal to %s (%s)",
                start,
                value
        );
    };

    private static final Validator EDGES = deferred((root, locator) -> {
        Locator routeLocator = locator.parent();
        List<String> nodes = toList(routeLocator.path("sites").getNode(root).fieldNames());
        nodes.addAll(toList(routeLocator.path("nodes").getNode(root).fieldNames()));
        return arrayItems(edge(nodes));
    });

    private static final Validator MODULE = allOf(
            property("nodes", UNIQUE_CROSS_NODES),
            UNIQUE_NODES,
            property("edges", EDGES)
    );

    private static final Validator PATH = locator -> root -> {
        Locator departure = locator.path("departure");
        Locator destination = locator.path("destination");
        String value = departure.getNode(root).asText();
        assertFor(!value.equals(destination
                        .getNode(root).asText()),
                destination,
                "must not be equal to %s (%s)",
                departure,
                value
        );
    };
    private static final Validator PATHS = deferred((root, locator) -> {
        Locator routeLocator = locator.parent();
        List<String> sites = toList(routeLocator.path("sites").getNode(root).fieldNames());
        return arrayItems(path(sites));
    });
    private static final Validator ROUTE = allOf(
            property("sites", UNIQUE_SITES),
            property("nodes", UNIQUE_CROSS_NODES),
            UNIQUE_NODES_KEYS,
            UNIQUE_NODES,
            property("edges", EDGES),
            property("paths", PATHS)
    );

    /**
     * @param nodes valid node names
     */
    static Validator edge(Collection<String> nodes) {
        return allOf(
                property("start", values(nodes)),
                property("end", values(nodes)),
                EDGE_NODES
        );
    }

    public static Validator module() {
        return MODULE;
    }

    /**
     * @param sites valid site names
     */
    static Validator path(Collection<String> sites) {
        return allOf(
                property("departure", values(sites)),
                property("destination", values(sites)),
                PATH
        );
    }

    /**
     *
     */
    static Validator route() {
        return ROUTE;
    }

    /**
     *
     */
    static <T> Stream<Tuple2<T, T>> unorderedStream(List<T> keys) {
        return IntStream.range(0, keys.size())
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, keys.size())
                        .mapToObj(j -> Tuple2.of(i, j)))
                .map(t -> Tuple2.of(keys.get(t._1), keys.get(t._2)));
    }

    /**
     * @param nodes valid node names
     */
    static Validator weight(List<String> nodes) {
        return allOf(
                property("departure", values(nodes)),
                property("destination", values(nodes))
        );
    }
}
