package com.food.ordering.system.order.service.dataaccess.outbox.payment.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.order.service.dataaccess.outbox.payment.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

	private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;

	private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

	public PaymentOutboxRepositoryImpl(PaymentOutboxJpaRepository paymentOutboxJpaRepository,
			PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
		this.paymentOutboxJpaRepository = paymentOutboxJpaRepository;
		this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
	}

	@Override
	public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
		return this.paymentOutboxDataAccessMapper.paymentOutboxEntityToOrderPaymentOutboxMessage(
				this.paymentOutboxJpaRepository.save(this.paymentOutboxDataAccessMapper
					.orderPaymentOutboxMessageToOutboxEntity(orderPaymentOutboxMessage)));
	}

	@Override
	public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String sagaType,
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		return Optional.of(this.paymentOutboxJpaRepository
			.findByTypeAndOutboxStatusAndSagaStatusIn(sagaType, outboxStatus, Arrays.asList(sagaStatus))
			.orElseThrow(() -> new PaymentOutboxNotFoundException(
					"Payment outbox object " + "could not be found for saga type " + sagaType))
			.stream()
			.map(this.paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage)
			.toList());
	}

	@Override
	public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId,
			SagaStatus... sagaStatus) {
		return this.paymentOutboxJpaRepository
			.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
			.map(this.paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
	}

	@Override
	public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		this.paymentOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus,
				Arrays.asList(sagaStatus));
	}

}
