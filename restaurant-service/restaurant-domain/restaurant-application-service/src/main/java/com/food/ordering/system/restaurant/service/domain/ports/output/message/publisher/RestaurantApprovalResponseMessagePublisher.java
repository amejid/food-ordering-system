package com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher;

import java.util.function.BiConsumer;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;

public interface RestaurantApprovalResponseMessagePublisher {

	void publish(OrderOutboxMessage orderOutboxMessage, BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback);

}
