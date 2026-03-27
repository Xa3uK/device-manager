package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.infra.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, Long> {
}
