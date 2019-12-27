package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Tuple2Test {

	@Test
	public void testEquals() {
		final Tuple2<String, String> t1 = new Tuple2<>("a", "b");
		final Tuple2<String, String> t11 = new Tuple2<>("a", "b");
		final Tuple2<String, String> t2 = new Tuple2<>("a", "c");
		final Tuple2<String, String> t3 = new Tuple2<>("c", "b");
		final Tuple2<String, Double> t4 = new Tuple2<>("c", 1.0);
		final Tuple2<String, String> t5 = new Tuple2<>("a", null);
		final Tuple2<String, String> t51 = new Tuple2<>("a", null);
		final Tuple2<String, String> t6 = new Tuple2<>(null, "b");
		final Tuple2<String, String> t61 = new Tuple2<>(null, "b");

		assertFalse(t1.equals(null));
		assertFalse(t1.equals(new Object()));
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(t4));
		assertFalse(t1.equals(t5));
		assertFalse(t1.equals(t6));
		assertFalse(t2.equals(t1));
		assertFalse(t3.equals(t1));
		assertFalse(t4.equals(t1));
		assertFalse(t5.equals(t1));
		assertFalse(t6.equals(t1));

		assertTrue(t1.equals(t1));
		assertTrue(t1.equals(t11));
		assertTrue(t11.equals(t1));
		assertTrue(t11.equals(t11));
		assertTrue(t5.equals(t51));
		assertTrue(t6.equals(t61));
	}

	@Test
	public void testHashCode() {
		final Tuple2<String, String> t1 = new Tuple2<>("a", "b");
		final Tuple2<String, String> t11 = new Tuple2<>("a", "b");

		final Tuple2<String, String> t5 = new Tuple2<>("a", null);
		final Tuple2<String, String> t51 = new Tuple2<>("a", null);
		final Tuple2<String, String> t6 = new Tuple2<>(null, "b");
		final Tuple2<String, String> t61 = new Tuple2<>(null, "b");

		assertThat(t1.hashCode(), equalTo(t11.hashCode()));
		assertThat(t5.hashCode(), equalTo(t51.hashCode()));
		assertThat(t6.hashCode(), equalTo(t61.hashCode()));
	}

	@Test
	public void testToString() {
		final Tuple2<String, String> t1 = new Tuple2<>("a", "b");

		assertThat(t1, hasToString("(a, b)"));
	}

}
