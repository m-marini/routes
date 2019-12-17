package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mmarini.routes.model.Constants;

public class MapEdgeTest implements Constants {

	@Test
	public void test() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		assertThat(edge, notNullValue());
		assertThat(edge.getBegin(), equalTo(begin));
		assertThat(edge.getEnd(), equalTo(end));
		assertThat(edge.getPriority(), equalTo(DEFAULT_PRIORITY));
		assertThat(edge.getSpeedLimit(), equalTo(DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS));
		assertThat(edge.getId().toString(), equalTo("590864a2-f026-3db9-9edc-cfc9b60fe90b"));
	}

	@Test
	public void testSetPriority() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end).setPriority(1);

		assertThat(edge, notNullValue());
		assertThat(edge.getBegin(), equalTo(begin));
		assertThat(edge.getEnd(), equalTo(end));
		assertThat(edge.getPriority(), equalTo(1));
		assertThat(edge.getSpeedLimit(), equalTo(DEFAULT_SPEED_LIMIT_KMH * KMH_TO_MPS));
		assertThat(edge.getId().toString(), equalTo("590864a2-f026-3db9-9edc-cfc9b60fe90b"));
	}

	@Test
	public void testSetSpeedLimit() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);

		assertThat(edge, notNullValue());
		assertThat(edge.getBegin(), equalTo(begin));
		assertThat(edge.getEnd(), equalTo(end));
		assertThat(edge.getPriority(), equalTo(DEFAULT_PRIORITY));
		assertThat(edge.getSpeedLimit(), equalTo(10.0));
		assertThat(edge.getId().toString(), equalTo("590864a2-f026-3db9-9edc-cfc9b60fe90b"));
	}

}
