package com.food.ordering.system.order.service.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.event.payload.OrderPaymentEventPayload;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
class OrderApplicationServiceTests {

	@Autowired
	private OrderApplicationService orderApplicationService;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private PaymentOutboxRepository paymentOutboxRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private CreateOrderCommand createOrderCommand;

	private CreateOrderCommand createOrderCommandWrongPrice;

	private CreateOrderCommand createOrderCommandWrongProductPrice;

	private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");

	private final UUID RESTAURANT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb45");

	private final UUID PRODUCT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb48");

	private final UUID ORDER_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afb");

	private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afc");

	private final BigDecimal PRICE = new BigDecimal("200.00");

	@BeforeAll
	void init() {
		this.createOrderCommand = CreateOrderCommand.builder()
			.customerId(this.CUSTOMER_ID)
			.restaurantId(this.RESTAURANT_ID)
			.address(OrderAddress.builder().street("street_1").postalCode("1000AB").city("Paris").build())
			.price(this.PRICE)
			.items(List.of(
					OrderItem.builder()
						.productId(this.PRODUCT_ID)
						.quantity(1)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("50.00"))
						.build(),
					OrderItem.builder()
						.productId(this.PRODUCT_ID)
						.quantity(3)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("150.00"))
						.build()))
			.build();

		this.createOrderCommandWrongPrice = CreateOrderCommand.builder()
			.customerId(this.CUSTOMER_ID)
			.restaurantId(this.RESTAURANT_ID)
			.address(OrderAddress.builder().street("street_1").postalCode("1000AB").city("Paris").build())
			.price(new BigDecimal("250.00"))
			.items(List.of(
					OrderItem.builder()
						.productId(this.PRODUCT_ID)
						.quantity(1)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("50.00"))
						.build(),
					OrderItem.builder()
						.productId(this.PRODUCT_ID)
						.quantity(3)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("150.00"))
						.build()))
			.build();

		this.createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
			.customerId(this.CUSTOMER_ID)
			.restaurantId(this.RESTAURANT_ID)
			.address(OrderAddress.builder().street("street_1").postalCode("1000AB").city("Paris").build())
			.price(new BigDecimal("210.00"))
			.items(List.of(
					OrderItem.builder()
						.productId(this.PRODUCT_ID)
						.quantity(1)
						.price(new BigDecimal("60.00"))
						.subTotal(new BigDecimal("60.00"))
						.build(),
					OrderItem.builder()
						.productId(this.PRODUCT_ID)
						.quantity(3)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("150.00"))
						.build()))
			.build();

		Customer customer = new Customer(new CustomerId(this.CUSTOMER_ID));

		Restaurant restaurantResponse = Restaurant.builder()
			.restaurantId(new RestaurantId(this.createOrderCommand.getRestaurantId()))
			.products(List.of(
					new Product(new ProductId(this.PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
					new Product(new ProductId(this.PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
			.active(true)
			.build();

		Order order = this.orderDataMapper.createOrderCommandToOrder(this.createOrderCommand);
		order.setId(new OrderId(this.ORDER_ID));

		Mockito.when(this.customerRepository.findCustomer(this.CUSTOMER_ID)).thenReturn(Optional.of(customer));
		Mockito
			.when(this.restaurantRepository.findRestaurantInformation(
					this.orderDataMapper.createOrderCommandToRestaurant(this.createOrderCommand)))
			.thenReturn(Optional.of(restaurantResponse));
		Mockito.when(this.orderRepository.save(any(Order.class))).thenReturn(order);
		Mockito.when(this.paymentOutboxRepository.save(any(OrderPaymentOutboxMessage.class)))
			.thenReturn(getOrderPaymentOutboxMessage());
	}

	@Test
	void testCreateOrder() {
		CreateOrderResponse createOrderResponse = this.orderApplicationService.createOrder(this.createOrderCommand);
		assertThat(createOrderResponse.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(createOrderResponse.getMessage()).isEqualTo("Order Created Successfully");
		assertThat(createOrderResponse.getOrderTrackingId()).isNotNull();
	}

	@Test
	void testCreateOrderWithWrongTotalPrice() {
		assertThatThrownBy(() -> this.orderApplicationService.createOrder(this.createOrderCommandWrongPrice))
			.isInstanceOf(OrderDomainException.class)
			.hasMessage("Total price: 250.00 is not equal to Order items total: 200.00!");

	}

	@Test
	void testCreateOrderWithWrongProductPrice() {
		assertThatThrownBy(() -> this.orderApplicationService.createOrder(this.createOrderCommandWrongProductPrice))
			.isInstanceOf(OrderDomainException.class)
			.hasMessage("Order item price: 60.00 is not valid for product " + this.PRODUCT_ID);
	}

	@Test
	void testCreateOrderWithPassiveRestaurant() {
		Restaurant restaurantResponse = Restaurant.builder()
			.restaurantId(new RestaurantId(this.createOrderCommand.getRestaurantId()))
			.products(List.of(
					new Product(new ProductId(this.PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
					new Product(new ProductId(this.PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
			.active(false)
			.build();
		Mockito
			.when(this.restaurantRepository.findRestaurantInformation(
					this.orderDataMapper.createOrderCommandToRestaurant(this.createOrderCommand)))
			.thenReturn(Optional.of(restaurantResponse));
		assertThatThrownBy(() -> this.orderApplicationService.createOrder(this.createOrderCommand))
			.isInstanceOf(OrderDomainException.class)
			.hasMessage("Restaurant with id " + this.RESTAURANT_ID + " is currently not active!");
	}

	private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
		OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
			.orderId(this.ORDER_ID.toString())
			.customerId(this.CUSTOMER_ID.toString())
			.price(this.PRICE)
			.createdAt(ZonedDateTime.now())
			.paymentOrderStatus(PaymentOrderStatus.PENDING.name())
			.build();

		return OrderPaymentOutboxMessage.builder()
			.id(UUID.randomUUID())
			.sagaId(this.SAGA_ID)
			.createdAt(ZonedDateTime.now())
			.type(SagaConstants.ORDER_SAGA_NAME)
			.payload(createPayload(orderPaymentEventPayload))
			.orderStatus(OrderStatus.PENDING)
			.sagaStatus(SagaStatus.STARTED)
			.outboxStatus(OutboxStatus.STARTED)
			.version(0)
			.build();
	}

	private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
		try {
			return this.objectMapper.writeValueAsString(orderPaymentEventPayload);
		}
		catch (JsonProcessingException ex) {
			throw new OrderDomainException("Cannot serialize OrderPaymentEventPayload!");
		}
	}

}
