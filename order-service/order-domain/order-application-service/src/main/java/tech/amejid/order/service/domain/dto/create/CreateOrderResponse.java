package tech.amejid.order.service.domain.dto.create;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tech.amejid.domain.valueobject.OrderStatus;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrderResponse {

	@NotNull
	private final UUID orderTrackingId;

	@NotNull
	private final OrderStatus orderStatus;

	@NotNull
	private final String message;

}
