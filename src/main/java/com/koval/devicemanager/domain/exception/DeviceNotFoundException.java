package com.koval.devicemanager.domain.exception;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(Long id) {
        super("Device not found with id: " + id);
    }

    public DeviceNotFoundException(List<Long> ids) {
        super("Devices not found with ids: " + ids.stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }
}
