package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.dto.request.CreateDeviceRequest;
import com.koval.devicemanager.api.dto.response.CreateDeviceResponse;
import com.koval.devicemanager.domain.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDeviceResponse create(@RequestBody CreateDeviceRequest request) {
        return new CreateDeviceResponse(deviceService.create(request.getName(), request.getBrand()));
    }
}
