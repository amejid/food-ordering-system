package com.food.ordering.system.payment.service.domain;

import java.util.function.Function;
import java.util.function.Predicate;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {

	private final PaymentRequestHelper paymentRequestHelper;

	private static final int MAX_EXECUTIONS = 100;

	public PaymentRequestMessageListenerImpl(PaymentRequestHelper paymentRequestHelper) {
		this.paymentRequestHelper = paymentRequestHelper;
	}

	@Override
	public void completePayment(PaymentRequest paymentRequest) {
		processPayment(this.paymentRequestHelper::persistPayment, paymentRequest, "completePayment");
	}

	@Override
	public void cancelPayment(PaymentRequest paymentRequest) {
		processPayment(this.paymentRequestHelper::persistCancelPayment, paymentRequest, "cancelPayment");
	}

	private void processPayment(Predicate<PaymentRequest> func, PaymentRequest paymentRequest, String methodName) {
		int execution = 1;
		boolean result;
		do {
			log.info("Executing {} operation for {} time order id: {}", methodName, execution,
					paymentRequest.getOrderId());
			try {
				result = func.test(paymentRequest);
				execution++;
			}
			catch (OptimisticLockingFailureException ex) {
				log.warn("Caught OptimisticLockingFailureException in {} with message {}!. Retrying for order id: {}",
						methodName, ex.getMessage(), paymentRequest.getOrderId());
				result = false;
			}
		}
		while (!result && execution < MAX_EXECUTIONS);

		if (!result) {
			throw new PaymentApplicationServiceException(
					"Could not complete completePayment operation. Throwing exception!");
		}
	}

}
