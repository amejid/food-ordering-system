package com.food.ordering.system.order.service.dataaccess.customer.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
@Entity
public class CustomerEntity {

	@Id
	private UUID id;

	private String username;

	private String firstName;

	private String lastName;

}
