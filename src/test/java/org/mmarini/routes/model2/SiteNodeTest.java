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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Point2D;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.routes.model2.CrossNode.createNode;
import static org.mmarini.routes.model2.SiteNode.createSite;

class SiteNodeTest {

    public static final long SEED = 1234L;
    public static final int MIN_COORDINATE_VALUE = -2000;
    public static final int MAX_COORDINATE_VALUE = 2000 + 1;

    static Stream<Arguments> points() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .generate();
    }

    static Stream<Arguments> points2() {
        return ArgumentGenerator.create(SEED)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .uniform(MIN_COORDINATE_VALUE, MAX_COORDINATE_VALUE)
                .generate();
    }

    @ParameterizedTest
    @MethodSource("points")
    void create(int x, int y) {
        SiteNode node = createSite(x, y);

        assertThat(node.getLocation(), equalTo(new Point2D.Double(x, y)));

        Point2D value = node.apply(new MapElementVisitorAdapter<>() {

            @Override
            public Point2D visit(SiteNode node) {
                return node.getLocation();
            }
        });
        assertThat(value, equalTo(new Point2D.Double(x, y)));
    }

    @ParameterizedTest
    @MethodSource("points")
    void equalsAndHash(int x, int y) {
        SiteNode node = createSite(x, y);

        // itself
        assertEquals(node, node);

        // null
        assertNotEquals(null, node);

        // wrong class
        assertNotEquals(node, new Object());

        // different location
        assertNotEquals(node, createSite(x + 1, y + 1));

        // same location
        assertEquals(node, createSite(x, y));

        // hashCode
        assertThat(node.hashCode(), equalTo(createSite(x, y).hashCode()));
    }

    @ParameterizedTest
    @MethodSource("points2")
    void getDistanceSq(int x, int y, int x1, int y1) {
        Point2D point2 = new Point2D.Double(x1, y1);

        SiteNode node = createSite(x, y);

        double result = node.distanceSqFrom(point2);
        double expected = (x1 - x) * (x1 - x) + (y1 - y) * (y1 - y);

        assertThat(result, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("points")
    void isSameLocation(int x, int y) {
        SiteNode node = createSite(x, y);

        // itself
        assertTrue(node.isSameLocation(node));

        // different location
        assertFalse(node.isSameLocation(createSite(x + 1, y + 1)));

        // same location
        assertEquals(node, createSite(x, y));

        // site same location
        assertTrue(node.isSameLocation(createNode(x, y)));
    }

    @ParameterizedTest
    @MethodSource("points2")
    void setLocation(int x, int y, int x1, int y1) {
        Point2D point2 = new Point2D.Double(x1, y1);

        SiteNode node = createSite(x, y);

        SiteNode result = node.setLocation(point2);

        assertNotNull(result);
        assertThat(result,
                hasProperty("location",
                        allOf(
                                hasProperty("x", equalTo((double) x1)),
                                hasProperty("y", equalTo((double) y1))
                        )));
    }

    @ParameterizedTest
    @MethodSource("points")
    void toString(int x, int y) {
        SiteNode node = createSite(x, y);

        assertThat(node, hasToString("SiteNode[" + (double) x + ", " + (double) y + "]"));
    }

}
