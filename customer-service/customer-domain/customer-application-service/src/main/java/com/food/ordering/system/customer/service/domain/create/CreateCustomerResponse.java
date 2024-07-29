package com.food.ordering.system.customer.service.domain.create;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCustomerResponse {

	@NotNull
	private final UUID customerId;

	@NotNull
	private final String message;

}
