package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestaurantApprovalEventKafkaPublisher implements RestaurantApprovalResponseMessagePublisher {

	private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

	private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;

	private final RestaurantServiceConfigData restaurantServiceConfigData;

	private final KafkaMessageHelper kafkaMessageHelper;

	public RestaurantApprovalEventKafkaPublisher(RestaurantMessagingDataMapper dataMapper,
			KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer,
			RestaurantServiceConfigData restaurantServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
		this.restaurantMessagingDataMapper = dataMapper;
		this.kafkaProducer = kafkaProducer;
		this.restaurantServiceConfigData = restaurantServiceConfigData;
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
			RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel = this.restaurantMessagingDataMapper
				.orderEventPayloadToRestaurantApprovalResponseAvroModel(sagaId, orderEventPayload);

			this.kafkaProducer.send(this.restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(), sagaId,
					restaurantApprovalResponseAvroModel,
					this.kafkaMessageHelper.getKafkaCallback(
							this.restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
							restaurantApprovalResponseAvroModel, orderOutboxMessage, outboxCallback,
							orderEventPayload.getOrderId(), "RestaurantApprovalResponseAvroModel"));

			log.info("RestaurantApprovalResponseAvroModel sent to kafka for order id: {} and saga id: {}",
					restaurantApprovalResponseAvroModel.getOrderId(), sagaId);
		}
		catch (Exception ex) {
			log.error(
					"Error while sending RestaurantApprovalResponseAvroModel message"
							+ " to kafka with order id: {} and saga id: {}, error: {}",
					orderEventPayload.getOrderId(), sagaId, ex.getMessage());
		}
	}

}
