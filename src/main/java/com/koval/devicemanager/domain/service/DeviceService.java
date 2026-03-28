package com.koval.devicemanager.domain.service;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.exception.PageSizeExceededException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;



@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public Device create(String name, String brand) {
        Device device = new Device();
        device.setName(name);
        device.setBrand(brand);
        device.setState(DeviceState.AVAILABLE);
        return deviceRepository.save(device);
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

        return deviceRepository.update(changes);
    }

    public Device getById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private static final int MAX_PAGE_SIZE = 100;

    public Page<Device> getAll(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new PageSizeExceededException(pageable.getPageSize(), MAX_PAGE_SIZE);
        }
        return deviceRepository.findAll(pageable);
    }

    public List<Device> getAllByBrand(String brand) {
        return deviceRepository.findAllByBrand(brand);
    }

    public List<Device> getAllByState(DeviceState state) {
        return deviceRepository.findAllByState(state);
    }

    public void delete(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        if (device.getState() == DeviceState.IN_USE) {
            throw new IllegalStateException("Device cannot be deleted while it is in use");
        }

        deviceRepository.delete(id);
    }
}
