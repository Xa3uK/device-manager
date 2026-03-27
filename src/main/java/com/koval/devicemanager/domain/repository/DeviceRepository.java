package com.koval.devicemanager.domain.repository;

import com.koval.devicemanager.domain.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}
