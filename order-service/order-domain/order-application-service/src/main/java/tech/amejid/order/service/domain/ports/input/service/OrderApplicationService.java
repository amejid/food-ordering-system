package tech.amejid.order.service.domain.ports.input.service;

import jakarta.validation.Valid;
import tech.amejid.order.service.domain.dto.create.CreateOrderCommand;
import tech.amejid.order.service.domain.dto.create.CreateOrderResponse;
import tech.amejid.order.service.domain.dto.track.TrackOrderQuery;
import tech.amejid.order.service.domain.dto.track.TrackOrderResponse;

public interface OrderApplicationService {

	CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);

	TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);

}
