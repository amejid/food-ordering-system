package com.food.ordering.system.customer.service.dataaccess.customer.repository;

import java.util.UUID;

import com.food.ordering.system.customer.service.dataaccess.customer.entity.CustomerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

}
