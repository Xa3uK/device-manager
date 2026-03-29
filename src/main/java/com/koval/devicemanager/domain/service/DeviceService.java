package com.koval.devicemanager.domain.service;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DeviceRepository deviceRepository;

    public Device create(String name, String brand) {
        Device device = new Device();
        device.setName(name);
        device.setBrand(brand);
        device.setState(DeviceState.AVAILABLE);
        Device saved = deviceRepository.save(device);
        log.info("Created device id={}", saved.getId());
        return saved;
    }

    public Device update(Long id, String name, String brand, DeviceState state) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        if (existing.getState() == DeviceState.IN_USE && (name != null || brand != null)) {
            throw new IllegalStateException("Name and brand cannot be updated while device is in use");
        }

        Device changes = new Device();
        changes.setId(id);
        changes.setName(name);
        changes.setBrand(brand);
        changes.setState(state);

        Device updated = deviceRepository.update(changes);
        log.info("Updated device id={}", id);
        return updated;
    }

    public Device getById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    public Page<Device> getAll(Pageable pageable) {
        return deviceRepository.findAll(clamp(pageable));
    }

    public Page<Device> getAllByBrand(String brand, Pageable pageable) {
        return deviceRepository.findAllByBrand(brand, clamp(pageable));
    }

    public Page<Device> getAllByState(DeviceState state, Pageable pageable) {
        return deviceRepository.findAllByState(state, clamp(pageable));
    }

    public void delete(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        if (device.getState() == DeviceState.IN_USE) {
            throw new IllegalStateException("Device cannot be deleted while it is in use");
        }

        deviceRepository.delete(id);
        log.info("Deleted device id={}", id);
    }

    private Pageable clamp(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }
}
