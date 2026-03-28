package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.dto.request.CreateDeviceRequest;
import com.koval.devicemanager.api.dto.request.UpdateDeviceRequest;
import com.koval.devicemanager.api.dto.response.DeviceResponse;
import com.koval.devicemanager.api.dto.response.PageResponse;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Device management endpoints")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "Create a new device")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@RequestBody CreateDeviceRequest request) {
        return new DeviceResponse(deviceService.create(request.getName(), request.getBrand()));
    }

    @Operation(summary = "Partially update a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "Invalid field value or explicit null provided"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "422", description = "Device is in use and cannot be updated")
    })
    @PatchMapping("/{id}")
    public DeviceResponse update(
            @Parameter(description = "Device ID") @PathVariable Long id,
            @RequestBody UpdateDeviceRequest request) {
        return new DeviceResponse(
                deviceService.update(id, request.getName(), request.getBrand(), request.getState())
        );
    }

    @Operation(summary = "Get a device by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{id}")
    public DeviceResponse getById(@Parameter(description = "Device ID") @PathVariable Long id) {
        return new DeviceResponse(deviceService.getById(id));
    }

    @Operation(
            summary = "Get devices",
            description = "Returns a paginated list of devices. Optionally filter by `brand` (case-insensitive) or `state`. Maximum page size is 100."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of devices"),
            @ApiResponse(responseCode = "400", description = "Invalid state value or page size exceeds maximum")
    })
    @GetMapping
    public PageResponse<DeviceResponse> getAll(
            @Parameter(description = "Filter by brand (case-insensitive)") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by state") @RequestParam(required = false) DeviceState state,
            @ParameterObject Pageable pageable) {
        if (brand != null) {
            return new PageResponse<>(deviceService.getAllByBrand(brand, pageable).map(DeviceResponse::new));
        }
        if (state != null) {
            return new PageResponse<>(deviceService.getAllByState(state, pageable).map(DeviceResponse::new));
        }
        return new PageResponse<>(deviceService.getAll(pageable).map(DeviceResponse::new));
    }

    @Operation(summary = "Delete a device")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "422", description = "Device is in use and cannot be deleted")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "Device ID") @PathVariable Long id) {
        deviceService.delete(id);
    }
}
