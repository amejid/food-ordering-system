package tech.amejid.order.service.domain.ports.output.repository;

import java.util.Optional;

import tech.amejid.order.service.domain.entity.Order;
import tech.amejid.order.service.domain.valueobject.TrackingId;

public interface OrderRepository {

	Order save(Order order);

	Optional<Order> findByTrackingId(TrackingId trackingId);

}
