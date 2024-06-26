package com.food.ordering.system.restaurant.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;

public interface RestaurantRepository {

	Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);

}
