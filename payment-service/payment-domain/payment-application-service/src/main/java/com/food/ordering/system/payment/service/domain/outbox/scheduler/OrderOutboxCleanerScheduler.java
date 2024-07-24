package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

	private final OrderOutboxHelper orderOutboxHelper;

	public OrderOutboxCleanerScheduler(OrderOutboxHelper orderOutboxHelper) {
		this.orderOutboxHelper = orderOutboxHelper;
	}

	@Override
	@Transactional
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {
		Optional<List<OrderOutboxMessage>> outboxMessagesResponse = this.orderOutboxHelper
			.getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
		if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
			List<OrderOutboxMessage> outboxMessages = outboxMessagesResponse.get();
			log.info("Received {} OrderOutboxMessage for clean-up!", outboxMessages.size());
			this.orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
			log.info("Deleted {} OrderOutboxMessage!", outboxMessages.size());
		}
	}

}
