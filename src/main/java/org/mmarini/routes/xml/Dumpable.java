/*
 * Dumpable.java
 *
 * $Id: Dumpable.java,v 1.3 2009/05/08 21:28:51 marco Exp $
 *
 * 14/gen/09
 *
 * Copyright notice
 */
package org.mmarini.routes.xml;

import org.w3c.dom.Element;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: Dumpable.java,v 1.3 2009/05/08 21:28:51 marco Exp $
 * 
 */
public interface Dumpable {
	/**
	 * 
	 * @param root
	 * @return
	 */
	public abstract void dump(Element root);
}
