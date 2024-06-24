package com.food.ordering.system.payment.service.domain.valueobject;

import java.util.UUID;

import com.food.ordering.system.domain.valueobject.BaseId;

public class PaymentId extends BaseId<UUID> {

	public PaymentId(UUID value) {
		super(value);
	}

}
