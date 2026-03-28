package com.koval.devicemanager.infra.mapper;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public Device toDomain(DeviceEntity entity) {
        return Device.builder()
                .id(entity.getId())
                .name(entity.getName())
                .brand(entity.getBrand())
                .state(entity.getState())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public DeviceEntity toEntity(Device device) {
        return DeviceEntity.builder()
                .name(device.getName())
                .brand(device.getBrand())
                .state(device.getState())
                .build();
    }

    public void mergeToEntity(Device source, DeviceEntity target) {
        if (source.getName() != null) target.setName(source.getName());
        if (source.getBrand() != null) target.setBrand(source.getBrand());
        if (source.getState() != null) target.setState(source.getState());
    }
}
