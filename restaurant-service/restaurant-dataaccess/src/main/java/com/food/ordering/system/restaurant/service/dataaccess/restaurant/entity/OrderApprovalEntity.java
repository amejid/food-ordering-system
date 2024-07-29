package com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity;

import java.util.UUID;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
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
@Table(name = "order_approval", schema = "restaurant")
@Entity
public class OrderApprovalEntity {

	@Id
	private UUID id;

	private UUID restaurantId;

	private UUID orderId;

	@Enumerated(EnumType.STRING)
	private OrderApprovalStatus status;

}
