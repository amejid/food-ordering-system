package tech.amejid.order.service.domain.ports.input.message.listener.payment;

import tech.amejid.order.service.domain.dto.message.PaymentResponse;

public interface PaymentResponseMessageListener {

	void paymentCompleted(PaymentResponse paymentResponse);

	void paymentCancelled(PaymentResponse paymentResponse);

}
