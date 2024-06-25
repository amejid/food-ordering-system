package com.food.ordering.system.payment.service.dataaccess.creditentry.repository;

import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.payment.service.dataaccess.creditentry.entity.CreditEntryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditEntryJpaRepository extends JpaRepository<CreditEntryEntity, UUID> {

	Optional<CreditEntryEntity> findByCustomerId(UUID customerId);

}
