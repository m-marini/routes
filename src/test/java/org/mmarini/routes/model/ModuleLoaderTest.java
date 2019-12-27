package org.mmarini.routes.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ModuleLoaderTest {

	@Test
	public void test() throws JsonProcessingException, IOException {
		final File file = new File("src/test/resources/module.yml");

		final Module m = ModuleLoader.load(file);
		assertThat(m, notNullValue());

		final Iterable<MapNode> nodes = m.getNodes();
		assertThat(nodes,
				containsInAnyOrder(hasProperty("location", equalTo(new Point2D.Double(-9, -83))),
						hasProperty("location", equalTo(new Point2D.Double(-12, -25))),
						hasProperty("location", equalTo(new Point2D.Double(-24, -50)))));

		final Iterator<MapEdge> edges = m.getEdges().iterator();
		assertTrue(edges.hasNext());
		{
			final MapEdge edge = edges.next();
			assertThat(edge, hasProperty("beginLocation", equalTo(new Point2D.Double(-9, -83))));
			assertThat(edge, hasProperty("endLocation", equalTo(new Point2D.Double(-24, -50))));
			assertThat(edge, hasProperty("priority", equalTo(0)));
			assertThat(edge, hasProperty("speedLimit", closeTo(130.0 / 3.6, 0.1)));
		}
		assertTrue(edges.hasNext());
		{
			final MapEdge edge = edges.next();
			assertThat(edge, hasProperty("beginLocation", equalTo(new Point2D.Double(-9, -83))));
			assertThat(edge, hasProperty("endLocation", equalTo(new Point2D.Double(-12, -25))));
			assertThat(edge, hasProperty("priority", equalTo(1)));
			assertThat(edge, hasProperty("speedLimit", closeTo(100.0 / 3.6, 0.1)));
		}
		assertFalse(edges.hasNext());
	}
}
