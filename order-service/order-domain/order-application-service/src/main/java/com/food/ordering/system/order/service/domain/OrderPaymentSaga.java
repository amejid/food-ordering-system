package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.domain.DomainConstants;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
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
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

	private final OrderDomainService orderDomainService;

	private final OrderSagaHelper orderSagaHelper;

	private final PaymentOutboxHelper paymentOutboxHelper;

	private final ApprovalOutboxHelper approvalOutboxHelper;

	private final OrderDataMapper orderDataMapper;

	public OrderPaymentSaga(OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper,
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
	public void process(PaymentResponse paymentResponse) {
		Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse = this.paymentOutboxHelper
			.getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(paymentResponse.getSagaId()),
					SagaStatus.STARTED);

		if (orderPaymentOutboxMessageResponse.isEmpty()) {
			log.info("An outbox message with saga id: {} is already processed", paymentResponse.getSagaId());
			return;
		}

		OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

		OrderPaidEvent orderPaidEvent = completePaymentForOrder(paymentResponse);

		SagaStatus sagaStatus = this.orderSagaHelper
			.orderStatusToSagaStatus(orderPaidEvent.getOrder().getOrderStatus());

		this.paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
				orderPaidEvent.getOrder().getOrderStatus(), sagaStatus));

		this.approvalOutboxHelper.saveApprovalOutboxMessage(
				this.orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
				orderPaidEvent.getOrder().getOrderStatus(), sagaStatus, OutboxStatus.STARTED,
				UUID.fromString(paymentResponse.getSagaId()));

		log.info("Order with id: {} has been paid", orderPaidEvent.getOrder().getId().getValue());
	}

	@Override
	@Transactional
	public void rollback(PaymentResponse paymentResponse) {
		Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse = this.paymentOutboxHelper
			.getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(paymentResponse.getSagaId()),
					getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

		if (orderPaymentOutboxMessageResponse.isEmpty()) {
			log.info("An outbox message with saga id: {} is already rolled back", paymentResponse.getSagaId());
			return;
		}

		OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

		Order order = rollbackPaymentForOrder(paymentResponse);

		SagaStatus sagaStatus = this.orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

		this.paymentOutboxHelper
			.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage, order.getOrderStatus(), sagaStatus));

		if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
			this.approvalOutboxHelper
				.save(getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(), order.getOrderStatus(), sagaStatus));
		}

		log.info("Order with id: {} is cancelled", order.getId().getValue());
	}

	private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
			OrderPaymentOutboxMessage orderPaymentOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
		orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
		orderPaymentOutboxMessage.setOrderStatus(orderStatus);
		orderPaymentOutboxMessage.setSagaStatus(sagaStatus);

		return orderPaymentOutboxMessage;
	}

	private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId, OrderStatus orderStatus,
			SagaStatus sagaStatus) {
		Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = this.approvalOutboxHelper
			.getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(sagaId), SagaStatus.COMPENSATING);

		if (orderApprovalOutboxMessageResponse.isEmpty()) {
			throw new OrderDomainException(
					"Approval outbox message could not be found in " + SagaStatus.COMPENSATING.name() + " state");
		}

		OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

		orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
		orderApprovalOutboxMessage.setOrderStatus(orderStatus);
		orderApprovalOutboxMessage.setSagaStatus(sagaStatus);

		return orderApprovalOutboxMessage;
	}

	private OrderPaidEvent completePaymentForOrder(PaymentResponse paymentResponse) {
		log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
		Order order = this.orderSagaHelper.findOrder(paymentResponse.getOrderId());
		OrderPaidEvent orderPaidEvent = this.orderDomainService.payOrder(order);
		this.orderSagaHelper.saveOrder(order);
		return orderPaidEvent;
	}

	private Order rollbackPaymentForOrder(PaymentResponse paymentResponse) {
		log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
		Order order = this.orderSagaHelper.findOrder(paymentResponse.getOrderId());
		this.orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
		this.orderSagaHelper.saveOrder(order);
		return order;
	}

	private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
		return switch (paymentStatus) {
			case COMPLETED -> new SagaStatus[] { SagaStatus.STARTED };
			case CANCELLED -> new SagaStatus[] { SagaStatus.PROCESSING };
			case FAILED -> new SagaStatus[] { SagaStatus.STARTED, SagaStatus.PROCESSING };
		};
	}

}
