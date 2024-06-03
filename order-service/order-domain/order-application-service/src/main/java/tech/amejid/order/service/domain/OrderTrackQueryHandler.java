package tech.amejid.order.service.domain;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.amejid.order.service.domain.dto.track.TrackOrderQuery;
import tech.amejid.order.service.domain.dto.track.TrackOrderResponse;
import tech.amejid.order.service.domain.entity.Order;
import tech.amejid.order.service.domain.exception.OrderNotFoundException;
import tech.amejid.order.service.domain.mapper.OrderDataMapper;
import tech.amejid.order.service.domain.ports.output.repository.OrderRepository;
import tech.amejid.order.service.domain.valueobject.TrackingId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTrackQueryHandler {

	private final OrderDataMapper orderDataMapper;

	private final OrderRepository orderRepository;

	@Transactional(readOnly = true)
	public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
		Optional<Order> orderResult = this.orderRepository
			.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));

		if (orderResult.isEmpty()) {
			log.warn("Could not find order with tracking id: {}", trackOrderQuery.getOrderTrackingId());
			throw new OrderNotFoundException(
					"Could not find order with tracking id: " + trackOrderQuery.getOrderTrackingId());
		}

		return this.orderDataMapper.orderToTrackOrderResponse(orderResult.get());
	}

}
