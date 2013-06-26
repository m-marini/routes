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

	private Map<MapNode, String> nodeMap;

	private int nodeCount;

	private DocumentBuilder builder;

	private Transformer transformer;

	private Element routesElement;

	private MapElementVisitor siteBuilder;

	private MapElementVisitor nodeBuilder;

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
			public void visit(SiteNode node) {
				Element elem = createSiteElement(node);
				getRoutesElement().appendChild(elem);
			}

		};
		nodeBuilder = new MapElementVisitorAdapter() {

			/**
			 * @see org.mmarini.routes.model.MapElementVisitorAdapter#visit(org.mmarini.routes.model.MapNode)
			 */
			@Override
			public void visit(MapNode node) {
				Element elem = createNodeElement(node);
				getRoutesElement().appendChild(elem);
			}

		};
	}

	/**
	 * 
	 * @param document
	 * @param simulator
	 */
	private void build(Document document, Simulator simulator) {
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
	public void build(File file, Simulator simulator)
			throws ParserConfigurationException, TransformerException,
			SAXException {
		DocumentBuilder builder = getBuilder();
		if (getBuilder() == null) {
			builder = createBuilder();
			setBuilder(builder);
		}
		Document document = builder.newDocument();
		build(document, simulator);
		document.normalizeDocument();
		Transformer tr = getTransformer();
		if (tr == null) {
			tr = createTransfomer();
			setTransformer(tr);
		}
		Source source = new DOMSource(document);
		Result result = new StreamResult(file);
		tr.transform(source, result);
	}

	/**
         * 
         */
	private void buildRoutesElement() {
		Element routes = document.createElementNS(URI, "rts:" + ROUTES_ELEM);
		routes.setAttribute("xmlns:xsi",
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		routes.setAttribute("xsi:schemaLocation", SCHEMA_LOCATION);
		setRoutesElement(routes);
		Element defElem = createDefaultElement();
		routes.appendChild(defElem);
		Simulator sim = getSimulator();
		MapElementVisitor builder = getSiteBuilder();
		for (MapNode node : sim.getMapNodes()) {
			node.apply(builder);
		}
		for (Path path : sim.getPaths()) {
			Element elem = createPathElement(path);
			routes.appendChild(elem);
		}
		builder = getNodeBuilder();
		for (MapNode node : sim.getMapNodes()) {
			node.apply(builder);
		}
		for (MapEdge edge : sim.getMapEdges()) {
			Element elem = createEdgeElement(edge);
			routes.appendChild(elem);
		}
		document.appendChild(routes);
	}

	/**
         * 
         */
	private void computeDefaultValues() {
		Map<Double, Integer> map = new HashMap<Double, Integer>(0);
		Simulator sim = getSimulator();

		map.clear();
		for (MapEdge edge : sim.getMapEdges()) {
			double value = edge.getSpeedLimit();
			Integer ct = map.get(value);
			if (ct == null) {
				ct = 0;
			}
			map.put(value, ct + 1);
		}
		int count = 0;
		for (Map.Entry<Double, Integer> entry : map.entrySet()) {
			int ct = entry.getValue();
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
	private DocumentBuilder createBuilder()
			throws ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL url = getClass().getResource(SCHEMA_RESOURCE);
		Schema schema = schemaFactory.newSchema(url);
		factory.setSchema(schema);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	/**
	 * @param routes
	 */
	private Element createDefaultElement() {
		computeDefaultValues();
		Element elem = getDocument().createElement(DEFAULT_ELEM);
		elem.appendChild(createDoubleElement(FREQUENCE_ELEM, getSimulator()
				.getFrequence()));
		elem.appendChild(createSpeedElem(getDefaultSpeedLimit()));
		return elem;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	private Element createDoubleElement(String name, double value) {
		Element elem = getDocument().createElement(name);
		Text text = getDocument().createTextNode(String.valueOf(value));
		elem.appendChild(text);
		return elem;
	}

	/**
	 * @param edge
	 * @return
	 */
	private Element createEdgeElement(MapEdge edge) {
		Document doc = getDocument();
		Element elem = doc.createElement(EDGE_ELEM);
		Element elem1 = doc.createElement(START_ELEM);
		MapNode begin = edge.getBegin();
		Map<MapNode, String> map = getNodeMap();
		String id = map.get(begin);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		elem1 = doc.createElement(END_ELEM);
		MapNode end = edge.getEnd();
		id = map.get(end);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		double speedLimit = edge.getSpeedLimit();
		if (speedLimit != getDefaultSpeedLimit()) {
			elem.appendChild(createSpeedElem(speedLimit));
		}
		int priority = edge.getPriority();
		if (priority != 0) {
			elem.appendChild(createIntElement(PRIORITY_ELEM, priority));
		}
		return elem;
	}

	/**
	 * @return
	 */
	private String createID() {
		int n = getNodeCount();
		setNodeCount(n + 1);
		StringBuilder bfr = new StringBuilder();
		bfr.append("Node_");
		bfr.append(n);
		return bfr.toString();
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	private Element createIntElement(String name, int value) {
		Element elem = getDocument().createElement(name);
		Text text = getDocument().createTextNode(String.valueOf(value));
		elem.appendChild(text);
		return elem;
	}

	/**
	 * @param node
	 * @return
	 */
	private Element createNodeElement(MapNode node) {
		return createNodeElement(NODE_ELEM, node);
	}

	/**
	 * 
	 * @param name
	 * @param node
	 * @return
	 */
	private Element createNodeElement(String name, MapNode node) {
		Element elem = getDocument().createElement(name);
		String id = createID();
		elem.setAttribute(ID_ATTR, id);
		elem
				.appendChild(createDoubleElement(X_ELEM, node.getLocation()
						.getX()));
		elem
				.appendChild(createDoubleElement(Y_ELEM, node.getLocation()
						.getY()));
		getNodeMap().put(node, id);
		return elem;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private Element createPathElement(Path path) {
		Document doc = getDocument();
		Element elem = doc.createElement(PATH_ELEM);
		Element elem1 = doc.createElement(DEPARTURE_ELEM);
		MapNode begin = path.getDeparture();
		Map<MapNode, String> map = getNodeMap();
		String id = map.get(begin);
		elem1.appendChild(doc.createTextNode(id));
		elem.appendChild(elem1);
		elem1 = doc.createElement(DESTINATION_ELEM);
		MapNode end = path.getDestination();
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
	private Element createSiteElement(SiteNode node) {
		Element elem = createNodeElement(SITE_ELEM, node);
		return elem;
	}

	/**
	 * @param speed
	 * @return
	 */
	private Element createSpeedElem(double speed) {
		return createDoubleElement(SPEED_LIMIT_ELEM, speed * 3.6f);
	}

	/**
	 * @return
	 * @throws TransformerConfigurationException
	 */
	private Transformer createTransfomer()
			throws TransformerConfigurationException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
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
	 * @param builder
	 *            the builder to set
	 */
	private void setBuilder(DocumentBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @param defaultSpeedLimit
	 *            the defaultSpeedLimit to set
	 */
	private void setDefaultSpeedLimit(double defaultSpeedLimit) {
		this.defaultSpeedLimit = defaultSpeedLimit;
	}

	/**
	 * @param document
	 *            the document to set
	 */
	private void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * @param nodeCount
	 *            the nodeCount to set
	 */
	private void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	/**
	 * @param routesElement
	 *            the routesElement to set
	 */
	private void setRoutesElement(Element routesElement) {
		this.routesElement = routesElement;
	}

	/**
	 * @param simulator
	 *            the simulator to set
	 */
	private void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * @param transformer
	 *            the transformer to set
	 */
	private void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}
}
