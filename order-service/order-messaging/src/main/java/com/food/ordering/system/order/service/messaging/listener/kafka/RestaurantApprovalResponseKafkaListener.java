package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

	private final RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener;

	private final OrderMessagingDataMapper orderMessagingDataMapper;

	public RestaurantApprovalResponseKafkaListener(
			RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener,
			OrderMessagingDataMapper orderMessagingDataMapper) {
		this.restaurantApprovalResponseMessageListener = restaurantApprovalResponseMessageListener;
		this.orderMessagingDataMapper = orderMessagingDataMapper;
	}

	@Override
	@KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
			topics = "${order-service.restaurant-approval-response-topic-name}")
	public void receive(@Payload List<RestaurantApprovalResponseAvroModel> messages,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		log.info("{} number of restaurant approval responses received with keys: {}, partitions: {}, offsets: {}",
				messages.size(), keys.toString(), partitions.toString(), offsets.toString());

		messages.forEach(restaurantApprovalResponseAvroModel -> {
			try {
				if (OrderApprovalStatus.APPROVED == restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
					log.info("Processing approved order for order id: {}",
							restaurantApprovalResponseAvroModel.getOrderId());
					this.restaurantApprovalResponseMessageListener.orderApproved(this.orderMessagingDataMapper
						.approvalResponseAvroModelToApprovalResponse(restaurantApprovalResponseAvroModel));
				}
				else if (OrderApprovalStatus.REJECTED == restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
					log.info("Processing rejected order for order id: {}",
							restaurantApprovalResponseAvroModel.getOrderId());
					this.restaurantApprovalResponseMessageListener.orderRejected(this.orderMessagingDataMapper
						.approvalResponseAvroModelToApprovalResponse(restaurantApprovalResponseAvroModel));
				}
			}
			catch (OptimisticLockingFailureException ex) {
				// NO-OP for optimistic lock.
				log.error("Caught optimistic locking exception in RestaurantApprovalResponseAvroModel for order id: {}",
						restaurantApprovalResponseAvroModel.getOrderId(), ex);
			}
			catch (OrderNotFoundException ex) {
				log.error("Order not found for order id: {}", restaurantApprovalResponseAvroModel.getOrderId(), ex);
			}
		});
	}

}
