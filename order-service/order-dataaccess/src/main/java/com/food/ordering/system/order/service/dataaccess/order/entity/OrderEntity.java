package com.food.ordering.system.order.service.dataaccess.order.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.food.ordering.system.domain.valueobject.OrderStatus;
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
@Table(name = "orders")
@Entity
public class OrderEntity {

	@Id
	private UUID id;

	private UUID customerId;

	private UUID restaurantId;

	private UUID trackingId;

	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	private String failureMessages;

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
	private OrderAddressEntity address;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItemEntity> items;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OrderEntity that)) {
			return false;
		}
		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

}
