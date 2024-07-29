package com.food.ordering.system.domain.event.payload;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentOrderEventPayload {

	@JsonProperty
	private String paymentId;

	@JsonProperty
	private String customerId;

	@JsonProperty
	private String orderId;

	@JsonProperty
	private BigDecimal price;

	@JsonProperty
	private ZonedDateTime createdAt;

	@JsonProperty
	private String paymentStatus;

	@JsonProperty
	private List<String> failureMessages;

}
