package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
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
	public void testEquals() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge e1 = MapEdge.create(begin, end);
		final MapEdge e11 = MapEdge.create(begin, end);
		final MapEdge e12 = e1.setPriority(1);
		final MapEdge e13 = e1.setSpeedLimit(1);

		final MapEdge e2 = MapEdge.create(begin, MapNode.create(20, 20));
		final MapEdge e3 = MapEdge.create(MapNode.create(20, 20), end);

		assertThat(e1, notNullValue());
		assertThat(e11, notNullValue());
		assertThat(e12, notNullValue());
		assertThat(e13, notNullValue());
		assertThat(e2, notNullValue());
		assertThat(e3, notNullValue());

		assertThat(e1, not(sameInstance(e11)));
		assertThat(e1, not(sameInstance(e12)));
		assertThat(e1, not(sameInstance(e13)));
		assertThat(e1, not(sameInstance(e2)));
		assertThat(e1, not(sameInstance(e3)));

		assertFalse(e1.equals(null));
		assertFalse(e1.equals(new Object()));
		assertFalse(e1.equals(e2));
		assertFalse(e1.equals(e3));
		assertFalse(e2.equals(e1));
		assertFalse(e3.equals(e1));

		assertTrue(e1.equals(e1));
		assertTrue(e1.equals(e11));
		assertTrue(e1.equals(e12));
		assertTrue(e1.equals(e13));

		assertTrue(e11.equals(e1));
		assertTrue(e11.equals(e11));
		assertTrue(e11.equals(e12));
		assertTrue(e11.equals(e13));

		assertTrue(e12.equals(e1));
		assertTrue(e12.equals(e11));
		assertTrue(e12.equals(e12));
		assertTrue(e12.equals(e13));

		assertTrue(e13.equals(e1));
		assertTrue(e13.equals(e11));
		assertTrue(e13.equals(e12));
		assertTrue(e13.equals(e13));
	}

	@Test
	public void testHashCode() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge e1 = MapEdge.create(begin, end);
		final MapEdge e11 = MapEdge.create(begin, end);
		final MapEdge e12 = e1.setPriority(1);
		final MapEdge e13 = e1.setSpeedLimit(1);

		assertThat(e1, notNullValue());
		assertThat(e11, notNullValue());
		assertThat(e12, notNullValue());
		assertThat(e13, notNullValue());

		assertThat(e1, not(sameInstance(e11)));
		assertThat(e1, not(sameInstance(e12)));
		assertThat(e1, not(sameInstance(e13)));

		assertThat(e1.hashCode(), equalTo(e11.hashCode()));
		assertThat(e1.hashCode(), equalTo(e12.hashCode()));
		assertThat(e1.hashCode(), equalTo(e13.hashCode()));
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

	@Test
	public void testToString() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		assertThat(edge, notNullValue());
		assertThat(edge, hasToString("MapEdge [590864a2-f026-3db9-9edc-cfc9b60fe90b]"));
	}

}
