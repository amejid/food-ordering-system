package com.food.ordering.system.order.service.dataaccess.restaurant.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

	private final RestaurantJpaRepository restaurantJpaRepository;

	private final RestaurantDataAccessMapper restaurantDataAccessMapper;

	@Override
	public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
		List<UUID> restaurantProducts = this.restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
		Optional<List<RestaurantEntity>> restaurantEntities = this.restaurantJpaRepository
			.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts);
		return restaurantEntities.map(this.restaurantDataAccessMapper::restaurantEntityToRestaurant);
	}

}
