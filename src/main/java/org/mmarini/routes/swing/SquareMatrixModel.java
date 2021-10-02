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

package org.mmarini.routes.swing;

import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SquareMatrixModel<T> {
    private final List<T> indices;
    private final double[][] values;

    /**
     * @param indices the indices
     * @param values  the values
     */
    public SquareMatrixModel(List<T> indices, double[][] values) {
        this.indices = indices;
        this.values = values;
    }

    /**
     * @param row     the row index
     * @param columns the column index
     */
    public OptionalDouble get(T row, T columns) {
        final int i = indexOf(row);
        final int j = indexOf(columns);
        return get(i, j);
    }

    /**
     * @param row    the row index
     * @param column the column index
     */
    private OptionalDouble get(int row, int column) {
        return row >= 0 && column >= 0 ? OptionalDouble.of(values[row][column]) : OptionalDouble.empty();
    }

    /**
     *
     */
    public List<T> getIndices() {
        return indices;
    }

    /**
     *
     */
    public double[][] getValues() {
        return values;
    }

    /**
     * @param index the index
     */
    public int indexOf(T index) {
        return indices.indexOf(index);
    }


    /**
     * @param mapper the mapper
     * @param <U>    the indices type
     */
    public <U> SquareMatrixModel<U> map(Function<T, U> mapper) {
        return new SquareMatrixModel<>(
                indices.stream().map(mapper).collect(Collectors.toList()),
                values
        );
    }
}
