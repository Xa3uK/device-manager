package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.dto.request.CreateDeviceRequest;
import com.koval.devicemanager.api.dto.request.UpdateDeviceRequest;
import com.koval.devicemanager.api.dto.response.DeviceResponse;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@RequestBody CreateDeviceRequest request) {
        return new DeviceResponse(deviceService.create(request.getName(), request.getBrand()));
    }

    @PatchMapping("/{id}")
    public DeviceResponse update(@PathVariable Long id, @RequestBody UpdateDeviceRequest request) {
        return new DeviceResponse(
                deviceService.update(id, request.getName(), request.getBrand(), request.getState())
        );
    }

    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable Long id) {
        return new DeviceResponse(deviceService.getById(id));
    }

    @GetMapping
    public List<DeviceResponse> getAll() {
        return deviceService.getAll().stream().map(DeviceResponse::new).toList();
    }

    @GetMapping("/brand/{brand}")
    public List<DeviceResponse> getAllByBrand(@PathVariable String brand) {
        return deviceService.getAllByBrand(brand).stream().map(DeviceResponse::new).toList();
    }

    @GetMapping("/state/{state}")
    public List<DeviceResponse> getAllByState(@PathVariable DeviceState state) {
        return deviceService.getAllByState(state).stream().map(DeviceResponse::new).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deviceService.delete(id);
    }
}
