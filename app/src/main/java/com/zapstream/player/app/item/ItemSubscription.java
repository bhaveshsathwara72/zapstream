package com.zapstream.player.app.item;

import java.io.Serializable;

public class ItemSubscription implements Serializable {

	private final String deviceId;
	private final String registeredDate;

	public ItemSubscription(String deviceId, String registeredDate) {
		this.deviceId = deviceId;
		this.registeredDate = registeredDate;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getRegisteredDate() {
		return registeredDate;
	}
}