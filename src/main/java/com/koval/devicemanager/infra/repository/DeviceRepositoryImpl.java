package com.koval.devicemanager.infra.repository;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import com.koval.devicemanager.infra.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Device update(Device device) {
        DeviceEntity entity = jpaRepository.findById(device.getId())
                .orElseThrow(() -> new DeviceNotFoundException(device.getId()));
        mapper.mergeToEntity(device, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Device> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Device> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Device> findAllByBrand(String brand, Pageable pageable) {
        return jpaRepository.findAllByBrandIgnoreCase(brand, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Device> findAllByState(DeviceState state, Pageable pageable) {
        return jpaRepository.findAllByState(state, pageable).map(mapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
