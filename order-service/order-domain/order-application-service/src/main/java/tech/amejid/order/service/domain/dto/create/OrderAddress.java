package tech.amejid.order.service.domain.dto.create;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderAddress {

	@NotNull
	@Max(50)
	private final String street;

	@NotNull
	@Max(10)
	private final String postalCode;

	@NotNull
	@Max(50)
	private final String city;

}
