package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentCancelledKafkaMessagePublisher implements PaymentCancelledMessagePublisher {

	private final PaymentMessagingDataMapper paymentMessagingDataMapper;

	private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;

	private final PaymentServiceConfigData paymentServiceConfigData;

	private final KafkaMessageHelper kafkaMessageHelper;

	public PaymentCancelledKafkaMessagePublisher(PaymentMessagingDataMapper paymentMessagingDataMapper,
			KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer,
			PaymentServiceConfigData paymentServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
		this.paymentMessagingDataMapper = paymentMessagingDataMapper;
		this.kafkaProducer = kafkaProducer;
		this.paymentServiceConfigData = paymentServiceConfigData;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(PaymentCancelledEvent domainEvent) {
		String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

		log.info("Received PaymentCancelledEvent for order id: {}", orderId);

		try {
			PaymentResponseAvroModel paymentResponseAvroModel = this.paymentMessagingDataMapper
				.paymentCancelledEventToPaymentResponseAvroModel(domainEvent);

			this.kafkaProducer.send(this.paymentServiceConfigData.getPaymentResponseTopicName(), orderId,
					paymentResponseAvroModel,
					this.kafkaMessageHelper.getKafkaCallback(
							this.paymentServiceConfigData.getPaymentResponseTopicName(), paymentResponseAvroModel,
							orderId, "PaymentResponseAvroModel"));

			log.info("PaymentResponseAvroModel sent to Kafka for order id: {}", orderId);
		}
		catch (Exception ex) {
			log.error("Error while sending PaymentResponseAvroModel message to Kafka with order id: {}", orderId, ex);
		}
	}

}
