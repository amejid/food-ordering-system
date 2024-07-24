package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {

	private final OrderApprovalSaga orderApprovalSaga;

	public RestaurantApprovalResponseMessageListenerImpl(OrderApprovalSaga orderApprovalSaga) {
		this.orderApprovalSaga = orderApprovalSaga;
	}

	@Override
	public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
		this.orderApprovalSaga.process(restaurantApprovalResponse);
		log.info("Order approved with id: {}", restaurantApprovalResponse.getOrderId());
	}

	@Override
	public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
		this.orderApprovalSaga.rollback(restaurantApprovalResponse);
		log.info("Order Approval Saga rollback operation is completed for order id: {} with failure messages {}",
				restaurantApprovalResponse.getOrderId(),
				String.join(Order.FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponse.getFailureMessages()));
	}

}
