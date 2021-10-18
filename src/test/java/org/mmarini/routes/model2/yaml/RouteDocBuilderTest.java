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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mmarini.routes.model2.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class RouteDocBuilderTest {
    static final double SPEED_LIMIT = 10.0;
    static final int PRIORITY = 0;
    static final double FREQUENCY = 1.2;

    @Test
    void build() throws JsonProcessingException {
              /*
        Given a topology of
        0 --1--> 1 -----> 2
          <-----   <--0--
         */
        SiteNode node0 = SiteNode.createSite(0, 0);
        SiteNode node2 = SiteNode.createSite(100, 0);
        MapNode node1 = MapNode.createNode(50, 0);
        MapEdge edge01 = new MapEdge(node0, node1, SPEED_LIMIT, PRIORITY);
        MapEdge edge10 = new MapEdge(node1, node0, SPEED_LIMIT, PRIORITY);
        MapEdge edge12 = new MapEdge(node1, node2, SPEED_LIMIT, PRIORITY);
        MapEdge edge21 = new MapEdge(node2, node1, SPEED_LIMIT, PRIORITY);
        StatusImpl status = StatusImpl.create(
                Topology.create(
                        List.of(node0, node2),
                        List.of(node0, node2, node1),
                        List.of(edge01, edge10, edge12, edge21)
                ), 0,
                List.of(), FREQUENCY);

        String result = RouteDocBuilder.mapper.writeValueAsString(
                RouteDocBuilder.build(status));
        assertThat(result, equalTo(TestUtils.text(
                "---",
                "default:",
                "  frequence: 1.2",
                "sites:",
                "  Node_0:",
                "    x: 0.0",
                "    \"y\": 0.0",
                "  Node_1:",
                "    x: 100.0",
                "    \"y\": 0.0",
                "paths:",
                "- departure: \"Node_0\"",
                "  destination: \"Node_1\"",
                "  weight: 1.0",
                "- departure: \"Node_1\"",
                "  destination: \"Node_0\"",
                "  weight: 1.0",
                "nodes:",
                "  Node_2:",
                "    x: 50.0",
                "    \"y\": 0.0",
                "edges:",
                "- start: \"Node_0\"",
                "  end: \"Node_2\"",
                "  priority: 0",
                "  speedLimit: 36.0",
                "- start: \"Node_2\"",
                "  end: \"Node_0\"",
                "  priority: 0",
                "  speedLimit: 36.0",
                "- start: \"Node_2\"",
                "  end: \"Node_1\"",
                "  priority: 0",
                "  speedLimit: 36.0",
                "- start: \"Node_1\"",
                "  end: \"Node_2\"",
                "  priority: 0",
                "  speedLimit: 36.0"
        )));
    }

}