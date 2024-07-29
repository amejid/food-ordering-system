package com.food.ordering.system.domain.event.payload;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderPaymentEventPayload {

	@JsonProperty
	private String id;

	@JsonProperty
	private String sagaId;

	@JsonProperty
	private String orderId;

	@JsonProperty
	private String customerId;

	@JsonProperty
	private BigDecimal price;

	@JsonProperty
	private ZonedDateTime createdAt;

	@JsonProperty
	private String paymentOrderStatus;

}
