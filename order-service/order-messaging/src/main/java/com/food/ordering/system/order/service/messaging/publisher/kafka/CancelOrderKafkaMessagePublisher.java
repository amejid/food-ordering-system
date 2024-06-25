package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CancelOrderKafkaMessagePublisher implements OrderCancelledPaymentRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;

	private final OrderServiceConfigData orderServiceConfigData;

	private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

	private final OrderKafkaMessageHelper orderKafkaMessageHelper;

	public CancelOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
			OrderServiceConfigData orderServiceConfigData, KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
			OrderKafkaMessageHelper orderKafkaMessageHelper) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaProducer = kafkaProducer;
		this.orderKafkaMessageHelper = orderKafkaMessageHelper;
	}

	@Override
	public void publish(OrderCancelledEvent domainEvent) {
		String orderId = domainEvent.getOrder().getId().getValue().toString();

		log.info("Received OrderCancelledEvent for OrderId: {}", orderId);
		try {
			PaymentRequestAvroModel paymentRequestAvroModel = this.orderMessagingDataMapper
				.orderCancelledEventToPaymentRequestAvroModel(domainEvent);

			this.kafkaProducer.send(this.orderServiceConfigData.getPaymentRequestTopicName(), orderId,
					paymentRequestAvroModel,
					this.orderKafkaMessageHelper.getKafkaCallback(
							this.orderServiceConfigData.getPaymentResponseTopicName(), paymentRequestAvroModel, orderId,
							"PaymentRequestAvroModel"));

			log.info("PaymentRequestAvroModel message sent to Kafka for order id: {}",
					paymentRequestAvroModel.getOrderId());
		}
		catch (Exception ex) {
			log.error("Error while sending PaymentRequestAvroModel message to Kafka for order id: {}", orderId, ex);
		}
	}

}
