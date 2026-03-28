package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import com.koval.devicemanager.infra.mapper.DeviceMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public Device update(Device device) {
        DeviceEntity entity = jpaRepository.findById(device.getId())
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + device.getId()));
        mapper.mergeToEntity(device, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Device> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Device> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Device> findAllByBrand(String brand) {
        return jpaRepository.findAllByBrand(brand).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Device> findAllByState(DeviceState state) {
        return jpaRepository.findAllByState(state).stream().map(mapper::toDomain).toList();
    }
}
