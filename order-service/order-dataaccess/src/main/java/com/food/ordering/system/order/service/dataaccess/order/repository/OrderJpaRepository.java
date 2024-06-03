package com.food.ordering.system.order.service.dataaccess.order.repository;

import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

	Optional<OrderEntity> findByTrackingId(UUID trackingId);

}
