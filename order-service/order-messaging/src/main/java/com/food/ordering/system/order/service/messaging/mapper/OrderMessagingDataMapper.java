package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.CustomerModel;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;

import org.springframework.stereotype.Component;

@Component
public class OrderMessagingDataMapper {

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

	public PaymentRequestAvroModel orderPaymentEventToPaymentRequestAvroModel(String sagaId,
			OrderPaymentEventPayload orderPaymentEventPayload) {
		return PaymentRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID().toString())
			.setSagaId(sagaId)
			.setCustomerId(orderPaymentEventPayload.getCustomerId())
			.setOrderId(orderPaymentEventPayload.getOrderId())
			.setPrice(orderPaymentEventPayload.getPrice())
			.setCreatedAt(orderPaymentEventPayload.getCreatedAt().toInstant())
			.setPaymentOrderStatus(PaymentOrderStatus.valueOf(orderPaymentEventPayload.getPaymentOrderStatus()))
			.build();
	}

	public RestaurantApprovalRequestAvroModel orderApprovalEventToRestaurantApprovalRequestAvroModel(String sagaId,
			OrderApprovalEventPayload orderApprovalEventPayload) {
		return RestaurantApprovalRequestAvroModel.newBuilder()
			.setId(UUID.randomUUID().toString())
			.setSagaId(sagaId)
			.setOrderId(orderApprovalEventPayload.getOrderId())
			.setRestaurantId(orderApprovalEventPayload.getRestaurantId())
			.setRestaurantOrderStatus(
					RestaurantOrderStatus.valueOf(orderApprovalEventPayload.getRestaurantOrderStatus()))
			.setProducts(orderApprovalEventPayload.getProducts()
				.stream()
				.map(orderApprovalEventProduct -> Product.newBuilder()
					.setId(orderApprovalEventProduct.getId())
					.setQuantity(orderApprovalEventProduct.getQuantity())
					.build())
				.toList())
			.setPrice(orderApprovalEventPayload.getPrice())
			.setCreatedAt(orderApprovalEventPayload.getCreatedAt().toInstant())
			.build();
	}

	public CustomerModel customerAvroModeltoCustomerModel(CustomerAvroModel customerAvroModel) {
		return CustomerModel.builder()
			.id(customerAvroModel.getId())
			.username(customerAvroModel.getUsername())
			.firstName(customerAvroModel.getFirstName())
			.lastName(customerAvroModel.getLastName())
			.build();
	}

}
