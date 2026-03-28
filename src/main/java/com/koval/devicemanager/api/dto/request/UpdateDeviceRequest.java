package com.koval.devicemanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.koval.devicemanager.domain.model.DeviceState;
import lombok.Getter;

@Getter
public class UpdateDeviceRequest {

    @JsonSetter(nulls = Nulls.FAIL)
    private String name;

    @JsonSetter(nulls = Nulls.FAIL)
    private String brand;

    @JsonSetter(nulls = Nulls.FAIL)
    private DeviceState state;
}
