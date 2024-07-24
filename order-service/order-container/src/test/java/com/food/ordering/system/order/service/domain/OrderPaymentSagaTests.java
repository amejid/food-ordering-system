package com.food.ordering.system.order.service.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = OrderServiceApplication.class)
@Sql({ "classpath:sql/OrderPaymentSagaTestSetUp.sql" })
@Sql(value = { "classpath:sql/OrderPaymentSagaTestCleanUp.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderPaymentSagaTests {

	@Autowired
	private OrderPaymentSaga orderPaymentSaga;

	@Autowired
	private PaymentOutboxJpaRepository paymentOutboxJpaRepository;

	private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afa");

	private final UUID ORDER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb17");

	private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");

	private final UUID PAYMENT_ID = UUID.randomUUID();

	private final BigDecimal PRICE = new BigDecimal("100");

	@Test
	void testDoublePayment() {
		this.orderPaymentSaga.process(getPaymentResponse());
		this.orderPaymentSaga.process(getPaymentResponse());
	}

	@Test
	void testDoublePaymentWithThreads() throws InterruptedException {
		Thread thread1 = new Thread(() -> this.orderPaymentSaga.process(getPaymentResponse()));
		Thread thread2 = new Thread(() -> this.orderPaymentSaga.process(getPaymentResponse()));

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		assertPaymentOutbox();
	}

	@Test
	void testDoublePaymentWithLatch() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);

		Thread thread1 = new Thread(() -> {
			try {
				this.orderPaymentSaga.process(getPaymentResponse());
			}
			catch (OptimisticLockingFailureException ex) {
				log.error("OptimisticLockingFailureException occurred for thread1");
			}
			finally {
				latch.countDown();
			}
		});

		Thread thread2 = new Thread(() -> {
			try {
				this.orderPaymentSaga.process(getPaymentResponse());
			}
			catch (OptimisticLockingFailureException ex) {
				log.error("OptimisticLockingFailureException occurred for thread2");
			}
			finally {
				latch.countDown();
			}
		});

		thread1.start();
		thread2.start();

		latch.await();

		assertPaymentOutbox();

	}

	private void assertPaymentOutbox() {
		Optional<PaymentOutboxEntity> paymentOutboxEntity = this.paymentOutboxJpaRepository
			.findByTypeAndSagaIdAndSagaStatusIn(SagaConstants.ORDER_SAGA_NAME, this.SAGA_ID,
					List.of(SagaStatus.PROCESSING));
		assertThat(paymentOutboxEntity).isPresent();
	}

	private PaymentResponse getPaymentResponse() {
		return PaymentResponse.builder()
			.id(UUID.randomUUID().toString())
			.sagaId(this.SAGA_ID.toString())
			.paymentStatus(PaymentStatus.COMPLETED)
			.paymentId(this.PAYMENT_ID.toString())
			.orderId(this.ORDER_ID.toString())
			.customerId(this.CUSTOMER_ID.toString())
			.price(this.PRICE)
			.createdAt(Instant.now())
			.failureMessages(new ArrayList<>())
			.build();
	}

}
