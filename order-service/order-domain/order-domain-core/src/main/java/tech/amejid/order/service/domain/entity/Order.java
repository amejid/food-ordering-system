package tech.amejid.order.service.domain.entity;

import java.util.List;
import java.util.UUID;

import tech.amejid.domain.entity.AggregateRoot;
import tech.amejid.domain.valueobject.CustomerId;
import tech.amejid.domain.valueobject.Money;
import tech.amejid.domain.valueobject.OrderId;
import tech.amejid.domain.valueobject.OrderStatus;
import tech.amejid.domain.valueobject.RestaurantId;
import tech.amejid.order.service.domain.exception.OrderDomainException;
import tech.amejid.order.service.domain.valueobject.OrderItemId;
import tech.amejid.order.service.domain.valueobject.StreetAddress;
import tech.amejid.order.service.domain.valueobject.TrackingId;

public final class Order extends AggregateRoot<OrderId> {

	private final CustomerId customerId;

	private final RestaurantId restaurantId;

	private final StreetAddress deliveryAddress;

	private final Money price;

	private final List<OrderItem> items;

	private TrackingId trackingId;

	private OrderStatus orderStatus;

	private List<String> failureMessages;

	public void initializeOrder() {
		setId(new OrderId(UUID.randomUUID()));
		this.trackingId = new TrackingId(UUID.randomUUID());
		this.orderStatus = OrderStatus.PENDING;
		initializeOrderItems();
	}

	public void validateOrder() {
		validateInitialOrder();
		validateTotalPrice();
		validateItemsPrice();
	}

	public void pay() {
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new OrderDomainException("Order is not in a correct state for pay operation!");
		}
		this.orderStatus = OrderStatus.PAID;
	}

	public void approve() {
		if (this.orderStatus != OrderStatus.PAID) {
			throw new OrderDomainException("Order is not in a correct state for approve operation!");
		}
		this.orderStatus = OrderStatus.APPROVED;
	}

	public void initCancel(List<String> failureMessages) {
		if (this.orderStatus != OrderStatus.PAID) {
			throw new OrderDomainException("Order is not in a correct state for cancelling operation!");
		}
		this.orderStatus = OrderStatus.CANCELLING;
		updateFailureMessages(failureMessages);
	}

	public void cancel(List<String> failureMessages) {
		if (!(this.orderStatus == OrderStatus.PENDING || this.orderStatus == OrderStatus.CANCELLING)) {
			throw new OrderDomainException("Order is not in a correct state for cancel operation!");
		}
		this.orderStatus = OrderStatus.CANCELLED;
		updateFailureMessages(failureMessages);
	}

	private void updateFailureMessages(List<String> failureMessages) {
		if (this.failureMessages != null && failureMessages != null) {
			this.failureMessages.addAll(failureMessages.stream().filter(message -> !message.isEmpty()).toList());
		}
		if (this.failureMessages == null) {
			this.failureMessages = failureMessages;
		}
	}

	private void validateInitialOrder() {
		if (this.orderStatus != null || getId() != null) {
			throw new OrderDomainException("Order is not in a correct state for initialization!");
		}
	}

	private void validateTotalPrice() {
		if (this.price == null || !this.price.isGreaterThanZero()) {
			throw new OrderDomainException("Total price must be greater than zero!");
		}
	}

	private void validateItemsPrice() {
		Money orderItemsTotal = this.items.stream().map(orderItem -> {
			validateItemPrice(orderItem);

			return orderItem.getSubTotal();
		}).reduce(Money.ZERO, Money::add);

		if (!this.price.equals(orderItemsTotal)) {
			throw new OrderDomainException("Total price: " + this.price.getAmount()
					+ " is not equal to Order items total: " + orderItemsTotal.getAmount() + "!");
		}
	}

	private void validateItemPrice(OrderItem orderItem) {
		if (!orderItem.isPriceValid()) {
			throw new OrderDomainException("Order item price: " + orderItem.getPrice().getAmount()
					+ " is not valid for product " + orderItem.getProduct().getId().getValue());
		}

	}

	private void initializeOrderItems() {
		long itemId = 1;
		for (OrderItem orderItem : this.items) {
			orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
		}
	}

	private Order(Builder builder) {
		super.setId(builder.orderId);
		this.customerId = builder.customerId;
		this.restaurantId = builder.restaurantId;
		this.deliveryAddress = builder.deliveryAddress;
		this.price = builder.price;
		this.items = builder.items;
		this.trackingId = builder.trackingId;
		this.orderStatus = builder.orderStatus;
		this.failureMessages = builder.failureMessages;
	}

	public static Builder builder() {
		return new Builder();
	}

	public CustomerId getCustomerId() {
		return this.customerId;
	}

	public RestaurantId getRestaurantId() {
		return this.restaurantId;
	}

	public StreetAddress getDeliveryAddress() {
		return this.deliveryAddress;
	}

	public Money getPrice() {
		return this.price;
	}

	public List<OrderItem> getItems() {
		return this.items;
	}

	public TrackingId getTrackingId() {
		return this.trackingId;
	}

	public OrderStatus getOrderStatus() {
		return this.orderStatus;
	}

	public List<String> getFailureMessages() {
		return this.failureMessages;
	}

	public static final class Builder {

		private OrderId orderId;

		private CustomerId customerId;

		private RestaurantId restaurantId;

		private StreetAddress deliveryAddress;

		private Money price;

		private List<OrderItem> items;

		private TrackingId trackingId;

		private OrderStatus orderStatus;

		private List<String> failureMessages;

		private Builder() {
		}

		public Builder orderId(OrderId val) {
			this.orderId = val;
			return this;
		}

		public Builder customerId(CustomerId val) {
			this.customerId = val;
			return this;
		}

		public Builder restaurantId(RestaurantId val) {
			this.restaurantId = val;
			return this;
		}

		public Builder deliveryAddress(StreetAddress val) {
			this.deliveryAddress = val;
			return this;
		}

		public Builder price(Money val) {
			this.price = val;
			return this;
		}

		public Builder items(List<OrderItem> val) {
			this.items = val;
			return this;
		}

		public Builder trackingId(TrackingId val) {
			this.trackingId = val;
			return this;
		}

		public Builder orderStatus(OrderStatus val) {
			this.orderStatus = val;
			return this;
		}

		public Builder failureMessages(List<String> val) {
			this.failureMessages = val;
			return this;
		}

		public Order build() {
			return new Order(this);
		}

	}

}
