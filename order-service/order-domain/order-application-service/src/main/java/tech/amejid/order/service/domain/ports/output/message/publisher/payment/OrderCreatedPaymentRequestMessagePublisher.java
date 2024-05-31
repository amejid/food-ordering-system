package tech.amejid.order.service.domain.ports.output.message.publisher.payment;

import tech.amejid.domain.event.publisher.DomainEventPublisher;
import tech.amejid.order.service.domain.event.OrderCreatedEvent;

public interface OrderCreatedPaymentRequestMessagePublisher extends DomainEventPublisher<OrderCreatedEvent> {

}
