package com.food.ordering.system.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class KafkaMessageHelper {

	public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(String responseTopicName, T avroModel,
			String orderId, String avroModelName) {
		return new ListenableFutureCallback<>() {
			@Override
			public void onFailure(@NonNull Throwable ex) {
				log.error("Error while sending {} message {} to topic {}", avroModelName, avroModel.toString(),
						responseTopicName, ex);
			}

			@Override
			public void onSuccess(SendResult<String, T> result) {
				RecordMetadata metadata = result.getRecordMetadata();
				log.info(
						"Received successful response from Kafka for order id: {} Topic: {} Partition: {} Offset: {} Timestamp: {}",
						orderId, metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
			}
		};
	}

}
