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

package org.mmarini;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Utils {

    /**
     * Returns the cumulative values
     *
     * @param values the values
     */
    static double[] cumulative(double... values) {
        int n = values.length;
        if (n == 0) {
            return new double[0];
        } else if (n == 1) {
            return new double[]{1};
        } else {
            double[] cumulative = new double[n];
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += values[i];
                cumulative[i] = sum;
            }
            return cumulative;
        }
    }

    /**
     * Returns the collector of Map
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> entriesToMap() {
        return Collectors.toMap(Entry<K, V>::getKey, Entry<K, V>::getValue);
    }

    static <T> Optional<T> find(Stream<T> stream, Predicate<T> test) {
        return stream.dropWhile(Predicate.not(test)).limit(1).findAny();
    }

    static <K, V> Optional<V> getValue(Map<K, V> map, K value) {
        return Optional.ofNullable(map.get(value));
    }

    static <K, V> Function<K, Optional<V>> getValue(Map<K, V> map) {
        return key -> getValue(map, key);
    }

    static <K, V> Stream<Tuple2<K, V>> join(List<K> list1, List<V> list2) {
        return list1.stream().flatMap(k ->
                list2.stream().map(v -> Tuple2.of(k, v))
        );
    }

    static <K, V, V1> Function<Map.Entry<K, V>, Map.Entry<K, V1>> mapValue(Function<V, V1> mapper) {
        return entry -> Map.entry(entry.getKey(), mapper.apply(entry.getValue()));
    }

    /**
     * Returns an integer with probability given by cdf values
     *
     * @param random the random generator
     * @param cdf    the cumulative distribution function values must be monotonic increasing
     */
    static int nextCdf(Random random, double... cdf) {
        assert cdf.length > 0;
        double p = random.nextDouble() * cdf[cdf.length - 1];
        int n = cdf.length - 1;
        for (int i = 0; i < n; i++) {
            if (p < cdf[i]) {
                return i;
            }
        }
        return n;
    }

    /**
     * Returns a number with a Poisson distribution probability
     *
     * @param random the random generator
     * @param lambda the mean value
     */
    static int nextPoison(Random random, final double lambda) {
        int k = -1;
        double p = 1;
        final double l = Math.exp(-lambda);
        do {
            ++k;
            p *= random.nextDouble();
        } while (p > l);
        return k;
    }

    /**
     * Returns the preferences from cumulative function values
     *
     * @param cdf the cumulative function values
     */
    static double[] preferences(double... cdf) {
        int n = cdf.length;
        double[] preferences = new double[n];
        preferences[0] = cdf[0];
        for (int i = 1; i < n; i++) {
            preferences[i] = cdf[i] - cdf[i - 1];
        }
        return preferences;
    }

    /**
     * Returns the list of an iterable
     *
     * @param iterable the iterable
     * @param <T>      the item type
     */
    static <T> List<T> toList(Iterable<T> iterable) {
        Stream.Builder<T> b = Stream.builder();
        iterable.forEach(b::add);
        return b.build().collect(Collectors.toList());
    }

    /**
     * Returns the stream of an index, value of a list
     *
     * @param list the list
     * @param <T>  the item type
     */
    static <T> Stream<Tuple2<Integer, T>> zipWithIndex(List<T> list) {
        return IntStream.range(0, list.size())
                .mapToObj(i -> Tuple2.of(i, list.get(i)));
    }
}
