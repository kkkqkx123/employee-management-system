package com.example.demo.position.exception;

import com.example.demo.common.exception.BusinessException;

public class PositionAlreadyExistsException extends BusinessException {
    public PositionAlreadyExistsException(String message) {
        super(message);
    }

    public PositionAlreadyExistsException(String message, Throwable cause) {
        super("POSITION_ALREADY_EXISTS", message, cause);
    }

    public static PositionAlreadyExistsException forCode(String code) {
        return new PositionAlreadyExistsException("Position already exists with code: " + code);
    }
}