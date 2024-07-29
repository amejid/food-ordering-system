package com.food.ordering.system.kafka.producer.service.impl;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;

import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

	private final KafkaTemplate<K, V> kafkaTemplate;

	public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void send(String topicName, K key, V message, BiConsumer<SendResult<K, V>, Throwable> callback) {
		log.info("Sending message='{}' to topic='{}'", message, topicName);

		try {
			CompletableFuture<SendResult<K, V>> kafkaResultFuture = this.kafkaTemplate.send(topicName, key, message);
			kafkaResultFuture.whenComplete(callback);
		}
		catch (KafkaException ex) {
			log.error("Error sending message='{}' to topic='{}' and exception:{}", message, topicName, ex.getMessage());
			throw new KafkaProducerException("Error on kafka producer with key " + key + " and message " + message);
		}
	}

	@PreDestroy
	public void close() {
		if (this.kafkaTemplate != null) {
			log.info("Closing kafka producer");
			this.kafkaTemplate.destroy();
		}
	}

}
