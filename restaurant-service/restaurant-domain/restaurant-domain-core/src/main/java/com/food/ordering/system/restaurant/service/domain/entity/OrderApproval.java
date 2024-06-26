package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

public final class OrderApproval extends BaseEntity<OrderApprovalId> {

	private final RestaurantId restaurantId;

	private final OrderId orderId;

	private final OrderApprovalStatus orderApprovalStatus;

	private OrderApproval(Builder builder) {
		setId(builder.orderApprovalId);
		this.restaurantId = builder.restaurantId;
		this.orderId = builder.orderId;
		this.orderApprovalStatus = builder.orderApprovalStatus;
	}

	public static Builder builder() {
		return new Builder();
	}

	public RestaurantId getRestaurantId() {
		return this.restaurantId;
	}

	public OrderId getOrderId() {
		return this.orderId;
	}

	public OrderApprovalStatus getOrderApprovalStatus() {
		return this.orderApprovalStatus;
	}

	public static final class Builder {

		private OrderApprovalId orderApprovalId;

		private RestaurantId restaurantId;

		private OrderId orderId;

		private OrderApprovalStatus orderApprovalStatus;

		private Builder() {
		}

		public Builder orderApprovalId(OrderApprovalId val) {
			this.orderApprovalId = val;
			return this;
		}

		public Builder restaurantId(RestaurantId val) {
			this.restaurantId = val;
			return this;
		}

		public Builder orderId(OrderId val) {
			this.orderId = val;
			return this;
		}

		public Builder orderApprovalStatus(OrderApprovalStatus val) {
			this.orderApprovalStatus = val;
			return this;
		}

		public OrderApproval build() {
			return new OrderApproval(this);
		}

	}

}
