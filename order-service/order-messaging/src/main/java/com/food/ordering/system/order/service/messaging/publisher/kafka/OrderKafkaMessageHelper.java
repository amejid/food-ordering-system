package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderKafkaMessageHelper {

	public <T> CompletableFuture<SendResult<String, T>> getKafkaCallback(String responseTopicName, T requestAvroModel,
			String orderId, String requestAvroModelName) {
		CompletableFuture<SendResult<String, T>> future = new CompletableFuture<>();

		Callback callback = (metadata, exception) -> {
			if (exception != null) {
				log.error("Error while sending {} message {} to topic {}", requestAvroModelName,
						requestAvroModel.toString(), responseTopicName, exception);
				future.completeExceptionally(exception);
			}
			else {
				log.info(
						"Received successful response from Kafka for order id: {} Topic: {} Partition: {} Offset: {} Timestamp: {}",
						orderId, metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
				ProducerRecord<String, T> producerRecord = new ProducerRecord<>(responseTopicName, orderId,
						requestAvroModel);
				SendResult<String, T> result = new SendResult<>(producerRecord, metadata);
				future.complete(result);
			}
		};
		return future;
	}

}
