package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
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

	/**
	 * <pre>
	 *  -- e1 ->O
	 *          ^
	 *          |
	 *          e2
	 *          |
	 * </pre>
	 */
	@Test
	public void testCross() {
		final MapNode n1 = MapNode.create(0, 0);
		final MapNode n2 = MapNode.create(10, 0);
		final MapNode n3 = MapNode.create(10, 10);
		final MapEdge e1 = MapEdge.create(n1, n2);
		final MapEdge e2 = MapEdge.create(n3, n2);

		final int resulte1e2 = e1.cross(e2);
		assertThat(resulte1e2, lessThan(0));

		final int resulte2e1 = e2.cross(e1);
		assertThat(resulte2e1, greaterThan(0));
	}

	/**
	 * <pre>
	 *  -- e1 ->O
	 *          ^
	 *          |
	 *          e2
	 *          |
	 * </pre>
	 */
	@Test
	public void testCrossFront() {
		final MapNode n1 = MapNode.create(0, 0);
		final MapNode n2 = MapNode.create(10, 0);
		final MapNode n3 = MapNode.create(20, 0);
		final MapEdge e1 = MapEdge.create(n1, n2);
		final MapEdge e2 = MapEdge.create(n3, n2);

		final int resulte1e2 = e1.cross(e2);
		assertThat(resulte1e2, equalTo(0));

		final int resulte2e1 = e2.cross(e1);
		assertThat(resulte2e1, equalTo(0));
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
