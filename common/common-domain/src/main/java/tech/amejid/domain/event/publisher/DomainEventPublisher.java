package tech.amejid.domain.event.publisher;

import tech.amejid.domain.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {

	void publish(T domainEvent);

}
