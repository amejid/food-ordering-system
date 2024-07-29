package com.food.ordering.system.payment.service.messaging.listener.kafka;

import java.sql.SQLException;

import com.food.ordering.system.domain.event.payload.OrderPaymentEventPayload;
import com.food.ordering.system.kafka.consumer.KafkaSingleItemConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import debezium.order.payment_outbox.Envelope;
import debezium.order.payment_outbox.Value;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;

import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentRequestKafkaListener implements KafkaSingleItemConsumer<Envelope> {

	private final PaymentRequestMessageListener paymentRequestMessageListener;

	private final PaymentMessagingDataMapper paymentMessagingDataMapper;

	private final KafkaMessageHelper kafkaMessageHelper;

	public PaymentRequestKafkaListener(PaymentRequestMessageListener paymentRequestMessageListener,
			PaymentMessagingDataMapper paymentMessagingDataMapper, KafkaMessageHelper kafkaMessageHelper) {
		this.paymentRequestMessageListener = paymentRequestMessageListener;
		this.paymentMessagingDataMapper = paymentMessagingDataMapper;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
			topics = "${payment-service.payment-request-topic-name}")
	public void receive(@Payload Envelope message, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition, @Header(KafkaHeaders.OFFSET) Long offset) {

		if (message.getBefore() == null && DebeziumOp.CREATE.getValue().equals(message.getOp())) {
			log.info("Incoming message in PaymentRequestKafkaListener: {} with key: {}, partition: {} and offset: {}",
					message, key, partition, offset);
			Value paymentRequestAvroModel = message.getAfter();
			OrderPaymentEventPayload orderPaymentEventPayload = this.kafkaMessageHelper
				.getOrderEventPayload(paymentRequestAvroModel.getPayload(), OrderPaymentEventPayload.class);
			try {
				if (PaymentOrderStatus.PENDING.name().equals(orderPaymentEventPayload.getPaymentOrderStatus())) {
					log.info("Processing payment for order id: {}", orderPaymentEventPayload.getOrderId());
					this.paymentRequestMessageListener.completePayment(this.paymentMessagingDataMapper
						.paymentRequestAvroModelToPaymentRequest(orderPaymentEventPayload, paymentRequestAvroModel));
				}
				else if (PaymentOrderStatus.CANCELLED.name().equals(orderPaymentEventPayload.getPaymentOrderStatus())) {
					log.info("Cancelling payment for order id: {}", orderPaymentEventPayload.getOrderId());
					this.paymentRequestMessageListener.cancelPayment(this.paymentMessagingDataMapper
						.paymentRequestAvroModelToPaymentRequest(orderPaymentEventPayload, paymentRequestAvroModel));
				}
			}
			catch (DataAccessException ex) {
				SQLException sqlException = (SQLException) ex.getRootCause();
				if (sqlException != null && sqlException.getSQLState() != null
						&& PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
					log.error(
							"Caught unique constraint exception with sql state: {} in PaymentRequestKafkaListener for order id: {}",
							sqlException.getSQLState(), orderPaymentEventPayload.getOrderId());
				}
				else {
					throw new PaymentApplicationServiceException(
							"Throwing DataAccessException in PaymentRequestKafkaListener " + ex.getMessage(), ex);
				}
			}
			catch (PaymentNotFoundException ex) {
				// NO-OP
				log.error("No payment found for order id: {}", orderPaymentEventPayload.getOrderId());
			}
			catch (Exception ex) {
				throw new PaymentApplicationServiceException(
						"Throwing unexpected exception in PaymentRequestKafkaListener: " + ex.getMessage(), ex);
			}
		}
	}

}
