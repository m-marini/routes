/**
 *
 */
package org.mmarini.routes.model.v2;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

/**
 *
 */
public class Tuple2<T1, T2> {
	private final T1 elem1;
	private final T2 elem2;

	/**
	 *
	 * @param elem1
	 * @param elem2
	 */
	public Tuple2(final T1 elem1, final T2 elem2) {
		super();
		this.elem1 = elem1;
		this.elem2 = elem2;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
		if (elem1 == null) {
			if (other.elem1 != null) {
				return false;
			}
		} else if (!elem1.equals(other.elem1)) {
			return false;
		}
		if (elem2 == null) {
			if (other.elem2 != null) {
				return false;
			}
		} else if (!elem2.equals(other.elem2)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the element 1
	 */
	public T1 getElem1() {
		return elem1;
	}

	/**
	 * @return the element 2
	 */
	public T2 getElem2() {
		return elem2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elem1 == null) ? 0 : elem1.hashCode());
		result = prime * result + ((elem2 == null) ? 0 : elem2.hashCode());
		return result;
	}

	/**
	 *
	 * @return
	 */
	public Entry<T1, T2> toEntry() {
		return new SimpleImmutableEntry<T1, T2>(elem1, elem2);
	}

	@Override
	public String toString() {
		return "Tuple2 [" + elem1 + ", " + elem2 + "]";
	}
}
