package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.DomainConstants;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.exception.PaymentDomainException;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.repository.OrderOutboxRepository;
import com.food.ordering.system.saga.order.SagaConstants;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderOutboxHelper {

	private final OrderOutboxRepository orderOutboxRepository;

	private final ObjectMapper objectMapper;

	public OrderOutboxHelper(OrderOutboxRepository orderOutboxRepository, ObjectMapper objectMapper) {
		this.orderOutboxRepository = orderOutboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(UUID sagaId,
			PaymentStatus paymentStatus) {
		return this.orderOutboxRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(
				SagaConstants.ORDER_SAGA_NAME, sagaId, paymentStatus, OutboxStatus.COMPLETED);
	}

	@Transactional(readOnly = true)
	public Optional<List<OrderOutboxMessage>> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
		return this.orderOutboxRepository.findByTypeAndOutboxStatus(SagaConstants.ORDER_SAGA_NAME, outboxStatus);
	}

	@Transactional
	public void deleteOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
		this.orderOutboxRepository.deleteByTypeAndOutboxStatus(SagaConstants.ORDER_SAGA_NAME, outboxStatus);
	}

	@Transactional
	public void saveOrderOutboxMessage(OrderEventPayload orderEventPayload, PaymentStatus paymentStatus,
			OutboxStatus outboxStatus, UUID sagaId) {
		save(OrderOutboxMessage.builder()
			.id(UUID.randomUUID())
			.sagaId(sagaId)
			.createdAt(orderEventPayload.getCreatedAt())
			.processedAt(ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)))
			.type(SagaConstants.ORDER_SAGA_NAME)
			.payload(createPayload(orderEventPayload))
			.paymentStatus(paymentStatus)
			.outboxStatus(outboxStatus)
			.build());
	}

	@Transactional
	public void updateOutboxMessage(OrderOutboxMessage orderOutboxMessage, OutboxStatus outboxStatus) {
		orderOutboxMessage.setOutboxStatus(outboxStatus);
		save(orderOutboxMessage);
		log.info("Order outbox table status is updated as: {}", outboxStatus.name());
	}

	private String createPayload(OrderEventPayload orderEventPayload) {
		try {
			return this.objectMapper.writeValueAsString(orderEventPayload);
		}
		catch (JsonProcessingException ex) {
			log.error("Could not create OrderEventPayload json!", ex);
			throw new PaymentDomainException("Could not create OrderEventPayload json!", ex);
		}
	}

	private void save(OrderOutboxMessage orderOutboxMessage) {
		OrderOutboxMessage response = this.orderOutboxRepository.save(orderOutboxMessage);
		if (response == null) {
			log.error("Could not save OrderOutboxMessage!");
			throw new PaymentDomainException("Could not save OrderOutboxMessage!");
		}
		log.info("OrderOutboxMessage is saved with id: {}", orderOutboxMessage.getId());
	}

}
