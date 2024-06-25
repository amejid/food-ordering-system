package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;

	private final OrderServiceConfigData orderServiceConfigData;

	private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;

	private final OrderKafkaMessageHelper orderKafkaMessageHelper;

	public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
			OrderServiceConfigData orderServiceConfigData,
			KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
			OrderKafkaMessageHelper orderKafkaMessageHelper) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaProducer = kafkaProducer;
		this.orderKafkaMessageHelper = orderKafkaMessageHelper;
	}

	@Override
	public void publish(OrderPaidEvent domainEvent) {
		String orderId = domainEvent.getOrder().getId().getValue().toString();

		try {
			RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = this.orderMessagingDataMapper
				.orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);

			this.kafkaProducer.send(this.orderServiceConfigData.getRestaurantApprovalRequestTopicName(), orderId,
					restaurantApprovalRequestAvroModel,
					this.orderKafkaMessageHelper.getKafkaCallback(
							this.orderServiceConfigData.getRestaurantApprovalResponseTopicName(),
							restaurantApprovalRequestAvroModel, orderId, "RestaurantApprovalRequestAvroModel"));

			log.info("RestaurantApprovalRequestAvroModel message sent to Kafka for order id: {}", orderId);
		}
		catch (Exception ex) {
			log.error("Error while sending RestaurantApprovalRequestAvroModel message to Kafka for order id: {}",
					orderId, ex);
		}
	}

}
