/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */
package org.mmarini.routes.model;

import java.awt.geom.Point2D;

/**
 * @author marco.marini@mmarini.org
 * @version $Id: SiteNode.java,v 1.10 2010/10/19 20:33:00 marco Exp $
 *
 */
public class SiteNode extends MapNode {

	/**
	     *
	     */
	public SiteNode() {
	}

	/**
	 * @param node
	 */
	public SiteNode(final SiteNode node) {
		super(node);
	}

	/**
	 * @see org.mmarini.routes.model.MapElement#apply(org.mmarini.routes.model.MapElementVisitor)
	 */
	@Override
	public void apply(final MapElementVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @see org.mmarini.routes.model.MapNode#clone()
	 */
	@Override
	public Object clone() {
		return new SiteNode(this);
	}

	/**
	 *
	 * @return
	 */
	public SiteNode createClone() {
		final SiteNode site = new SiteNode();
		return site;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Site(");
		final Point2D location = getLocation();
		builder.append(location.getX());
		builder.append(",");
		builder.append(location.getY());
		builder.append(")");
		return builder.toString();
	}
}
