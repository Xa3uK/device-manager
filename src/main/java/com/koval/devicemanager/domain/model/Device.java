package com.koval.devicemanager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    private Long id;
    private String name;
    private String brand;
    private DeviceState state;
    private Instant createdAt;
    private Instant updatedAt;
}
