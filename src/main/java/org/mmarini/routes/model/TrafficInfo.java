/**
 * 
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: TrafficInfo.java,v 1.3 2010/10/19 20:32:59 marco Exp $
 */
public class TrafficInfo {
	private int veicleCount;
	private int delayCount;
	private double totalDelayTime;
	private SiteNode destination;

	/**
	 * 
	 */
	public TrafficInfo() {
	}

	/**
	 * Get the average delay time.
	 * 
	 * @return the average time
	 */
	public double getAverageDelayTime() {
		final int ct = getDelayCount();
		if (ct == 0) {
			return 0.;
		}
		return getTotalDelayTime() / ct;
	}

	/**
	 * @return the delayCount
	 */
	public int getDelayCount() {
		return delayCount;
	}

	/**
	 * @return the destination
	 */
	public SiteNode getDestination() {
		return destination;
	}

	/**
	 * @return the totalDelayTime
	 */
	public double getTotalDelayTime() {
		return totalDelayTime;
	}

	/**
	 * @return the veicleCount
	 */
	public int getVeicleCount() {
		return veicleCount;
	}

	/**
	 * @param delayCount the delayCount to set
	 */
	public void setDelayCount(final int delayCount) {
		this.delayCount = delayCount;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(final SiteNode destination) {
		this.destination = destination;
	}

	/**
	 * @param totalDelayTime the totalDelayTime to set
	 */
	public void setTotalDelayTime(final double totalDelayTime) {
		this.totalDelayTime = totalDelayTime;
	}

	/**
	 * @param veicleCount the veicleCount to set
	 */
	public void setVeicleCount(final int veicleCount) {
		this.veicleCount = veicleCount;
	}

}
