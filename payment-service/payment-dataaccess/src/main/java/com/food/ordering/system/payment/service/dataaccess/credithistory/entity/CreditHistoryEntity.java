package com.food.ordering.system.payment.service.dataaccess.credithistory.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.food.ordering.system.payment.service.domain.valueobject.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "credit_history")
@Entity
public class CreditHistoryEntity {

	@Id
	private UUID id;

	private UUID customerId;

	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private TransactionType type;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreditHistoryEntity that = (CreditHistoryEntity) o;
		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}
