package com.food.ordering.system.kafka.producer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.food.ordering.system.kafka.config.data.KafkaConfigData;
import com.food.ordering.system.kafka.config.data.KafkaProducerConfigData;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig<K extends Serializable, V extends SpecificRecordBase> {

	private final KafkaConfigData kafkaConfigData;

	private final KafkaProducerConfigData kafkaProducerConfigData;

	@Bean
	public Map<String, Object> producerConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfigData.getBootstrapServers());
		props.put(this.kafkaConfigData.getSchemaRegistryUrlKey(), this.kafkaConfigData.getSchemaRegistryUrl());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, this.kafkaProducerConfigData.getKeySerializerClass());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, this.kafkaProducerConfigData.getValueSerializerClass());
		props.put(ProducerConfig.BATCH_SIZE_CONFIG,
				this.kafkaProducerConfigData.getBatchSize() * this.kafkaProducerConfigData.getBatchSizeBoostFactor());
		props.put(ProducerConfig.LINGER_MS_CONFIG, this.kafkaProducerConfigData.getLingerMs());
		props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, this.kafkaProducerConfigData.getCompressionType());
		props.put(ProducerConfig.ACKS_CONFIG, this.kafkaProducerConfigData.getAcks());
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, this.kafkaProducerConfigData.getRequestTimeoutMs());
		props.put(ProducerConfig.RETRIES_CONFIG, this.kafkaProducerConfigData.getRetryCount());

		return props;
	}

	@Bean
	public ProducerFactory<K, V> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfig());
	}

	@Bean
	public KafkaTemplate<K, V> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

}
