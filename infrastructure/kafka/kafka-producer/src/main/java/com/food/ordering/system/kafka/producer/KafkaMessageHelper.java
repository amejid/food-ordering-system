package com.food.ordering.system.kafka.producer;

import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class KafkaMessageHelper {

	private final ObjectMapper objectMapper;

	public KafkaMessageHelper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(String responseTopicName,
			T avroModel, U outboxMessage, BiConsumer<U, OutboxStatus> outboxCallback, String orderId,
			String avroModelName) {
		return new ListenableFutureCallback<>() {
			@Override
			public void onFailure(@NonNull Throwable ex) {
				log.error("Error while sending {} with message: {} and outbox type: {} to topic {}", avroModelName,
						avroModel.toString(), outboxMessage.getClass(), responseTopicName, ex);
				outboxCallback.accept(outboxMessage, OutboxStatus.FAILED);
			}

			@Override
			public void onSuccess(SendResult<String, T> result) {
				RecordMetadata metadata = result.getRecordMetadata();
				log.info(
						"Received successful response from Kafka for order id: {} Topic: {} Partition: {} Offset: {} Timestamp: {}",
						orderId, metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
				outboxCallback.accept(outboxMessage, OutboxStatus.COMPLETED);
			}
		};
	}

	public <T> T getOrderEventPayload(String payload, Class<T> outputType) {
		try {
			return this.objectMapper.readValue(payload, outputType);
		}
		catch (JsonProcessingException ex) {
			log.error("Error occurred while parsing {} from payload: {}", outputType.getName(), payload, ex);
			throw new OrderDomainException("Error occurred while parsing " + outputType.getName() + " from payload",
					ex);
		}
	}

}
