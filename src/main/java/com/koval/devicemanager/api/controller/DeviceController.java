package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.dto.request.CreateDeviceRequest;
import com.koval.devicemanager.api.dto.request.UpdateDeviceRequest;
import com.koval.devicemanager.api.dto.response.DeviceResponse;
import com.koval.devicemanager.api.dto.response.PageResponse;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeviceController implements DeviceApi {

    private final DeviceService deviceService;

    @Override
    public DeviceResponse create(@Valid CreateDeviceRequest request) {
        return new DeviceResponse(deviceService.create(request.getName(), request.getBrand()));
    }

    @Override
    public DeviceResponse update(Long id, @Valid UpdateDeviceRequest request) {
        return new DeviceResponse(
                deviceService.update(id, request.getName(), request.getBrand(), request.getState())
        );
    }

    @Override
    public DeviceResponse getById(Long id) {
        return new DeviceResponse(deviceService.getById(id));
    }

    @Override
    public PageResponse<DeviceResponse> getAll(String brand, DeviceState state, Pageable pageable) {
        if (brand != null) {
            return new PageResponse<>(deviceService.getAllByBrand(brand, pageable).map(DeviceResponse::new));
        }
        if (state != null) {
            return new PageResponse<>(deviceService.getAllByState(state, pageable).map(DeviceResponse::new));
        }
        return new PageResponse<>(deviceService.getAll(pageable).map(DeviceResponse::new));
    }

    @Override
    public void delete(Long id) {
        deviceService.delete(id);
    }
}
