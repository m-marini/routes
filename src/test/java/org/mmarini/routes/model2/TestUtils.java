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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;

public class TestUtils {

    static <T> Matcher<Optional<T>> optionalEmpty() {
        return equalTo(Optional.empty());
    }

    static <T> Matcher<Optional<T>> optionalOf(Matcher<T> matcher) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(matcher);
            }

            @Override
            public boolean matches(Object o) {
                if (o == null ||
                        !(o instanceof Optional)) {
                    return false;
                }
                Optional<?> opt = (Optional<?>) o;
                if (opt.isEmpty()) {
                    return false;
                } else {
                    return matcher.matches(opt.get());
                }
            }
        };
    }


    static <T> Matcher<Optional<T>> optionalOf(T item) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(item);
            }

            @Override
            public boolean matches(Object o) {
                if (o == null ||
                        !(o instanceof Optional)) {
                    return false;
                }
                Optional<?> opt = (Optional<?>) o;
                if (opt.isEmpty()) {
                    return false;
                } else {
                    return opt.get().equals(item);
                }
            }
        };
    }

    static Matcher<Point2D> pointCloseTo(Point2D expectedPoint, double epsilon) {
        return new BaseMatcher<>() {
            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("was ")
                        .appendValue(item);
                if (item != null && item instanceof Point2D) {
                    description.appendText(" within a distance of ")
                            .appendValue(expectedPoint.distance((Point2D) item));
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expectedPoint)
                        .appendText(" within a distance of ")
                        .appendValue(epsilon);
            }

            @Override
            public boolean matches(Object o) {
                if (o == null ||
                        !(o instanceof Point2D)) {
                    return false;
                }
                Point2D point = (Point2D) o;
                double dist = point.distance(expectedPoint);
                return dist <= epsilon;
            }
        };
    }
}
