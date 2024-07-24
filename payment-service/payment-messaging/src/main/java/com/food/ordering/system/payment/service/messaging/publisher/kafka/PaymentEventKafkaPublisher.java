package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventKafkaPublisher implements PaymentResponseMessagePublisher {

	private final PaymentMessagingDataMapper paymentMessagingDataMapper;

	private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;

	private final PaymentServiceConfigData paymentServiceConfigData;

	private final KafkaMessageHelper kafkaMessageHelper;

	public PaymentEventKafkaPublisher(PaymentMessagingDataMapper paymentMessagingDataMapper,
			KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer,
			PaymentServiceConfigData paymentServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
		this.paymentMessagingDataMapper = paymentMessagingDataMapper;
		this.kafkaProducer = kafkaProducer;
		this.paymentServiceConfigData = paymentServiceConfigData;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(OrderOutboxMessage orderOutboxMessage,
			BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback) {
		OrderEventPayload orderEventPayload = this.kafkaMessageHelper
			.getOrderEventPayload(orderOutboxMessage.getPayload(), OrderEventPayload.class);

		String sagaId = orderOutboxMessage.getSagaId().toString();

		log.info("Received OrderOutboxMessage for order id: {} and saga id: {}", orderEventPayload.getOrderId(),
				sagaId);

		try {
			PaymentResponseAvroModel paymentResponseAvroModel = this.paymentMessagingDataMapper
				.orderEventPayloadToPaymentResponseAvroModel(sagaId, orderEventPayload);

			this.kafkaProducer.send(this.paymentServiceConfigData.getPaymentResponseTopicName(), sagaId,
					paymentResponseAvroModel,
					this.kafkaMessageHelper.getKafkaCallback(
							this.paymentServiceConfigData.getPaymentResponseTopicName(), paymentResponseAvroModel,
							orderOutboxMessage, outboxCallback, orderEventPayload.getOrderId(),
							"PaymentResponseAvroModel"));

			log.info("PaymentResponseAvroModel sent to kafka for order id: {} and saga id: {}",
					paymentResponseAvroModel.getOrderId(), sagaId);
		}
		catch (Exception ex) {
			log.error(
					"Error while sending PaymentRequestAvroModel message"
							+ " to kafka with order id: {} and saga id: {}, error: {}",
					orderEventPayload.getOrderId(), sagaId, ex.getMessage());
		}
	}

}
