package com.koval.devicemanager.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateDeviceRequest {

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = ".*[a-zA-Z0-9].*", message = "must contain at least one letter or number")
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = ".*[a-zA-Z0-9].*", message = "must contain at least one letter or number")
    private String brand;
}
