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
	private DocumentBuilder createBuilder() throws ParserConfigurationException, SAXException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	/**
	 * @param root
	 * @param name
	 * @return
	 */
	public Element createElement(final Element root, final String name) {
		final Element element = root.getOwnerDocument().createElement(name);
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
	public void dump(final File file, final Simulator simulator)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = getBuilder();
		if (getBuilder() == null) {
			builder = createBuilder();
			setBuilder(builder);
		}
		final Document document = builder.newDocument();
		final Element root = document.createElement("Simulator");
		document.appendChild(root);
		simulator.dump(root);
		final Writer out = new FileWriter(file);
		final OutputFormat fmt = new OutputFormat(document);
		fmt.setIndenting(true);
		final XMLSerializer serializer = new XMLSerializer(out, fmt);
		serializer.serialize(document);
		out.close();
	}

	/**
	 * @param root
	 * @param value
	 */
	private void dumpObject(final Element root, final Object value) {
		if (value == null) {
			root.setAttribute("ref", "null");
		} else {
			root.setAttribute("id", "x" + Integer.toHexString(System.identityHashCode(value)));
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
	public void dumpObject(final Element root, final String name, final Iterable<?> iterable) {
		final Element el = createElement(root, name);
		int i = 0;
		for (final Object value : iterable) {
			dumpObject(el, name, String.valueOf(i), value);
			++i;
		}
	}

	/**
	 * @param root
	 * @param name
	 * @param object
	 */
	public Element dumpObject(final Element root, final String name, final Object object) {
		final Element el = createElement(root, name);
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
	public void dumpObject(final Element root, final String name, final String index, final Object value) {
		final Element el = dumpObject(root, name, value);
		el.setAttribute("index", index);
	}

	/**
	 * @param root
	 * @param name
	 * @param iterable
	 */
	public void dumpReference(final Element root, final String name, final Iterable<?> iterable) {
		final Element el = createElement(root, name);
		int i = 0;
		for (final Object value : iterable) {
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
	public Element dumpReference(final Element root, final String name, final Object value) {
		final Element el = createElement(root, name);
		if (value == null) {
			el.setAttribute("ref", "null");
		} else {
			el.setAttribute("ref", "x" + Integer.toHexString(System.identityHashCode(value)));
		}
		return el;
	}

	/**
	 * @param root
	 * @param name
	 * @param index
	 * @param value
	 */
	public void dumpReference(final Element root, final String name, final String index, final Object value) {
		final Element el = dumpReference(root, name, value);
		el.setAttribute("index", index);
	}

	/**
	 * 
	 * @param root
	 * @param name
	 * @param value
	 * @return
	 */
	private Text dumpValue(final Element root, final Object value) {
		final Document doc = root.getOwnerDocument();
		final Text node = doc.createTextNode(String.valueOf(value));
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
	public Element dumpValue(final Element root, final String name, final Object value) {
		final Element el = createElement(root, name);
		dumpValue(el, value);
		return el;
	}

	/**
	 * @param root
	 * @param name
	 * @param index
	 * @param value
	 */
	public void dumpValue(final Element root, final String name, final String index, final Object value) {
		final Element el = dumpValue(root, name, value);
		el.setAttribute("index", index);
	}

	/**
	 * @return the builder
	 */
	private DocumentBuilder getBuilder() {
		return builder;
	}

	/**
	 * @param builder the builder to set
	 */
	private void setBuilder(final DocumentBuilder builder) {
		this.builder = builder;
	}
}
