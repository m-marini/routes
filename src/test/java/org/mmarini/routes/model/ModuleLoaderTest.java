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

package org.mmarini.routes.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleLoaderTest {

    @Test
    public void test() throws IOException {
        final File file = new File("src/test/resources/module.yml");

        final Module m = ModuleLoader.load(file);
        assertThat(m, notNullValue());

        final Iterable<MapNode> nodes = m.getNodes();
        assertThat(nodes,
                containsInAnyOrder(hasProperty("location", equalTo(new Point2D.Double(-9, -83))),
                        hasProperty("location", equalTo(new Point2D.Double(-12, -25))),
                        hasProperty("location", equalTo(new Point2D.Double(-24, -50)))));

        final Iterator<MapEdge> edges = m.getEdges().iterator();
        assertTrue(edges.hasNext());
        {
            final MapEdge edge = edges.next();
            assertThat(edge, hasProperty("beginLocation", equalTo(new Point2D.Double(-9, -83))));
            assertThat(edge, hasProperty("endLocation", equalTo(new Point2D.Double(-24, -50))));
            assertThat(edge, hasProperty("priority", equalTo(0)));
            assertThat(edge, hasProperty("speedLimit", closeTo(130.0 / 3.6, 0.1)));
        }
        assertTrue(edges.hasNext());
        {
            final MapEdge edge = edges.next();
            assertThat(edge, hasProperty("beginLocation", equalTo(new Point2D.Double(-9, -83))));
            assertThat(edge, hasProperty("endLocation", equalTo(new Point2D.Double(-12, -25))));
            assertThat(edge, hasProperty("priority", equalTo(1)));
            assertThat(edge, hasProperty("speedLimit", closeTo(100.0 / 3.6, 0.1)));
        }
        assertFalse(edges.hasNext());
    }
}
