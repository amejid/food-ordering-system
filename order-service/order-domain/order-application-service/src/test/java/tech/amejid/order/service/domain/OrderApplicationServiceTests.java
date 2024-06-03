package tech.amejid.order.service.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import tech.amejid.domain.valueobject.CustomerId;
import tech.amejid.domain.valueobject.Money;
import tech.amejid.domain.valueobject.OrderId;
import tech.amejid.domain.valueobject.OrderStatus;
import tech.amejid.domain.valueobject.ProductId;
import tech.amejid.domain.valueobject.RestaurantId;
import tech.amejid.order.service.domain.dto.create.CreateOrderCommand;
import tech.amejid.order.service.domain.dto.create.CreateOrderResponse;
import tech.amejid.order.service.domain.dto.create.OrderAddress;
import tech.amejid.order.service.domain.dto.create.OrderItem;
import tech.amejid.order.service.domain.entity.Customer;
import tech.amejid.order.service.domain.entity.Order;
import tech.amejid.order.service.domain.entity.Product;
import tech.amejid.order.service.domain.entity.Restaurant;
import tech.amejid.order.service.domain.exception.OrderDomainException;
import tech.amejid.order.service.domain.mapper.OrderDataMapper;
import tech.amejid.order.service.domain.ports.input.service.OrderApplicationService;
import tech.amejid.order.service.domain.ports.output.repository.CustomerRepository;
import tech.amejid.order.service.domain.ports.output.repository.OrderRepository;
import tech.amejid.order.service.domain.ports.output.repository.RestaurantRepository;

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

	private CreateOrderCommand createOrderCommand;

	private CreateOrderCommand createOrderCommandWrongPrice;

	private CreateOrderCommand createOrderCommandWrongProductPrice;

	private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");

	private final UUID RESTAURANT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb45");

	private final UUID PRODUCT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb48");

	private final UUID ORDER_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afb");

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

		Customer customer = new Customer();
		customer.setId(new CustomerId(this.CUSTOMER_ID));

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
			.hasMessage("Restaurant with id " + this.RESTAURANT_ID + " is currently not active!"
			);
	}

}
