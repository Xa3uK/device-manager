package com.koval.devicemanager.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Device {

    private Long id;
    private String name;
    private String brand;
    private DeviceState state;
    private Instant createdAt;
    private Instant updatedAt;
}
