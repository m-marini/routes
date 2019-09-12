/**
 * 
 */
package org.mmarini.routes.model;

/**
 * @author Marco
 * 
 */
public class MapProfile {
	private int siteCount;

	private double width;

	private double height;

	private double minWeight;

	private double frequence;

	/**
	     * 
	     */
	public MapProfile() {
	}

	/**
	 * @return the frequence
	 */
	public double getFrequence() {
		return frequence;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return the minWeight
	 */
	public double getMinWeight() {
		return minWeight;
	}

	/**
	 * @return the siteCount
	 */
	public int getSiteCount() {
		return siteCount;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param frequence the frequence to set
	 */
	public void setFrequence(final double frequence) {
		this.frequence = frequence;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(final double height) {
		this.height = height;
	}

	/**
	 * @param minWeight the minWeight to set
	 */
	public void setMinWeight(final double minWeight) {
		this.minWeight = minWeight;
	}

	/**
	 * @param siteCount the siteCount to set
	 */
	public void setSiteCount(final int siteCount) {
		this.siteCount = siteCount;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(final double width) {
		this.width = width;
	}

}
