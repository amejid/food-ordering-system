package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentCancelledEvent extends PaymentEvent {

	public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt) {
		super(payment, createdAt, Collections.emptyList());
	}

}
