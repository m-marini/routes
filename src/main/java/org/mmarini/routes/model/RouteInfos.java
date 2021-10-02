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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco
 */
public class RouteInfos {
    private final List<SiteNode> nodes;

    private double[][] frequence;

    /**
     *
     */
    public RouteInfos() {
        nodes = new ArrayList<>(0);
    }

    /**
     *
     */
    public void clear() {
        getNodes().clear();
    }

    /**
     * @param simulator
     */
    public void computeInfos(final Simulator simulator) {
        final List<SiteNode> list = getNodes();
        list.clear();
        for (final SiteNode node : simulator.getSiteNodes()) {
            list.add(node);
        }
        final int n = list.size();
        final double[][] freq = new double[n][n];
        final double fr = simulator.getFrequence() / (n - 1) / 2;
        for (final Path path : simulator.getPaths()) {
            final SiteNode from = path.getDeparture();
            final SiteNode to = path.getDestination();
            final int i = list.indexOf(from);
            final int j = list.indexOf(to);
            freq[i][j] = freq[j][i] = freq[i][j] + path.getWeight() * fr;
        }
        setFrequence(freq);
    }

    /**
     * @return the frequence
     */
    public double[][] getFrequence() {
        return frequence;
    }

    /**
     * @param frequence the frequence to set
     */
    private void setFrequence(final double[][] frequence) {
        this.frequence = frequence;
    }

    /**
     * @param row
     * @param col
     * @return
     */
    public double getFrequence(final int row, final int col) {
        return getFrequence()[row][col];
    }

    /**
     * @param index
     * @return
     */
    public SiteNode getNode(final int index) {
        return getNodes().get(index);
    }

    /**
     * @return the nodes
     */
    public List<SiteNode> getNodes() {
        return nodes;
    }

    /**
     * @return
     */
    public int getNodesCount() {
        return getNodes().size();
    }
}
