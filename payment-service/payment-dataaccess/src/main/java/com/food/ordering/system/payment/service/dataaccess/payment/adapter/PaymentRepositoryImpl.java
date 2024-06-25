package com.food.ordering.system.payment.service.dataaccess.payment.adapter;

import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.payment.service.dataaccess.payment.mapper.PaymentDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.payment.repository.PaymentJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;

import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

	private final PaymentJpaRepository paymentJpaRepository;

	private final PaymentDataAccessMapper paymentDataAccessMapper;

	public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository,
			PaymentDataAccessMapper paymentDataAccessMapper) {
		this.paymentJpaRepository = paymentJpaRepository;
		this.paymentDataAccessMapper = paymentDataAccessMapper;
	}

	@Override
	public Payment save(Payment payment) {
		return this.paymentDataAccessMapper.paymentEntityToPayment(
				this.paymentJpaRepository.save(this.paymentDataAccessMapper.paymentToPaymentEntity(payment)));
	}

	@Override
	public Optional<Payment> findByOrderId(UUID orderId) {
		return this.paymentJpaRepository.findByOrderId(orderId)
			.map(this.paymentDataAccessMapper::paymentEntityToPayment);
	}

}
