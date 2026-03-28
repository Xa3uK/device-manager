package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, Long> {

    List<DeviceEntity> findAllByBrand(String brand);

    List<DeviceEntity> findAllByState(DeviceState state);
}
