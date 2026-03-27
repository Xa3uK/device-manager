package com.koval.devicemanager.domain.repository;

import com.koval.devicemanager.domain.model.Device;

import java.util.Optional;

public interface DeviceRepository {

    Device save(Device device);

    Optional<Device> findById(Long id);
}
