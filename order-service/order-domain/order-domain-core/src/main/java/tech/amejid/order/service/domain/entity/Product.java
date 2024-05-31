package tech.amejid.order.service.domain.entity;

import tech.amejid.domain.entity.BaseEntity;
import tech.amejid.domain.valueobject.Money;
import tech.amejid.domain.valueobject.ProductId;

public class Product extends BaseEntity<ProductId> {

	private String name;

	private Money price;

	public Product(ProductId productId, String name, Money price) {
		super.setId(productId);
		this.name = name;
		this.price = price;
	}

	public Product(ProductId productId) {
		super.setId(productId);
	}

	public void updateWithConfirmedNameAndPrice(String name, Money price) {
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return this.name;
	}

	public Money getPrice() {
		return this.price;
	}

}
