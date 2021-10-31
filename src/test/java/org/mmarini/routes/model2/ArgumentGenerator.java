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

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;

public class ArgumentGenerator {

    public static final int DEFAULT_NUM_TESTS = 100;

    public static ArgumentGenerator create(long seed) {
        return create(DEFAULT_NUM_TESTS, seed);
    }

    public static ArgumentGenerator create(int n, Random random) {
        return new ArgumentGenerator(n, random, new ArrayList<>());
    }

    public static ArgumentGenerator create(int n, long seed) {
        return new ArgumentGenerator(n, new Random(seed), new ArrayList<>());
    }

    private final int n;
    private final Random random;
    private final List<Object[]> data;

    public ArgumentGenerator(int n, Random random, List<Object[]> data) {
        this.n = n;
        this.random = random;
        this.data = data;
    }

    public ArgumentGenerator append(Function<Random, Object> f, Object... values) {
        final Object[] data = new Object[n];
        System.arraycopy(values, 0, data, 0, values.length);
        for (int i = values.length; i < n; i++) {
            data[i] = f.apply(random);
        }
        this.data.add(data);
        return new ArgumentGenerator(n, random, this.data);
    }

    public ArgumentGenerator exponential(double min, double max) {
        assert min > 0;
        assert max >= min;
        return append(random -> Math.exp(random.nextDouble() * Math.log(max / min)) * min, min, max, sqrt(min * max));
    }

    public ArgumentGenerator gaussian(double mean, double sigma) {
        assert sigma >= 0;
        return append(random -> random.nextGaussian() * sigma + mean, mean, mean - sigma, mean + sigma);
    }

    public Stream<Arguments> generate() {
        return IntStream.range(0, n)
                .mapToObj(i ->
                        Arguments.of(data.stream().map(data -> data[i]).toArray())
                );
    }

    public ArgumentGenerator nextBoolean(double probability) {
        return append(random -> random.nextDouble() < probability);
    }

    public ArgumentGenerator uniform(int min, int max) {
        assert max >= min;
        return append(random -> random.nextInt(max - min) + min, min, max, (min + max) / 2);
    }

    public ArgumentGenerator uniform(double min, double max) {
        assert max >= min;
        return append(random -> random.nextDouble() * (max - min) + min, min, max, (min + max) / 2);
    }
}
