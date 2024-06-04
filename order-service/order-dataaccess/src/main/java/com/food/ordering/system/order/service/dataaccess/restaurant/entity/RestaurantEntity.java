package com.food.ordering.system.order.service.dataaccess.restaurant.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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
@IdClass(RestaurantEntityId.class)
@Table(name = "order_restaurant_m_view", schema = "restaurant")
@Entity
public class RestaurantEntity {

	@Id
	private UUID restaurantId;

	@Id
	private UUID productId;

	private String restaurantName;

	private Boolean restaurantActive;

	private String productName;

	private BigDecimal productPrice;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RestaurantEntity that)) {
			return false;
		}
		return Objects.equals(this.restaurantId, that.restaurantId) && Objects.equals(this.productId, that.productId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.restaurantId, this.productId);
	}

}
