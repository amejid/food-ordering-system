package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.domain.DomainConstants;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

	private final OrderDomainService orderDomainService;

	private final OrderSagaHelper orderSagaHelper;

	private final PaymentOutboxHelper paymentOutboxHelper;

	private final ApprovalOutboxHelper approvalOutboxHelper;

	private final OrderDataMapper orderDataMapper;

	public OrderApprovalSaga(OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper,
			PaymentOutboxHelper paymentOutboxHelper, ApprovalOutboxHelper approvalOutboxHelper,
			OrderDataMapper orderDataMapper) {
		this.orderDomainService = orderDomainService;
		this.orderSagaHelper = orderSagaHelper;
		this.paymentOutboxHelper = paymentOutboxHelper;
		this.approvalOutboxHelper = approvalOutboxHelper;
		this.orderDataMapper = orderDataMapper;
	}

	@Override
	@Transactional
	public void process(RestaurantApprovalResponse restaurantApprovalResponse) {

		Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = this.approvalOutboxHelper
			.getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(restaurantApprovalResponse.getSagaId()),
					SagaStatus.PROCESSING);

		if (orderApprovalOutboxMessageResponse.isEmpty()) {
			log.info("An outbox message with saga id: {} is already processed", restaurantApprovalResponse.getSagaId());
			return;
		}

		OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

		Order order = approveOrder(restaurantApprovalResponse);

		SagaStatus sagaStatus = this.orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

		this.approvalOutboxHelper
			.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage, order.getOrderStatus(), sagaStatus));

		this.paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(restaurantApprovalResponse.getSagaId(),
				order.getOrderStatus(), sagaStatus));

		log.info("Order with id: {} has been approved", order.getId().getValue());
	}

	@Override
	@Transactional
	public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {
		Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = this.approvalOutboxHelper
			.getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(restaurantApprovalResponse.getSagaId()),
					SagaStatus.PROCESSING);

		if (orderApprovalOutboxMessageResponse.isEmpty()) {
			log.info("An outbox message with saga id: {} is already rolled back",
					restaurantApprovalResponse.getSagaId());
			return;
		}

		OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

		OrderCancelledEvent orderCancelledEvent = cancelOrder(restaurantApprovalResponse);

		SagaStatus sagaStatus = this.orderSagaHelper
			.orderStatusToSagaStatus(orderCancelledEvent.getOrder().getOrderStatus());

		this.approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
				orderCancelledEvent.getOrder().getOrderStatus(), sagaStatus));

		this.paymentOutboxHelper.savePaymentOutboxMessage(
				this.orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(orderCancelledEvent),
				orderCancelledEvent.getOrder().getOrderStatus(), sagaStatus, OutboxStatus.STARTED,
				UUID.fromString(restaurantApprovalResponse.getSagaId()));

		log.info("Order with id: {} has been cancelled", orderCancelledEvent.getOrder().getId().getValue());
	}

	private Order approveOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
		log.info("Approving order with id: {}", restaurantApprovalResponse.getOrderId());
		Order order = this.orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
		this.orderDomainService.approveOrder(order);
		this.orderSagaHelper.saveOrder(order);
		return order;
	}

	private OrderCancelledEvent cancelOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
		log.info("Cancelling order with id: {}", restaurantApprovalResponse.getOrderId());
		Order order = this.orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
		OrderCancelledEvent orderCancelledEvent = this.orderDomainService.cancelOrderPayment(order,
				restaurantApprovalResponse.getFailureMessages());
		this.orderSagaHelper.saveOrder(order);

		return orderCancelledEvent;
	}

	private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(
			OrderApprovalOutboxMessage orderApprovalOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
		orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
		orderApprovalOutboxMessage.setOrderStatus(orderStatus);
		orderApprovalOutboxMessage.setSagaStatus(sagaStatus);

		return orderApprovalOutboxMessage;
	}

	private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId, OrderStatus orderStatus,
			SagaStatus sagaStatus) {
		Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse = this.paymentOutboxHelper
			.getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(sagaId), SagaStatus.PROCESSING);

		if (orderPaymentOutboxMessageResponse.isEmpty()) {
			throw new OrderDomainException(
					"Payment outbox message could not be found in " + SagaStatus.PROCESSING.name() + " state");
		}

		OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

		orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
		orderPaymentOutboxMessage.setOrderStatus(orderStatus);
		orderPaymentOutboxMessage.setSagaStatus(sagaStatus);

		return orderPaymentOutboxMessage;
	}

}
