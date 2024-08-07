package com.food.ordering.system.order.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

public interface OrderRepository {

	Order save(Order order);

	Optional<Order> findById(OrderId orderId);

	Optional<Order> findByTrackingId(TrackingId trackingId);

}
