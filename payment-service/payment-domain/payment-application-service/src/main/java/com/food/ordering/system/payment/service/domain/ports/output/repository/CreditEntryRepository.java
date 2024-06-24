package com.food.ordering.system.payment.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;

public interface CreditEntryRepository {

	CreditEntry save(CreditEntry creditEntry);

	Optional<CreditEntry> findByCustomerId(CustomerId customerId);

}
