package tech.amejid.order.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.amejid.order.service.domain.dto.create.CreateOrderCommand;
import tech.amejid.order.service.domain.dto.create.CreateOrderResponse;
import tech.amejid.order.service.domain.dto.track.TrackOrderQuery;
import tech.amejid.order.service.domain.dto.track.TrackOrderResponse;
import tech.amejid.order.service.domain.ports.input.service.OrderApplicationService;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class OrderApplicationServiceImpl implements OrderApplicationService {

	private final OrderCreateCommandHandler orderCreateCommandHandler;

	private final OrderTrackQueryHandler orderTrackQueryHandler;

	@Override
	public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
		return this.orderCreateCommandHandler.createOrder(createOrderCommand);
	}

	@Override
	public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
		return this.orderTrackQueryHandler.trackOrder(trackOrderQuery);
	}

}
