/*
 * SaxMapParser.java
 *
 * $Id: XmlMapBuilder.java,v 1.6 2010/10/19 20:33:00 marco Exp $
 *
 * 01/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.mmarini.routes.model.MapEdge;
import org.mmarini.routes.model.MapElementVisitor;
import org.mmarini.routes.model.MapElementVisitorAdapter;
import org.mmarini.routes.model.MapNode;
import org.mmarini.routes.model.Simulator;
import org.mmarini.routes.model.SiteNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: XmlMapBuilder.java,v 1.6 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class XmlMapBuilder implements XmlConstants {

	private Simulator simulator;

	private Document document;

	private double defaultSpeedLimit;

	private final Map<MapNode, String> nodeMap;

	private int nodeCount;

	private DocumentBuilder builder;

	private Transformer transformer;

	private Element routesElement;

	private final MapElementVisitor siteBuilder;

	private final MapElementVisitor nodeBuilder;

	/**
	     * 
	     */
	public XmlMapBuilder() {
		nodeMap = new HashMap<MapNode, String>(0);
		siteBuilder = new MapElementVisitorAdapter() {

			/**
			 * @see org.mmarini.routes.model.MapElementVisitorAdapter#visit(org.mmarini.routes.model.SiteNode)
			 */
			@Override
			public void visit(final SiteNode node) {
				final Element elem = createSiteElement(node);
				getRoutesElement().appendChild(elem);
			}

		};
		nodeBuilder = new MapElementVisitorAdapter() {

			/**
			 * @see org.mmarini.routes.model.MapElementVisitorAdapter#visit(org.mmarini.routes.model.MapNode)
			 */
			@Override
			public void visit(final MapNode node) {
				final Element elem = createNodeElement(node);
				getRoutesElement().appendChild(elem);
			}

		};
	}

	/**
	 * 
	 * @param document
	 * @param simulator
	 */
	private void build(final Document document, final Simulator simulator) {
		setSimulator(simulator);
		setDocument(document);
		buildRoutesElement();
		getNodeMap().clear();
		setNodeCount(0);
	}

	/**
	 * @param file
	 * @param simulator
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SAXException
	 */
	public void build(final File file, final Simulator simulator)
			throws ParserConfigurationException, TransformerException, SAXException {
		DocumentBuilder builder = getBuilder();
		if (getBuilder() == null) {
			builder = createBuilder();
			setBuilder(builder);
		}
		final Document document = builder.newDocument();
		build(document, simulator);
		document.normalizeDocument();
		Transformer tr = getTransformer();
		if (tr == null) {
			tr = createTransfomer();
			setTransformer(tr);
		}
		final Source source = new DOMSource(document);
		final Result result = new StreamResult(file);
		tr.transform(source, result);
	}

	/**
	     * 
	     */
	private void buildRoutesElement() {
		final Element routes = document.createElementNS(URI, "rts:" + ROUTES_ELEM);
		routes.setAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		routes.setAttribute("xsi:schemaLocation", SCHEMA_LOCATION);
		setRoutesElement(routes);
		final Element defElem = createDefaultElement();
		routes.appendChild(defElem);
		final Simulator sim = getSimulator();
		MapElementVisitor builder = getSiteBuilder();
		for (final MapNode node : sim.getMapNodes()) {
			node.apply(builder);
		}
		for (final Path path : sim.getPaths()) {
			final Element elem = createPathElement(path);
			routes.appendChild(elem);
		}
		builder = getNodeBuilder();
		for (final MapNode node : sim.getMapNodes()) {
			node.apply(builder);
		}
		for (final MapEdge edge : sim.getMapEdges()) {
			final Element elem = createEdgeElement(edge);
			routes.appendChild(elem);
		}
		document.appendChild(routes);
	}

	/**
	     * 
	     */
	private void computeDefaultValues() {
		final Map<Double, Integer> map = new HashMap<Double, Integer>(0);
		final Simulator sim = getSimulator();

		map.clear();
		for (final MapEdge edge : sim.getMapEdges()) {
			final double value = edge.getSpeedLimit();
			Integer ct = map.get(value);
			if (ct == null) {
				ct = 0;
			}
			map.put(value, ct + 1);
		}
		int count = 0;
		for (final Map.Entry<Double, Integer> entry : map.entrySet()) {
			final int ct = entry.getValue();
			if (ct > count) {
				count = ct;
				setDefaultSpeedLimit(entry.getKey());
			}
		}
	}

	/**
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private DocumentBuilder createBuilder() throws ParserConfigurationException, SAXException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final URL url = getClass().getResource(SCHEMA_RESOURCE);
		final Schema schema = schemaFactory.newSchema(url);
		factory.setSchema(schema);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	/**
	 * @param routes
	 */
	private Element createDefaultElement() {
		computeDefaultValues();
		final Element elem = getDocument().createElement(DEFAULT_ELEM);
		elem.appendChild(createDoubleElement(FREQUENCE_ELEM, getSimulator().getFrequence()));
		elem.appendChild(createSpeedElem(getDefaultSpeedLimit()));
		return elem;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	private Element createDoubleElement(final String name, final double value) {
		final Element elem = getDocument().createElement(name);
		final Text text = getDocument().createTextNode(String.valueOf(value));
		elem.appendChild(text);
		return elem;
	}

	/**
	 * @param edge
	 * @return
	 */
	private Element createEdgeElement(final MapEdge edge) {
		final Document doc = getDocument();
		final Element elem = doc.createElement(EDGE_ELEM);
		Element elem1 = doc.createElement(START_ELEM);
		final MapNode begin = edge.getBegin();
		final Map<MapNode, String> map = getNodeMap();
		String id = map.get(begin);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		elem1 = doc.createElement(END_ELEM);
		final MapNode end = edge.getEnd();
		id = map.get(end);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		final double speedLimit = edge.getSpeedLimit();
		if (speedLimit != getDefaultSpeedLimit()) {
			elem.appendChild(createSpeedElem(speedLimit));
		}
		final int priority = edge.getPriority();
		if (priority != 0) {
			elem.appendChild(createIntElement(PRIORITY_ELEM, priority));
		}
		return elem;
	}

	/**
	 * @return
	 */
	private String createID() {
		final int n = getNodeCount();
		setNodeCount(n + 1);
		final StringBuilder bfr = new StringBuilder();
		bfr.append("Node_");
		bfr.append(n);
		return bfr.toString();
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	private Element createIntElement(final String name, final int value) {
		final Element elem = getDocument().createElement(name);
		final Text text = getDocument().createTextNode(String.valueOf(value));
		elem.appendChild(text);
		return elem;
	}

	/**
	 * @param node
	 * @return
	 */
	private Element createNodeElement(final MapNode node) {
		return createNodeElement(NODE_ELEM, node);
	}

	/**
	 * 
	 * @param name
	 * @param node
	 * @return
	 */
	private Element createNodeElement(final String name, final MapNode node) {
		final Element elem = getDocument().createElement(name);
		final String id = createID();
		elem.setAttribute(ID_ATTR, id);
		elem.appendChild(createDoubleElement(X_ELEM, node.getLocation().getX()));
		elem.appendChild(createDoubleElement(Y_ELEM, node.getLocation().getY()));
		getNodeMap().put(node, id);
		return elem;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private Element createPathElement(final Path path) {
		final Document doc = getDocument();
		final Element elem = doc.createElement(PATH_ELEM);
		Element elem1 = doc.createElement(DEPARTURE_ELEM);
		final MapNode begin = path.getDeparture();
		final Map<MapNode, String> map = getNodeMap();
		String id = map.get(begin);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		elem1 = doc.createElement(DESTINATION_ELEM);
		final MapNode end = path.getDestination();
		id = map.get(end);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		elem.appendChild(createDoubleElement(WEIGHT_ELEM, path.getWeight()));
		return elem;
	}

	/**
	 * @param node
	 * @return
	 */
	private Element createSiteElement(final SiteNode node) {
		final Element elem = createNodeElement(SITE_ELEM, node);
		return elem;
	}

	/**
	 * @param speed
	 * @return
	 */
	private Element createSpeedElem(final double speed) {
		return createDoubleElement(SPEED_LIMIT_ELEM, speed * 3.6f);
	}

	/**
	 * @return
	 * @throws TransformerConfigurationException
	 */
	private Transformer createTransfomer() throws TransformerConfigurationException {
		final TransformerFactory factory = TransformerFactory.newInstance();
		final Transformer transformer = factory.newTransformer();
		return transformer;
	}

	/**
	 * @return the builder
	 */
	private DocumentBuilder getBuilder() {
		return builder;
	}

	/**
	 * @return the defaultSpeedLimit
	 */
	private double getDefaultSpeedLimit() {
		return defaultSpeedLimit;
	}

	/**
	 * @return the document
	 */
	private Document getDocument() {
		return document;
	}

	/**
	 * @return the nodeBuilder
	 */
	private MapElementVisitor getNodeBuilder() {
		return nodeBuilder;
	}

	/**
	 * @return the nodeCount
	 */
	private int getNodeCount() {
		return nodeCount;
	}

	/**
	 * @return the nodeMap
	 */
	private Map<MapNode, String> getNodeMap() {
		return nodeMap;
	}

	/**
	 * @return the routesElement
	 */
	private Element getRoutesElement() {
		return routesElement;
	}

	/**
	 * @return the simulator
	 */
	private Simulator getSimulator() {
		return simulator;
	}

	/**
	 * @return the siteBuilder
	 */
	private MapElementVisitor getSiteBuilder() {
		return siteBuilder;
	}

	/**
	 * @return the transformer
	 */
	private Transformer getTransformer() {
		return transformer;
	}

	/**
	 * @param builder the builder to set
	 */
	private void setBuilder(final DocumentBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @param defaultSpeedLimit the defaultSpeedLimit to set
	 */
	private void setDefaultSpeedLimit(final double defaultSpeedLimit) {
		this.defaultSpeedLimit = defaultSpeedLimit;
	}

	/**
	 * @param document the document to set
	 */
	private void setDocument(final Document document) {
		this.document = document;
	}

	/**
	 * @param nodeCount the nodeCount to set
	 */
	private void setNodeCount(final int nodeCount) {
		this.nodeCount = nodeCount;
	}

	/**
	 * @param routesElement the routesElement to set
	 */
	private void setRoutesElement(final Element routesElement) {
		this.routesElement = routesElement;
	}

	/**
	 * @param simulator the simulator to set
	 */
	private void setSimulator(final Simulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * @param transformer the transformer to set
	 */
	private void setTransformer(final Transformer transformer) {
		this.transformer = transformer;
	}
}
