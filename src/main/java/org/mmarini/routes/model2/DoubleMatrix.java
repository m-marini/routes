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

import org.mmarini.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class DoubleMatrix<K> {

    /**
     * Returns the double matrix from a stream
     */
    public static <K> DoubleMatrix<? extends K> from(Stream<Tuple2<Tuple2<? extends K, ? extends K>, Double>> stream, List<? extends K> keys) {
        int n = keys.size();
        double[][] values = new double[n][n];
        stream.forEach(entry -> {
            int i = keys.indexOf(entry._1._1);
            int j = keys.indexOf(entry._1._2);
            if (i >= 0 && j >= 0) {
                values[i][j] = entry._2;
            }
        });
        return new DoubleMatrix<>(keys, values);
    }

    private final List<K> keys;
    private final double[][] values;

    /**
     * @param keys   the keys
     * @param values the values
     */
    public DoubleMatrix(List<K> keys, double[][] values) {
        requireNonNull(keys);
        requireNonNull(values);
        assert values.length == keys.size();

        this.keys = keys;
        this.values = values;
    }

    /**
     * Returns the stream of entries
     */
    public Stream<Tuple2<Tuple2<K, K>, Double>> entries() {
        Stream.Builder<Tuple2<Tuple2<K, K>, Double>> b = Stream.builder();
        int n = keys.size();
        for (int i = 0; i < n; i++) {
            K from = keys.get(i);
            for (int j = 0; j < n; j++) {
                double v = values[i][j];
                if (v != 0.0) {
                    K to = keys.get(j);
                    b.add(Tuple2.of(Tuple2.of(from, to), v));
                }
            }
        }
        return b.build();
    }

    /**
     * Returns the keys
     */
    public List<K> getKeys() {
        return keys;
    }

    /**
     * Returns the value for a give path
     *
     * @param from the source of path
     * @param to   the destination of path
     */
    public OptionalDouble getValue(K from, K to) {
        int i = keys.indexOf(from);
        int j = keys.indexOf(to);
        return i >= 0 && j >= 0 ? OptionalDouble.of(values[i][j]) : OptionalDouble.empty();
    }

    /**
     * Returns the values
     */
    public double[][] getValues() {
        return values;
    }

    /**
     * Returns the mapped the double matrix
     *
     * @param keys   the new keys
     * @param mapper the mapper from new key to old keys
     * @param <K1>   the new key type
     */
    public <K1> DoubleMatrix<K1> map(List<K1> keys, Function<K1, Optional<K>> mapper) {
        int n = keys.size();
        double[][] weights = new double[n][n];
        IntStream.range(0, n).forEach(i -> {
            K1 from = keys.get(i);
            mapper.apply(from).ifPresent(oldFrom ->
                    IntStream.range(0, n).forEach(j -> {
                        K1 to = keys.get(j);
                        mapper.apply(to).ifPresent(oldTo ->
                                getValue(oldFrom, oldTo).ifPresent(w ->
                                        weights[i][j] = w
                                ));
                    }));
        });
        return new DoubleMatrix<>(keys, weights);
    }
}
