package com.example.demo.position.exception;

import com.example.demo.common.exception.BusinessException;

public class PositionNotFoundException extends BusinessException {
    public PositionNotFoundException(String message) {
        super(message);
    }

    public PositionNotFoundException(String message, Throwable cause) {
        super("POSITION_NOT_FOUND", message, cause);
    }

    public PositionNotFoundException(Long positionId) {
        super("Position not found with ID: " + positionId);
    }

    public PositionNotFoundException(String field, String value) {
        super("Position not found with " + field + ": " + value);
    }
}