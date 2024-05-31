package tech.amejid.order.service.domain;

import java.util.List;

import tech.amejid.order.service.domain.entity.Order;
import tech.amejid.order.service.domain.entity.Restaurant;
import tech.amejid.order.service.domain.event.OrderCancelledEvent;
import tech.amejid.order.service.domain.event.OrderCreatedEvent;
import tech.amejid.order.service.domain.event.OrderPaidEvent;

public interface OrderDomainService {

	OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant);

	OrderPaidEvent payOrder(Order order);

	void approveOrder(Order order);

	OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages);

	void cancelOrder(Order order, List<String> failureMessages);

}
