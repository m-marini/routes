/**
 * 
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Itinerary.java,v 1.3 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class Itinerary {
	private SiteNode destination;
	private double expectedTime;

	/**
	 * 
	 */
	public Itinerary() {
	}

	/**
	 * @return the destination
	 */
	public SiteNode getDestination() {
		return destination;
	}

	/**
	 * @return the expectedTime
	 */
	public double getExpectedTime() {
		return expectedTime;
	}

	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(SiteNode destination) {
		this.destination = destination;
	}

	/**
	 * @param expectedTime
	 *            the expectedTime to set
	 */
	public void setExpectedTime(double expectedTime) {
		this.expectedTime = expectedTime;
	}

}
