package com.koval.devicemanager.domain.exception;

import com.koval.devicemanager.domain.model.DeviceState;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InvalidDeviceStateException extends RuntimeException {

    public InvalidDeviceStateException(String value) {
        super(String.format("Invalid state value: '%s'. Valid values are: %s",
                value,
                Arrays.stream(DeviceState.values()).map(Enum::name).collect(Collectors.joining(", "))
        ));
    }
}
