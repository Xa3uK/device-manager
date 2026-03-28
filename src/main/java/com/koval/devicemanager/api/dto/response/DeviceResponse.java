package com.koval.devicemanager.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import lombok.Getter;

import java.time.Instant;

// Currently used for all device operations (create, update, get).
// Consider splitting into CreateDeviceResponse, UpdateDeviceResponse, GetDeviceResponse
// if their fields start to diverge.
@Getter
@JsonPropertyOrder({"id", "name", "brand", "state", "createdAt", "updatedAt"})
public class DeviceResponse {
    private final Long id;
    private final String name;
    private final String brand;
    private final DeviceState state;
    private final Instant createdAt;
    private final Instant updatedAt;

    public DeviceResponse(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.brand = device.getBrand();
        this.state = device.getState();
        this.createdAt = device.getCreatedAt();
        this.updatedAt = device.getUpdatedAt();
    }
}
