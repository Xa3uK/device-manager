package com.koval.devicemanager.api.dto.response;

import lombok.Value;

import java.time.Instant;

@Value
public class ErrorResponse {

    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
}
