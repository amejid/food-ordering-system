package com.food.ordering.system.restaurant.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

public abstract class OrderApprovalEvent implements DomainEvent<OrderApproval> {

	private final OrderApproval orderApproval;

	private final RestaurantId restaurantId;

	private final List<String> failureMessages;

	private final ZonedDateTime createdAt;

	protected OrderApprovalEvent(OrderApproval orderApproval, RestaurantId restaurantId, List<String> failureMessages,
			ZonedDateTime createdAt) {
		this.orderApproval = orderApproval;
		this.restaurantId = restaurantId;
		this.failureMessages = failureMessages;
		this.createdAt = createdAt;
	}

	public OrderApproval getOrderApproval() {
		return this.orderApproval;
	}

	public RestaurantId getRestaurantId() {
		return this.restaurantId;
	}

	public List<String> getFailureMessages() {
		return this.failureMessages;
	}

	public ZonedDateTime getCreatedAt() {
		return this.createdAt;
	}

}
