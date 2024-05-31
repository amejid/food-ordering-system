package tech.amejid.order.service.domain.valueobject;

import java.util.UUID;

import tech.amejid.domain.valueobject.BaseId;

public class TrackingId extends BaseId<UUID> {

	public TrackingId(UUID value) {
		super(value);
	}

}
