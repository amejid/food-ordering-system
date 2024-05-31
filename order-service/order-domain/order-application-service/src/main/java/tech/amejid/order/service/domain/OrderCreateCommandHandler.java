package tech.amejid.order.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.amejid.order.service.domain.dto.create.CreateOrderCommand;
import tech.amejid.order.service.domain.dto.create.CreateOrderResponse;
import tech.amejid.order.service.domain.event.OrderCreatedEvent;
import tech.amejid.order.service.domain.mapper.OrderDataMapper;
import tech.amejid.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {

	private final OrderCreateHelper orderCreateHelper;

	private final OrderDataMapper orderDataMapper;

	private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;

	public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
		OrderCreatedEvent orderCreatedEvent = this.orderCreateHelper.persistOrder(createOrderCommand);
		log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
		this.orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);
		return this.orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder());
	}

}
