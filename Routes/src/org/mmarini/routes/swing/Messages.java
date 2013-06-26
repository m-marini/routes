/*
 * Messages.java
 *
 * $Id: Messages.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 04/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Messages.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 * 
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.mmarini.routes.swing.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	private Messages() {
	}
}
