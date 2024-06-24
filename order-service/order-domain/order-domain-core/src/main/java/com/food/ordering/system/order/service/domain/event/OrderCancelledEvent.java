package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderCancelledEvent extends OrderEvent {

	private final DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher;

	public OrderCancelledEvent(Order order, ZonedDateTime createdAt,
			DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher) {
		super(order, createdAt);
		this.orderCancelledEventDomainEventPublisher = orderCancelledEventDomainEventPublisher;
	}

	@Override
	public void fire() {
		this.orderCancelledEventDomainEventPublisher.publish(this);

	}

}
