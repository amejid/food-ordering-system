package com.food.ordering.system.restaurant.service.domain;

import java.util.List;

import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;

public interface RestaurantDomainService {

	OrderApprovalEvent validateOrder(Restaurant restaurant, List<String> failureMessages);

}
