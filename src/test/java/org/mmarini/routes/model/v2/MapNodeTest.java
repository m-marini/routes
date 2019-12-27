package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MapNodeTest {

	@Test
	public void test() {
		final MapNode node = MapNode.create(0, 0);
		assertThat(node, notNullValue());
		assertThat(node.getX(), equalTo(0.0));
		assertThat(node.getY(), equalTo(0.0));
		assertThat(node.getId().toString(), equalTo("4ae71336-e44b-39bf-b9d2-752e234818a5"));
	}

	@Test
	public void testEquals() {
		final MapNode node1 = MapNode.create(0, 0);
		final MapNode node11 = MapNode.create(0, 0);
		final MapNode node2 = MapNode.create(0, 10);

		assertNotNull(node1);
		assertNotNull(node2);

		assertFalse(node1.equals(null));
		assertFalse(node1.equals(new Object()));
		assertFalse(node1.equals(node2));
		assertFalse(node2.equals(node1));

		assertTrue(node1.equals(node1));
		assertTrue(node1.equals(node11));

		assertTrue(node11.equals(node1));
		assertTrue(node11.equals(node11));
	}

	@Test
	public void testHashcode() {
		final MapNode node1 = MapNode.create(0, 0);
		final MapNode node11 = MapNode.create(0, 0);

		assertNotNull(node1);

		assertThat(node1.hashCode(), equalTo(node11.hashCode()));
	}

	@Test
	public void testToString() {
		final MapNode node = MapNode.create(0, 0);
		assertThat(node, notNullValue());
		assertThat(node, hasToString("MapNode [4ae71336-e44b-39bf-b9d2-752e234818a5, 0.0, 0.0]"));
	}
}
