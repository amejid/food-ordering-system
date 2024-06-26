package com.food.ordering.system.restaurant.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

public class OrderApprovedEvent extends OrderApprovalEvent {

	private final DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher;

	public OrderApprovedEvent(OrderApproval orderApproval, RestaurantId restaurantId, List<String> failureMessages,
			ZonedDateTime createdAt, DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher) {
		super(orderApproval, restaurantId, failureMessages, createdAt);
		this.orderApprovedEventDomainEventPublisher = orderApprovedEventDomainEventPublisher;
	}

	@Override
	public void fire() {
		this.orderApprovedEventDomainEventPublisher.publish(this);
	}

}
