/*
 *
 * Copyright (c) 2023 Marco Marini, marco.marini@mmarini.org
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
 */

package org.mmarini.routes.model2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.geom.Point2D;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.closeTo;
import static org.mmarini.routes.model2.TestUtils.nodeCloseTo;
import static org.mmarini.routes.model2.TestUtils.pointCloseTo;

class RoundAboutBuilderTest {

    public static final double EDGE_LENGTH = 42d;
    public static final int NUM_ENTRIES = 4;

    @Test
    void testCreateCrossEdges() {
        RoundAboutBuilder b = new RoundAboutBuilder(NUM_ENTRIES, EDGE_LENGTH);
        MapEdge[] edges = b.createCrossEdges().toArray(MapEdge[]::new);
        assertThat(edges, arrayWithSize(NUM_ENTRIES * 4 / 2));
        assertThat(edges[0].getEnd(), nodeCloseTo(67.6, 9, 100e-3));
        assertThat(edges[0].getBegin(), nodeCloseTo(-67.6, 9, 100e-3));
        assertThat(edges[1].getEnd(), nodeCloseTo(67.6, 3, 100e-3));
        assertThat(edges[1].getBegin(), nodeCloseTo(-67.6, 3, 100e-3));
    }

    @Test
    void testCreatePoints() {
        RoundAboutBuilder b = new RoundAboutBuilder(NUM_ENTRIES, EDGE_LENGTH);
        MapNode[] points = b.createNodes();
        assertThat(points, arrayWithSize(NUM_ENTRIES * 8));
        MapNode point = points[0];
        assertThat(points[0].getLocation(), pointCloseTo(54.9, 0, 100e-3));
        point = points[1];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(38.8, -38.8)), closeTo(0, 100e-3));
        point = points[2];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(0, -54.9)), closeTo(0, 100e-3));
        point = points[3];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(-38.8, -38.8)), closeTo(0, 100e-3));
        point = points[4];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(-54.9, 0)), closeTo(0, 100e-3));
        point = points[5];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(-38.8, 38.8)), closeTo(0, 100e-3));
        point = points[6];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(0, 54.9)), closeTo(0, 100e-3));
        point = points[7];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(38.8, 38.8)), closeTo(0, 100e-3));
        point = points[8];
        assertThat(
                point.toString(),
                point.getLocation().distance(new Point2D.Double(60.9, 0)), closeTo(0, 100e-3));

        point = points[16];
        assertThat(point.toString(), new Point2D.Double(67.6, 9).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[17];
        assertThat(point.toString(), new Point2D.Double(67.6, 3).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[18];
        assertThat(point.toString(), new Point2D.Double(67.6, -3).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[19];
        assertThat(point.toString(), new Point2D.Double(67.6, -9).distance(point.getLocation()), closeTo(0, 100e-3));

        point = points[20];
        assertThat(point.toString(), new Point2D.Double(9, -67.6).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[21];
        assertThat(point.toString(), new Point2D.Double(3, -67.6).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[22];
        assertThat(point.toString(), new Point2D.Double(-3, -67.6).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[23];
        assertThat(point.toString(), new Point2D.Double(-9, -67.6).distance(point.getLocation()), closeTo(0, 100e-3));

        point = points[24];
        assertThat(point.toString(), new Point2D.Double(-67.6, -9).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[25];
        assertThat(point.toString(), new Point2D.Double(-67.6, -3).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[26];
        assertThat(point.toString(), new Point2D.Double(-67.6, 3).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[27];
        assertThat(point.toString(), new Point2D.Double(-67.6, 9).distance(point.getLocation()), closeTo(0, 100e-3));

        point = points[28];
        assertThat(point.toString(), new Point2D.Double(-9, 67.6).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[29];
        assertThat(point.toString(), new Point2D.Double(-3, 67.6).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[30];
        assertThat(point.toString(), new Point2D.Double(3, 67.6).distance(point.getLocation()), closeTo(0, 100e-3));
        point = points[31];
        assertThat(point.toString(), new Point2D.Double(9, 67.6).distance(point.getLocation()), closeTo(0, 100e-3));

    }

    @ParameterizedTest
    @CsvSource(value = {
            "4,54.9",
            "6,81.1",
    })
    void testGetInnerRadius(int numEntries, double expected) {
        RoundAboutBuilder b = new RoundAboutBuilder(numEntries, EDGE_LENGTH);
        double p = b.getInnerRadius();
        assertThat(p, closeTo(expected, 100e-3));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "4,60.9",
            "6,87.1",
    })
    void testGetOuterRadius(int numEntries, double expected) {
        RoundAboutBuilder b = new RoundAboutBuilder(numEntries, EDGE_LENGTH);
        double p = b.getOuterRadius();
        assertThat(p, closeTo(expected, 100e-3));
    }

}