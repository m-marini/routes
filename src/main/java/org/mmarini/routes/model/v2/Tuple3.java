/**
 *
 */
package org.mmarini.routes.model.v2;

/**
 *
 */
public class Tuple3<T1, T2, T3> {
	private final T1 elem1;
	private final T2 elem2;
	private final T3 elem3;

	/**
	 * @param elem1
	 * @param elem2
	 * @param elem3
	 */
	public Tuple3(final T1 elem1, final T2 elem2, final T3 elem3) {
		super();
		this.elem1 = elem1;
		this.elem2 = elem2;
		this.elem3 = elem3;
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
		final Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) obj;
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
		if (elem3 == null) {
			if (other.elem3 != null) {
				return false;
			}
		} else if (!elem3.equals(other.elem3)) {
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

	/**
	 * @return the elem3
	 */
	public T3 getElem3() {
		return elem3;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elem1 == null) ? 0 : elem1.hashCode());
		result = prime * result + ((elem2 == null) ? 0 : elem2.hashCode());
		result = prime * result + ((elem3 == null) ? 0 : elem3.hashCode());
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("(").append(elem1).append(", ").append(elem2).append(", ").append(elem3).append(")");
		return builder.toString();
	}
}
