package com.food.ordering.system.restaurant.service.dataaccess.restaurant.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;

import org.springframework.stereotype.Component;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

	private final RestaurantJpaRepository restaurantJpaRepository;

	private final RestaurantDataAccessMapper restaurantDataAccessMapper;

	public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository,
			RestaurantDataAccessMapper restaurantDataAccessMapper) {
		this.restaurantJpaRepository = restaurantJpaRepository;
		this.restaurantDataAccessMapper = restaurantDataAccessMapper;
	}

	@Override
	public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
		List<UUID> restaurantProducts = this.restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
		Optional<List<RestaurantEntity>> restaurantEntities = this.restaurantJpaRepository
			.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts);
		return restaurantEntities.map(this.restaurantDataAccessMapper::restaurantEntityToRestaurant);
	}

}
