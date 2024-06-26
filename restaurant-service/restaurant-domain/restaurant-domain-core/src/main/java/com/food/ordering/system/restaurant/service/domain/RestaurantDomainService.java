package com.food.ordering.system.restaurant.service.domain;

import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;

public interface RestaurantDomainService {

	OrderApprovalEvent validateOrder(Restaurant restaurant, List<String> failureMessages,
			DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher,
			DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher);

}
