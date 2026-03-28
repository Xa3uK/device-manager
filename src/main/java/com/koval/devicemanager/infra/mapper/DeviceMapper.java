package com.koval.devicemanager.infra.mapper;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public Device toDomain(DeviceEntity entity) {
        Device device = new Device();
        device.setId(entity.getId());
        device.setName(entity.getName());
        device.setBrand(entity.getBrand());
        device.setState(entity.getState());
        device.setCreatedAt(entity.getCreatedAt());
        device.setUpdatedAt(entity.getUpdatedAt());
        return device;
    }

    public DeviceEntity toEntity(Device device) {
        DeviceEntity entity = new DeviceEntity();
        entity.setName(device.getName());
        entity.setBrand(device.getBrand());
        entity.setState(device.getState());
        return entity;
    }

    public void mergeToEntity(Device source, DeviceEntity target) {
        if (source.getName() != null) target.setName(source.getName());
        if (source.getBrand() != null) target.setBrand(source.getBrand());
        if (source.getState() != null) target.setState(source.getState());
    }
}
