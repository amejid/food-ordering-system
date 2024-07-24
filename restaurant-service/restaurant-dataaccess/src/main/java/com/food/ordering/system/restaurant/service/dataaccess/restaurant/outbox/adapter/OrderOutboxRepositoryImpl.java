package com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;

import org.springframework.stereotype.Component;

@Component
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

	private final OrderOutboxJpaRepository orderOutboxJpaRepository;

	private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

	public OrderOutboxRepositoryImpl(OrderOutboxJpaRepository orderOutboxJpaRepository,
			OrderOutboxDataAccessMapper orderOutboxDataAccessMapper) {
		this.orderOutboxJpaRepository = orderOutboxJpaRepository;
		this.orderOutboxDataAccessMapper = orderOutboxDataAccessMapper;
	}

	@Override
	public OrderOutboxMessage save(OrderOutboxMessage orderPaymentOutboxMessage) {
		return this.orderOutboxDataAccessMapper.orderOutboxEntityToOrderOutboxMessage(this.orderOutboxJpaRepository
			.save(this.orderOutboxDataAccessMapper.orderOutboxMessageToOutboxEntity(orderPaymentOutboxMessage)));
	}

	@Override
	public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String sagaType, OutboxStatus outboxStatus) {
		return Optional.of(this.orderOutboxJpaRepository.findByTypeAndOutboxStatus(sagaType, outboxStatus)
			.orElseThrow(() -> new OrderOutboxNotFoundException(
					"Approval outbox object " + "cannot be found for saga type " + sagaType))
			.stream()
			.map(this.orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage)
			.collect(Collectors.toList()));
	}

	@Override
	public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId,
			OutboxStatus outboxStatus) {
		return this.orderOutboxJpaRepository.findByTypeAndSagaIdAndOutboxStatus(type, sagaId, outboxStatus)
			.map(this.orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
	}

	@Override
	public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
		this.orderOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
	}

}
