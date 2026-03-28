package com.koval.devicemanager.domain.exception;

public class PageSizeExceededException extends RuntimeException {

    public PageSizeExceededException(int requested, int max) {
        super(String.format("Requested page size %d exceeds the maximum allowed size of %d", requested, max));
    }
}
