package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MapNodeTest {

	@Test
	public void test() {
		final MapNode node = MapNode.create(0, 0);
		assertThat(node, notNullValue());
		assertThat(node.getX(), equalTo(0.0));
		assertThat(node.getY(), equalTo(0.0));
		assertThat(node.getId().toString(), equalTo("4ae71336-e44b-39bf-b9d2-752e234818a5"));
	}
}
