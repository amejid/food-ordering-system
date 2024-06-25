package com.food.ordering.system.payment.service.dataaccess.credithistory.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.dataaccess.credithistory.entity.CreditHistoryEntity;
import com.food.ordering.system.payment.service.dataaccess.credithistory.mapper.CreditHistoryDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.credithistory.repository.CreditHistoryJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;

import org.springframework.stereotype.Component;

@Component
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {

	private final CreditHistoryJpaRepository creditHistoryJpaRepository;

	private final CreditHistoryDataAccessMapper creditHistoryDataAccessMapper;

	public CreditHistoryRepositoryImpl(CreditHistoryJpaRepository creditHistoryJpaRepository,
			CreditHistoryDataAccessMapper creditHistoryDataAccessMapper) {
		this.creditHistoryJpaRepository = creditHistoryJpaRepository;
		this.creditHistoryDataAccessMapper = creditHistoryDataAccessMapper;
	}

	@Override
	public CreditHistory save(CreditHistory creditHistory) {
		return this.creditHistoryDataAccessMapper.creditHistoryEntityToCreditHistory(this.creditHistoryJpaRepository
			.save(this.creditHistoryDataAccessMapper.creditHistoryToCreditHistoryEntity(creditHistory)));
	}

	@Override
	public Optional<List<CreditHistory>> findByCustomerId(CustomerId customerId) {
		Optional<List<CreditHistoryEntity>> creditHistory = this.creditHistoryJpaRepository
			.findByCustomerId(customerId.getValue());
		return creditHistory.map(creditHistoryList -> creditHistoryList.stream()
			.map(this.creditHistoryDataAccessMapper::creditHistoryEntityToCreditHistory)
			.collect(Collectors.toList()));
	}

}
