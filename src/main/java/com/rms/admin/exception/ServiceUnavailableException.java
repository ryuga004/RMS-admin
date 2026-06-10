package com.rms.admin.exception;

import lombok.Getter;

@Getter
public class ServiceUnavailableException extends RuntimeException {

    private final String code;

    public ServiceUnavailableException(String code, String message) {
        super(message);
        this.code = code;
    }
}
