package com.food.ordering.system.order.service.dataaccess.restaurant.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEntityId implements Serializable {

	private UUID restaurantId;

	private UUID productId;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RestaurantEntityId that)) {
			return false;
		}
		return Objects.equals(this.restaurantId, that.restaurantId) && Objects.equals(this.productId, that.productId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.restaurantId, this.productId);
	}

}
