package com.koval.devicemanager.api.dto.request;

import com.koval.devicemanager.domain.model.DeviceState;
import lombok.Getter;

@Getter
public class UpdateDeviceRequest {
    private String name;
    private String brand;
    private DeviceState state;
}
