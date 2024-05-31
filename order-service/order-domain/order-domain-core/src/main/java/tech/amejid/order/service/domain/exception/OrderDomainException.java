package tech.amejid.order.service.domain.exception;

import tech.amejid.domain.exception.DomainException;

public class OrderDomainException extends DomainException {

	public OrderDomainException(String message) {
		super(message);
	}

	public OrderDomainException(String message, Throwable cause) {
		super(message, cause);
	}

}
