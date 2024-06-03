package com.food.ordering.system.kafka.producer.service.impl;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

	private final KafkaTemplate<K, V> kafkaTemplate;

	@Override
	public void send(String topicName, K key, V message, CompletableFuture<SendResult<K, V>> callback) {
		log.info("Sending message='{}' to topic='{}'", message, topicName);
		CompletableFuture<SendResult<K, V>> kafkaResultFuture = this.kafkaTemplate.send(topicName, key, message);

		kafkaResultFuture.whenComplete((result, exception) -> {
			if (exception != null) {
				log.error("Error sending message='{}' to topic='{}'", message, topicName, exception);
				throw new KafkaProducerException("Error on kafka producer with key " + key + " and message " + message);
			}
			else {
				callback.complete(result);
			}
		});
	}

	@PreDestroy
	public void close() {
		if (this.kafkaTemplate != null) {
			log.info("Closing kafka producer");
			this.kafkaTemplate.destroy();
		}
	}

}
