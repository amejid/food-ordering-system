package com.food.ordering.system.order.service.domain;

import java.util.UUID;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderCreateCommandHandler {

	private final OrderCreateHelper orderCreateHelper;

	private final OrderDataMapper orderDataMapper;

	private final PaymentOutboxHelper paymentOutboxHelper;

	private final OrderSagaHelper orderSagaHelper;

	public OrderCreateCommandHandler(OrderCreateHelper orderCreateHelper, OrderDataMapper orderDataMapper,
			PaymentOutboxHelper paymentOutboxHelper, OrderSagaHelper orderSagaHelper) {
		this.orderCreateHelper = orderCreateHelper;
		this.orderDataMapper = orderDataMapper;
		this.paymentOutboxHelper = paymentOutboxHelper;
		this.orderSagaHelper = orderSagaHelper;
	}

	@Transactional
	public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
		OrderCreatedEvent orderCreatedEvent = this.orderCreateHelper.persistOrder(createOrderCommand);
		log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
		CreateOrderResponse createOrderResponse = this.orderDataMapper
			.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order Created Successfully");

		this.paymentOutboxHelper.savePaymentOutboxMessage(
				this.orderDataMapper.orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
				orderCreatedEvent.getOrder().getOrderStatus(),
				this.orderSagaHelper.orderStatusToSagaStatus(orderCreatedEvent.getOrder().getOrderStatus()),
				OutboxStatus.STARTED, UUID.randomUUID());

		log.info("Returning CreateOrderResponse with order id: {}", orderCreatedEvent.getOrder().getId());

		return createOrderResponse;
	}

}
