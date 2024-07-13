package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {

	private final OrderDomainService orderDomainService;

	private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;

	private final OrderSagaHelper orderSagaHelper;

	public OrderPaymentSaga(OrderDomainService orderDomainService,
			OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher,
			OrderSagaHelper orderSagaHelper) {
		this.orderDomainService = orderDomainService;
		this.orderPaidRestaurantRequestMessagePublisher = orderPaidRestaurantRequestMessagePublisher;
		this.orderSagaHelper = orderSagaHelper;
	}

	@Override
	@Transactional
	public OrderPaidEvent process(PaymentResponse paymentResponse) {
		log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
		Order order = this.orderSagaHelper.findOrder(paymentResponse.getOrderId());
		OrderPaidEvent orderPaidEvent = this.orderDomainService.payOrder(order,
				this.orderPaidRestaurantRequestMessagePublisher);
		this.orderSagaHelper.saveOrder(order);
		log.info("Order with id: {} has been paid", order.getId().getValue());
		return orderPaidEvent;
	}

	@Override
	@Transactional
	public EmptyEvent rollback(PaymentResponse paymentResponse) {
		log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
		Order order = this.orderSagaHelper.findOrder(paymentResponse.getOrderId());
		this.orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
		this.orderSagaHelper.saveOrder(order);
		log.info("Order with id: {} has been cancelled", order.getId().getValue());
		return EmptyEvent.INSTANCE;
	}

}
