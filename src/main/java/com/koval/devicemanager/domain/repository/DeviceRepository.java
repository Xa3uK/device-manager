package com.koval.devicemanager.domain.repository;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DeviceRepository {

    Device save(Device device);

    Device update(Device device);

    Optional<Device> findById(Long id);

    Page<Device> findAll(Pageable pageable);

    Page<Device> findAllByBrand(String brand, Pageable pageable);

    Page<Device> findAllByState(DeviceState state, Pageable pageable);

    void delete(Long id);
}
