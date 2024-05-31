package tech.amejid.domain.entity;

import java.util.Objects;

public abstract class BaseEntity<T> {

	private T id;

	public T getId() {
		return this.id;
	}

	public void setId(T id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BaseEntity<?> that)) {
			return false;
		}
		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

}
