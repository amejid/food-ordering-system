package tech.amejid.order.service.domain.entity;

import tech.amejid.domain.entity.BaseEntity;
import tech.amejid.domain.valueobject.Money;
import tech.amejid.domain.valueobject.OrderId;
import tech.amejid.order.service.domain.valueobject.OrderItemId;

public final class OrderItem extends BaseEntity<OrderItemId> {

	private OrderId orderId;

	private final Product product;

	private final int quantity;

	private final Money price;

	private final Money subTotal;

	void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
		this.orderId = orderId;
		super.setId(orderItemId);
	}

	boolean isPriceValid() {
		return this.price.isGreaterThanZero() && this.price.equals(this.product.getPrice())
				&& this.price.multiply(this.quantity).equals(this.subTotal);
	}

	private OrderItem(Builder builder) {
		super.setId(builder.orderItemId);
		this.product = builder.product;
		this.quantity = builder.quantity;
		this.price = builder.price;
		this.subTotal = builder.subTotal;
	}

	public static Builder builder() {
		return new Builder();
	}

	public OrderId getOrderId() {
		return this.orderId;
	}

	public Product getProduct() {
		return this.product;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public Money getPrice() {
		return this.price;
	}

	public Money getSubTotal() {
		return this.subTotal;
	}

	public static final class Builder {

		private OrderItemId orderItemId;

		private Product product;

		private int quantity;

		private Money price;

		private Money subTotal;

		private Builder() {
		}

		public Builder orderItemId(OrderItemId val) {
			this.orderItemId = val;
			return this;
		}

		public Builder product(Product val) {
			this.product = val;
			return this;
		}

		public Builder quantity(int val) {
			this.quantity = val;
			return this;
		}

		public Builder price(Money val) {
			this.price = val;
			return this;
		}

		public Builder subTotal(Money val) {
			this.subTotal = val;
			return this;
		}

		public OrderItem build() {
			return new OrderItem(this);
		}

	}

}
