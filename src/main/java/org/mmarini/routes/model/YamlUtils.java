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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author mmarini
 */
public class YamlUtils {

    /**
     * @param node
     * @param defaultValue
     * @return
     */
    public static double jsonDouble(final JsonNode node, final double defaultValue) {
        return Optional.ofNullable(node).map(json -> json.asDouble(defaultValue)).orElse(defaultValue);
    }

    /**
     * @param node
     * @param defaultValue
     * @return
     */
    public static int jsonInt(final JsonNode node, final int defaultValue) {
        return Optional.ofNullable(node).map(json -> json.asInt(defaultValue)).orElse(defaultValue);
    }

    /**
     * @param <T>
     * @param iterator
     * @return
     */
    public static <T> List<T> toList(final Iterator<T> iterator) {
        final List<T> list = toStream(iterator).collect(Collectors.toList());
        return list;
    }

    /**
     * @param <T>
     * @param iterator
     * @return
     */
    public static <T> Stream<T> toStream(final Iterator<T> iterator) {
        final Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }
}
