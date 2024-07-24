package com.food.ordering.system.payment.service.domain;

import java.util.List;

import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;

public interface PaymentDomainService {

	PaymentEvent validateAndInitiatePayment(Payment payment, CreditEntry creditEntry,
			List<CreditHistory> creditHistories, List<String> failureMessages);

	PaymentEvent validateAndCancelPayment(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories,
			List<String> failureMessages);

}
