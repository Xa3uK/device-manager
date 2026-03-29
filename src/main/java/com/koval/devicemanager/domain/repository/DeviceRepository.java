package com.koval.devicemanager.domain.repository;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository {

    Device save(Device device);

    List<Device> saveAll(List<Device> devices);

    Device update(Device device);

    Optional<Device> findById(Long id);

    List<Device> findAllById(List<Long> ids);

    Page<Device> findAll(String brand, DeviceState state, Pageable pageable);

    void delete(Long id);

    void deleteAll(List<Long> ids);
}
