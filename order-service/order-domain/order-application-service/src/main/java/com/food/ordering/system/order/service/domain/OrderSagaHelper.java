package com.food.ordering.system.order.service.domain;

import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderSagaHelper {

	private final OrderRepository orderRepository;

	public OrderSagaHelper(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	Order findOrder(String orderId) {
		Optional<Order> orderResponse = this.orderRepository.findById(new OrderId(UUID.fromString(orderId)));

		if (orderResponse.isEmpty()) {
			log.error("Order with id: {} could not be found!", orderId);
			throw new OrderNotFoundException("Order with id: " + orderId + " could not be found!");
		}
		return orderResponse.get();
	}

	void saveOrder(Order order) {
		this.orderRepository.save(order);
	}

	SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
		return switch (orderStatus) {
			case PAID -> SagaStatus.PROCESSING;
			case APPROVED -> SagaStatus.SUCCEEDED;
			case CANCELLING -> SagaStatus.COMPENSATING;
			case CANCELLED -> SagaStatus.COMPENSATED;
			default -> SagaStatus.STARTED;
		};
	}

}
