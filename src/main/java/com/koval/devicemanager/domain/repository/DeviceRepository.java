package com.koval.devicemanager.domain.repository;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository {

    Device save(Device device);

    Device update(Device device);

    Optional<Device> findById(Long id);

    List<Device> findAll();

    List<Device> findAllByBrand(String brand);

    List<Device> findAllByState(DeviceState state);

    void delete(Long id);
}
