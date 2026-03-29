package com.koval.devicemanager.domain.service;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceInput;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DeviceRepository deviceRepository;

    // The initial device state may depend on business needs. Since I can't talk to the product team, I decided to
    // set it to AVAILABLE by default.
    public List<Device> createBulk(List<DeviceInput> inputs) {
        List<Device> devices = inputs.stream()
                .map(input -> Device.builder()
                        .name(input.name())
                        .brand(input.brand())
                        .state(DeviceState.AVAILABLE)
                        .build())
                .toList();
        List<Device> saved = deviceRepository.saveAll(devices);
        log.info("Created {} devices in bulk", saved.size());
        return saved;
    }

    public void deleteBulk(List<Long> ids) {
        List<Device> found = deviceRepository.findAllById(ids);

        if (found.size() != ids.size()) {
            Set<Long> foundIds = found.stream().map(Device::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new DeviceNotFoundException(missing);
        }

        List<Long> inUseIds = found.stream()
                .filter(d -> d.getState() == DeviceState.IN_USE)
                .map(Device::getId)
                .toList();
        if (!inUseIds.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete devices in use: " + inUseIds.stream().map(String::valueOf).collect(Collectors.joining(", "))
            );
        }

        deviceRepository.deleteAll(ids);
        log.info("Deleted {} devices in bulk", ids.size());
    }

    public Device create(String name, String brand) {
        Device saved = deviceRepository.save(Device.builder()
                .name(name)
                .brand(brand)
                .state(DeviceState.AVAILABLE)
                .build());
        log.info("Created device id={}", saved.getId());
        return saved;
    }

    public Device update(Long id, String name, String brand, DeviceState state) {
        Device existing = deviceRepository.findById(id)
            .orElseThrow(() -> new DeviceNotFoundException(id));

        if (existing.getState() == DeviceState.IN_USE && (name != null || brand != null)) {
            throw new IllegalStateException("Name and brand cannot be updated while device is in use");
        }

        Device updated = deviceRepository.update(Device.builder()
                .id(id)
                .name(name)
                .brand(brand)
                .state(state)
                .build());
        log.info("Updated device id={}", id);
        return updated;
    }

    public Device getById(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    public Page<Device> getAll(String brand, DeviceState state, Pageable pageable) {
        return deviceRepository.findAll(brand, state, clamp(pageable));
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
