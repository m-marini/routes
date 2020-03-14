package org.mmarini.routes.model.v2;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ModuleTest {
	@Test
	public void create() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 10);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(s1, s0);
		final Set<MapEdge> edges = Set.of(e0, e1);

		final MapModule result = new MapModule(edges);

		assertNotNull(result);
		assertThat(result.getEdges(), sameInstance(edges));
	}

	@Test
	public void createMap() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 10);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(s1, s0);
		final Set<MapEdge> edges = Set.of(e0, e1);

		final GeoMap map = GeoMap.create(edges);
		final MapModule result = MapModule.create(map);

		assertNotNull(result);
		assertThat(result.getEdges(), sameInstance(edges));
	}

	@Test
	public void testEquals() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 10);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(s1, s0);

		final MapModule m0 = new MapModule(Set.of(e0, e1));
		final MapModule m1 = new MapModule(Set.of(e0, e1));
		final MapModule m2 = new MapModule(Set.of(e0));
		final MapModule m3 = new MapModule(Set.of(e0, e1)) {
		};

		assertFalse(m0.equals(null));
		assertTrue(m0.equals(m0));
		assertTrue(m0.equals(m1));
		assertFalse(m0.equals(m2));
		assertFalse(m0.equals(m3));

		assertFalse(m1.equals(null));
		assertTrue(m1.equals(m0));
		assertTrue(m1.equals(m1));
		assertFalse(m1.equals(m2));
		assertFalse(m1.equals(m3));

		assertFalse(m1.equals(null));
		assertFalse(m2.equals(m0));
		assertFalse(m2.equals(m1));
		assertTrue(m2.equals(m2));
		assertFalse(m2.equals(m3));

	}

	@Test
	public void testHashCode() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 10);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(s1, s0);

		final MapModule m0 = new MapModule(Set.of(e0, e1));
		final MapModule m1 = new MapModule(Set.of(e0, e1));

		assertThat(m0.hashCode(), equalTo(m1.hashCode()));
	}

	@Test
	public void testToString() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 10);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(s1, s0);
		final Set<MapEdge> edges = Set.of(e0, e1);

		final MapModule result = new MapModule(edges);

		assertNotNull(result);
		assertThat(result, hasToString(matchesPattern("Module \\[edges=\\[MapEdge \\[.*\\], MapEdge \\[.*\\]\\]\\]")));
	}

	@Test
	public void transform() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(10, 0);
		final MapEdge e0 = MapEdge.create(s0, s1);
		final MapEdge e1 = MapEdge.create(s1, s0);

		final MapModule m0 = new MapModule(Set.of(e0, e1));

		final AffineTransform trans = AffineTransform.getTranslateInstance(10, 10);
		trans.rotate(Math.toRadians(60));
//		AffineTransform trans = AffineTransform.getRotateInstance(toRadians(60));
//		trans.translate(10, 10);

		final MapModule result = m0.transform(trans);

		assertNotNull(result);
		assertThat(result, not(equalTo(m0)));
		assertThat(result.getEdges(), hasSize(2));

		final List<MapEdge> edges = List.copyOf(result.getEdges());
		assertThat(edges.get(0).getBeginLocation(), equalTo((Point2D) new Point2D.Double(10, 10)));
		assertThat(edges.get(0).getEndLocation(), equalTo((Point2D) new Point2D.Double(10 + 5, 10 + 10 * sqrt(3) / 2)));
		assertThat(edges.get(1).getBeginLocation(),
				equalTo((Point2D) new Point2D.Double(10 + 5, 10 + 10 * sqrt(3) / 2)));
		assertThat(edges.get(1).getEndLocation(), equalTo((Point2D) new Point2D.Double(10, 10)));
	}
}
