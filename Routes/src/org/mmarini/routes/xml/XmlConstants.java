/*
 * XmlConstants.java
 *
 * $Id: XmlConstants.java,v 1.5 2009/05/29 20:47:24 marco Exp $
 *
 * 01/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: XmlConstants.java,v 1.5 2009/05/29 20:47:24 marco Exp $
 * 
 */
public interface XmlConstants {
	public static final String URI = "http://www.mmarini.org/routes-3-0-0";

	public static final String SCHEMA_LOCATION = URI + " routes-3-0-0.xsd";

	public static final String SCHEMA_RESOURCE = "/routes-3-0-0.xsd";

	public final static String ROUTES_ELEM = "routes";

	public static final String PATH_ELEM = "path";

	public static final String WEIGHT_ELEM = "weight";

	public static final String DEPARTURE_ELEM = "departure";

	public static final String DESTINATION_ELEM = "destination";

	public final static String NODE_ELEM = "node";

	public final static String SITE_ELEM = "site";

	public final static String EDGE_ELEM = "edge";

	public final static String FREQUENCE_ELEM = "frequence";

	public final static String SPEED_LIMIT_ELEM = "speedLimit";

	public final static String OUTCOME_PRIORITY_ELEM = "outcomePriority";

	public final static String PRIORITY_ELEM = "priority";

	public final static String START_ELEM = "start";

	public final static String END_ELEM = "end";

	public final static String X_ELEM = "x";

	public final static String Y_ELEM = "y";

	public final static String DEFAULT_ELEM = "default";

	public final static String ID_ATTR = "id";
}
