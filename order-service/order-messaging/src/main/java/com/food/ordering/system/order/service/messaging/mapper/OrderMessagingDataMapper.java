package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
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
			.setId(UUID.randomUUID().toString())
			.setSagaId("")
			.setCustomerId(order.getCustomerId().getValue().toString())
			.setOrderId(order.getId().getValue().toString())
			.setPrice(order.getPrice().getAmount())
			.setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
			.setPaymentOrderStatus(PaymentOrderStatus.PENDING)
			.build();
	}

	public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(
			OrderCancelledEvent orderCancelledEvent) {
		Order order = orderCancelledEvent.getOrder();

		return PaymentRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID().toString())
			.setSagaId("")
			.setCustomerId(order.getCustomerId().getValue().toString())
			.setOrderId(order.getId().getValue().toString())
			.setPrice(order.getPrice().getAmount())
			.setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
			.setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
			.build();
	}

	public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(
			OrderPaidEvent domainEvent) {
		Order order = domainEvent.getOrder();

		return RestaurantApprovalRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID().toString())
			.setSagaId("")
			.setOrderId(order.getId().getValue().toString())
			.setRestaurantId(order.getRestaurantId().getValue().toString())
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

	public PaymentResponse paymentResponseAvroModelToPaymentResponse(
			PaymentResponseAvroModel paymentResponseAvroModel) {
		return PaymentResponse.builder()
			.id(paymentResponseAvroModel.getId())
			.sagaId(paymentResponseAvroModel.getSagaId())
			.paymentId(paymentResponseAvroModel.getPaymentId())
			.customerId(paymentResponseAvroModel.getCustomerId())
			.orderId(paymentResponseAvroModel.getOrderId())
			.price(paymentResponseAvroModel.getPrice())
			.createdAt(paymentResponseAvroModel.getCreatedAt())
			.paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
			.failureMessages(paymentResponseAvroModel.getFailureMessages())
			.build();
	}

	public RestaurantApprovalResponse approvalResponseAvroModelToApprovalResponse(
			RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
		return RestaurantApprovalResponse.builder()
			.id(restaurantApprovalResponseAvroModel.getId())
			.sagaId(restaurantApprovalResponseAvroModel.getSagaId())
			.orderId(restaurantApprovalResponseAvroModel.getOrderId())
			.restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
			.createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
			.orderApprovalStatus(
					OrderApprovalStatus.valueOf(restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
			.failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
			.build();
	}

}
