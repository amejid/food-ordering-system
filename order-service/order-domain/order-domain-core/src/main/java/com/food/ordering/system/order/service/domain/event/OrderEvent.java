package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.order.service.domain.entity.Order;

public abstract class OrderEvent implements DomainEvent<Order> {

	private final Order order;

	private final ZonedDateTime createdAt;

	protected OrderEvent(Order order, ZonedDateTime createdAt) {
		this.order = order;
		this.createdAt = createdAt;
	}

	public Order getOrder() {
		return this.order;
	}

	public ZonedDateTime getCreatedAt() {
		return this.createdAt;
	}

}
