package com.food.ordering.system.order.service.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public class StreetAddress {

	private final UUID id;

	private final String street;

	private final String postalCode;

	private final String city;

	public StreetAddress(UUID id, String street, String postalCode, String city) {
		this.id = id;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
	}

	public UUID getId() {
		return this.id;
	}

	public String getStreet() {
		return this.street;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public String getCity() {
		return this.city;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StreetAddress that)) {
			return false;
		}
		return Objects.equals(this.street, that.street) && Objects.equals(this.postalCode, that.postalCode)
				&& Objects.equals(this.city, that.city);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.street, this.postalCode, this.city);
	}

}
