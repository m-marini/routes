/*
 * Dumper.java
 *
 * $Id: Dumper.java,v 1.4 2010/10/19 20:33:00 marco Exp $
 *
 * 14/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mmarini.routes.model.Simulator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Dumper.java,v 1.4 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class Dumper {
	private static Dumper instance = new Dumper();

	/**
	 * @return the instance
	 */
	public static Dumper getInstance() {
		return instance;
	}

	private DocumentBuilder builder;

	/**
         * 
         */
	protected Dumper() {
	}

	/**
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private DocumentBuilder createBuilder()
			throws ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	/**
	 * @param root
	 * @param name
	 * @return
	 */
	public Element createElement(Element root, String name) {
		Element element = root.getOwnerDocument().createElement(name);
		root.appendChild(element);
		return element;
	}

	/**
	 * 
	 * @param file
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void dump(File file, Simulator simulator)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = getBuilder();
		if (getBuilder() == null) {
			builder = createBuilder();
			setBuilder(builder);
		}
		Document document = builder.newDocument();
		Element root = document.createElement("Simulator");
		document.appendChild(root);
		simulator.dump(root);
		Writer out = new FileWriter(file);
		OutputFormat fmt = new OutputFormat(document);
		fmt.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(out, fmt);
		serializer.serialize(document);
		out.close();
	}

	/**
	 * @param root
	 * @param value
	 */
	private void dumpObject(Element root, Object value) {
		if (value == null) {
			root.setAttribute("ref", "null");
		} else {
			root.setAttribute("id",
					"x" + Integer.toHexString(System.identityHashCode(value)));
			dumpValue(root, "class", value.getClass().getName());
			if (value instanceof Dumpable) {
				((Dumpable) value).dump(root);
			} else {
				dumpValue(root, "value", value);
			}
		}
	}

	/**
	 * @param root
	 * @param name
	 * @param iterable
	 */
	public void dumpObject(Element root, String name, Iterable<?> iterable) {
		Element el = createElement(root, name);
		int i = 0;
		for (Object value : iterable) {
			dumpObject(el, name, String.valueOf(i), value);
			++i;
		}
	}

	/**
	 * @param root
	 * @param name
	 * @param object
	 */
	public Element dumpObject(Element root, String name, Object object) {
		Element el = createElement(root, name);
		dumpObject(el, object);
		return el;
	}

	/**
	 * 
	 * @param root
	 * @param name
	 * @param index
	 * @param value
	 */
	public void dumpObject(Element root, String name, String index, Object value) {
		Element el = dumpObject(root, name, value);
		el.setAttribute("index", index);
	}

	/**
	 * @param root
	 * @param name
	 * @param iterable
	 */
	public void dumpReference(Element root, String name, Iterable<?> iterable) {
		Element el = createElement(root, name);
		int i = 0;
		for (Object value : iterable) {
			dumpReference(el, name, String.valueOf(i), value);
			++i;
		}
	}

	/**
	 * @param root
	 * @param name
	 * @param value
	 * @return
	 */
	public Element dumpReference(Element root, String name, Object value) {
		Element el = createElement(root, name);
		if (value == null)
			el.setAttribute("ref", "null");
		else
			el.setAttribute("ref",
					"x" + Integer.toHexString(System.identityHashCode(value)));
		return el;
	}

	/**
	 * @param root
	 * @param name
	 * @param index
	 * @param value
	 */
	public void dumpReference(Element root, String name, String index,
			Object value) {
		Element el = dumpReference(root, name, value);
		el.setAttribute("index", index);
	}

	/**
	 * 
	 * @param root
	 * @param name
	 * @param value
	 * @return
	 */
	private Text dumpValue(Element root, Object value) {
		Document doc = root.getOwnerDocument();
		Text node = doc.createTextNode(String.valueOf(value));
		root.appendChild(node);
		return node;
	}

	/**
	 * 
	 * @param root
	 * @param name
	 * @param value
	 * @return
	 */
	public Element dumpValue(Element root, String name, Object value) {
		Element el = createElement(root, name);
		dumpValue(el, value);
		return el;
	}

	/**
	 * @param root
	 * @param name
	 * @param index
	 * @param value
	 */
	public void dumpValue(Element root, String name, String index, Object value) {
		Element el = dumpValue(root, name, value);
		el.setAttribute("index", index);
	}

	/**
	 * @return the builder
	 */
	private DocumentBuilder getBuilder() {
		return builder;
	}

	/**
	 * @param builder
	 *            the builder to set
	 */
	private void setBuilder(DocumentBuilder builder) {
		this.builder = builder;
	}
}
