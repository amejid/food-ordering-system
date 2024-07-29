package com.food.ordering.system.payment.service.dataaccess.creditentry.adapter;

import java.util.Optional;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.dataaccess.creditentry.mapper.CreditEntryDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.creditentry.repository.CreditEntryJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

@Component
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

	private final CreditEntryJpaRepository creditEntryJpaRepository;

	private final CreditEntryDataAccessMapper creditEntryDataAccessMapper;

	private final EntityManager entityManager;

	public CreditEntryRepositoryImpl(CreditEntryJpaRepository creditEntryJpaRepository,
			CreditEntryDataAccessMapper creditEntryDataAccessMapper, EntityManager entityManager) {
		this.creditEntryJpaRepository = creditEntryJpaRepository;
		this.creditEntryDataAccessMapper = creditEntryDataAccessMapper;
		this.entityManager = entityManager;
	}

	@Override
	public CreditEntry save(CreditEntry creditEntry) {
		return this.creditEntryDataAccessMapper.creditEntryEntityToCreditEntry(this.creditEntryJpaRepository
			.save(this.creditEntryDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry)));
	}

	@Override
	public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
		return this.creditEntryJpaRepository.findByCustomerId(customerId.getValue())
			.map(this.creditEntryDataAccessMapper::creditEntryEntityToCreditEntry);
	}

	@Override
	public void detach(CustomerId customerId) {
		this.entityManager.detach(this.creditEntryJpaRepository.findByCustomerId(customerId.getValue()).orElseThrow());
	}

}
