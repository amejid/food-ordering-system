package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;

	private final OrderServiceConfigData orderServiceConfigData;

	private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

	private final KafkaMessageHelper kafkaMessageHelper;

	public CreateOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
			OrderServiceConfigData orderServiceConfigData, KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
			KafkaMessageHelper kafkaMessageHelper) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaProducer = kafkaProducer;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(OrderCreatedEvent domainEvent) {
		String orderId = domainEvent.getOrder().getId().getValue().toString();

		log.info("Received OrderCreatedEvent for OrderId: {}", orderId);
		try {
			PaymentRequestAvroModel paymentRequestAvroModel = this.orderMessagingDataMapper
				.orderCreatedEventToPaymentRequestAvroModel(domainEvent);

			this.kafkaProducer.send(this.orderServiceConfigData.getPaymentRequestTopicName(), orderId,
					paymentRequestAvroModel,
					this.kafkaMessageHelper.getKafkaCallback(this.orderServiceConfigData.getPaymentResponseTopicName(),
							paymentRequestAvroModel, orderId, "PaymentRequestAvroModel"));

			log.info("PaymentRequestAvroModel message sent to Kafka for order id: {}",
					paymentRequestAvroModel.getOrderId());
		}
		catch (Exception ex) {
			log.error("Error while sending PaymentRequestAvroModel message to Kafka for order id: {}", orderId, ex);
		}
	}

}
