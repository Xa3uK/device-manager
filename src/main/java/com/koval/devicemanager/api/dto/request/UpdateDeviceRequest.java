package com.koval.devicemanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.koval.devicemanager.domain.model.DeviceState;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateDeviceRequest {

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(min = 1, max = 255)
    private String name;

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(min = 1, max = 255)
    private String brand;

    @JsonSetter(nulls = Nulls.FAIL)
    private DeviceState state;
}
