package tech.amejid.domain.valueobject;

import java.util.Objects;

public abstract class BaseId<T> {

	private final T value;

	protected BaseId(T value) {
		this.value = value;
	}

	public T getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BaseId<?> baseId)) {
			return false;
		}
		return Objects.equals(this.value, baseId.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.value);
	}

}
