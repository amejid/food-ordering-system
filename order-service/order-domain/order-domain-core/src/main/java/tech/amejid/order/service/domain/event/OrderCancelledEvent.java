package tech.amejid.order.service.domain.event;

import java.time.ZonedDateTime;

import tech.amejid.order.service.domain.entity.Order;

public class OrderCancelledEvent extends OrderEvent {

	public OrderCancelledEvent(Order order, ZonedDateTime createdAt) {
		super(order, createdAt);
	}

}
