package com.koval.devicemanager.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// Currently used for all device operations (create, update, get).
// Consider splitting into CreateDeviceResponse, UpdateDeviceResponse, GetDeviceResponse
// if their fields start to diverge.
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "brand", "state", "createdAt", "updatedAt"})
public class DeviceResponse {
    private final Long id;
    private final String name;
    private final String brand;
    private final DeviceState state;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static DeviceResponse from(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .brand(device.getBrand())
                .state(device.getState())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
