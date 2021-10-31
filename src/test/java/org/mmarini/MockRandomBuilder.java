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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class MockRandomBuilder {
    private final List<Double> doubles;
    private final List<Integer> integers;

    public MockRandomBuilder() {
        this.doubles = new ArrayList<>();
        this.integers = new ArrayList<>();
    }

    public Random build() {
        Iterator<Double> dIter = doubles.iterator();
        Iterator<Integer> iIter = integers.iterator();

        return new Random() {

            @Override
            public double nextDouble() {
                assert dIter.hasNext();
                return dIter.next();
            }

            @Override
            public double nextGaussian() {
                assert dIter.hasNext();
                return dIter.next();
            }

            @Override
            public int nextInt(int bound) {
                assert iIter.hasNext();
                return iIter.next() % bound;
            }
        };
    }

    public MockRandomBuilder nextDouble(double... values) {
        doubles.addAll(DoubleStream.of(values).boxed().collect(Collectors.toList()));
        return this;
    }

    public MockRandomBuilder nextInt(int... values) {
        integers.addAll(IntStream.of(values).boxed().collect(Collectors.toList()));
        return this;
    }
}
