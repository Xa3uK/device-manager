package com.koval.devicemanager.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class BulkCreateRequest {

    @NotEmpty
    @Size(max = 100)
    private List<@Valid CreateDeviceRequest> devices;
}
