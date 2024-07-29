package com.food.ordering.system.kafka.producer.service;

import java.io.Serializable;
import java.util.function.BiConsumer;

import org.apache.avro.specific.SpecificRecordBase;

import org.springframework.kafka.support.SendResult;

public interface KafkaProducer<K extends Serializable, V extends SpecificRecordBase> {

	void send(String topicName, K key, V message, BiConsumer<SendResult<K, V>, Throwable> callback);

}
