package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.sql.SQLException;
import java.util.List;

import com.food.ordering.system.domain.event.payload.PaymentOrderEventPayload;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import debezium.payment.order_outbox.Envelope;
import debezium.payment.order_outbox.Value;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentResponseKafkaListener implements KafkaConsumer<Envelope> {

	private final PaymentResponseMessageListener paymentResponseMessageListener;

	private final OrderMessagingDataMapper orderMessagingDataMapper;

	private final KafkaMessageHelper kafkaMessageHelper;

	public PaymentResponseKafkaListener(PaymentResponseMessageListener paymentResponseMessageListener,
			OrderMessagingDataMapper orderMessagingDataMapper, KafkaMessageHelper kafkaMessageHelper) {
		this.paymentResponseMessageListener = paymentResponseMessageListener;
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
			topics = "${order-service.payment-response-topic-name}")
	public void receive(@Payload List<Envelope> messages, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		log.info("{} number of payment responses received!", messages.stream()
			.filter(message -> message.getBefore() == null && DebeziumOp.CREATE.getValue().equals(message.getOp()))
			.toList()
			.size());

		messages.forEach(avroModel -> {
			if (avroModel.getBefore() == null && DebeziumOp.CREATE.getValue().equals(avroModel.getOp())) {
				log.info("Incoming message in PaymentResponseKafkaListener: {}", avroModel);
				Value paymentResponseAvroModel = avroModel.getAfter();
				PaymentOrderEventPayload paymentOrderEventPayload = this.kafkaMessageHelper
					.getOrderEventPayload(paymentResponseAvroModel.getPayload(), PaymentOrderEventPayload.class);
				try {
					if (PaymentStatus.COMPLETED.name().equals(paymentOrderEventPayload.getPaymentStatus())) {
						log.info("Processing successful payment response for order id: {}",
								paymentOrderEventPayload.getOrderId());
						this.paymentResponseMessageListener
							.paymentCompleted(this.orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(
									paymentOrderEventPayload, paymentResponseAvroModel));
					}
					else if (PaymentStatus.CANCELLED.name().equals(paymentOrderEventPayload.getPaymentStatus())
							|| PaymentStatus.FAILED.name().equals(paymentOrderEventPayload.getPaymentStatus())) {
						log.info("Processing unsuccessful payment response for order id: {}",
								paymentOrderEventPayload.getOrderId());
						this.paymentResponseMessageListener
							.paymentCancelled(this.orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(
									paymentOrderEventPayload, paymentResponseAvroModel));
					}
				}
				catch (OptimisticLockingFailureException ex) {
					// NO-OP for optimistic lock.
					log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for order id: {}",
							paymentOrderEventPayload.getOrderId(), ex);
				}
				catch (OrderNotFoundException ex) {
					log.error("Order not found for order id: {}", paymentOrderEventPayload.getOrderId(), ex);
				}
				catch (DataAccessException ex) {
					SQLException sqlException = (SQLException) ex.getRootCause();
					if (sqlException != null && sqlException.getSQLState() != null
							&& PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
						log.error(
								"Caught unique constraint exception with sql state: {} in PaymentResponseKafkaListener for order id: {}",
								sqlException.getSQLState(), paymentOrderEventPayload.getOrderId());
					}
				}
			}
		});
	}

}
