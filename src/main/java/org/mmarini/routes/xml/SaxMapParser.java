/*
 * SaxMapParser.java
 *
 * $Id: SaxMapParser.java,v 1.4 2010/10/19 20:33:00 marco Exp $
 *
 * 01/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SaxMapParser.java,v 1.4 2010/10/19 20:33:00 marco Exp $
 * 
 */
public class SaxMapParser implements XmlConstants {
	private static SaxMapParser instance = new SaxMapParser();

	/**
	 * @return the instance
	 */
	public static SaxMapParser getInstance() {
		return instance;
	}

	private SAXParserFactory factory;

	/**
	     * 
	     */
	protected SaxMapParser() {
	}

	/**
	 * 
	 * @return
	 * @throws SAXException
	 */
	private SAXParserFactory createFactory() throws SAXException {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final URL url = getClass().getResource(SCHEMA_RESOURCE);
		final Schema schema = schemaFactory.newSchema(url);
		factory.setSchema(schema);
		factory.setNamespaceAware(true);
		return factory;
	}

	/**
	 * @param builder
	 * @return
	 */
	private RouteHandler createHandler(final MapBuilder builder) {
		final RouteHandler handler = new RouteHandler();
		handler.setBuilder(builder);
		return handler;
	}

	/**
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private SAXParser createParser() throws ParserConfigurationException, SAXException {
		final SAXParser parser = retrieveFactory().newSAXParser();
		return parser;
	}

	/**
	 * @return the factory
	 */
	private SAXParserFactory getFactory() {
		return factory;
	}

	/**
	 * @param file
	 * @param builder
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void parse(final File file, final MapBuilder builder)
			throws ParserConfigurationException, SAXException, IOException {
		final SAXParser parser = createParser();
		final RouteHandler handler = createHandler(builder);
		parser.parse(file, handler);
	}

	/**
	 * 
	 * @param builder
	 * @param url
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void parse(final MapBuilder builder, final URL url)
			throws ParserConfigurationException, SAXException, IOException {
		final SAXParser parser = createParser();
		final RouteHandler handler = createHandler(builder);
		parser.parse(url.openStream(), handler);
	}

	/**
	 * 
	 * @return
	 * @throws SAXException
	 */
	private SAXParserFactory retrieveFactory() throws SAXException {
		SAXParserFactory factory = getFactory();
		if (factory == null) {
			factory = createFactory();
			setFactory(factory);
		}
		return factory;
	}

	/**
	 * @param factory the factory to set
	 */
	private void setFactory(final SAXParserFactory factory) {
		this.factory = factory;
	}

}
