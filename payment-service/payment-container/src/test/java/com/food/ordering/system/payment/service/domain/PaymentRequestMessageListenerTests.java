package com.food.ordering.system.payment.service.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.dataaccess.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.saga.order.SagaConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = PaymentServiceApplication.class)
class PaymentRequestMessageListenerTests {

	@Autowired
	private PaymentRequestMessageListener paymentRequestMessageListener;

	@Autowired
	private OrderOutboxJpaRepository orderOutboxJpaRepository;

	private static final String CUSTOMER_ID = "d215b5f8-0249-4dc5-89a3-51fd148cfb41";

	private static final BigDecimal PRICE = new BigDecimal("100");

	@Test
	void testDoublePayment() {
		String sagaId = UUID.randomUUID().toString();
		this.paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
		try {
			this.paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
		}
		catch (DataAccessException ex) {
			log.error("DataAccessException occurred with sql state: {}",
					((PSQLException) Objects.requireNonNull(ex.getRootCause())).getSQLState());
		}
		assertOrderOutbox(sagaId);
	}

	@Test
	void testDoublePaymentWithThreads() {
		String sagaId = UUID.randomUUID().toString();
		ExecutorService executor = null;

		try {
			executor = Executors.newFixedThreadPool(2);
			List<Callable<Object>> tasks = new ArrayList<>();

			tasks.add(Executors.callable(() -> {
				try {
					this.paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
				}
				catch (DataAccessException ex) {
					log.error("DataAccessException occurred for thread 1 with sql state: {}",
							((PSQLException) Objects.requireNonNull(ex.getRootCause())).getSQLState());
				}
			}));

			tasks.add(Executors.callable(() -> {
				try {
					this.paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
				}
				catch (DataAccessException ex) {
					log.error("DataAccessException occurred for thread 2 with sql state: {}",
							((PSQLException) Objects.requireNonNull(ex.getRootCause())).getSQLState());
				}
			}));

			executor.invokeAll(tasks);

			assertOrderOutbox(sagaId);
		}
		catch (InterruptedException ex) {
			log.error("Error calling complete payment!", ex);
		}
		finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}

	private void assertOrderOutbox(String sagaId) {
		Optional<OrderOutboxEntity> orderOutboxEntity = this.orderOutboxJpaRepository
			.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(SagaConstants.ORDER_SAGA_NAME, UUID.fromString(sagaId),
					PaymentStatus.COMPLETED, OutboxStatus.STARTED);
		assertThat(orderOutboxEntity).isPresent();
		assertThat(orderOutboxEntity.get().getSagaId()).hasToString(sagaId);
	}

	private PaymentRequest getPaymentRequest(String sagaId) {
		return PaymentRequest.builder()
			.id(UUID.randomUUID().toString())
			.sagaId(sagaId)
			.orderId(UUID.randomUUID().toString())
			.paymentOrderStatus(com.food.ordering.system.domain.valueobject.PaymentOrderStatus.PENDING)
			.customerId(CUSTOMER_ID)
			.price(PRICE)
			.createdAt(Instant.now())
			.build();
	}

}
