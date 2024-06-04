package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;

import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;

import org.springframework.stereotype.Component;

@Component
public class OrderMessagingDataMapper {

	public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent) {
		Order order = orderCreatedEvent.getOrder();

		return PaymentRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID())
			.setSagaId(UUID.fromString(""))
			.setCustomerId(order.getCustomerId().getValue())
			.setOrderId(order.getId().getValue())
			.setPrice(order.getPrice().getAmount())
			.setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
			.setPaymentOrderStatus(PaymentOrderStatus.PENDING)
			.build();
	}

	public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(
			OrderCancelledEvent orderCancelledEvent) {
		Order order = orderCancelledEvent.getOrder();

		return PaymentRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID())
			.setSagaId(UUID.fromString(""))
			.setCustomerId(order.getCustomerId().getValue())
			.setOrderId(order.getId().getValue())
			.setPrice(order.getPrice().getAmount())
			.setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
			.setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
			.build();
	}

	public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(
			OrderPaidEvent domainEvent) {
		Order order = domainEvent.getOrder();

		return RestaurantApprovalRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID())
			.setSagaId(UUID.fromString(""))
			.setOrderId(order.getId().getValue())
			.setRestaurantId(order.getRestaurantId().getValue())
			.setProducts(order.getItems()
				.stream()
				.map(orderItem -> Product.newBuilder()
					.setId(orderItem.getProduct().getId().getValue().toString())
					.setQuantity(orderItem.getQuantity())
					.build())
				.toList())
			.setPrice(order.getPrice().getAmount())
			.setCreatedAt(domainEvent.getCreatedAt().toInstant())
			.setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
			.build();
	}

}
