package com.food.ordering.system.order.service.dataaccess.order.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@IdClass(OrderItemEntityId.class)
@Table(name = "order_items")
@Entity
public class OrderItemEntity {

	@Id
	private Long id;

	@Id
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "order_id")
	private OrderEntity order;

	private UUID productId;

	private BigDecimal price;

	private Integer quantity;

	private BigDecimal subTotal;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OrderItemEntity that)) {
			return false;
		}
		return Objects.equals(this.id, that.id) && Objects.equals(this.order, that.order);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.order);
	}

}
