package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.event.payload.OrderApprovalEventPayload;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstants;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ApprovalOutboxHelper {

	private final ApprovalOutboxRepository approvalOutboxRepository;

	private final ObjectMapper objectMapper;

	public ApprovalOutboxHelper(ApprovalOutboxRepository approvalOutboxRepository, ObjectMapper objectMapper) {
		this.approvalOutboxRepository = approvalOutboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public Optional<List<OrderApprovalOutboxMessage>> getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		return this.approvalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(SagaConstants.ORDER_SAGA_NAME,
				outboxStatus, sagaStatus);
	}

	@Transactional(readOnly = true)
	public Optional<OrderApprovalOutboxMessage> getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID sagaId,
			SagaStatus... sagaStatus) {
		return this.approvalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(SagaConstants.ORDER_SAGA_NAME, sagaId,
				sagaStatus);
	}

	@Transactional
	public void save(OrderApprovalOutboxMessage orderPaymentOutboxMessage) {
		OrderApprovalOutboxMessage response = this.approvalOutboxRepository.save(orderPaymentOutboxMessage);

		if (response == null) {
			log.error("Could not save OrderApprovalOutboxMessage with outbox id: {}",
					orderPaymentOutboxMessage.getId());
			throw new OrderDomainException(
					"Could not save OrderApprovalOutboxMessage with outbox id: " + orderPaymentOutboxMessage.getId());
		}

		log.info("OrderApprovalOutboxMessage with outbox id: {} saved successfully!",
				orderPaymentOutboxMessage.getId());
	}

	@Transactional
	public void saveApprovalOutboxMessage(OrderApprovalEventPayload orderApprovalEventPayload, OrderStatus orderStatus,
			SagaStatus sagaStatus, OutboxStatus outboxStatus, UUID sagaId) {
		save(OrderApprovalOutboxMessage.builder()
			.id(UUID.randomUUID())
			.sagaId(sagaId)
			.createdAt(orderApprovalEventPayload.getCreatedAt())
			.type(SagaConstants.ORDER_SAGA_NAME)
			.payload(createPayload(orderApprovalEventPayload))
			.orderStatus(orderStatus)
			.sagaStatus(sagaStatus)
			.outboxStatus(outboxStatus)
			.build());
	}

	@Transactional
	public void deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		this.approvalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(SagaConstants.ORDER_SAGA_NAME,
				outboxStatus, sagaStatus);
	}

	private String createPayload(OrderApprovalEventPayload orderApprovalEventPayload) {
		try {
			return this.objectMapper.writeValueAsString(orderApprovalEventPayload);
		}
		catch (JsonProcessingException ex) {
			log.error("Could not serialize OrderApprovalEventPayload for order id: {}",
					orderApprovalEventPayload.getOrderId(), ex);
			throw new OrderDomainException("Could not serialize OrderApprovalEventPayload for order id: "
					+ orderApprovalEventPayload.getOrderId(), ex);
		}
	}

}
