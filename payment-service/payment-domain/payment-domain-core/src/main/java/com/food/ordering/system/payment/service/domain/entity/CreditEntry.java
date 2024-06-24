package com.food.ordering.system.payment.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.payment.service.domain.valueobject.CreditEntryId;

public final class CreditEntry extends BaseEntity<CreditEntryId> {

	private final CustomerId customerId;

	private Money totalCreditAmount;

	public void addCreditAmount(Money amount) {
		this.totalCreditAmount = this.totalCreditAmount.add(amount);
	}

	public void subtractCreditAmount(Money amount) {
		this.totalCreditAmount = this.totalCreditAmount.subtract(amount);
	}

	private CreditEntry(Builder builder) {
		setId(builder.creditEntryId);
		this.customerId = builder.customerId;
		this.totalCreditAmount = builder.totalCreditAmount;
	}

	public static Builder builder() {
		return new Builder();
	}

	public CustomerId getCustomerId() {
		return this.customerId;
	}

	public Money getTotalCreditAmount() {
		return this.totalCreditAmount;
	}

	public static final class Builder {

		private CreditEntryId creditEntryId;

		private CustomerId customerId;

		private Money totalCreditAmount;

		private Builder() {
		}

		public Builder id(CreditEntryId val) {
			this.creditEntryId = val;
			return this;
		}

		public Builder customerId(CustomerId val) {
			this.customerId = val;
			return this;
		}

		public Builder totalCreditAmount(Money val) {
			this.totalCreditAmount = val;
			return this;
		}

		public CreditEntry build() {
			return new CreditEntry(this);
		}

	}

}
