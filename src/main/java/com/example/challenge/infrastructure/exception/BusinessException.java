package com.example.challenge.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String messageKey;

    public BusinessException(String messageKey, HttpStatus status) {
        super(messageKey);
        this.messageKey = messageKey;
        this.status = status;
    }
}
