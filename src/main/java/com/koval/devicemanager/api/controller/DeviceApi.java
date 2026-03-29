package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.dto.request.BulkCreateRequest;
import com.koval.devicemanager.api.dto.request.BulkDeleteRequest;
import com.koval.devicemanager.api.dto.request.CreateDeviceRequest;
import com.koval.devicemanager.api.dto.request.UpdateDeviceRequest;
import com.koval.devicemanager.api.dto.response.DeviceResponse;
import com.koval.devicemanager.api.dto.response.PageResponse;
import com.koval.devicemanager.domain.model.DeviceState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "Device management endpoints")
public interface DeviceApi {

    @Operation(summary = "Create multiple devices")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Devices created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    List<DeviceResponse> createBulk(@RequestBody BulkCreateRequest request);

    @Operation(summary = "Delete multiple devices")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Devices deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "One or more devices not found"),
            @ApiResponse(responseCode = "422", description = "One or more devices are in use")
    })
    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBulk(@RequestBody BulkDeleteRequest request);

    @Operation(summary = "Create a new device")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DeviceResponse create(@RequestBody CreateDeviceRequest request);

    @Operation(summary = "Partially update a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "Invalid field value or explicit null provided"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "422", description = "Device is in use and cannot be updated")
    })
    @PatchMapping("/{id}")
    DeviceResponse update(
            @Parameter(description = "Device ID") @PathVariable Long id,
            @RequestBody UpdateDeviceRequest request);

    @Operation(summary = "Get a device by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{id}")
    DeviceResponse getById(@Parameter(description = "Device ID") @PathVariable Long id);

    @Operation(
            summary = "Get devices",
            description = "Returns a paginated list of devices. Optionally filter by `brand` (case-insensitive), `state`, or both combined. Page size is capped at 100."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of devices"),
            @ApiResponse(responseCode = "400", description = "Invalid state value or sort property")
    })
    @GetMapping
    PageResponse<DeviceResponse> getAll(
            @Parameter(description = "Filter by brand (case-insensitive)") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by state") @RequestParam(required = false) DeviceState state,
            @ParameterObject Pageable pageable);

    @Operation(summary = "Delete a device")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "422", description = "Device is in use and cannot be deleted")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@Parameter(description = "Device ID") @PathVariable Long id);
}
