package com.food.ordering.system.order.service.dataaccess.order.entity;

import java.io.Serializable;
import java.util.Objects;

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
public class OrderItemEntityId implements Serializable {

	private Long id;

	private OrderEntity order;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OrderItemEntityId that)) {
			return false;
		}
		return Objects.equals(this.id, that.id) && Objects.equals(this.order, that.order);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.order);
	}

}
