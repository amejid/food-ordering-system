package com.food.ordering.system.payment.service.domain.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;

public final class Payment extends AggregateRoot<PaymentId> {

	private final OrderId orderId;

	private final CustomerId customerId;

	private final Money price;

	private PaymentStatus paymentStatus;

	private ZonedDateTime createdAt;

	public void initializePayment() {
		setId(new PaymentId(UUID.randomUUID()));
		this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
	}

	public void validatePayment(List<String> failureMessages) {
		if (this.price == null || !this.price.isGreaterThanZero()) {
			failureMessages.add("Total price must be greater than zero");
		}
	}

	public void updateStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	private Payment(Builder builder) {
		setId(builder.paymentId);
		this.orderId = builder.orderId;
		this.customerId = builder.customerId;
		this.price = builder.price;
		this.paymentStatus = builder.paymentStatus;
		this.createdAt = builder.createdAt;
	}

	public static Builder builder() {
		return new Builder();
	}

	public OrderId getOrderId() {
		return this.orderId;
	}

	public CustomerId getCustomerId() {
		return this.customerId;
	}

	public Money getPrice() {
		return this.price;
	}

	public PaymentStatus getPaymentStatus() {
		return this.paymentStatus;
	}

	public ZonedDateTime getCreatedAt() {
		return this.createdAt;
	}

	public static final class Builder {

		private PaymentId paymentId;

		private OrderId orderId;

		private CustomerId customerId;

		private Money price;

		private PaymentStatus paymentStatus;

		private ZonedDateTime createdAt;

		private Builder() {
		}

		public Builder paymentId(PaymentId val) {
			this.paymentId = val;
			return this;
		}

		public Builder orderId(OrderId val) {
			this.orderId = val;
			return this;
		}

		public Builder customerId(CustomerId val) {
			this.customerId = val;
			return this;
		}

		public Builder price(Money val) {
			this.price = val;
			return this;
		}

		public Builder paymentStatus(PaymentStatus val) {
			this.paymentStatus = val;
			return this;
		}

		public Builder createdAt(ZonedDateTime val) {
			this.createdAt = val;
			return this;
		}

		public Payment build() {
			return new Payment(this);
		}

	}

}
