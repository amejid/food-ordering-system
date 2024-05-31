package tech.amejid.order.service.domain.entity;

import java.util.List;

import tech.amejid.domain.entity.AggregateRoot;
import tech.amejid.domain.valueobject.RestaurantId;

public final class Restaurant extends AggregateRoot<RestaurantId> {

	private final List<Product> products;

	private boolean active;

	private Restaurant(Builder builder) {
		super.setId(builder.restaurantId);
		this.products = builder.products;
		this.active = builder.active;
	}

	public static Builder builder() {
		return new Builder();
	}

	public List<Product> getProducts() {
		return this.products;
	}

	public boolean isActive() {
		return this.active;
	}

	public static final class Builder {

		private RestaurantId restaurantId;

		private List<Product> products;

		private boolean active;

		private Builder() {
		}

		public Builder restaurantId(RestaurantId val) {
			this.restaurantId = val;
			return this;
		}

		public Builder products(List<Product> val) {
			this.products = val;
			return this;
		}

		public Builder active(boolean val) {
			this.active = val;
			return this;
		}

		public Restaurant build() {
			return new Restaurant(this);
		}

	}

}
