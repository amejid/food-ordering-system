package com.food.ordering.system.payment.service.dataaccess.creditentry.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_entry")
@Entity
public class CreditEntryEntity {

	@Id
	private UUID id;

	private UUID customerId;

	private BigDecimal totalCreditAmount;

	@Version
	private int version;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreditEntryEntity that = (CreditEntryEntity) o;
		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}
