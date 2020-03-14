/*
 * Messages.java
 *
 * $Id: Messages.java,v 1.5 2010/10/19 20:32:59 marco Exp $
 *
 * 04/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.swing.v2;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The messages i18n builder
 */
public interface Messages {
	/** The messages table */
	public static final String BUNDLE_NAME = "org.mmarini.routes.swing.v2.messages"; //$NON-NLS-1$
	/** The bundle of resources */
	public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Returns the internationalized message
	 *
	 * @param key the key message
	 */
	public static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
