package com.koval.devicemanager.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class BulkDeleteRequest {

    @NotEmpty
    @Size(max = 100)
    private List<@NotNull Long> ids;
}
