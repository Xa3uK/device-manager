package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, Long> {

    Page<DeviceEntity> findAllByBrandIgnoreCase(String brand, Pageable pageable);

    Page<DeviceEntity> findAllByState(DeviceState state, Pageable pageable);

    Page<DeviceEntity> findAllByBrandIgnoreCaseAndState(String brand, DeviceState state, Pageable pageable);
}
