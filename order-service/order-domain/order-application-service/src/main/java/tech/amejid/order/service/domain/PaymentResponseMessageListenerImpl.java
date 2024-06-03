package tech.amejid.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import tech.amejid.order.service.domain.dto.message.PaymentResponse;
import tech.amejid.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

	@Override
	public void paymentCompleted(PaymentResponse paymentResponse) {

	}

	@Override
	public void paymentCancelled(PaymentResponse paymentResponse) {

	}

}
