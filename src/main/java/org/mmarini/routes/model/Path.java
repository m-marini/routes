/**
 *
 */
package org.mmarini.routes.model;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Path.java,v 1.3 2010/10/19 20:33:00 marco Exp $
 *
 */
public class Path implements Cloneable {
	private SiteNode departure;
	private SiteNode destination;
	private double weight;

	/**
	 *
	 */
	public Path() {
	}

	/**
	 *
	 * @param path
	 */
	public Path(final Path path) {
		departure = path.departure;
		destination = path.destination;
		weight = path.weight;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Path clone() {
		return new Path(this);
	}

	/**
	 * @return the departure
	 */
	public SiteNode getDeparture() {
		return departure;
	}

	/**
	 * @return the arrival
	 */
	public SiteNode getDestination() {
		return destination;
	}

	/**
	 * @return the frequence
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param departure the departure to set
	 */
	public void setDeparture(final SiteNode departure) {
		this.departure = departure;
	}

	/**
	 * @param arrival the arrival to set
	 */
	public void setDestination(final SiteNode arrival) {
		this.destination = arrival;
	}

	/**
	 * @param frequence the frequence to set
	 */
	public void setWeight(final double frequence) {
		this.weight = frequence;
	}

}
