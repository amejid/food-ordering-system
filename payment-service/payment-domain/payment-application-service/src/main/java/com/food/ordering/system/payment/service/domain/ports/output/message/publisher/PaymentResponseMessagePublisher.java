package com.food.ordering.system.payment.service.domain.ports.output.message.publisher;

import java.util.function.BiConsumer;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;

public interface PaymentResponseMessagePublisher {

	void publish(OrderOutboxMessage orderOutboxMessage, BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback);

}
