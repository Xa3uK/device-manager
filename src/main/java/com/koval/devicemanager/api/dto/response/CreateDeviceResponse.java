package com.koval.devicemanager.api.dto.response;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CreateDeviceResponse {
    private final Long id;
    private final String name;
    private final String brand;
    private final DeviceState state;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CreateDeviceResponse(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.brand = device.getBrand();
        this.state = device.getState();
        this.createdAt = device.getCreatedAt();
        this.updatedAt = device.getUpdatedAt();
    }
}
