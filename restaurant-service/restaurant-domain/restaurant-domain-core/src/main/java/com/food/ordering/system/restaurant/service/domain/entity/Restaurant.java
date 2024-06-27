package com.food.ordering.system.restaurant.service.domain.entity;

import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

public final class Restaurant extends AggregateRoot<RestaurantId> {

	private OrderApproval orderApproval;

	private boolean active;

	private final OrderDetail orderDetail;

	public void validateOrder(List<String> failureMessages) {
		if (this.orderDetail.getOrderStatus() != OrderStatus.PAID) {
			failureMessages.add("Payment is not completed for order: " + this.orderDetail.getId());
		}

		Money totalAmount = this.orderDetail.getProducts().stream().map(product -> {
			if (!product.isAvailable()) {
				failureMessages.add("Product with id: " + product.getId().getValue() + " is not available");
			}

			return product.getPrice().multiply(product.getQuantity());
		}).reduce(Money.ZERO, Money::add);

		if (!totalAmount.equals(this.orderDetail.getTotalAmount())) {
			failureMessages.add("Price total is not correct for order: " + this.orderDetail.getId());
		}
	}

	public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
		this.orderApproval = OrderApproval.builder()
			.orderApprovalId(new OrderApprovalId(UUID.randomUUID()))
			.restaurantId(this.getId())
			.orderId(getOrderDetail().getId())
			.approvalStatus(orderApprovalStatus)
			.build();

	}

	public void setActive(boolean active) {
		this.active = active;
	}

	private Restaurant(Builder builder) {
		setId(builder.restaurantId);
		this.orderApproval = builder.orderApproval;
		this.active = builder.active;
		this.orderDetail = builder.orderDetail;
	}

	public static Builder builder() {
		return new Builder();
	}

	public OrderApproval getOrderApproval() {
		return this.orderApproval;
	}

	public boolean isActive() {
		return this.active;
	}

	public OrderDetail getOrderDetail() {
		return this.orderDetail;
	}

	public static final class Builder {

		private RestaurantId restaurantId;

		private OrderApproval orderApproval;

		private boolean active;

		private OrderDetail orderDetail;

		private Builder() {
		}

		public Builder restaurantId(RestaurantId val) {
			this.restaurantId = val;
			return this;
		}

		public Builder orderApproval(OrderApproval val) {
			this.orderApproval = val;
			return this;
		}

		public Builder active(boolean val) {
			this.active = val;
			return this;
		}

		public Builder orderDetail(OrderDetail val) {
			this.orderDetail = val;
			return this;
		}

		public Restaurant build() {
			return new Restaurant(this);
		}

	}

}
