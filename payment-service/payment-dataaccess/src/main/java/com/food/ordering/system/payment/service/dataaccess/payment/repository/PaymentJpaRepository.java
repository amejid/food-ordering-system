package com.food.ordering.system.payment.service.dataaccess.payment.repository;

import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.payment.service.dataaccess.payment.entity.PaymentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

	Optional<PaymentEntity> findByOrderId(UUID orderId);

}
