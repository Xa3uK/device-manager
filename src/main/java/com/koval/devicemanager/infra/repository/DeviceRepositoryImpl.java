package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import com.koval.devicemanager.infra.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DeviceJpaRepository jpaRepository;
    private final DeviceMapper mapper;

    @Override
    public Device save(Device device) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(device)));
    }

    @Override
    public Optional<Device> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
