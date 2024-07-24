package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentRequestHelper {

	private final PaymentDomainService paymentDomainService;

	private final PaymentDataMapper paymentDataMapper;

	private final PaymentRepository paymentRepository;

	private final CreditEntryRepository creditEntryRepository;

	private final CreditHistoryRepository creditHistoryRepository;

	private final OrderOutboxHelper orderOutboxHelper;

	private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

	public PaymentRequestHelper(PaymentDomainService paymentDomainService, PaymentDataMapper paymentDataMapper,
			PaymentRepository paymentRepository, CreditEntryRepository creditEntryRepository,
			CreditHistoryRepository creditHistoryRepository, OrderOutboxHelper orderOutboxHelper,
			PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
		this.paymentDomainService = paymentDomainService;
		this.paymentDataMapper = paymentDataMapper;
		this.paymentRepository = paymentRepository;
		this.creditEntryRepository = creditEntryRepository;
		this.creditHistoryRepository = creditHistoryRepository;
		this.orderOutboxHelper = orderOutboxHelper;
		this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
	}

	@Transactional
	public void persistPayment(PaymentRequest paymentRequest) {
		if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)) {
			log.info("An outbox message with saga id: {} is already saved to database!", paymentRequest.getSagaId());
			return;
		}

		log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
		Payment payment = this.paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<>();
		PaymentEvent paymentEvent = this.paymentDomainService.validateAndInitiatePayment(payment, creditEntry,
				creditHistories, failureMessages);

		persistDbObject(payment, failureMessages, creditEntry, creditHistories);

		this.orderOutboxHelper.saveOrderOutboxMessage(
				this.paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
				paymentEvent.getPayment().getPaymentStatus(), OutboxStatus.STARTED,
				UUID.fromString(paymentRequest.getSagaId()));
	}

	public void persistCancelPayment(PaymentRequest paymentRequest) {
		if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)) {
			log.info("An outbox message with saga id: {} is already saved to database!", paymentRequest.getSagaId());
			return;
		}

		log.info("Received payment cancel event for order id: {}", paymentRequest.getOrderId());
		Optional<Payment> paymentResponse = this.paymentRepository
			.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));

		if (paymentResponse.isEmpty()) {
			log.error("Payment with order id: {} could not be found!", paymentRequest.getOrderId());
			throw new PaymentNotFoundException(
					"Payment with order id: " + paymentRequest.getOrderId() + " could not be found!");
		}

		Payment payment = paymentResponse.get();

		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<>();
		PaymentEvent paymentEvent = this.paymentDomainService.validateAndCancelPayment(payment, creditEntry,
				creditHistories, failureMessages);

		persistDbObject(payment, failureMessages, creditEntry, creditHistories);

		this.orderOutboxHelper.saveOrderOutboxMessage(
				this.paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
				paymentEvent.getPayment().getPaymentStatus(), OutboxStatus.STARTED,
				UUID.fromString(paymentRequest.getSagaId()));
	}

	private CreditEntry getCreditEntry(CustomerId customerId) {
		Optional<CreditEntry> creditEntry = this.creditEntryRepository.findByCustomerId(customerId);
		if (creditEntry.isEmpty()) {
			log.error("Could not find credit entry for customer: {}", customerId.getValue());
			throw new PaymentApplicationServiceException(
					"Could not find credit entry for customer: " + customerId.getValue());
		}
		return creditEntry.get();
	}

	private List<CreditHistory> getCreditHistory(CustomerId customerId) {
		Optional<List<CreditHistory>> creditHistories = this.creditHistoryRepository.findByCustomerId(customerId);
		if (creditHistories.isEmpty()) {
			log.error("Could not find credit history for customer: {}", customerId.getValue());
			throw new PaymentApplicationServiceException(
					"Could not find credit history for customer: " + customerId.getValue());
		}
		return creditHistories.get();
	}

	private void persistDbObject(Payment payment, List<String> failureMessages, CreditEntry creditEntry,
			List<CreditHistory> creditHistories) {
		this.paymentRepository.save(payment);
		if (failureMessages.isEmpty()) {
			this.creditEntryRepository.save(creditEntry);
			this.creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
		}
	}

	private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest paymentRequest,
			PaymentStatus paymentStatus) {
		Optional<OrderOutboxMessage> orderOutboxMessage = this.orderOutboxHelper
			.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(UUID.fromString(paymentRequest.getSagaId()),
					paymentStatus);

		if (orderOutboxMessage.isPresent()) {
			this.paymentResponseMessagePublisher.publish(orderOutboxMessage.get(),
					this.orderOutboxHelper::updateOutboxMessage);
			return true;
		}

		return false;
	}

}
