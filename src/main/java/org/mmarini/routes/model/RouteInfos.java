/**
 * 
 */
package org.mmarini.routes.model;

import java.util.ArrayList;
import java.util.List;

import org.mmarini.routes.xml.Path;

/**
 * @author Marco
 * 
 */
public class RouteInfos {
	private final List<SiteNode> nodes;

	private double[][] frequence;

	/**
	     * 
	     */
	public RouteInfos() {
		nodes = new ArrayList<SiteNode>(0);
	}

	/**
	     * 
	     * 
	     */
	public void clear() {
		getNodes().clear();
	}

	/**
	 * 
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
	private double[][] getFrequence() {
		return frequence;
	}

	/**
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public double getFrequence(final int row, final int col) {
		return getFrequence()[row][col];
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public SiteNode getNode(final int index) {
		return getNodes().get(index);
	}

	/**
	 * @return the nodes
	 */
	private List<SiteNode> getNodes() {
		return nodes;
	}

	/**
	 * 
	 * @return
	 */
	public int getNodesCount() {
		return getNodes().size();
	}

	/**
	 * @param frequence the frequence to set
	 */
	private void setFrequence(final double[][] frequence) {
		this.frequence = frequence;
	}
}
