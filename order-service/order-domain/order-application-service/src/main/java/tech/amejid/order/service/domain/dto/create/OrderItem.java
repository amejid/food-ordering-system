package tech.amejid.order.service.domain.dto.create;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItem {

	@NotNull
	private final UUID productId;

	@NotNull
	private final Integer quantity;

	@NotNull
	private final BigDecimal price;

	@NotNull
	private final BigDecimal subTotal;

}
