package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

	private final PaymentOutboxHelper paymentOutboxHelper;

	public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
		this.paymentOutboxHelper = paymentOutboxHelper;
	}

	@Override
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {

		Optional<List<OrderPaymentOutboxMessage>> outboxMessagesResponse = this.paymentOutboxHelper
			.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED,
					SagaStatus.FAILED, SagaStatus.COMPENSATED);

		if (outboxMessagesResponse.isPresent()) {
			List<OrderPaymentOutboxMessage> outboxMessages = outboxMessagesResponse.get();
			log.info("Received {} OrderPaymentOutboxMessage for cleanup. The payloads: {}", outboxMessages.size(),
					outboxMessages.stream()
						.map(OrderPaymentOutboxMessage::getPayload)
						.collect(Collectors.joining("\n")));

			this.paymentOutboxHelper.deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
					SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED);
			log.info("Deleted {} OrderPaymentOutboxMessage", outboxMessages.size());
		}
	}

}
