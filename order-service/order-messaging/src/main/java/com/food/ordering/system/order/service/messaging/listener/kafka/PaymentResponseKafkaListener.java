package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

	private final PaymentResponseMessageListener paymentResponseMessageListener;

	private final OrderMessagingDataMapper orderMessagingDataMapper;

	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
			topics = "${order-service.payment-response-topic-name}")
	public void receive(@Payload List<PaymentResponseAvroModel> messages, @Header List<Long> keys,
			@Header List<Integer> partitions, @Header List<Long> offsets) {
		log.info("{} number of payment responses received with keys: {}, partitions: {}, offsets: {}", messages.size(),
				keys.toString(), partitions.toString(), offsets.toString());

		messages.forEach(paymentResponseAvroModel -> {
			if (PaymentStatus.COMPLETED == paymentResponseAvroModel.getPaymentStatus()) {
				log.info("Processing successful payment response for order id: {}",
						paymentResponseAvroModel.getOrderId());
				this.paymentResponseMessageListener.paymentCompleted(this.orderMessagingDataMapper
					.paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
			}
			else if (PaymentStatus.CANCELLED == paymentResponseAvroModel.getPaymentStatus()
					|| PaymentStatus.FAILED == paymentResponseAvroModel.getPaymentStatus()) {
				log.info("Processing unsuccessful payment response for order id: {}",
						paymentResponseAvroModel.getOrderId());
				this.paymentResponseMessageListener.paymentCancelled(this.orderMessagingDataMapper
					.paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
			}
		});
	}

}