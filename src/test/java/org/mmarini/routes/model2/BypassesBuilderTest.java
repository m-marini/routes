package org.mmarini.routes.model2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BypassesBuilderTest {

    static Stream<Arguments> nodesCases() {
        return Stream.concat(nodesx4Inner(),
                Stream.concat(nodesx4Outer(),
                        nodesx3()
                ));
    }

    static Stream<Arguments> nodesx3() {
        return nodesxn(3, 0, 0, 1, 0.5, 1, 1);
    }

    static Stream<Arguments> nodesx4Inner() {
        return nodesxn(3,
                0, 0,
                1, 0.5,
                1, 1,
                0.7, 0.5);
    }

    static Stream<Arguments> nodesx4Outer() {
        return nodesxn(4,
                0, 0,
                1, 0.5,
                1, 1,
                0.4, 0.5);
    }

    static Stream<Arguments> nodesxn(int envelopeSize, double... coords) {
        if (coords.length % 2 != 0) {
            throw new IllegalArgumentException("even coordinates required");
        }
        if (coords.length / 2 < envelopeSize) {
            throw new IllegalArgumentException("envelop size greater or equal than number of ccordinates required");
        }
        List<MapNode> nodes = IntStream.range(0, coords.length / 2)
                .mapToObj(i -> new Point2D.Double(coords[i * 2], coords[i * 2 + 1]))
                .map(SiteNode::new)
                .collect(Collectors.toList());
        List<MapNode> ring = nodes.subList(0, envelopeSize);
        Stream<List<MapNode>> tests = permute(nodes);
        return tests.map(n -> Arguments.of(n, ring));
    }

    private static <T> Stream<List<T>> permute(List<T> nodes) {
        Stream.Builder<List<T>> args = Stream.builder();
        int n = nodes.size();
        int[] indexes = new int[n];
        List<T> permuted = new ArrayList<>(nodes);
        args.add(new ArrayList<>(permuted));
        for (int i = 0; i < n; ) {
            if (indexes[i] < i) {
                //                swap(elements, i % 2 == 0 ? 0 : indexes[i], i);
                int j = i % 2 == 0 ? 0 : indexes[i];
                T tmp = permuted.get(i);
                permuted.set(i, permuted.get(j));
                permuted.set(j, tmp);

                args.add(new ArrayList<>(permuted));
                indexes[i]++;
                i = 0;
            } else {
                indexes[i] = 0;
                i++;
            }
        }
        return args.build();
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,0,1,1,2,2",
            "0,1,1,2,2,3,3",
            "1,1,1,2,1,1,2",
            "-1,1,1,1,2,2,1",
    })
    void envelopePts(int expected,
                     double x0, double y0,
                     double x1, double y1,
                     double x2, double y2) {
        assertEquals(expected, BypassesBuilder.envelope(
                new Point2D.Double(x0, y0),
                new Point2D.Double(x1, y1),
                new Point2D.Double(x2, y2)
        ));
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,0,1,1,2,2",
            "0,1,1,2,2,3,3",
            "1,1,1,2,1,1,2",
            "-1,1,1,1,2,2,1",
    })
    void envelopePts1(int expected,
                      double x0, double y0,
                      double x1, double y1,
                      double x2, double y2) {
        assertEquals(expected, BypassesBuilder.envelope(List.of(
                        new Point2D.Double(x0, y0),
                        new Point2D.Double(x1, y1),
                        new Point2D.Double(x2, y2))
                .stream()
                .map(SiteNode::new)
                .collect(Collectors.toList())
        ));
    }

    @ParameterizedTest
    @MethodSource("nodesCases")
    void jarvisMarch(List<MapNode> nodes, List<MapNode> expected) {
        List<MapNode> ring = BypassesBuilder.jarvisMarch(nodes);
        assertThat(ring, contains(expected.stream().toArray(MapNode[]::new)));
    }
}