package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.routes.model.v2.TestUtils.genArguments;
import static org.mmarini.routes.model.v2.TestUtils.genDouble;

import java.awt.geom.Point2D;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.routes.model.Constants;

public class MapEdgeTest implements Constants {

	static DoubleStream distanceRange() {
		return genArguments().mapToDouble(i -> genDouble(i, 0, 100));
	}

	static Stream<Point2D> locationRange() {
		return genArguments().mapToObj(i -> new Point2D.Double(genDouble(i, 0, 100), genDouble(i, 0, 100)));
	}

	@Test
	public void changeNodeBeginLocation() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = MapNode.create(5, 5);
		final MapEdge result = edge.changeNode(begin, newNode);
		assertNotNull(result);
		assertThat(result.getId(), not(equalTo(edge.getId())));
		assertThat(result.getBegin(), sameInstance(newNode));
		assertThat(result.getEnd(), sameInstance(end));
		assertThat(result.getPriority(), equalTo(edge.getPriority()));
		assertThat(result.getSpeedLimit(), equalTo(edge.getSpeedLimit()));
	}

	@Test
	public void changeNodeBeginToNode() {
		final MapNode begin = SiteNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = MapNode.create(0, 0);
		final MapEdge result = edge.changeNode(begin, newNode);
		assertNotNull(result);
		assertThat(result.getId(), equalTo(edge.getId()));
		assertThat(result.getBegin(), sameInstance(newNode));
		assertThat(result.getEnd(), sameInstance(end));
		assertThat(result.getPriority(), equalTo(edge.getPriority()));
		assertThat(result.getSpeedLimit(), equalTo(edge.getSpeedLimit()));
	}

	@Test
	public void changeNodeBeginToSite() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = SiteNode.create(0, 0);
		final MapEdge result = edge.changeNode(begin, newNode);
		assertNotNull(result);
		assertThat(result.getId(), equalTo(edge.getId()));
		assertThat(result.getBegin(), sameInstance(newNode));
		assertThat(result.getEnd(), sameInstance(end));
		assertThat(result.getPriority(), equalTo(edge.getPriority()));
		assertThat(result.getSpeedLimit(), equalTo(edge.getSpeedLimit()));
	}

	@Test
	public void changeNodeEndLocation() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = MapNode.create(5, 5);
		final MapEdge result = edge.changeNode(end, newNode);
		assertNotNull(result);
		assertThat(result.getId(), not(equalTo(edge.getId())));
		assertThat(result.getBegin(), sameInstance(begin));
		assertThat(result.getEnd(), sameInstance(newNode));
		assertThat(result.getPriority(), equalTo(edge.getPriority()));
		assertThat(result.getSpeedLimit(), equalTo(edge.getSpeedLimit()));
	}

	@Test
	public void changeNodeEndToNode() {
		final MapNode begin = SiteNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = MapNode.create(10, 10);
		final MapEdge result = edge.changeNode(end, newNode);
		assertNotNull(result);
		assertThat(result.getId(), equalTo(edge.getId()));
		assertThat(result.getBegin(), sameInstance(begin));
		assertThat(result.getEnd(), sameInstance(newNode));
		assertThat(result.getPriority(), equalTo(edge.getPriority()));
		assertThat(result.getSpeedLimit(), equalTo(edge.getSpeedLimit()));
	}

	@Test
	public void changeNodeEndToSite() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = SiteNode.create(10, 10);
		final MapEdge result = edge.changeNode(end, newNode);
		assertNotNull(result);
		assertThat(result.getId(), equalTo(edge.getId()));
		assertThat(result.getBegin(), sameInstance(begin));
		assertThat(result.getEnd(), sameInstance(newNode));
		assertThat(result.getPriority(), equalTo(edge.getPriority()));
		assertThat(result.getSpeedLimit(), equalTo(edge.getSpeedLimit()));
	}

	@Test
	public void changeNodeNoNode() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end);

		final MapNode newNode = MapNode.create(5, 5);
		final MapEdge result = edge.changeNode(newNode, newNode);
		assertNotNull(result);
		assertThat(result, sameInstance(edge));
	}

	@Test
	public void compareTo() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 0);
		final MapEdge edge1 = MapEdge.create(begin, end);
		final MapEdge edge2 = MapEdge.create(end, begin);

		final int result12 = edge1.compareTo(edge2);
		final int result21 = edge2.compareTo(edge1);

		assertThat(result21, lessThan(0));
		assertThat(result12, greaterThan(0));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getDirection(final Point2D pt) {
		final MapNode begin = MapNode.create(50, 50);
		final MapNode end = MapNode.create(pt);
		final MapEdge edge = MapEdge.create(begin, end);

		final double dx = pt.getX() - 50;
		final double dy = pt.getY() - 50;

		final Point2D result = edge.getDirection();
		assertNotNull(result);
		assertThat(result.getX(), equalTo(dx));
		assertThat(result.getY(), equalTo(dy));
	}

	@ParameterizedTest
	@MethodSource("distanceRange")
	public void getDistance(final double distance) {
		final MapNode begin = MapNode.create(50, 50);
		final MapNode end = MapNode.create(150, 50);
		final MapEdge edge = MapEdge.create(begin, end);

		final Point2D result = edge.getLocation(distance);
		assertNotNull(result);
		assertThat(result.getX(), equalTo(50 + distance));
		assertThat(result.getY(), equalTo(50.0));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getDistanceAlongX(final Point2D pt) {
		final MapNode begin = MapNode.create(0, 50);
		final MapNode end = MapNode.create(100, 50);
		final MapEdge edge = MapEdge.create(begin, end);

		final double result = edge.getDistance(pt);

		assertNotNull(result);
		assertThat(result, closeTo(Math.abs(pt.getY() - 50), 1e-3));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getDistanceAlongY(final Point2D pt) {
		final MapNode begin = MapNode.create(50, 0);
		final MapNode end = MapNode.create(50, 100);
		final MapEdge edge = MapEdge.create(begin, end);

		final double result = edge.getDistance(pt);

		assertNotNull(result);
		assertThat(result, closeTo(Math.abs(pt.getX() - 50), 1e-3));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getDistanceFromBegin(final Point2D pt) {
		final MapNode begin = MapNode.create(100, 100);
		final MapNode end = MapNode.create(200, 200);
		final MapEdge edge = MapEdge.create(begin, end);

		final double result = edge.getDistance(pt);
		final double expected = pt.distance(begin.getLocation());

		assertNotNull(result);
		assertThat(result, closeTo(expected, 1e-3));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getDistanceFromEnd(final Point2D pt) {
		final MapNode begin = MapNode.create(200, 200);
		final MapNode end = MapNode.create(100, 100);
		final MapEdge edge = MapEdge.create(begin, end);

		final double result = edge.getDistance(pt);
		final double expected = pt.distance(end.getLocation());

		assertNotNull(result);
		assertThat(result, closeTo(expected, 1e-3));
	}

	@ParameterizedTest
	@MethodSource("locationRange")
	public void getLength(final Point2D pt) {
		final MapNode begin = MapNode.create(50, 50);
		final MapNode end = MapNode.create(pt);
		final MapEdge edge = MapEdge.create(begin, end);

		final double dx = pt.getX() - 50;
		final double dy = pt.getY() - 50;
		final double expected = Math.sqrt(dx * dx + dy * dy);

		final double result = edge.getLength();
		assertThat(result, closeTo(expected, 1e-3));
	}

	@Test
	public void getShortName() {
		final MapNode begin = MapNode.create(0, 0);
		final MapNode end = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(begin, end).setSpeedLimit(10);
		final String result = edge.getShortName();
		assertThat(result, notNullValue());
		assertThat(result, equalTo("590864"));
	}

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

		assertThat(e1.compareTo(e1), equalTo(0));
		assertThat(e1.compareTo(e11), equalTo(0));
		assertThat(e1.compareTo(e12), equalTo(0));
		assertThat(e1.compareTo(e13), equalTo(0));

		assertThat(e11.compareTo(e1), equalTo(0));
		assertThat(e11.compareTo(e11), equalTo(0));
		assertThat(e11.compareTo(e12), equalTo(0));
		assertThat(e11.compareTo(e13), equalTo(0));

		assertThat(e12.compareTo(e1), equalTo(0));
		assertThat(e12.compareTo(e11), equalTo(0));
		assertThat(e12.compareTo(e12), equalTo(0));
		assertThat(e12.compareTo(e13), equalTo(0));

		assertThat(e13.compareTo(e1), equalTo(0));
		assertThat(e13.compareTo(e11), equalTo(0));
		assertThat(e13.compareTo(e12), equalTo(0));
		assertThat(e13.compareTo(e13), equalTo(0));
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
