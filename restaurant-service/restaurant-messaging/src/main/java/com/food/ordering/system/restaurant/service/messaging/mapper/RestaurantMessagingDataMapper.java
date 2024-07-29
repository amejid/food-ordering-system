package com.food.ordering.system.restaurant.service.messaging.mapper;

import java.time.Instant;
import java.util.UUID;

import com.food.ordering.system.domain.event.payload.OrderApprovalEventPayload;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantOrderStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import debezium.order.restaurant_approval_outbox.Value;

import org.springframework.stereotype.Component;

@Component
public class RestaurantMessagingDataMapper {

	public RestaurantApprovalRequest restaurantApprovalRequestAvroModelToRestaurantApproval(
			OrderApprovalEventPayload orderApprovalEventPayload, Value restaurantApprovalRequestAvroModel) {
		return RestaurantApprovalRequest.builder()
			.id(restaurantApprovalRequestAvroModel.getId())
			.sagaId(restaurantApprovalRequestAvroModel.getSagaId())
			.restaurantId(orderApprovalEventPayload.getRestaurantId())
			.orderId(orderApprovalEventPayload.getOrderId())
			.restaurantOrderStatus(RestaurantOrderStatus.valueOf(orderApprovalEventPayload.getRestaurantOrderStatus()))
			.products(orderApprovalEventPayload.getProducts()
				.stream()
				.map(avroModel -> Product.builder()
					.productId(new ProductId(UUID.fromString(avroModel.getId())))
					.quantity(avroModel.getQuantity())
					.build())
				.toList())
			.price(orderApprovalEventPayload.getPrice())
			.createdAt(Instant.parse(restaurantApprovalRequestAvroModel.getCreatedAt()))
			.build();
	}

}
