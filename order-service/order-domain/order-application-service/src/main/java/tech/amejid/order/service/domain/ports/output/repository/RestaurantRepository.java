package tech.amejid.order.service.domain.ports.output.repository;

import java.util.Optional;

import tech.amejid.order.service.domain.entity.Restaurant;

public interface RestaurantRepository {

	Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);

}
