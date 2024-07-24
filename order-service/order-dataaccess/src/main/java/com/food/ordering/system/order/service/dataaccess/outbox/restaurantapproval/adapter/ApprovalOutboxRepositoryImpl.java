package com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.exception.ApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.repository.ApprovalOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import org.springframework.stereotype.Component;

@Component
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {

	private final ApprovalOutboxJpaRepository approvalOutboxJpaRepository;

	private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

	public ApprovalOutboxRepositoryImpl(ApprovalOutboxJpaRepository approvalOutboxJpaRepository,
			ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper) {
		this.approvalOutboxJpaRepository = approvalOutboxJpaRepository;
		this.approvalOutboxDataAccessMapper = approvalOutboxDataAccessMapper;
	}

	@Override
	public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
		return this.approvalOutboxDataAccessMapper.approvalOutboxEntityToOrderApprovalOutboxMessage(
				this.approvalOutboxJpaRepository.save(this.approvalOutboxDataAccessMapper
					.orderCreatedOutboxMessageToOutboxEntity(orderApprovalOutboxMessage)));
	}

	@Override
	public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String sagaType,
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		return Optional.of(this.approvalOutboxJpaRepository
			.findByTypeAndOutboxStatusAndSagaStatusIn(sagaType, outboxStatus, Arrays.asList(sagaStatus))
			.orElseThrow(() -> new ApprovalOutboxNotFoundException(
					"Approval outbox object " + "could be found for saga type " + sagaType))
			.stream()
			.map(this.approvalOutboxDataAccessMapper::approvalOutboxEntityToOrderApprovalOutboxMessage)
			.toList());
	}

	@Override
	public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId,
			SagaStatus... sagaStatus) {
		return this.approvalOutboxJpaRepository
			.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
			.map(this.approvalOutboxDataAccessMapper::approvalOutboxEntityToOrderApprovalOutboxMessage);

	}

	@Override
	public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		this.approvalOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus,
				Arrays.asList(sagaStatus));
	}

}
