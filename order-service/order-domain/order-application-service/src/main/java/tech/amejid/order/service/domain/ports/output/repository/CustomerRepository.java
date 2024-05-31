package tech.amejid.order.service.domain.ports.output.repository;

import java.util.Optional;
import java.util.UUID;

import tech.amejid.order.service.domain.entity.Customer;

public interface CustomerRepository {

	Optional<Customer> findCustomer(UUID customerId);

}
