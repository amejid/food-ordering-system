package com.food.ordering.system.order.service.application.rest;

import java.util.UUID;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/orders", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class OrderController {

	private final OrderApplicationService orderApplicationService;

	@PostMapping
	public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderCommand createOrderCommand) {
		log.info("Creating order for customer: {} at restaurant: {}", createOrderCommand.getCustomerId(),
				createOrderCommand.getRestaurantId());
		CreateOrderResponse createOrderResponse = this.orderApplicationService.createOrder(createOrderCommand);
		log.info("Order created successfully with tracking id: {}", createOrderResponse.getOrderTrackingId());
		return ResponseEntity.ok(createOrderResponse);
	}

	@GetMapping("/{trackingId}")
	public ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable UUID trackingId) {
		TrackOrderResponse trackOrderResponse = this.orderApplicationService
			.trackOrder(TrackOrderQuery.builder().orderTrackingId(trackingId).build());
		log.info("Order details fetched successfully for tracking id: {}", trackOrderResponse.getOrderTrackingId());
		return ResponseEntity.ok(trackOrderResponse);
	}

}
