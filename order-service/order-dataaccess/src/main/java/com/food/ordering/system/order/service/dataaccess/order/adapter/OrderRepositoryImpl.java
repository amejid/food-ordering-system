package com.food.ordering.system.order.service.dataaccess.order.adapter;

import java.util.Optional;

import com.food.ordering.system.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryImpl implements OrderRepository {

	private final OrderJpaRepository orderJpaRepository;

	private final OrderDataAccessMapper orderDataAccessMapper;

	public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderDataAccessMapper orderDataAccessMapper) {
		this.orderJpaRepository = orderJpaRepository;
		this.orderDataAccessMapper = orderDataAccessMapper;
	}

	@Override
	public Order save(Order order) {
		return this.orderDataAccessMapper
			.orderEntityToOrder(this.orderJpaRepository.save(this.orderDataAccessMapper.orderToOrderEntity(order)));
	}

	@Override
	public Optional<Order> findByTrackingId(TrackingId trackingId) {
		return this.orderJpaRepository.findByTrackingId(trackingId.getValue())
			.map(this.orderDataAccessMapper::orderEntityToOrder);
	}

}
