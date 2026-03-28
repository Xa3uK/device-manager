package com.koval.devicemanager.domain.exception;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(Long id) {
        super("Device not found with id: " + id);
    }
}
