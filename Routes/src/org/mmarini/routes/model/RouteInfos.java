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
	private List<SiteNode> nodes;

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
	public void computeInfos(Simulator simulator) {
		List<SiteNode> list = getNodes();
		list.clear();
		for (SiteNode node : simulator.getSiteNodes()) {
			list.add(node);
		}
		int n = list.size();
		double[][] freq = new double[n][n];
		double fr = simulator.getFrequence() / (n - 1) / 2;
		for (Path path : simulator.getPaths()) {
			SiteNode from = path.getDeparture();
			SiteNode to = path.getDestination();
			int i = list.indexOf(from);
			int j = list.indexOf(to);
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
	public double getFrequence(int row, int col) {
		return getFrequence()[row][col];
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public SiteNode getNode(int index) {
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
	 * @param frequence
	 *            the frequence to set
	 */
	private void setFrequence(double[][] frequence) {
		this.frequence = frequence;
	}
}
