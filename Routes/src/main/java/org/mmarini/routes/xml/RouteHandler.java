/*
 * RouteHandler.java
 *
 * $Id: RouteHandler.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 *
 * 01/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

import java.util.HashMap;
import java.util.Map;

import org.mmarini.routes.model.Constants;
import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.SiteNode;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: RouteHandler.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class RouteHandler extends DefaultHandler implements XmlConstants,
		Constants {

	private StringBuffer buffer;

	private Map<String, MapNode> nodeTable;

	private SiteNode siteNode;

	private MapNode mapNode;

	private MapEdge edge;

	private boolean defaultElement;

	private double defaultSpeedLimit;

	private double x;

	private double y;

	private Path path;

	private MapBuilder builder;

	private Locator documentLocator;

	/**
         * 
         */
	public RouteHandler() {
		buffer = new StringBuffer();
		nodeTable = new HashMap<String, MapNode>(0);
		setDefaultSpeedLimit(DEFAULT_SPEED_LIMIT);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] bfr, int offset, int size)
			throws SAXException {
		getBuffer().append(bfr, offset, size);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		getBuilder().init();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (SITE_ELEM.equals(localName)) {
			SiteNode site = getSiteNode();
			site.setLocation(getX(), getY());
			getBuilder().add(site);
		} else if (NODE_ELEM.equals(localName)) {
			MapNode node = getMapNode();
			node.setLocation(getX(), getY());
			getBuilder().add(node);
		} else if (EDGE_ELEM.equals(localName)) {
			MapEdge edge = getEdge();
			getBuilder().add(edge);
		} else if (SPEED_LIMIT_ELEM.equals(localName)) {
			double value = parseDouble() / 3.6f;
			if (isDefaultElement()) {
				setDefaultSpeedLimit(value);
			} else {
				getEdge().setSpeedLimit(value);
			}
		} else if (X_ELEM.equals(localName)) {
			setX(parseDouble());
		} else if (Y_ELEM.equals(localName)) {
			setY(parseDouble());
		} else if (FREQUENCE_ELEM.equals(localName)) {
			getBuilder().applyFrequence(parseDouble());
		} else if (WEIGHT_ELEM.equals(localName)) {
			getPath().setWeight(parseDouble());
		} else if (DEPARTURE_ELEM.equals(localName)) {
			String id = getBuffer().toString();
			MapNode node = getNodeTable().get(id);
			if (node == null)
				throw new SAXParseException("Missing node " + id,
						getDocumentLocator());
			if (!(node instanceof SiteNode))
				throw new SAXParseException(
						"Node " + id + " is not a SiteNode",
						getDocumentLocator());
			getPath().setDeparture((SiteNode) node);
		} else if (DESTINATION_ELEM.equals(localName)) {
			String id = getBuffer().toString();
			MapNode node = getNodeTable().get(id);
			if (node == null)
				throw new SAXParseException("Missing node " + id,
						getDocumentLocator());
			if (!(node instanceof SiteNode))
				throw new SAXParseException(
						"Node " + id + " is not a SiteNode",
						getDocumentLocator());
			getPath().setDestination((SiteNode) node);
		} else if (PATH_ELEM.equals(localName)) {
			getBuilder().add(getPath());
		} else if (START_ELEM.equals(localName)) {
			String id = getBuffer().toString();
			MapNode node = getNodeTable().get(id);
			if (node == null)
				throw new SAXParseException("Missing node " + id,
						getDocumentLocator());
			getEdge().setBegin(node);
		} else if (END_ELEM.equals(localName)) {
			String id = getBuffer().toString();
			MapNode node = getNodeTable().get(id);
			if (node == null)
				throw new SAXParseException("Missing node " + id,
						getDocumentLocator());
			getEdge().setEnd(node);
		} else if (PRIORITY_ELEM.equals(localName)) {
			getEdge().setPriority(parseInt());
		} else if (DEFAULT_ELEM.equals(localName)) {
			setDefaultElement(false);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		e.printStackTrace();
		throw e;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		e.printStackTrace();
		throw e;
	}

	/**
	 * @return the buffer
	 */
	private StringBuffer getBuffer() {
		return buffer;
	}

	/**
	 * @return the builder
	 */
	private MapBuilder getBuilder() {
		return builder;
	}

	/**
	 * @return the defaultSpeedLimit
	 */
	private double getDefaultSpeedLimit() {
		return defaultSpeedLimit;
	}

	/**
	 * @return the documentLocator
	 */
	private Locator getDocumentLocator() {
		return documentLocator;
	}

	/**
	 * @return the edge
	 */
	private MapEdge getEdge() {
		return edge;
	}

	/**
	 * @return the mapNode
	 */
	private MapNode getMapNode() {
		return mapNode;
	}

	/**
	 * @return the nodeTable
	 */
	private Map<String, MapNode> getNodeTable() {
		return nodeTable;
	}

	/**
	 * @return the path
	 */
	private Path getPath() {
		return path;
	}

	/**
	 * @return the siteNode
	 */
	private SiteNode getSiteNode() {
		return siteNode;
	}

	/**
	 * @return the x
	 */
	private double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	private double getY() {
		return y;
	}

	/**
	 * @return the defaultElement
	 */
	private boolean isDefaultElement() {
		return defaultElement;
	}

	/**
	 * @return
	 * @throws SAXParseException
	 */
	private double parseDouble() throws SAXParseException {
		double value = 0;
		try {
			value = Double.parseDouble(getBuffer().toString());
		} catch (NumberFormatException e) {
			throw new SAXParseException(e.getMessage(), getDocumentLocator());
		}
		return value;
	}

	/**
	 * @return
	 * @throws SAXParseException
	 */
	private int parseInt() throws SAXParseException {
		int value = 0;
		try {
			value = Integer.parseInt(getBuffer().toString());
		} catch (NumberFormatException e) {
			throw new SAXParseException(e.getMessage(), getDocumentLocator());
		}
		return value;
	}

	/**
         * 
         */
	private void resetBuffer() {
		getBuffer().setLength(0);
	}

	/**
	 * @param builder
	 *            the builder to set
	 */
	public void setBuilder(MapBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @param defaultElement
	 *            the defaultElement to set
	 */
	private void setDefaultElement(boolean defaultElement) {
		this.defaultElement = defaultElement;
	}

	/**
	 * @param defaultSpeedLimit
	 *            the defaultSpeedLimit to set
	 */
	private void setDefaultSpeedLimit(double defaultSpeedLimit) {
		this.defaultSpeedLimit = defaultSpeedLimit;
	}

	/**
	 * @param documentLocator
	 *            the documentLocator to set
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.documentLocator = locator;
	}

	/**
	 * @param edge
	 *            the edge to set
	 */
	private void setEdge(MapEdge edge) {
		this.edge = edge;
	}

	/**
	 * @param mapNode
	 *            the mapNode to set
	 */
	private void setMapNode(MapNode mapNode) {
		this.mapNode = mapNode;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	private void setPath(Path path) {
		this.path = path;
	}

	/**
	 * @param siteNode
	 *            the siteNode to set
	 */
	private void setSiteNode(SiteNode siteNode) {
		this.siteNode = siteNode;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	private void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	private void setY(double y) {
		this.y = y;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		getBuilder().clear();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {
		resetBuffer();
		if (SITE_ELEM.equals(localName)) {
			SiteNode site = new SiteNode();
			setSiteNode(site);
			String id = attr.getValue(ID_ATTR);
			getNodeTable().put(id, site);
		} else if (NODE_ELEM.equals(localName)) {
			MapNode node = new MapNode();
			setMapNode(node);
			String id = attr.getValue(ID_ATTR);
			getNodeTable().put(id, node);
		} else if (EDGE_ELEM.equals(localName)) {
			MapEdge edge = new MapEdge();
			edge.setSpeedLimit(getDefaultSpeedLimit());
			setEdge(edge);
		} else if (PATH_ELEM.equals(localName)) {
			Path path = new Path();
			setPath(path);
		} else if (DEFAULT_ELEM.equals(localName)) {
			setDefaultElement(true);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		e.printStackTrace();
		throw e;
	}
}
