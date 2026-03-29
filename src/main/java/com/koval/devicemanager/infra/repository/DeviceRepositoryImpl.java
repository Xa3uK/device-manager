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
    public List<Device> saveAll(List<Device> devices) {
        return jpaRepository.saveAll(devices.stream().map(mapper::toEntity).toList())
                .stream().map(mapper::toDomain).toList();
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
    public List<Device> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<Device> findAll(String brand, DeviceState state, Pageable pageable) {
        if (brand != null && state != null) {
            return jpaRepository.findAllByBrandIgnoreCaseAndState(brand, state, pageable).map(mapper::toDomain);
        }
        if (brand != null) {
            return jpaRepository.findAllByBrandIgnoreCase(brand, pageable).map(mapper::toDomain);
        }
        if (state != null) {
            return jpaRepository.findAllByState(state, pageable).map(mapper::toDomain);
        }
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.softDeleteById(id);
    }

    @Override
    public void deleteAll(List<Long> ids) {
        jpaRepository.softDeleteAllByIdIn(ids);
    }
}
