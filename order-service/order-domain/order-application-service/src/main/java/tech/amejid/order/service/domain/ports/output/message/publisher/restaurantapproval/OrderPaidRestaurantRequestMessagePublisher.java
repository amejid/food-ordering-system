package tech.amejid.order.service.domain.ports.output.message.publisher.restaurantapproval;

import tech.amejid.domain.event.publisher.DomainEventPublisher;
import tech.amejid.order.service.domain.event.OrderPaidEvent;

public interface OrderPaidRestaurantRequestMessagePublisher extends DomainEventPublisher<OrderPaidEvent> {

}
