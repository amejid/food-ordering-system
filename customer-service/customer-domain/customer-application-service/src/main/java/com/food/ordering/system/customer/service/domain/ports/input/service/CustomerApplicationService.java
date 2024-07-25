package com.food.ordering.system.customer.service.domain.ports.input.service;

import javax.validation.Valid;

import com.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.create.CreateCustomerResponse;

public interface CustomerApplicationService {

	CreateCustomerResponse createCustomer(@Valid CreateCustomerCommand createCustomerCommand);

}
