package com.koval.devicemanager.api.dto.request;

import lombok.Getter;

@Getter
public class UpdateDeviceRequest {
    private String name;
    private String brand;
    private String state;
}
