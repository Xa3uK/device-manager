package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.dto.request.BulkCreateRequest;
import com.koval.devicemanager.api.dto.request.BulkDeleteRequest;
import com.koval.devicemanager.api.dto.request.CreateDeviceRequest;
import com.koval.devicemanager.api.dto.request.UpdateDeviceRequest;
import com.koval.devicemanager.api.dto.response.DeviceResponse;
import com.koval.devicemanager.api.dto.response.PageResponse;
import com.koval.devicemanager.domain.model.DeviceInput;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeviceController implements DeviceApi {

    private final DeviceService deviceService;

    @Override
    public List<DeviceResponse> createBulk(@Valid BulkCreateRequest request) {
        List<DeviceInput> inputs = request.getDevices().stream()
                .map(d -> new DeviceInput(d.getName(), d.getBrand()))
                .toList();
        return deviceService.createBulk(inputs).stream().map(DeviceResponse::from).toList();
    }

    @Override
    public void deleteBulk(@Valid BulkDeleteRequest request) {
        deviceService.deleteBulk(request.getIds());
    }

    @Override
    public DeviceResponse create(@Valid CreateDeviceRequest request) {
        return DeviceResponse.from(deviceService.create(request.getName(), request.getBrand()));
    }

    @Override
    public DeviceResponse update(Long id, @Valid UpdateDeviceRequest request) {
        return DeviceResponse.from(deviceService.update(id, request.getName(), request.getBrand(), request.getState()));
    }

    @Override
    public DeviceResponse getById(Long id) {
        return DeviceResponse.from(deviceService.getById(id));
    }

    @Override
    public PageResponse<DeviceResponse> getAll(String brand, DeviceState state, Pageable pageable) {
        return new PageResponse<>(deviceService.getAll(brand, state, pageable).map(DeviceResponse::from));
    }

    @Override
    public void delete(Long id) {
        deviceService.delete(id);
    }
}
