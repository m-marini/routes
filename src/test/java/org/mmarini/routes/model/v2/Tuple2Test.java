package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Tuple2Test {

	@Test
	public void create() {
		final Tuple2<String, String> t1 = Tuple.of("a", "b");
		assertThat(t1.get1(), equalTo("a"));
		assertThat(t1.get2(), equalTo("b"));
	}

	@Test
	public void testEquals() {
		final Tuple2<String, String> t1 = Tuple.of("a", "b");
		final Tuple2<String, String> t11 = Tuple.of("a", "b");
		final Tuple2<String, String> t2 = Tuple.of("a", "c");
		final Tuple2<String, String> t3 = Tuple.of("c", "b");
		final Tuple2<String, Double> t4 = Tuple.of("c", 1.0);

		assertFalse(t1.equals(null));
		assertFalse(t1.equals(new Object()));
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(t4));
		assertFalse(t2.equals(t1));
		assertFalse(t3.equals(t1));
		assertFalse(t4.equals(t1));

		assertTrue(t1.equals(t1));
		assertTrue(t1.equals(t11));
		assertTrue(t11.equals(t1));
		assertTrue(t11.equals(t11));
	}

	@Test
	public void testHashCode() {
		final Tuple2<String, String> t1 = Tuple.of("a", "b");
		final Tuple2<String, String> t11 = Tuple.of("a", "b");

		assertThat(t1.hashCode(), equalTo(t11.hashCode()));
	}

	@Test
	public void testToString() {
		final Tuple2<String, String> t1 = Tuple.of("a", "b");

		assertThat(t1, hasToString("(a, b)"));
	}

}
