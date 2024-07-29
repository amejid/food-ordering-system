package com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.outbox.OutboxStatus;
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
@Table(name = "order_outbox")
@Entity
public class OrderOutboxEntity {

	@Id
	private UUID id;

	private UUID sagaId;

	private ZonedDateTime createdAt;

	private ZonedDateTime processedAt;

	private String type;

	private String payload;

	@Enumerated(EnumType.STRING)
	private OutboxStatus outboxStatus;

	@Enumerated(EnumType.STRING)
	private OrderApprovalStatus approvalStatus;

	private int version;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		OrderOutboxEntity that = (OrderOutboxEntity) o;
		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}