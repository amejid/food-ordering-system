package tech.amejid.order.service.domain.mapper;

import java.util.List;
import java.util.UUID;

import tech.amejid.domain.valueobject.CustomerId;
import tech.amejid.domain.valueobject.Money;
import tech.amejid.domain.valueobject.ProductId;
import tech.amejid.domain.valueobject.RestaurantId;
import tech.amejid.order.service.domain.dto.create.CreateOrderCommand;
import tech.amejid.order.service.domain.dto.create.CreateOrderResponse;
import tech.amejid.order.service.domain.dto.create.OrderAddress;
import tech.amejid.order.service.domain.dto.track.TrackOrderResponse;
import tech.amejid.order.service.domain.entity.Order;
import tech.amejid.order.service.domain.entity.OrderItem;
import tech.amejid.order.service.domain.entity.Product;
import tech.amejid.order.service.domain.entity.Restaurant;
import tech.amejid.order.service.domain.valueobject.StreetAddress;

import org.springframework.stereotype.Component;

@Component
public class OrderDataMapper {

	public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
		return Restaurant.builder()
			.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
			.products(createOrderCommand.getItems()
				.stream()
				.map(orderItem -> new Product(new ProductId(orderItem.getProductId())))
				.toList())
			.build();
	}

	public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
		return Order.builder()
			.customerId(new CustomerId(createOrderCommand.getCustomerId()))
			.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
			.deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
			.price(new Money(createOrderCommand.getPrice()))
			.items(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
			.build();
	}

	public CreateOrderResponse orderToCreateOrderResponse(Order order) {
		return CreateOrderResponse.builder()
			.orderTrackingId(order.getTrackingId().getValue())
			.orderStatus(order.getOrderStatus())
			.build();
	}

	public TrackOrderResponse orderToTrackOrderResponse(Order order) {
		return TrackOrderResponse.builder()
			.orderTrackingId(order.getTrackingId().getValue())
			.orderStatus(order.getOrderStatus())
			.failureMessages(order.getFailureMessages())
			.build();
	}

	private List<OrderItem> orderItemsToOrderItemEntities(
			List<tech.amejid.order.service.domain.dto.create.OrderItem> orderItems) {
		return orderItems.stream()
			.map(orderItem -> OrderItem.builder()
				.product(new Product(new ProductId(orderItem.getProductId())))
				.price(new Money(orderItem.getPrice()))
				.quantity(orderItem.getQuantity())
				.subTotal(new Money(orderItem.getSubTotal()))
				.build())
			.toList();
	}

	private StreetAddress orderAddressToStreetAddress(OrderAddress orderAddress) {
		return new StreetAddress(UUID.randomUUID(), orderAddress.getStreet(), orderAddress.getPostalCode(),
				orderAddress.getCity());
	}

}
