package com.example.demo.position.exception;

import com.example.demo.common.exception.BusinessException;

public class PositionInUseException extends BusinessException {
    public PositionInUseException(String message) {
        super(message);
    }

    public PositionInUseException(String message, Throwable cause) {
        super("POSITION_IN_USE", message, cause);
    }

    public PositionInUseException(Long positionId, int employeeCount) {
        super("Position with ID " + positionId + " cannot be deleted as it is assigned to " + employeeCount + " employee(s)");
    }
}