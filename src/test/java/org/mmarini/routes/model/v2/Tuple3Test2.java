package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Tuple3Test2 {

	@Test
	public void testEquals() {
		final Tuple3<String, String, String> t1 = new Tuple3<>("a", "b", "c");
		final Tuple3<String, String, String> t11 = new Tuple3<>("a", "b", "c");
		final Tuple3<String, String, String> t2 = new Tuple3<>("a", "d", "c");
		final Tuple3<String, String, String> t3 = new Tuple3<>("d", "b", "c");
		final Tuple3<String, String, String> t_1 = new Tuple3<>("a", "b", "d");
		final Tuple3<String, String, Double> t4 = new Tuple3<>("a", "c", 1.0);
		final Tuple3<String, Double, String> t5 = new Tuple3<>("a", 1.0, "c");
		final Tuple3<Double, String, String> t6 = new Tuple3<>(1.0, "a", "c");
		final Tuple3<String, String, String> t7 = new Tuple3<>("a", "b", null);
		final Tuple3<String, String, String> t71 = new Tuple3<>("a", "b", null);
		final Tuple3<String, String, String> t8 = new Tuple3<>("a", null, "c");
		final Tuple3<String, String, String> t81 = new Tuple3<>("a", null, "c");
		final Tuple3<String, String, String> t9 = new Tuple3<>(null, "b", "c");
		final Tuple3<String, String, String> t91 = new Tuple3<>(null, "b", "c");

		assertFalse(t1.equals(null));
		assertFalse(t1.equals(new Object()));
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(t4));
		assertFalse(t1.equals(t5));
		assertFalse(t1.equals(t6));
		assertFalse(t1.equals(t6));
		assertFalse(t1.equals(t8));
		assertFalse(t1.equals(t9));
		assertFalse(t1.equals(t_1));
		assertFalse(t2.equals(t1));
		assertFalse(t3.equals(t1));
		assertFalse(t4.equals(t1));
		assertFalse(t5.equals(t1));
		assertFalse(t6.equals(t1));
		assertFalse(t7.equals(t1));
		assertFalse(t8.equals(t1));
		assertFalse(t9.equals(t1));
		assertFalse(t_1.equals(t1));

		assertTrue(t1.equals(t1));
		assertTrue(t1.equals(t11));
		assertTrue(t11.equals(t1));
		assertTrue(t11.equals(t11));
		assertTrue(t7.equals(t7));
		assertTrue(t7.equals(t71));
		assertTrue(t71.equals(t7));
		assertTrue(t71.equals(t71));
		assertTrue(t8.equals(t8));
		assertTrue(t8.equals(t81));
		assertTrue(t81.equals(t8));
		assertTrue(t81.equals(t81));
		assertTrue(t9.equals(t9));
		assertTrue(t9.equals(t91));
		assertTrue(t91.equals(t9));
		assertTrue(t91.equals(t91));
	}

	@Test
	public void testHashCode() {
		final Tuple3<String, String, String> t1 = new Tuple3<>("a", "b", "c");
		final Tuple3<String, String, String> t11 = new Tuple3<>("a", "b", "c");
		final Tuple3<String, String, String> t7 = new Tuple3<>("a", "b", null);
		final Tuple3<String, String, String> t71 = new Tuple3<>("a", "b", null);
		final Tuple3<String, String, String> t8 = new Tuple3<>("a", null, "c");
		final Tuple3<String, String, String> t81 = new Tuple3<>("a", null, "c");
		final Tuple3<String, String, String> t9 = new Tuple3<>(null, "b", "c");
		final Tuple3<String, String, String> t91 = new Tuple3<>(null, "b", "c");

		assertThat(t1.hashCode(), equalTo(t11.hashCode()));
		assertThat(t7.hashCode(), equalTo(t71.hashCode()));
		assertThat(t8.hashCode(), equalTo(t81.hashCode()));
		assertThat(t9.hashCode(), equalTo(t91.hashCode()));
	}

	@Test
	public void testToString() {
		final Tuple3<String, String, String> t1 = new Tuple3<>("a", "b", "c");

		assertThat(t1, hasToString("(a, b, c)"));
	}

}
