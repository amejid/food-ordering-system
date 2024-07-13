package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

	private final OrderPaymentSaga orderPaymentSaga;

	public PaymentResponseMessageListenerImpl(OrderPaymentSaga orderPaymentSaga) {
		this.orderPaymentSaga = orderPaymentSaga;
	}

	@Override
	public void paymentCompleted(PaymentResponse paymentResponse) {
		OrderPaidEvent orderPaidEvent = this.orderPaymentSaga.process(paymentResponse);
		log.info("Publishing OrderPaidEvent for order id: {}", paymentResponse.getOrderId());
		orderPaidEvent.fire();
	}

	@Override
	public void paymentCancelled(PaymentResponse paymentResponse) {
		this.orderPaymentSaga.rollback(paymentResponse);
		log.info("Order is rolled back with failure messages: {}",
				String.join(Order.FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
	}

}
