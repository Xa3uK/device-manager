package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, Long> {

    Page<DeviceEntity> findAllByBrandIgnoreCase(String brand, Pageable pageable);

    Page<DeviceEntity> findAllByState(DeviceState state, Pageable pageable);

    Page<DeviceEntity> findAllByBrandIgnoreCaseAndState(String brand, DeviceState state, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceEntity d SET d.deletedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceEntity d SET d.deletedAt = CURRENT_TIMESTAMP WHERE d.id IN :ids")
    void softDeleteAllByIdIn(@Param("ids") List<Long> ids);
}
