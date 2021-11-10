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

import org.mmarini.routes.model2.TrafficInfo;

import java.util.StringJoiner;

/**
 * The traffic information record is rendered in traffic information table
 */
public class TrafficInfoView {
    private final NodeView destination;
    private final TrafficInfo info;

    /**
     * Creates the traffic information record
     *
     * @param destination the destination information
     * @param info        the traffic information
     */
    public TrafficInfoView(NodeView destination, TrafficInfo info) {
        assert destination != null;
        assert info != null;
        this.destination = destination;
        this.info = info;
    }

    /**
     * Returns the destination information
     */
    public NodeView getDestination() {
        return destination;
    }

    /**
     * Returns the traffic information
     */
    public TrafficInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TrafficInfoView.class.getSimpleName() + "[", "]")
                .add("destination=" + destination)
                .toString();
    }
}
