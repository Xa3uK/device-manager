package com.koval.devicemanager.domain.service;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
