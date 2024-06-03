package com.food.ordering.system.kafka.consumer.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.food.ordering.system.kafka.config.data.KafkaConfigData;
import com.food.ordering.system.kafka.config.data.KafkaConsumerConfigData;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {

	private final KafkaConfigData kafkaConfigData;

	private final KafkaConsumerConfigData kafkaConsumerConfigData;

	@Bean
	public Map<String, Object> consumerConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfigData.getBootstrapServers());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.kafkaConsumerConfigData.getKeyDeserializer());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.kafkaConsumerConfigData.getValueDeserializer());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.kafkaConsumerConfigData.getAutoOffsetReset());
		props.put(this.kafkaConfigData.getSchemaRegistryUrlKey(), this.kafkaConfigData.getSchemaRegistryUrl());
		props.put(this.kafkaConsumerConfigData.getSpecificAvroReaderKey(),
				this.kafkaConsumerConfigData.getSpecificAvroReader());
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, this.kafkaConsumerConfigData.getSessionTimeoutMs());
		props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, this.kafkaConsumerConfigData.getHeartbeatIntervalMs());
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, this.kafkaConsumerConfigData.getMaxPollIntervalMs());
		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
				this.kafkaConsumerConfigData.getMaxPartitionFetchBytesDefault()
						* this.kafkaConsumerConfigData.getMaxPartitionFetchBytesBoostFactor());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, this.kafkaConsumerConfigData.getMaxPollRecords());

		return props;
	}

	@Bean
	public ConsumerFactory<K, V> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfig());
	}

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setBatchListener(this.kafkaConsumerConfigData.getBatchListener());
		factory.setConcurrency(this.kafkaConsumerConfigData.getConcurrencyLevel());
		factory.setAutoStartup(this.kafkaConsumerConfigData.getAutoStartup());
		factory.getContainerProperties().setPollTimeout(this.kafkaConsumerConfigData.getPollTimeoutMs());

		return factory;
	}

}
